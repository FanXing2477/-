<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <template #header>
        <h2>注册</h2>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名 *" size="large" prefix-icon="User" />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" size="large" prefix-icon="Message" />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" size="large" prefix-icon="Iphone" />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码 * (6-20位)" size="large" prefix-icon="Lock" />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码 *" size="large" prefix-icon="Lock" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="links">
        <el-link type="primary" @click="$router.push('/login')">已有账号？立即登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/index'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '', email: '', phone: '', password: '', confirmPassword: ''
})

const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const phoneRegex = /^1[3-9]\d{9}$/

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ validator: (rule, value, cb) => {
    if (!value) cb()
    else if (!emailRegex.test(value)) cb(new Error('邮箱格式不正确'))
    else cb()
  }, trigger: 'blur' }],
  phone: [{ validator: (rule, value, cb) => {
    if (!value) cb()
    else if (!phoneRegex.test(value)) cb(new Error('手机号格式不正确'))
    else cb()
  }, trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, cb) => {
      if (value !== form.password) cb(new Error('两次密码不一致'))
      else cb()
    }, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  const hasEmail = form.email && emailRegex.test(form.email)
  const hasPhone = form.phone && phoneRegex.test(form.phone)
  if (!hasEmail && !hasPhone) {
    ElMessage.error('邮箱和手机号至少填写一个且格式正确')
    return
  }
  
  loading.value = true
  try {
    const data = {
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword
    }
    if (hasEmail) data.email = form.email
    if (hasPhone) data.phone = form.phone
    
    const res = await register(data)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.auth-card {
  width: 450px;
  max-width: 90%;
}
.auth-card :deep(.el-card__header) {
  text-align: center;
  font-size: 24px;
}
.links {
  text-align: center;
  margin-top: 16px;
}
</style>