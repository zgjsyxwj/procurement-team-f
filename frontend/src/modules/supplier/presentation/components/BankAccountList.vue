<template>
  <div>
    <div class="flex items-center justify-between mb-2">
      <span class="text-sm text-gray-500">银行信息（非必填；填写则户名/开户银行/账号三项必填）</span>
      <a-button v-if="editable" type="link" size="small" @click="addRow">+ 新增一组</a-button>
    </div>

    <a-empty v-if="accounts.length === 0" description="暂无银行信息" :image="simpleImage" />

    <div
      v-for="(account, index) in accounts"
      :key="index"
      class="mb-3 p-3 border border-gray-200 rounded-lg"
    >
      <a-row :gutter="12" align="middle">
        <a-col :span="7">
          <a-input
            v-model:value="account.accountName"
            placeholder="户名"
            :disabled="!editable"
          />
        </a-col>
        <a-col :span="8">
          <a-input
            v-model:value="account.bankName"
            placeholder="开户银行名称"
            :disabled="!editable"
          />
        </a-col>
        <a-col :span="7">
          <a-input
            v-model:value="account.accountNumber"
            placeholder="银行账号"
            :disabled="!editable"
          />
        </a-col>
        <a-col :span="2">
          <a-button v-if="editable" type="link" danger size="small" @click="removeRow(index)">
            删除
          </a-button>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Empty } from 'ant-design-vue';
import type { BankAccount } from '../../domain/value-objects/bank-account.vo';
import { emptyBankAccount } from '../../domain/value-objects/bank-account.vo';

const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;

const accounts = defineModel<BankAccount[]>('accounts', { required: true });

withDefaults(defineProps<{ editable?: boolean }>(), { editable: true });

function addRow() {
  accounts.value = [...accounts.value, emptyBankAccount()];
}

function removeRow(index: number) {
  accounts.value = accounts.value.filter((_, i) => i !== index);
}
</script>
