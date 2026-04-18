<template>
  <div class="home-container">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="user-card">
        <el-avatar :size="80" :src="user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        <div class="user-name">{{ user.nickname || '未设置昵称' }}</div>
        <div class="user-motto">{{ user.motto || '暂无座右铭' }}</div>
      </div>
      
      <el-menu :default-active="activeMenu" router class="side-menu">
        <el-menu-item index="/home">
          <el-icon><Document /></el-icon>
          <span>我的笔记</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <span>浏览记录</span>
        </el-menu-item>
        <el-menu-item index="/friend">
          <el-icon><User /></el-icon>
          <span>我的好友</span>
        </el-menu-item>
        <el-menu-item index="/user/profile">
          <el-icon><Setting /></el-icon>
          <span>个人设置</span>
        </el-menu-item>
      </el-menu>
      
      <el-button type="danger" @click="logout" class="logout-btn">退出登录</el-button>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="toolbar">
        <el-button type="primary" @click="$router.push('/note/create')">
          <el-icon><Plus /></el-icon> 新建笔记
        </el-button>
        <el-input 
          v-model="tagFilter" 
          placeholder="按标签筛选（多个用逗号分隔）" 
          style="width: 250px"
          clearable
        />
        <el-button @click="fetchNotesByTag">搜索</el-button>
        <el-button @click="resetFilter">全部</el-button>
      </div>

      <div class="note-list">
        <el-card v-for="note in notes" :key="note.id" class="note-card" @click="goToDetail(note.id)" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">{{ note.title || '无标题' }}</span>
              <el-button text type="danger" @click.stop="handleDelete(note.id)">删除</el-button>
            </div>
          </template>
          <div class="preview">{{ (note.content || '').substring(0, 150) }}...</div>
          <div class="meta">
            <el-tag v-for="tag in note.tags" :key="tag" size="small" class="tag">{{ tag }}</el-tag>
            <span class="time">更新于 {{ formatDate(note.updateTime) }}</span>
          </div>
        </el-card>

        <el-empty v-if="notes.length === 0" description="暂无笔记，快去创建一篇吧~" />
      </div>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchNotes"
          @size-change="handleSizeChange"
        />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Clock, User, Setting, Plus } from '@element-plus/icons-vue'
import { getNotesByTime, getNotesByTags, deleteNote } from '@/api/index'

const router = useRouter()
const notes = ref([])
const tagFilter = ref('')
const activeMenu = ref('/home')
const page = reactive({ current: 1, size: 10, total: 0 })
const user = reactive({ username: '', motto: '', avatarUrl: '' })

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

const fetchNotes = async () => {
  try {
    const res = await getNotesByTime(page.current, page.size)
    notes.value = res.data?.records || []
    page.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取笔记失败')
  }
}

const fetchNotesByTag = async () => {
  if (!tagFilter.value) return fetchNotes()
  try {
    const res = await getNotesByTags(tagFilter.value, page.current, page.size)
    notes.value = res.data?.records || []
    page.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

const resetFilter = () => {
  tagFilter.value = ''
  fetchNotes()
}

const handleSizeChange = (size) => {
  page.size = size
  fetchNotes()
}

const goToDetail = (id) => router.push(`/note/detail/${id}`)

const handleDelete = async (id) => {
  ElMessageBox.confirm('确定删除这篇笔记吗？删除后可在回收站找回', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteNote(id)
    ElMessage.success('删除成功')
    fetchNotes()
  }).catch(() => {})
}

const logout = () => {
  ElMessageBox.confirm('确定退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    localStorage.clear()
    ElMessage.success('已退出')
    router.push('/login')
  }).catch(() => {})
}

const loadUserInfo = () => {
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    const info = JSON.parse(userStr)
    user.nickname = info.nickname
    user.motto = info.motto
    user.avatarUrl = info.avatarUrl
  }
}

onMounted(() => {
  fetchNotes()
  loadUserInfo()
})
</script>

<style scoped>
.home-container { display: flex; min-height: 100vh; background: #f5f7fa; }
.sidebar { width: 260px; background: white; display: flex; flex-direction: column; border-right: 1px solid #e4e7ed; }
.user-card { text-align: center; padding: 20px; border-bottom: 1px solid #eee; }
.user-name { font-size: 16px; font-weight: bold; margin-top: 12px; }
.user-motto { font-size: 12px; color: #909399; margin-top: 6px; }
.side-menu { flex: 1; border-right: none; }
.logout-btn { margin: 20px; width: calc(100% - 40px); }
.main-content { flex: 1; padding: 20px; overflow-y: auto; }
.toolbar { display: flex; gap: 12px; margin-bottom: 20px; align-items: center; flex-wrap: wrap; }
.note-list { display: flex; flex-direction: column; gap: 16px; }
.note-card { cursor: pointer; transition: all 0.2s; }
.note-card:hover { transform: translateY(-2px); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header .title { font-size: 16px; font-weight: bold; }
.preview { color: #606266; line-height: 1.6; margin-bottom: 12px; }
.meta { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.tag { margin-right: 8px; }
.time { font-size: 12px; color: #909399; }
.pagination { margin-top: 24px; display: flex; justify-content: flex-end; }
</style>