<template>
  <el-dialog
    :model-value="visible"
    :title="t('account.createdTitle')"
    width="420px"
    @update:model-value="close"
  >
    <el-descriptions :column="1" border>
      <el-descriptions-item :label="t('account.account')">{{ account }}</el-descriptions-item>
      <el-descriptions-item :label="t('account.initialPassword')">
        <span class="password">{{ password }}</span>
      </el-descriptions-item>
    </el-descriptions>
    <p class="form-tip">{{ t('account.tip') }}</p>
    <template #footer>
      <el-button type="primary" @click="close">{{ t('account.acknowledge') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{ visible: boolean; account?: string; password?: string }>()
const emit = defineEmits<{ (event: 'close'): void }>()

const { t } = useI18n()

function close() {
  emit('close')
}
</script>

<style scoped>
.password {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 15px;
  letter-spacing: 1px;
}
</style>
