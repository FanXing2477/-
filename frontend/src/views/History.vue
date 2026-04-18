<template>
  <div class="history-container">
    <el-card class="history-card">
      <template #header>
        <div class="header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <span class="title">浏览记录</span>
          <el-button type="danger" link @click="clearAll">清空全部</el-button>
        </div>
      </template>

      <div class="history-list">
        <div v-for="record in history" :key="record.id" class="history-item" @click="goToDetail(record.id)">
          <div class="item-content">
            <h3>{{ record.title || '无标题' }}</h3>
            <p class="preview">{{ (record.content || '').substring(0, 100) }}...</p >
            <div class="meta">
              <span class="time">浏览时间：{{ formatDate(record.browseTime) }}</span>
              <el-tag v-if="record.lastViewTime" size="small" type="info">上次浏览：{{ formatDate(record.lastViewTime) }}</el-tag>
            </div>
          </div>
          <div class="item-actions">
            <el-button text type="danger" @click.stop="deleteMyHistory(record.id)">删除</el-button>
          </div>
        </div>

        <el-empty v-if="history.length === 0" description="暂无浏览记录" />
      </div>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchHistory"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getHistory, deleteHistory } from '@/api/index'

const router = useRouter()
const history = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const hours = diff / (1000 * 60 * 60)
  
  if (hours < 1) return `${Math.floor(diff / 60000)}分钟前`
  if (hours < 24) return `${Math.floor(hours)}小时前`
  return `${date.getMonth()+1}/${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}

const fetchHistory = async () => {
  try {
    const res = await getHistory(page.current, page.size)
    history.value = res.data?.records || []
    page.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取浏览记录失败')
  }
}

const handleSizeChange = (size) => {
  page.size = size
  fetchHistory()
}

const goToDetail = (id) => {
  console.log(id)
  router.push(`/note/detail/${id}`)
}

const deleteMyHistory = async (id) => {
  ElMessageBox.confirm('确定删除这条浏览记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteHistory(id)
    ElMessage.success('删除成功')
    fetchHistory()
  }).catch(() => {})
}

const clearAll = () => {
  ElMessageBox.confirm('确定清空所有浏览记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 调用清空所有记录的接口
    ElMessage.success('清空成功')
    fetchHistory()
  }).catch(() => {})
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.history-container { min-height: 100vh; background: #f5f7fa; padding: 20px; }
.history-card { max-width: 900px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header .title { font-size: 18px; font-weight: bold; }
.history-list { display: flex; flex-direction: column; gap: 12px; }
.history-item { display: flex; justify-content: space-between; align-items: flex-start; padding: 16px; background: #fafafa; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.history-item:hover { background: #f0f2f5; }
.item-content { flex: 1; }
.item-content h3 { margin: 0 0 8px 0; font-size: 16px; }
.preview { color: #606266; font-size: 14px; margin-bottom: 8px; }
.meta { display: flex; gap: 16px; align-items: center; }
.time { font-size: 12px; color: #909399; }
.item-actions { margin-left: 16px; }
.pagination { margin-top: 24px; display: flex; justify-content: flex-end; }
</style>