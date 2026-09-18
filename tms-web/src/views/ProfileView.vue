<template>
  <div class="page">
    <el-card>
      <template #header>个人中心</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="登录账号">{{ user.member?.account }}</el-descriptions-item>
        <el-descriptions-item label="所属客户">{{ user.member?.tenantName }}</el-descriptions-item>
        <el-descriptions-item label="所属机构">{{ user.member?.orgName }}</el-descriptions-item>
        <el-descriptions-item label="邮箱验证">
          <el-tag :type="user.member?.emailVerified ? 'success' : 'info'">
            {{ user.member?.emailVerified ? '已验证' : '未验证' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="form">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="选填，更换邮箱后需要重新验证" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">保存资料</el-button>
          <el-button :disabled="!form.email" @click="verifyEmail">发送邮箱验证</el-button>
          <el-button @click="router.push({ name: 'change-password' })">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { sendEmailVerify, updateProfile } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ nickname: '', email: '', phone: '' })
const rules: FormRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

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
    ElMessage.success('资料已保存')
  } finally {
    loading.value = false
  }
}

async function verifyEmail() {
  await sendEmailVerify()
  ElMessage.success('验证邮件已发送，请在邮箱中完成验证')
}
</script>

<style scoped>
.form {
  max-width: 520px;
  margin-top: 20px;
}
</style>
