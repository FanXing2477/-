<template>
  <div class="index-container">
    <div class="hero">
      <h1 class="title">📝 智能笔记</h1>
      <p class="subtitle">Smart Note - 让记录更有价值</p >
      <div class="button-group">
        <el-button type="primary" size="large" @click="$router.push('/login')">登录</el-button>
        <el-button size="large" @click="$router.push('/register')">注册</el-button>
        <el-button text size="large" @click="$router.push('/login')">忘记密码？</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.index-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.hero {
  text-align: center;
  color: white;
}
.title {
  font-size: 56px;
  margin-bottom: 16px;
  font-weight: bold;
}
.subtitle {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 40px;
}
.button-group {
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: center;
}
</style>