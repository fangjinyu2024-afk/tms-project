<template>
  <div class="page">
    <el-card>
      <template #header>{{ t('profile.title') }}</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="t('profile.account')">
          {{ user.member?.account }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('profile.tenant')">
          {{ user.member?.tenantName }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('profile.org')">
          {{ user.member?.orgName }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('profile.emailVerified')">
          <el-tag :type="user.member?.emailVerified ? 'success' : 'info'">
            {{ user.member?.emailVerified ? t('profile.verified') : t('profile.unverified') }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="form">
        <el-form-item :label="t('profile.nickname')" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item :label="t('profile.email')" prop="email">
          <el-input v-model="form.email" :placeholder="t('profile.emailPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('profile.phone')" prop="phone">
          <el-input v-model="form.phone" :placeholder="t('profile.phonePlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">
            {{ t('profile.saveProfile') }}
          </el-button>
          <el-button :disabled="!form.email" @click="verifyEmail">
            {{ t('profile.sendVerify') }}
          </el-button>
          <el-button @click="router.push({ name: 'change-password' })">
            {{ t('menu.changePassword') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { sendEmailVerify, updateProfile } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const { t } = useI18n()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ nickname: '', email: '', phone: '' })
const rules = computed<FormRules>(() => ({
  nickname: [{ required: true, message: t('profile.nicknameRequired'), trigger: 'blur' }],
  email: [{ type: 'email', message: t('profile.emailInvalid'), trigger: 'blur' }]
}))

onMounted(() => {
  form.nickname = user.member?.nickname ?? ''
  form.email = user.member?.email ?? ''
  form.phone = user.member?.phone ?? ''
})

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await updateProfile({ nickname: form.nickname, email: form.email, phone: form.phone })
    await user.loadProfile()
    ElMessage.success(t('profile.profileSaved'))
  } finally {
    loading.value = false
  }
}

async function verifyEmail() {
  await sendEmailVerify()
  ElMessage.success(t('profile.verifySent'))
}
</script>

<style scoped>
.form {
  max-width: 520px;
  margin-top: 20px;
}
</style>
