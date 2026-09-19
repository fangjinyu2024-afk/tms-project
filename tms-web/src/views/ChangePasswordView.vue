<template>
  <div class="wrapper">
    <el-card class="card">
      <h3>{{ user.mustChangePassword ? t('password.firstTitle') : t('password.title') }}</h3>
      <p class="form-tip">{{ t('password.tip') }}</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('password.oldPassword')" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="t('password.newPassword')" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="t('password.confirmPassword')" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">{{ t('common.save') }}</el-button>
          <el-button @click="backOrLogout">
            {{ user.mustChangePassword ? t('layout.signOut') : t('password.back') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const { t } = useI18n()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rules = computed<FormRules>(() => ({
  oldPassword: [{ required: true, message: t('password.oldRequired'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: t('password.newRequired'), trigger: 'blur' },
    { min: 8, max: 64, message: t('password.lengthRule'), trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) =>
        value === form.newPassword ? callback() : callback(new Error(t('password.mismatch'))),
      trigger: 'blur'
    }
  ]
}))

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    user.clear()
    ElMessage.success(t('password.changed'))
    await router.push({ name: 'login' })
  } finally {
    loading.value = false
  }
}

async function backOrLogout() {
  if (user.mustChangePassword) {
    await user.logout()
    await router.push({ name: 'login' })
    return
  }
  router.back()
}
</script>

<style scoped>
.wrapper {
  height: 100%;
  display: grid;
  place-items: center;
  background: #f3f6fa;
}

.card {
  width: 520px;
}
</style>
