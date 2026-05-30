<template>
  <div class="p-6 max-w-5xl mx-auto">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-semibold text-gray-800">证件类型字典</h2>
      <a-button type="primary" @click="openCreate">新增证件类型</a-button>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <a-table
        :columns="columns"
        :data-source="certTypes"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
              {{ CERT_TYPE_STATUS_LABEL[record.status as CertificateTypeStatus] }}
            </a-tag>
          </template>
          <template v-if="column.key === 'fields'">
            {{ (record.fields ?? []).length }} 个字段
          </template>
          <template v-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="openFields(record)">差异化字段</a-button>
              <a-button
                type="link"
                size="small"
                :danger="record.status === 'ACTIVE'"
                @click="toggleStatus(record)"
              >
                {{ record.status === 'ACTIVE' ? '停用' : '启用' }}
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑证件类型 -->
    <a-modal
      v-model:open="formOpen"
      :title="editingId ? '编辑证件类型' : '新增证件类型'"
      :confirm-loading="saving"
      @ok="handleSave"
    >
      <a-form layout="vertical">
        <a-form-item label="证件类型名称" required>
          <a-input v-model:value="form.name" placeholder="请输入名称（唯一）" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" placeholder="选填" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 差异化字段维护 -->
    <a-modal
      v-model:open="fieldsOpen"
      title="维护差异化字段"
      width="760px"
      :confirm-loading="savingFields"
      @ok="handleSaveFields"
    >
      <div class="flex justify-end mb-2">
        <a-button type="link" size="small" @click="addField">+ 新增字段</a-button>
      </div>
      <a-table
        :columns="fieldColumns"
        :data-source="editFields"
        :pagination="false"
        row-key="rowKey"
        size="small"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'fieldKey'">
            <a-input v-model:value="record.fieldKey" placeholder="字段标识" />
          </template>
          <template v-if="column.key === 'fieldLabel'">
            <a-input v-model:value="record.fieldLabel" placeholder="显示名" />
          </template>
          <template v-if="column.key === 'fieldType'">
            <a-select v-model:value="record.fieldType" style="width: 110px">
              <a-select-option value="TEXT">文本</a-select-option>
              <a-select-option value="NUMBER">数值</a-select-option>
              <a-select-option value="DATE">日期</a-select-option>
              <a-select-option value="SELECT">选择</a-select-option>
            </a-select>
          </template>
          <template v-if="column.key === 'required'">
            <a-checkbox v-model:checked="record.required" />
          </template>
          <template v-if="column.key === 'actions'">
            <a-button type="link" size="small" danger @click="removeField(index)">删除</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  fetchCertTypes,
  executeSaveCertType,
  executeChangeCertTypeStatus,
  executeUpdateCertTypeFields,
} from '../../application/manage-cert-types.usecase';
import { CERT_TYPE_STATUS_LABEL, type CertificateTypeStatus } from '../../types/vo/supplier-info.vo';
import type {
  CertTypeDto,
  CertTypeFieldDto,
  SaveCertTypeRequest,
} from '../../types/dto/certificate.dto';

const loading = ref(false);
const certTypes = ref<CertTypeDto[]>([]);

const formOpen = ref(false);
const saving = ref(false);
const editingId = ref<number | null>(null);
const form = reactive<SaveCertTypeRequest>({ name: '', remark: '' });

const fieldsOpen = ref(false);
const savingFields = ref(false);
const fieldsTypeId = ref<number | null>(null);
// 编辑态字段加 rowKey 以稳定渲染
type EditField = CertTypeFieldDto & { rowKey: number };
const editFields = ref<EditField[]>([]);
let rowSeq = 0;

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '状态', key: 'status' },
  { title: '差异化字段', key: 'fields' },
  { title: '操作', key: 'actions', width: 240 },
];

const fieldColumns = [
  { title: '字段标识', key: 'fieldKey' },
  { title: '显示名', key: 'fieldLabel' },
  { title: '类型', key: 'fieldType' },
  { title: '必填', key: 'required', width: 70 },
  { title: '操作', key: 'actions', width: 80 },
];

onMounted(load);

async function load() {
  loading.value = true;
  try {
    certTypes.value = await fetchCertTypes();
  } catch {
    message.error('加载证件类型失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  form.name = '';
  form.remark = '';
  formOpen.value = true;
}

function openEdit(record: CertTypeDto) {
  editingId.value = record.id;
  form.name = record.name;
  form.remark = record.remark ?? '';
  formOpen.value = true;
}

async function handleSave() {
  saving.value = true;
  const result = await executeSaveCertType(editingId.value, { ...form });
  saving.value = false;
  if (result.success) {
    message.success('保存成功');
    formOpen.value = false;
    await load();
  } else {
    message.error(result.error || '保存失败');
  }
}

async function toggleStatus(record: CertTypeDto) {
  const next: CertificateTypeStatus = record.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
  const result = await executeChangeCertTypeStatus(record.id, next);
  if (result.success) {
    message.success('状态已调整');
    await load();
  } else {
    message.error(result.error || '调整失败');
  }
}

function openFields(record: CertTypeDto) {
  fieldsTypeId.value = record.id;
  editFields.value = (record.fields ?? []).map((f) => ({ ...f, rowKey: rowSeq++ }));
  fieldsOpen.value = true;
}

function addField() {
  editFields.value.push({
    rowKey: rowSeq++,
    fieldKey: '',
    fieldLabel: '',
    fieldType: 'TEXT',
    required: false,
    sortOrder: editFields.value.length,
  });
}

function removeField(index: number) {
  editFields.value.splice(index, 1);
}

async function handleSaveFields() {
  if (fieldsTypeId.value == null) return;
  savingFields.value = true;
  const payload: CertTypeFieldDto[] = editFields.value.map((f, idx) => ({
    id: f.id,
    fieldKey: f.fieldKey,
    fieldLabel: f.fieldLabel,
    fieldType: f.fieldType,
    required: f.required,
    sortOrder: idx,
  }));
  const result = await executeUpdateCertTypeFields(fieldsTypeId.value, payload);
  savingFields.value = false;
  if (result.success) {
    message.success('字段已保存');
    fieldsOpen.value = false;
    await load();
  } else {
    message.error(result.error || '保存失败');
  }
}
</script>
