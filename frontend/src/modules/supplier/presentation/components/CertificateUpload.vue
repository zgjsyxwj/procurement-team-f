<template>
  <a-modal
    :open="open"
    :title="title"
    :confirm-loading="loading"
    @ok="handleOk"
    @cancel="emit('update:open', false)"
  >
    <a-form layout="vertical">
      <a-form-item label="证件类型" required>
        <a-select
          v-model:value="selectedTypeId"
          placeholder="请选择证件类型"
          @change="onTypeChange"
        >
          <a-select-option v-for="t in certTypes" :key="t.id" :value="t.id">
            {{ t.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="有效期起始" required>
            <a-date-picker
              v-model:value="validFrom"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="有效期截止" required>
            <a-date-picker
              v-model:value="validTo"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 差异化字段（按所选证件类型动态渲染） -->
      <template v-for="field in currentFields" :key="field.fieldKey">
        <a-form-item :label="field.fieldLabel" :required="field.required">
          <a-date-picker
            v-if="field.fieldType === 'DATE'"
            v-model:value="extraFields[field.fieldKey]"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
          <a-input
            v-else
            v-model:value="extraFields[field.fieldKey]"
            :placeholder="`请输入${field.fieldLabel}`"
          />
        </a-form-item>
      </template>

      <a-form-item label="证件文件" required>
        <a-upload
          :before-upload="beforeUpload"
          :file-list="fileList"
          :max-count="1"
          @remove="onRemove"
        >
          <a-button>选择文件（PDF/JPG/PNG，≤100MB）</a-button>
        </a-upload>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import type { UploadProps } from 'ant-design-vue';
import { validateCertificateFile } from '../../infrastructure/adapters/oss-upload.adapter';
import type { CertTypeDto, UploadCertificateRequest } from '../../types/dto/certificate.dto';

const props = withDefaults(
  defineProps<{
    open: boolean;
    certTypes: CertTypeDto[];
    loading?: boolean;
    title?: string;
  }>(),
  { loading: false, title: '上传证件' },
);

const emit = defineEmits<{
  'update:open': [value: boolean];
  submit: [payload: { file: File; request: UploadCertificateRequest }];
}>();

const selectedTypeId = ref<number | undefined>(undefined);
const validFrom = ref<string | undefined>(undefined);
const validTo = ref<string | undefined>(undefined);
const extraFields = reactive<Record<string, string>>({});
const fileList = ref<UploadProps['fileList']>([]);
const selectedFile = ref<File | null>(null);

const currentFields = computed(
  () => props.certTypes.find((t) => t.id === selectedTypeId.value)?.fields ?? [],
);

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      selectedTypeId.value = undefined;
      validFrom.value = undefined;
      validTo.value = undefined;
      fileList.value = [];
      selectedFile.value = null;
      Object.keys(extraFields).forEach((k) => delete extraFields[k]);
    }
  },
);

function onTypeChange() {
  Object.keys(extraFields).forEach((k) => delete extraFields[k]);
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const result = validateCertificateFile(file as File);
  if (!result.valid) {
    message.error(result.error || '文件校验失败');
    return false;
  }
  selectedFile.value = file as File;
  fileList.value = [{ uid: '-1', name: (file as File).name, status: 'done' }];
  return false; // 阻止自动上传，由父组件统一提交
};

function onRemove() {
  selectedFile.value = null;
  fileList.value = [];
}

function handleOk() {
  if (!selectedTypeId.value) {
    message.error('请选择证件类型');
    return;
  }
  if (!validFrom.value || !validTo.value) {
    message.error('请选择有效期');
    return;
  }
  if (!selectedFile.value) {
    message.error('请选择证件文件');
    return;
  }
  emit('submit', {
    file: selectedFile.value,
    request: {
      certTypeId: selectedTypeId.value,
      validFrom: validFrom.value,
      validTo: validTo.value,
      extraFields: { ...extraFields },
    },
  });
}
</script>
