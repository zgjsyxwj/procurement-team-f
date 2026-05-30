<template>
  <div class="p-6 max-w-6xl mx-auto">
    <a-spin :spinning="loading">
      <div v-if="profile">
        <!-- 顶部：状态与引导 -->
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <h2 class="text-xl font-semibold text-gray-800">{{ profile.name }}</h2>
            <SupplierStatusTag :status="profile.status" />
            <span class="text-gray-400 text-sm">编号：{{ profile.supplierCode }}</span>
          </div>
        </div>

        <!-- 入驻引导 -->
        <a-alert
          v-if="isDraftStage(profile.status)"
          class="mb-4"
          type="info"
          show-icon
          message="请完善企业基本信息、银行信息、联系人与证件，并提交准入审核。"
        />

        <!-- 待审核变更提示 + 撤回 -->
        <a-alert
          v-if="pendingChange"
          class="mb-4"
          type="warning"
          show-icon
        >
          <template #message>
            您有一条待审核的信息变更，审核通过后生效。
            <a-button type="link" size="small" :loading="withdrawing" @click="handleWithdraw">
              撤回变更
            </a-button>
          </template>
        </a-alert>

        <!-- 企业基本信息 -->
        <div class="bg-white rounded-xl shadow-sm p-5 mb-4">
          <div class="flex items-center justify-between mb-3">
            <span class="font-medium text-gray-700">企业基本信息</span>
            <a-space>
              <a-select
                v-model:value="category"
                :disabled="!editable"
                style="width: 120px"
              >
                <a-select-option value="DOMESTIC">国内</a-select-option>
                <a-select-option value="OVERSEAS">国外</a-select-option>
              </a-select>
            </a-space>
          </div>
          <SupplierBasicInfoForm
            v-model="basicInfo"
            :editable="editable"
            :errors="basicErrors"
          />

          <a-divider />
          <BankAccountList v-model:accounts="bankAccounts" :editable="editable" />

          <div v-if="editable" class="mt-4 flex justify-end gap-2">
            <a-button type="primary" :loading="saving" @click="handleSave">
              {{ submissionRequiresReview(profile.status) ? '提交变更（待审核）' : '保存' }}
            </a-button>
            <a-button
              v-if="canSubmitForReview(profile.status)"
              type="primary"
              ghost
              :loading="submitting"
              @click="handleSubmitForReview"
            >
              提交准入审核
            </a-button>
          </div>
        </div>

        <!-- 联系人 -->
        <div class="bg-white rounded-xl shadow-sm p-5 mb-4">
          <ContactList
            ref="contactListRef"
            :contacts="contacts"
            :loading="contactsLoading"
            :saving="contactSaving"
            @save="handleSaveContact"
            @set-primary="handleSetPrimary"
            @delete="handleDeleteContact"
          />
        </div>

        <!-- 证件 -->
        <div class="bg-white rounded-xl shadow-sm p-5">
          <div class="flex items-center justify-between mb-3">
            <span class="font-medium text-gray-700">证件</span>
            <a-button type="primary" size="small" @click="uploadOpen = true">上传证件</a-button>
          </div>
          <CertificateList
            :certificates="certificates"
            :loading="certsLoading"
            :cert-types="certTypes"
          />
        </div>

        <CertificateUpload
          v-model:open="uploadOpen"
          :cert-types="certTypes"
          :loading="uploading"
          @submit="handleUploadCertificate"
        />
      </div>
    </a-spin>

    <!-- 首次登录建议修改密码（可跳过，复用模块01弹窗） -->
    <ChangePasswordDialog v-model:open="pwdDialogOpen" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { useAuthStore } from '@/modules/auth/presentation/stores/auth.store';
import ChangePasswordDialog from '@/modules/auth/presentation/components/ChangePasswordDialog.vue';
import SupplierStatusTag from '../components/SupplierStatusTag.vue';
import SupplierBasicInfoForm from '../components/SupplierBasicInfoForm.vue';
import BankAccountList from '../components/BankAccountList.vue';
import ContactList from '../components/ContactList.vue';
import CertificateList from '../components/CertificateList.vue';
import CertificateUpload from '../components/CertificateUpload.vue';
import {
  isDraftStage,
  isSupplierEditable,
  submissionRequiresReview,
  canSubmitForReview,
} from '../../domain/value-objects/supplier-status.vo';
import { BASIC_INFO_FIELDS } from '../../domain/value-objects/basic-info-fields.vo';
import { fetchMyProfile, validateBasicInfo } from '../../application/manage-supplier-info.usecase';
import {
  executeSubmitChange,
  executeSubmitForReview,
  fetchPendingChange,
  executeWithdraw,
} from '../../application/submit-info-change.usecase';
import {
  fetchContacts,
  executeSaveContact,
  executeSetPrimary,
  executeDeleteContact,
} from '../../application/manage-contacts.usecase';
import {
  fetchSelectableCertTypes,
  fetchMyCertificates,
  executeUploadCertificate,
} from '../../application/manage-certificates.usecase';
import type { SupplierDto, BankAccountDto } from '../../types/dto/supplier.dto';
import type { ContactDto, SaveContactRequest } from '../../types/dto/contact.dto';
import type { CertificateDto, CertTypeDto, UploadCertificateRequest } from '../../types/dto/certificate.dto';
import type { ChangeRequestDto } from '../../types/dto/change.dto';
import type { SupplierCategory } from '../../types/vo/supplier-info.vo';

const loading = ref(false);
const profile = ref<SupplierDto | null>(null);
const pendingChange = ref<ChangeRequestDto | null>(null);

const basicInfo = reactive<Record<string, string>>({});
const basicErrors = ref<Record<string, string>>({});
const bankAccounts = ref<BankAccountDto[]>([]);
const category = ref<SupplierCategory>('DOMESTIC');

const saving = ref(false);
const submitting = ref(false);
const withdrawing = ref(false);

const editable = computed(() => !!profile.value && isSupplierEditable(profile.value.status) && !pendingChange.value);

// 联系人
const contacts = ref<ContactDto[]>([]);
const contactsLoading = ref(false);
const contactSaving = ref(false);
const contactListRef = ref<InstanceType<typeof ContactList> | null>(null);

// 证件
const certificates = ref<CertificateDto[]>([]);
const certsLoading = ref(false);
const certTypes = ref<CertTypeDto[]>([]);
const uploadOpen = ref(false);
const uploading = ref(false);

// 首次登录建议改密
const authStore = useAuthStore();
const pwdDialogOpen = ref(false);

onMounted(() => {
  if (authStore.isFirstLogin) {
    pwdDialogOpen.value = true;
  }
  load();
});

async function load() {
  loading.value = true;
  try {
    profile.value = await fetchMyProfile();
    hydrateForm(profile.value);
    pendingChange.value = await fetchPendingChange();
    await Promise.all([loadContacts(), loadCertificates(), loadCertTypes()]);
  } catch {
    message.error('加载企业信息失败');
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
  category.value = dto.category;
}

async function loadContacts() {
  contactsLoading.value = true;
  try {
    contacts.value = await fetchContacts(null);
  } finally {
    contactsLoading.value = false;
  }
}

async function loadCertificates() {
  certsLoading.value = true;
  try {
    certificates.value = await fetchMyCertificates();
  } finally {
    certsLoading.value = false;
  }
}

async function loadCertTypes() {
  certTypes.value = await fetchSelectableCertTypes();
}

async function handleSave() {
  basicErrors.value = {};
  const errors = validateBasicInfo(basicInfo);
  if (errors) {
    basicErrors.value = errors as Record<string, string>;
    message.error('请检查表单填写');
    return;
  }
  saving.value = true;
  const result = await executeSubmitChange({
    basicInfo: { ...basicInfo },
    bankAccounts: bankAccounts.value,
  });
  saving.value = false;
  if (result.success) {
    message.success(
      profile.value && submissionRequiresReview(profile.value.status)
        ? '变更已提交，等待审核'
        : '保存成功',
    );
    await load();
  } else {
    message.error(result.error || '保存失败');
  }
}

async function handleSubmitForReview() {
  submitting.value = true;
  const result = await executeSubmitForReview();
  submitting.value = false;
  if (result.success) {
    message.success('已提交准入审核');
    await load();
  } else {
    message.error(result.error || '提交失败');
  }
}

async function handleWithdraw() {
  withdrawing.value = true;
  const result = await executeWithdraw();
  withdrawing.value = false;
  if (result.success) {
    message.success('已撤回变更');
    await load();
  } else {
    message.error(result.error || '撤回失败');
  }
}

// ===== 联系人事件 =====
async function handleSaveContact(payload: { contactId: number | null; data: SaveContactRequest }) {
  contactSaving.value = true;
  const result = await executeSaveContact(null, payload.contactId, payload.data);
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
  const result = await executeSetPrimary(null, contact.id);
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
      const result = await executeDeleteContact(null, contact);
      if (result.success) {
        message.success('删除成功');
        await loadContacts();
      } else {
        message.error(result.error || '删除失败');
      }
    },
  });
}

// ===== 证件事件 =====
async function handleUploadCertificate(payload: { file: File; request: UploadCertificateRequest }) {
  uploading.value = true;
  const result = await executeUploadCertificate(payload.file, payload.request);
  uploading.value = false;
  if (result.success) {
    message.success('证件已上传，等待审核');
    uploadOpen.value = false;
    await loadCertificates();
  } else {
    message.error(result.error || '上传失败');
  }
}
</script>
