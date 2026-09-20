<template>
  <div class="field" :class="{ full }">
    <label :for="name">
      <span v-if="required" class="required">*</span>{{ t(labelKey) }}
    </label>
    <textarea
      v-if="type === 'textarea'"
      :id="name"
      :name="name"
      :value="modelValue"
      :required="required"
      @input="onInput"
    />
    <select
      v-else-if="type === 'select'"
      :id="name"
      :name="name"
      :value="modelValue"
      :required="required"
      @change="onInput"
    >
      <option v-if="placeholderKey" value="">{{ t(placeholderKey) }}</option>
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ t(option.labelKey) }}
      </option>
    </select>
    <input
      v-else
      :id="name"
      :name="name"
      :type="type"
      :value="modelValue"
      :required="required"
      :placeholder="placeholderKey ? t(placeholderKey) : undefined"
      @input="onInput"
    />
    <small v-if="helpKey">{{ t(helpKey) }}</small>
    <small v-if="error" class="error">{{ error }}</small>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

withDefaults(
  defineProps<{
    name: string
    labelKey: string
    modelValue?: string
    type?: 'text' | 'password' | 'email' | 'date' | 'number' | 'textarea' | 'select'
    required?: boolean
    full?: boolean
    placeholderKey?: string
    helpKey?: string
    error?: string
    options?: { value: string; labelKey: string }[]
  }>(),
  { type: 'text', modelValue: '' }
)

const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>()

const { t } = useI18n()

function onInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value)
}
</script>
