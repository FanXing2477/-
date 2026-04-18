<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <template #header>
        <h2>登录</h2>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="account">
          <el-input 
            v-model="form.account" 
            placeholder="邮箱 / 手机号"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="links">
        <el-link type="primary" @click="$router.push('/register')">没有账号？立即注册</el-link>
        <el-link type="primary" @click="showForgot = true">忘记密码？</el-link>
      </div>
    </el-card>

    <!-- 忘记密码弹窗 -->
    <el-dialog v-model="showForgot" title="重置密码" width="400px">
      <el-form :model="forgotForm" :rules="forgotRules" ref="forgotFormRef">
        <el-form-item prop="account">
          <el-input v-model="forgotForm.account" placeholder="邮箱 / 手机号" />
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input v-model="forgotForm.newPassword" type="password" placeholder="新密码" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="forgotForm.confirmPassword" type="password" placeholder="确认密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgot = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword" :loading="resetLoading">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, changePassword, getUserInfo } from '@/api/index'
import { parseToken } from '@/utils/jwt'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const showForgot = ref(false)
const resetLoading = ref(false)
const forgotFormRef = ref()

const form = reactive({ account: '', password: '' })
const forgotForm = reactive({ account: '', newPassword: '', confirmPassword: '' })

const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const phoneRegex = /^1[3-9]\d{9}$/

const rules = {
  account: [{ required: true, message: '请输入邮箱或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const forgotRules = {
  account: [{ required: true, message: '请输入邮箱或手机号', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.newPassword) callback(new Error('两次密码不一致'))
        else callback()
      }, trigger: 'blur'
    }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  const isEmail = emailRegex.test(form.account)
  const isPhone = phoneRegex.test(form.account)
  if (!isEmail && !isPhone) {
    ElMessage.error('请输入正确的邮箱或手机号')
    return
  }
  
  loading.value = true
  try {
    const loginData = { password: form.password }
    if (isEmail) loginData.email = form.account
    else loginData.phone = form.account
    
    const res = await login(loginData)
    if (res.code === 200) {
      const token = res.data // 或 res.data，看你后端返回结构
      localStorage.setItem('token', token)

      const userRes = await getUserInfo()
      console.log('用户信息', userRes)
      localStorage.setItem('userInfo', JSON.stringify(userRes.data))

      ElMessage.success('登录成功')
      router.push('/home')
    } 
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleResetPassword = async () => {
  const valid = await forgotFormRef.value?.validate()
  if (!valid) return
  
  const isEmail = emailRegex.test(forgotForm.account)
  const isPhone = phoneRegex.test(forgotForm.account)
  if (!isEmail && !isPhone) {
    ElMessage.error('请输入正确的邮箱或手机号')
    return
  }
  
  resetLoading.value = true
  try {
    const data = { newPassword: forgotForm.newPassword, confirmPassword: forgotForm.confirmPassword }
    if (isEmail) data.email = forgotForm.account
    else data.phone = forgotForm.account
    
    const res = await changePassword(data)
    if (res.code === 200) {
      ElMessage.success('密码重置成功，请登录')
      showForgot.value = false
      forgotForm.account = ''
      forgotForm.newPassword = ''
      forgotForm.confirmPassword = ''
    } else {
      ElMessage.error(res.msg || '重置失败')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '重置失败')
  } finally {
    resetLoading.value = false
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
  width: 400px;
  max-width: 90%;
}
.auth-card :deep(.el-card__header) {
  text-align: center;
  font-size: 24px;
}
.links {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
}
</style>