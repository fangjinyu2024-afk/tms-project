<template>
  <div class="login">
    <el-card class="login-card">
      <div class="login-brand">
        <div class="brand-icon">T</div>
        <div>
          <strong>TMS 终端管理系统</strong>
          <small>POS 终端交付后的集中管理平台</small>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="登录账号" prop="account">
          <el-input v-model="form.account" placeholder="请输入登录账号" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            autocomplete="current-password"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">登录</el-button>
        <div class="login-foot">
          <el-link type="primary" :underline="false" @click="forgotVisible = true">找回密码</el-link>
        </div>
      </el-form>
    </el-card>

    <el-dialog v-model="forgotVisible" title="找回密码" width="420px">
      <p class="form-tip">
        仅对已验证邮箱的账号可用；无邮箱的成员请联系有权限的管理员重置密码。
      </p>
      <el-input v-model="forgotAccount" placeholder="请输入登录账号" />
      <template #footer>
        <el-button @click="forgotVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForgot">发送找回邮件</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { forgotPassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const user = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const forgotVisible = ref(false)
const forgotAccount = ref('')

const form = reactive({ account: '', password: '' })
const rules: FormRules = {
  account: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

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
    ElMessage.warning('请输入登录账号')
    return
  }
  await forgotPassword(forgotAccount.value)
  forgotVisible.value = false
  ElMessage.success('若该账号已验证邮箱，找回邮件已发送')
}
</script>

<style scoped>
.login {
  height: 100%;
  display: grid;
  place-items: center;
  background: linear-gradient(140deg, #1677ff 0%, #0958d9 100%);
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
