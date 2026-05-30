<template>
  <div class="p-6 max-w-6xl mx-auto">
    <a-spin :spinning="loading">
      <div v-if="supplier">
        <!-- 头部 -->
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <a-button size="small" @click="goBack">返回</a-button>
            <h2 class="text-xl font-semibold text-gray-800">{{ supplier.name }}</h2>
            <SupplierStatusTag :status="supplier.status" />
            <span class="text-gray-400 text-sm">编号：{{ supplier.supplierCode }}</span>
          </div>
          <a-space>
            <a-button @click="inviteOpen = true">发送/重发邀请</a-button>
            <a-button type="primary" @click="statusOpen = true">状态调整</a-button>
          </a-space>
        </div>

        <a-tabs v-model:activeKey="activeTab" class="bg-white rounded-xl shadow-sm px-5">
          <!-- 信息 -->
          <a-tab-pane key="info" tab="企业信息">
            <SupplierBasicInfoForm v-model="basicInfo" :editable="true" :errors="basicErrors" />
            <a-divider />
            <BankAccountList v-model:accounts="bankAccounts" :editable="true" />
            <div class="flex justify-end mt-4">
              <a-button type="primary" :loading="savingInfo" @click="handleSaveInfo">
                保存（即时生效）
              </a-button>
            </div>
          </a-tab-pane>

          <!-- 联系人 -->
          <a-tab-pane key="contacts" tab="联系人">
            <ContactList
              ref="contactListRef"
              :contacts="contacts"
              :loading="contactsLoading"
              :saving="contactSaving"
              :show-invite="true"
              @save="handleSaveContact"
              @set-primary="handleSetPrimary"
              @delete="handleDeleteContact"
              @invite="handleInviteContact"
            />
          </a-tab-pane>

          <!-- 证件 -->
          <a-tab-pane key="certs" tab="证件">
            <div class="flex justify-end mb-3">
              <a-button type="primary" size="small" @click="certUploadOpen = true">
                手动添加证件
              </a-button>
            </div>
            <CertificateList
              :certificates="certificates"
              :loading="certsLoading"
              :cert-types="certTypes"
              :show-review="true"
              @approve="handleApproveCert"
              @reject="handleRejectCert"
            />
          </a-tab-pane>

          <!-- 变更记录 -->
          <a-tab-pane key="history" tab="变更记录">
            <div class="mb-3">
              <a-range-picker v-model:value="historyRange" value-format="YYYY-MM-DD" @change="loadHistory" />
            </div>
            <a-table
              :columns="historyColumns"
              :data-source="history"
              :loading="historyLoading"
              :pagination="false"
              row-key="id"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'changeType'">
                  {{ CHANGE_TYPE_LABEL[record.changeType as ChangeType] }}
                </template>
                <template v-if="column.key === 'source'">
                  {{ CHANGE_SOURCE_LABEL[record.source as ChangeSource] }}
                </template>
                <template v-if="column.key === 'status'">
                  <a-tag :color="CHANGE_REQUEST_STATUS_COLOR[record.status as ChangeRequestStatus]">
                    {{ CHANGE_REQUEST_STATUS_LABEL[record.status as ChangeRequestStatus] }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'actions'">
                  <a-button type="link" size="small" @click="viewDiff(record)">查看对比</a-button>
                </template>
              </template>
            </a-table>
          </a-tab-pane>
        </a-tabs>

        <!-- 弹窗 -->
        <StatusChangeDialog
          v-model:open="statusOpen"
          :supplier-id="supplier.id"
          :loading="statusLoading"
          @confirm="handleChangeStatus"
        />
        <InviteDialog
          v-model:open="inviteOpen"
          :supplier-name="supplier.name"
          :loading="inviteLoading"
          @confirm="handleInvite"
        />
        <CertificateUpload
          v-model:open="certUploadOpen"
          :cert-types="certTypes"
          :loading="certUploading"
          title="手动添加证件（直接通过）"
          @submit="handleAddCertificate"
        />
        <a-modal v-model:open="diffOpen" title="变更对比" :footer="null" width="720px">
          <ChangeDiffView v-if="selectedChange" :change="selectedChange" />
        </a-modal>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message, Modal, Textarea } from 'ant-design-vue';
import SupplierStatusTag from '../components/SupplierStatusTag.vue';
import SupplierBasicInfoForm from '../components/SupplierBasicInfoForm.vue';
import BankAccountList from '../components/BankAccountList.vue';
import ContactList from '../components/ContactList.vue';
import CertificateList from '../components/CertificateList.vue';
import CertificateUpload from '../components/CertificateUpload.vue';
import StatusChangeDialog from '../components/StatusChangeDialog.vue';
import InviteDialog from '../components/InviteDialog.vue';
import ChangeDiffView from '../components/ChangeDiffView.vue';
import { BASIC_INFO_FIELDS } from '../../domain/value-objects/basic-info-fields.vo';
import { validateBasicInfo } from '../../application/manage-supplier-info.usecase';
import { validateBankAccounts } from '../../domain/rules/bank-account-validation.rule';
import {
  fetchSupplierDetail,
  executeInvite,
  executeChangeStatus,
  fetchChangeHistory,
} from '../../application/manage-suppliers.usecase';
import { updateSupplier } from '../../infrastructure/services/supplier.service';
import {
  fetchContacts,
  executeSaveContact,
  executeSetPrimary,
  executeDeleteContact,
  executeInviteContact,
} from '../../application/manage-contacts.usecase';
import {
  fetchSelectableCertTypes,
  fetchSupplierCertificates,
  executeAddCertificateByBuyer,
} from '../../application/manage-certificates.usecase';
import {
  executeApproveCertificate,
  executeRejectCertificate,
} from '../../application/review-change.usecase';
import {
  CHANGE_TYPE_LABEL,
  CHANGE_SOURCE_LABEL,
  CHANGE_REQUEST_STATUS_LABEL,
  CHANGE_REQUEST_STATUS_COLOR,
  type ChangeType,
  type ChangeSource,
  type ChangeRequestStatus,
} from '../../types/vo/supplier-info.vo';
import type { SupplierDto, BankAccountDto } from '../../types/dto/supplier.dto';
import type { ContactDto, SaveContactRequest } from '../../types/dto/contact.dto';
import type { CertificateDto, CertTypeDto, UploadCertificateRequest } from '../../types/dto/certificate.dto';
import type { ChangeRequestDto } from '../../types/dto/change.dto';

const route = useRoute();
const router = useRouter();
const supplierId = Number(route.params.id);

const loading = ref(false);
const supplier = ref<SupplierDto | null>(null);
const activeTab = ref('info');

// 信息编辑
const basicInfo = reactive<Record<string, string>>({});
const basicErrors = ref<Record<string, string>>({});
const bankAccounts = ref<BankAccountDto[]>([]);
const savingInfo = ref(false);

// 联系人
const contacts = ref<ContactDto[]>([]);
const contactsLoading = ref(false);
const contactSaving = ref(false);
const contactListRef = ref<InstanceType<typeof ContactList> | null>(null);

// 证件
const certificates = ref<CertificateDto[]>([]);
const certsLoading = ref(false);
const certTypes = ref<CertTypeDto[]>([]);
const certUploadOpen = ref(false);
const certUploading = ref(false);

// 变更记录
const history = ref<ChangeRequestDto[]>([]);
const historyLoading = ref(false);
const historyRange = ref<[string, string] | undefined>(undefined);
const diffOpen = ref(false);
const selectedChange = ref<ChangeRequestDto | null>(null);

// 状态/邀请
const statusOpen = ref(false);
const statusLoading = ref(false);
const inviteOpen = ref(false);
const inviteLoading = ref(false);

const historyColumns = [
  { title: '变更类型', key: 'changeType' },
  { title: '来源', key: 'source' },
  { title: '状态', key: 'status' },
  { title: '提交人', dataIndex: 'submitterName', key: 'submitterName' },
  { title: '提交时间', dataIndex: 'submittedAt', key: 'submittedAt' },
  { title: '操作', key: 'actions', width: 120 },
];

onMounted(load);

async function load() {
  loading.value = true;
  try {
    supplier.value = await fetchSupplierDetail(supplierId);
    hydrateForm(supplier.value);
    await Promise.all([loadContacts(), loadCertificates(), loadCertTypes(), loadHistory()]);
  } catch {
    message.error('加载供应商详情失败');
  } finally {
    loading.value = false;
  }
}

function hydrateForm(dto: SupplierDto) {
  for (const field of BASIC_INFO_FIELDS) {
    const value = dto[field.key];
    basicInfo[field.key as string] = value === null || value === undefined ? '' : String(value);
  }
  bankAccounts.value = (dto.bankAccounts ?? []).map((b) => ({ ...b }));
}

async function handleSaveInfo() {
  basicErrors.value = {};
  const errors = validateBasicInfo(basicInfo);
  if (errors) {
    basicErrors.value = errors as Record<string, string>;
    message.error('请检查表单填写');
    return;
  }
  if (validateBankAccounts(bankAccounts.value).length > 0) {
    message.error('银行信息填写不完整：户名、开户银行、银行账号须同时填写');
    return;
  }
  savingInfo.value = true;
  try {
    await updateSupplier(supplierId, { ...basicInfo });
    message.success('保存成功，已即时生效');
    await load();
  } catch {
    message.error('保存失败');
  } finally {
    savingInfo.value = false;
  }
}

// ===== 联系人 =====
async function loadContacts() {
  contactsLoading.value = true;
  try {
    contacts.value = await fetchContacts(supplierId);
  } finally {
    contactsLoading.value = false;
  }
}

async function handleSaveContact(payload: { contactId: number | null; data: SaveContactRequest }) {
  contactSaving.value = true;
  const result = await executeSaveContact(supplierId, payload.contactId, payload.data);
  contactSaving.value = false;
  if (result.success) {
    message.success('保存成功');
    contactListRef.value?.close();
    await loadContacts();
  } else {
    message.error(result.error || '保存失败');
  }
}

async function handleSetPrimary(contact: ContactDto) {
  const result = await executeSetPrimary(supplierId, contact.id);
  if (result.success) {
    message.success('已设为主要联系人');
    await loadContacts();
  } else {
    message.error(result.error || '设置失败');
  }
}

function handleDeleteContact(contact: ContactDto) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除联系人 "${contact.name}" 吗？`,
    async onOk() {
      const result = await executeDeleteContact(supplierId, contact);
      if (result.success) {
        message.success('删除成功');
        await loadContacts();
      } else {
        message.error(result.error || '删除失败');
      }
    },
  });
}

async function handleInviteContact(contact: ContactDto) {
  const result = await executeInviteContact(supplierId, contact.id);
  if (result.success) {
    message.success('邀请已发送');
  } else {
    message.error(result.error || '邀请失败');
  }
}

// ===== 证件 =====
async function loadCertificates() {
  certsLoading.value = true;
  try {
    certificates.value = await fetchSupplierCertificates(supplierId);
  } finally {
    certsLoading.value = false;
  }
}

async function loadCertTypes() {
  certTypes.value = await fetchSelectableCertTypes();
}

async function handleAddCertificate(payload: { file: File; request: UploadCertificateRequest }) {
  certUploading.value = true;
  const result = await executeAddCertificateByBuyer(supplierId, payload.file, payload.request);
  certUploading.value = false;
  if (result.success) {
    message.success('证件已添加');
    certUploadOpen.value = false;
    await loadCertificates();
  } else {
    message.error(result.error || '添加失败');
  }
}

// ===== 变更记录 =====
async function loadHistory() {
  historyLoading.value = true;
  try {
    const query = historyRange.value
      ? { startTime: historyRange.value[0], endTime: historyRange.value[1] }
      : undefined;
    history.value = await fetchChangeHistory(supplierId, query);
  } finally {
    historyLoading.value = false;
  }
}

function viewDiff(change: ChangeRequestDto) {
  selectedChange.value = change;
  diffOpen.value = true;
}

async function handleApproveCert(cert: CertificateDto) {
  const result = await executeApproveCertificate(cert.id);
  if (result.success) {
    message.success('证件已通过');
    await loadCertificates();
  } else {
    message.error(result.error || '操作失败');
  }
}

function handleRejectCert(cert: CertificateDto) {
  let reason = '';
  Modal.confirm({
    title: '驳回证件',
    content: () =>
      h(Textarea, {
        rows: 3,
        placeholder: '请填写驳回原因',
        'onUpdate:value': (v: string) => {
          reason = v;
        },
      }),
    async onOk() {
      const result = await executeRejectCertificate(cert.id, reason);
      if (result.success) {
        message.success('证件已驳回');
        await loadCertificates();
      } else {
        message.error(result.error || '操作失败');
        return Promise.reject();
      }
    },
  });
}

// ===== 状态/邀请 =====
async function handleChangeStatus(payload: { targetStatus: 'ACTIVE' | 'DISABLED'; remark?: string }) {
  statusLoading.value = true;
  const result = await executeChangeStatus(supplierId, payload);
  statusLoading.value = false;
  if (result.success) {
    message.success('状态已调整');
    statusOpen.value = false;
    await load();
  } else {
    message.error(result.error || '状态调整失败');
  }
}

async function handleInvite() {
  inviteLoading.value = true;
  const result = await executeInvite(supplierId);
  inviteLoading.value = false;
  if (result.success) {
    message.success('邀请已发送');
    inviteOpen.value = false;
  } else {
    message.error(result.error || '邀请失败');
  }
}

function goBack() {
  router.push('/suppliers');
}
</script>
