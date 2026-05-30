<template>
  <div class="p-6 max-w-2xl mx-auto">
    <div class="flex items-center gap-3 mb-6">
      <a-button @click="goBack">返回</a-button>
      <h2 class="text-xl font-semibold text-gray-800">创建供应商</h2>
    </div>

    <div class="bg-white rounded-xl shadow-sm p-6">
      <a-form layout="vertical">
        <a-form-item label="企业名称" required>
          <a-input v-model:value="form.name" placeholder="请输入企业名称" />
        </a-form-item>
        <a-form-item label="供应商分类" required>
          <a-select v-model:value="form.category" placeholder="请选择分类">
            <a-select-option value="DOMESTIC">国内</a-select-option>
            <a-select-option value="OVERSEAS">国外</a-select-option>
          </a-select>
        </a-form-item>

        <a-divider orientation="left">主要联系人</a-divider>
        <a-form-item label="姓名" required>
          <a-input v-model:value="form.contactName" placeholder="请输入主要联系人姓名" />
        </a-form-item>
        <a-form-item label="手机号" required>
          <a-input v-model:value="form.contactPhone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="邮箱" required>
          <a-input v-model:value="form.contactEmail" placeholder="请输入邮箱" />
        </a-form-item>

        <div class="flex justify-end gap-2 mt-4">
          <a-button :loading="savingOnly" @click="submit(false)">仅保存</a-button>
          <a-button type="primary" :loading="savingInvite" @click="submit(true)">
            保存并发送邀请
          </a-button>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { executeCreateSupplier } from '../../application/create-supplier.usecase';
import type { CreateSupplierCommand } from '../../types/command/create-supplier.command';

const router = useRouter();
const savingOnly = ref(false);
const savingInvite = ref(false);

const form = reactive<CreateSupplierCommand>({
  name: '',
  category: 'DOMESTIC',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  sendInvitation: false,
});

async function submit(sendInvitation: boolean) {
  const loadingRef = sendInvitation ? savingInvite : savingOnly;
  loadingRef.value = true;
  const result = await executeCreateSupplier({ ...form, sendInvitation });
  loadingRef.value = false;
  if (result.success) {
    message.success(sendInvitation ? '供应商已创建并发送邀请' : '供应商已创建');
    router.push('/suppliers');
  } else {
    message.error(result.error || '创建失败');
  }
}

function goBack() {
  router.push('/suppliers');
}
</script>
