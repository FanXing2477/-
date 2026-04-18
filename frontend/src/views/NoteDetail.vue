<template>
  <div class="detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <div class="title-actions">
            <el-button type="primary" link @click="showEditDialog = true" v-if="canEdit">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-dropdown @command="handleVisibility">
              <el-button link>
                权限设置 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="0">仅自己可见</el-dropdown-item>
                  <el-dropdown-item :command="1">所有人可见</el-dropdown-item>
                  <el-dropdown-item :command="2">部分好友可编辑</el-dropdown-item>
                  <el-dropdown-item :command="3">所有人可见</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="danger" link @click="handleDelete">删除</el-button>
          </div>
        </div>
      </template>

      <div class="note-content">
        <h1 class="note-title">{{ note.title }}</h1>
        
        <div class="note-meta">
          <div class="tags">
            <el-tag v-for="tag in note.tags" :key="tag" size="small">{{ tag }}</el-tag>
          </div>
          <span class="time">最后更新：{{ formatDate(note.updateTime) }}</span>
        </div>

        <el-divider />

        <div class="markdown-body" v-html="renderedContent"></div>
      </div>

      <!-- AI分析区域 -->
      <div class="ai-section">
        <div class="ai-header">
          <h3>🤖 AI智能分析</h3>
          <el-button type="primary" size="small" @click="analyzeNote" :loading="analyzing">
            {{ aiResult ? '重新分析' : '生成分析' }}
          </el-button>
        </div>
        <div v-if="aiResult" class="ai-result">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="摘要">{{ aiResult.summary || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="关键词">{{ aiResult.keywords || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="建议标签">{{ aiResult.suggestedTags || '暂无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <el-empty v-else description="点击按钮让AI帮你分析这篇笔记" :image-size="80" />
      </div>
       <!-- 分享设置 -->
      <div class="share-section" v-if="visibility === 1">
        <el-alert type="info" :closable="false">
          <template #title>
            <span>分享链接：{{ shareUrl }}</span>
            <el-button size="small" @click="copyLink" style="margin-left: 12px">复制链接</el-button>
          </template>
        </el-alert>
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑笔记" width="80%">
      <el-form :model="editForm" ref="editFormRef">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editForm.content" type="textarea" :rows="12" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分享给好友弹窗 -->
    <el-dialog v-model="showShareDialog" title="分享给好友" width="400px">
      <el-select v-model="shareTarget" placeholder="选择好友" style="width: 100%">
        <el-option v-for="friend in friendList" :key="friend.id" :label="friend.username" :value="friend.id" />
      </el-select>
      <el-select v-model="sharePermission" placeholder="权限类型" style="width: 100%; margin-top: 12px">
        <el-option label="仅查看" :value="1" />
        <el-option label="可编辑" :value="2" />
      </el-select>
      <template #footer>
        <el-button @click="showShareDialog = false">取消</el-button>
        <el-button type="primary" @click="doShare">确认分享</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, ArrowDown } from '@element-plus/icons-vue'
import { getNoteDetail, updateNote, deleteNote, setNoteVisibility, aiAnalyze, shareToUser, getFriendList, addHistory } from '@/api/index'
import { parseToken } from '@/utils/jwt'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()
const noteId = ref(route.params.id)

const note = reactive({ title: '', content: '', tags: [] })
const aiResult = ref(null)
const analyzing = ref(false)
const saving = ref(false)
const canEdit = ref(true)
const visibility = ref(0)
const shareUrl = ref('')
const showEditDialog = ref(false)
const showShareDialog = ref(false)
const friendList = ref([])
const shareTarget = ref(null)
const sharePermission = ref(1)

const editForm = reactive({ title: '', content: '' })

const renderedContent = computed(() => {
  return note.content ? marked(note.content) : ''
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}
const fetchNoteDetail = async () => {
  try {
    const res = await getNoteDetail(noteId.value)
    console.log(noteId.value)
    note.title = res.data?.title || ''
    note.content = res.data?.content || ''
    note.tags = res.data?.tags || []
    visibility.value = res.data?.visibility || 0
    shareUrl.value = `${window.location.origin}/share/${noteId.value}`

    const token = localStorage.getItem('token')
    let currentUserId = null
    if(token) {
      const claims = parseToken(token)
      currentUserId = claims.userId
    }
    const noteUserId = res.data?.userId
    canEdit.value = currentUserId !== null && currentUserId === noteUserId
    
    if (res.data?.aiAnalysis) {
      aiResult.value = res.data.aiAnalysis
    }
    await addHistory(noteId.value)
  } catch (error) {
    ElMessage.error('获取笔记详情失败')
    router.back()
  }
}

const analyzeNote = async () => {
  analyzing.value = true
  try {
    const res = await aiAnalyze(noteId.value)
    aiResult.value = res.data
    ElMessage.success('AI分析完成')
  } catch (error) {
    ElMessage.error('AI分析失败，请稍后重试')
  } finally {
    analyzing.value = false
  }
}

const saveEdit = async () => {
  saving.value = true
  try {
    await updateNote({
      noteId: noteId.value,
      title: editForm.title,
      content: editForm.content
    })
    note.title = editForm.title
    note.content = editForm.content
    ElMessage.success('保存成功')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleVisibility = (command) => {
  ElMessageBox.confirm('确定修改笔记可见权限吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await setNoteVisibility(noteId.value, command)
    visibility.value = command
    ElMessage.success('权限设置成功')
  }).catch(() => {})
}

const handleDelete = () => {
  ElMessageBox.confirm('确定删除这篇笔记吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteNote(noteId.value)
    ElMessage.success('删除成功')
    router.push('/home')
  }).catch(() => {})
}

const copyLink = () => {
  navigator.clipboard.writeText(shareUrl.value)
  ElMessage.success('链接已复制')
}

const fetchFriends = async () => {
  try {
    const res = await getFriendList()
    friendList.value = res.data || []
  } catch (error) {}
}

const doShare = async () => {
  if (!shareTarget.value) {
    ElMessage.warning('请选择好友')
    return
  }
  await shareToUser(noteId.value, shareTarget.value, sharePermission.value)
  ElMessage.success('分享成功')
  showShareDialog.value = false
}

onMounted(() => {
  fetchNoteDetail()
  fetchFriends()
})
</script>

<style scoped>
.detail-container { min-height: 100vh; background: #f5f7fa; padding: 20px; }
.detail-card { max-width: 1000px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.title-actions { display: flex; gap: 12px; }
.note-title { font-size: 28px; margin: 0 0 16px 0; }
.note-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.tags { display: flex; gap: 8px; }
.time { color: #909399; font-size: 12px; }
.markdown-body { min-height: 300px; padding: 16px; background: #fafafa; border-radius: 8px; }
.markdown-body :deep(h1) { font-size: 28px; margin: 24px 0 16px; }
.markdown-body :deep(h2) { font-size: 24px; margin: 20px 0 12px; }
.markdown-body :deep(code) { background: #f0f0f0; padding: 2px 6px; border-radius: 4px; }
.markdown-body :deep(pre) { background: #2d2d2d; color: #f8f8f2; padding: 16px; border-radius: 8px; overflow-x: auto; }
.ai-section { margin-top: 32px; padding-top: 24px; border-top: 1px solid #e4e7ed; }
.ai-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.share-section { margin-top: 16px; }
</style>