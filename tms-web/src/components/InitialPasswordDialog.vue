<template>
  <el-dialog :model-value="visible" title="账号已创建" width="420px" @update:model-value="close">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="登录账号">{{ account }}</el-descriptions-item>
      <el-descriptions-item label="初始密码">
        <span class="password">{{ password }}</span>
      </el-descriptions-item>
    </el-descriptions>
    <p class="form-tip">
      初始密码由系统生成，仅本次显示一次，请自行转交本人；对方首次登录必须修改密码。
    </p>
    <template #footer>
      <el-button type="primary" @click="close">我已记录</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean; account?: string; password?: string }>()
const emit = defineEmits<{ (event: 'close'): void }>()

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
