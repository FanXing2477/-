<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <span class="title">个人设置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="infoForm" label-width="100px">
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="uploadAvatar"
              >
                <img :src="infoForm?.avatarUrl || defaultAvatar" class="avatar" />
                <div class="upload-hint">点击更换头像</div>
              </el-upload>
            </el-form-item>

            <el-form-item label="昵称">
              <el-input v-model="infoForm.nickname" style="width: 300px" />
              <el-button type="primary" @click="saveNickname" :loading="nickLoading" style="margin-left: 12px">保存</el-button>
            </el-form-item>

            <el-form-item label="座右铭">
              <el-input v-model="infoForm.motto" placeholder="写一句座右铭吧~" style="width: 300px" />
              <el-button type="primary" @click="saveMotto" :loading="mottoLoading" style="margin-left: 12px">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px" style="max-width: 400px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changeMyPassword" :loading="pwdLoading">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 账号安全 -->
        <el-tab-pane label="账号安全" name="security">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '未绑定' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo.phone || '未绑定' }}</el-descriptions-item>
          </el-descriptions>
          
          <div class="danger-zone">
            <h4>危险操作区</h4>
            <el-button type="danger" @click="handleDeleteAccount">注销账号</el-button>
            <span class="hint">注销后所有笔记将被删除，且不可恢复</span>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { updateNickname, updateMotto, updateAvatar, deleteUser, updatePassword } from '@/api/index'

const router = useRouter()
const activeTab = ref('info')
const pwdFormRef = ref()
const nickLoading = ref(false)
const mottoLoading = ref(false)
const pwdLoading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const infoForm = reactive({
  nickname: '',
  motto: '',
  avatarUrl: ''
})

const userInfo = reactive({
  username: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
        else callback()
      }, trigger: 'blur'
    }
  ]
}

const loadUserInfo = () => {
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    const info = JSON.parse(userStr)
    infoForm.nickname = info.nickname || ''
    infoForm.motto = info.motto || ''
    infoForm.avatarUrl = info.avatarUrl || ''
    userInfo.username = info.username || ''
    userInfo.email = info.email || ''
    userInfo.phone = info.phone || ''
  }
}

const saveNickname = async () => {
  nickLoading.value = true
  try {
    await updateNickname(infoForm.nickname)
    const userStr = localStorage.getItem('userInfo')
    const info = JSON.parse(userStr)
    info.nickname = infoForm.nickname
    localStorage.setItem('userInfo', JSON.stringify(info))
    ElMessage.success('昵称修改成功')
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    nickLoading.value = false
  }
}

const saveMotto = async () => {
  mottoLoading.value = true
  try {
    await updateMotto(infoForm.motto)
    const userStr = localStorage.getItem('userInfo')
    const info = JSON.parse(userStr)
    info.motto = infoForm.motto
    localStorage.setItem('userInfo', JSON.stringify(info))
    ElMessage.success('座右铭修改成功')
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    mottoLoading.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt10M) ElMessage.error('图片大小不能超过10MB')
  return isImage && isLt10M
}

const uploadAvatar = async (options) => {
  try {
    const res = await updateAvatar(options.file)
    console.log('res:', res)
    
    if (res && res.code === 200) {
      infoForm.avatarUrl = res.data
      
      // 更新 localStorage
      const userStr = localStorage.getItem('userInfo')
      if (userStr) {
        const info = JSON.parse(userStr)
        info.avatarUrl = res.data
        localStorage.setItem('userInfo', JSON.stringify(info))
      } else {
        // 如果 userInfo 不存在，就创建一个
        const info = {
          avatarUrl: res.data
        }
        localStorage.setItem('userInfo', JSON.stringify(info))
      }
      
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res?.message || '上传失败')
    }
  } catch (error) {
    console.error('上传错误:', error)
    ElMessage.error('上传失败')
  }
}

const changeMyPassword = async () => {
  // 表单验证
  const valid = await pwdFormRef.value?.validate()
  if (!valid) return

  pwdLoading.value = true
  try {
    // 调用修改密码接口（需要原密码）
    const params = {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
      confirmPassword: pwdForm.confirmPassword
    }
    
    console.log('修改密码参数:', params)
    
    // 调用新接口 /user/update_password
    const res = await updatePassword(params)
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      setTimeout(() => {
        localStorage.clear()
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '修改失败')
  } finally {
    pwdLoading.value = false
  }
}

const handleDeleteAccount = () => {
  ElMessageBox.confirm('注销后所有数据将被永久删除，且无法恢复。确定注销吗？', '危险操作', {
    confirmButtonText: '确定注销',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    await deleteUser()
    localStorage.clear()
    ElMessage.success('账号已注销')
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container { min-height: 100vh; background: #f5f7fa; padding: 20px; }
.profile-card { max-width: 800px; margin: 0 auto; }
.header { display: flex; align-items: center; gap: 16px; }
.header .title { font-size: 18px; font-weight: bold; }
.avatar-uploader { text-align: center; cursor: pointer; }
.avatar { width: 100px; height: 100px; border-radius: 50%; object-fit: cover; }
.upload-hint { font-size: 12px; color: #909399; margin-top: 8px; }
.danger-zone { margin-top: 32px; padding-top: 24px; border-top: 1px solid #fde2e2; }
.danger-zone h4 { color: #f56c6c; margin-bottom: 16px; }
.hint { margin-left: 12px; font-size: 12px; color: #909399; }
</style>