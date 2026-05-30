<template>
  <a-form layout="vertical">
    <a-row :gutter="16">
      <a-col v-for="field in BASIC_INFO_FIELDS" :key="field.key as string" :span="colSpan(field.type)">
        <a-form-item
          :label="field.label"
          :required="field.required"
          :validate-status="errors[field.key as string] ? 'error' : undefined"
          :help="errors[field.key as string]"
        >
          <!-- 文本域 -->
          <a-textarea
            v-if="field.type === 'textarea'"
            v-model:value="model[field.key as string]"
            :disabled="!editable"
            :rows="2"
            :placeholder="`请输入${field.label}`"
          />
          <!-- 日期 -->
          <a-date-picker
            v-else-if="field.type === 'date'"
            v-model:value="model[field.key as string]"
            value-format="YYYY-MM-DD"
            :disabled="!editable"
            style="width: 100%"
            :placeholder="`请选择${field.label}`"
          />
          <!-- 一般纳税人（布尔） -->
          <a-select
            v-else-if="field.type === 'boolean'"
            v-model:value="model[field.key as string]"
            :disabled="!editable"
            allow-clear
            :placeholder="`请选择${field.label}`"
          >
            <a-select-option value="true">是</a-select-option>
            <a-select-option value="false">否</a-select-option>
          </a-select>
          <!-- 数值 -->
          <a-input
            v-else-if="field.type === 'number' || field.type === 'integer'"
            v-model:value="model[field.key as string]"
            :disabled="!editable"
            :placeholder="`请输入${field.label}`"
          />
          <!-- 文本 -->
          <a-input
            v-else
            v-model:value="model[field.key as string]"
            :disabled="!editable"
            :placeholder="`请输入${field.label}`"
          />
        </a-form-item>
      </a-col>
    </a-row>
  </a-form>
</template>

<script setup lang="ts">
import {
  BASIC_INFO_FIELDS,
  type BasicFieldType,
} from '../../domain/value-objects/basic-info-fields.vo';

const model = defineModel<Record<string, string>>({ required: true });

withDefaults(
  defineProps<{ editable?: boolean; errors?: Record<string, string> }>(),
  { editable: true, errors: () => ({}) },
);

function colSpan(type: BasicFieldType): number {
  return type === 'textarea' ? 24 : 8;
}
</script>
