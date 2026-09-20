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
      <el-form ref="formRef" :model="form" :rules="rules" :validate-on-rule-change="false" label-position="top" @submit.prevent>
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
          />
        </el-form-item>
        <el-form-item :label="t('login.captcha')" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              :placeholder="t('login.captchaPlaceholder')"
              autocomplete="off"
              maxlength="6"
              @keyup.enter="submit"
            />
            <img
              v-if="captcha.image"
              class="captcha-img"
              :src="captcha.image"
              :alt="t('login.captcha')"
              :title="t('login.captchaRefresh')"
              @click="loadCaptcha"
            />
            <div v-else class="captcha-img captcha-placeholder" @click="loadCaptcha" />
          </div>
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">
          {{ t('login.submit') }}
        </el-button>
        <div class="login-foot">
          <el-link type="primary" :underline="false" @click="openForgot">
            {{ t('login.forgot') }}
          </el-link>
        </div>
      </el-form>
    </el-card>

    <el-dialog v-model="forgotVisible" :title="t('login.forgotTitle')" width="420px">
      <p class="form-tip">{{ t('login.forgotTip') }}</p>
      <el-input v-model="forgotForm.account" :placeholder="t('login.accountPlaceholder')" />
      <div class="captcha-row forgot-captcha">
        <el-input
          v-model="forgotForm.captchaCode"
          :placeholder="t('login.captchaPlaceholder')"
          autocomplete="off"
          maxlength="6"
        />
        <img
          v-if="forgotCaptcha.image"
          class="captcha-img"
          :src="forgotCaptcha.image"
          :alt="t('login.captcha')"
          :title="t('login.captchaRefresh')"
          @click="loadForgotCaptcha"
        />
        <div v-else class="captcha-img captcha-placeholder" @click="loadForgotCaptcha" />
      </div>
      <template #footer>
        <el-button @click="forgotVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForgot">{{ t('login.forgotSubmit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import LanguageSwitch from '@/components/LanguageSwitch.vue'
import { fetchCaptcha, forgotPassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const { t } = useI18n()

const formRef = ref<FormInstance>()
const loading = ref(false)
const forgotVisible = ref(false)

const form = reactive({ account: '', password: '', captchaCode: '' })
const captcha = reactive({ captchaId: '', image: '' })
const forgotForm = reactive({ account: '', captchaCode: '' })
const forgotCaptcha = reactive({ captchaId: '', image: '' })

const rules = computed<FormRules>(() => ({
  account: [{ required: true, message: t('login.accountRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.passwordRequired'), trigger: 'blur' }],
  captchaCode: [{ required: true, message: t('login.captchaRequired'), trigger: 'blur' }]
}))

onMounted(loadCaptcha)

async function loadCaptcha() {
  const data = await fetchCaptcha()
  captcha.captchaId = data.captchaId
  captcha.image = data.image
  form.captchaCode = ''
}

async function loadForgotCaptcha() {
  const data = await fetchCaptcha()
  forgotCaptcha.captchaId = data.captchaId
  forgotCaptcha.image = data.image
  forgotForm.captchaCode = ''
}

function openForgot() {
  forgotForm.account = ''
  forgotVisible.value = true
  void loadForgotCaptcha()
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    const data = await user.login(form.account, form.password, captcha.captchaId, form.captchaCode)
    if (data.mustChangePassword) {
      await router.push({ name: 'change-password' })
      return
    }
    const redirect = route.query.redirect as string | undefined
    await router.push(redirect || '/')
  } catch (error) {
    // 验证码一次性消费，登录失败后旧图已失效，必须换一张
    await loadCaptcha()
    throw error
  } finally {
    loading.value = false
  }
}

async function submitForgot() {
  if (!forgotForm.account) {
    ElMessage.warning(t('login.accountRequired'))
    return
  }
  if (!forgotForm.captchaCode) {
    ElMessage.warning(t('login.captchaRequired'))
    return
  }
  try {
    await forgotPassword({
      account: forgotForm.account,
      captchaId: forgotCaptcha.captchaId,
      captchaCode: forgotForm.captchaCode
    })
  } catch (error) {
    await loadForgotCaptcha()
    throw error
  }
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

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.captcha-img {
  flex: none;
  width: 150px;
  height: 50px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  background: #fff;
  transition: border-color 0.2s;
}

.captcha-img:hover {
  border-color: #1677ff;
}

.captcha-placeholder {
  background: #f5f7fa;
}

.forgot-captcha {
  margin-top: 12px;
}

.submit {
  width: 100%;
}

.login-foot {
  margin-top: 12px;
  text-align: right;
}
</style>
