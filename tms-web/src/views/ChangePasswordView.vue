<template>
  <div class="wrapper">
    <el-card class="card">
      <h3>{{ user.mustChangePassword ? '首次登录，请修改初始密码' : '修改密码' }}</h3>
      <p class="form-tip">修改成功后当前登录会话将失效，需要使用新密码重新登录。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
          <el-button @click="backOrLogout">{{ user.mustChangePassword ? '退出登录' : '返回' }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 64, message: '新密码长度为 8 至 64 位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) =>
        value === form.newPassword ? callback() : callback(new Error('两次输入的新密码不一致')),
      trigger: 'blur'
    }
  ]
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    user.clear()
    ElMessage.success('密码已修改，请重新登录')
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
  width: 480px;
}
</style>
