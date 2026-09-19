<template>
  <div class="login">
    <div class="lang">
      <LanguageSwitch />
    </div>
    <el-card class="login-card">
      <div class="login-brand">
        <div class="brand-icon">T</div>
        <div>
          <strong>{{ t('app.fullName') }}</strong>
          <small>{{ t('app.description') }}</small>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item :label="t('login.account')" prop="account">
          <el-input v-model="form.account" :placeholder="t('login.accountPlaceholder')" autocomplete="username" />
        </el-form-item>
        <el-form-item :label="t('login.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="t('login.passwordPlaceholder')"
            autocomplete="current-password"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">
          {{ t('login.submit') }}
        </el-button>
        <div class="login-foot">
          <el-link type="primary" :underline="false" @click="forgotVisible = true">
            {{ t('login.forgot') }}
          </el-link>
        </div>
      </el-form>
    </el-card>

    <el-dialog v-model="forgotVisible" :title="t('login.forgotTitle')" width="420px">
      <p class="form-tip">{{ t('login.forgotTip') }}</p>
      <el-input v-model="forgotAccount" :placeholder="t('login.accountPlaceholder')" />
      <template #footer>
        <el-button @click="forgotVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForgot">{{ t('login.forgotSubmit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import LanguageSwitch from '@/components/LanguageSwitch.vue'
import { forgotPassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)
const forgotVisible = ref(false)
const forgotAccount = ref('')

const form = reactive({ account: '', password: '' })
const rules = computed<FormRules>(() => ({
  account: [{ required: true, message: t('login.accountRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.passwordRequired'), trigger: 'blur' }]
}))

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    const data = await user.login(form.account, form.password)
    if (data.mustChangePassword) {
      await router.push({ name: 'change-password' })
      return
    }
    const redirect = route.query.redirect as string | undefined
    await router.push(redirect || '/')
  } finally {
    loading.value = false
  }
}

async function submitForgot() {
  if (!forgotAccount.value) {
    ElMessage.warning(t('login.accountRequired'))
    return
  }
  await forgotPassword(forgotAccount.value)
  forgotVisible.value = false
  ElMessage.success(t('login.forgotSent'))
}
</script>

<style scoped>
.login {
  position: relative;
  height: 100%;
  display: grid;
  place-items: center;
  background: linear-gradient(140deg, #1677ff 0%, #0958d9 100%);
}

.lang {
  position: absolute;
  top: 18px;
  right: 24px;
  color: #fff;
}

.login-card {
  width: 380px;
}

.login-brand {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #1677ff;
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 20px;
  font-weight: 700;
}

.login-brand small {
  display: block;
  color: #66778d;
  font-size: 12px;
}

.submit {
  width: 100%;
}

.login-foot {
  margin-top: 12px;
  text-align: right;
}
</style>
