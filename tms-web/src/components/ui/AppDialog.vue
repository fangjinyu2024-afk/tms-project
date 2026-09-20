<template>
  <Teleport to="body">
    <div v-if="modelValue" class="overlay" :class="{ 'drawer-overlay': drawer }" @click.self="close">
      <section
        role="dialog"
        aria-modal="true"
        :aria-label="title"
        class="dialog"
        :class="[size, { drawer }]"
      >
        <div class="dialog-head">
          {{ title }}
          <button type="button" class="x" :aria-label="t('common.close')" @click="close">×</button>
        </div>
        <div class="dialog-body"><slot /></div>
        <div v-if="$slots.footer" class="dialog-foot"><slot name="footer" /></div>
      </section>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    /** 原型的三档宽度：narrow / 默认 760px / wide 970px */
    size?: '' | 'narrow' | 'wide'
    drawer?: boolean
  }>(),
  { size: '' }
)

const emit = defineEmits<{ (event: 'update:modelValue', value: boolean): void }>()

const { t } = useI18n()

function close() {
  emit('update:modelValue', false)
}

/** 弹窗打开时锁住页面滚动，与原型 layer() 行为一致 */
watch(
  () => props.modelValue,
  (open) => {
    document.body.style.overflow = open ? 'hidden' : ''
  },
  { immediate: true }
)
</script>
