<template>
  <div class="friend-container">
    <el-card class="friend-card">
      <template #header>
        <div class="header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <span class="title">好友管理</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 我的好友 -->
        <!-- 简化版模板 -->
        <el-tab-pane label="我的好友" name="friends">
          <div class="search-bar">
            <el-input v-model="searchKeyword" placeholder="搜索好友" style="width: 250px" clearable />
            <el-button type="primary" @click="searchMyFriend">搜索用户</el-button>
          </div>

          <div class="friend-list">
            <div v-for="friend in friendList" :key="friend.friendId" class="friend-item">
              <el-avatar :size="40" :src="friend.avatarUrl" />
              <div class="friend-info">
                <div class="name">{{ friend.friendName }}</div>
              </div>
              <div class="friend-actions">
                <el-button size="small" type="primary" @click="viewFriendNotes(friend)">
                  查看笔记
                </el-button>
                
                <el-dropdown @command="(cmd) => handleFriendAction(cmd, friend)">
                  <el-button size="small">操作</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="chat">发消息</el-dropdown-item>
                      <el-dropdown-item command="share">分享笔记</el-dropdown-item>
                      <el-dropdown-item command="group">修改分组</el-dropdown-item>
                      <el-dropdown-item command="remove" divided>删除好友</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <el-empty v-if="friendList.length === 0" description="暂无好友" :image-size="60" />
          </div>
        </el-tab-pane>

        <!-- 好友申请 -->
        <el-tab-pane label="好友申请" name="requests">
          <div v-for="req in requests" :key="req.id" class="request-item">
            <el-avatar :size="40" :src="req.fromUser?.avatarUrl" />
            <div class="request-info">
              <div class="name">{{ req.fromUser?.username }}</div>
              <div class="message">{{ req.message || '申请添加你为好友' }}</div>
            </div>
            <div class="request-actions">
              <el-button type="success" size="small" @click="handleMyRequest(req.id, 1)">同意</el-button>
              <el-button type="danger" size="small" @click="handleMyRequest(req.id, 2)">拒绝</el-button>
            </div>
          </div>
          <el-empty v-if="requests.length === 0" description="暂无好友申请" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 搜索用户弹窗 -->
    <el-dialog v-model="showSearchDialog" title="搜索用户" width="400px">
      <el-form>
        <el-form-item label="邮箱">
          <el-input v-model="searchEmail" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchPhone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSearchDialog = false">取消</el-button>
        <el-button type="primary" @click="doSearch" :loading="searchLoading">搜索</el-button>
      </template>
    </el-dialog>

    <!-- 搜索结果弹窗 -->
    <el-dialog v-model="showResultDialog" title="搜索结果" width="400px">
      <div v-if="searchResult" class="search-result">
        <el-avatar :size="50" :src="searchResult.avatarUrl" />
        <div class="result-info">
          <div class="name">{{ searchResult.username }}</div>
          <div class="account">{{ searchResult.email || searchResult.phone }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showResultDialog = false">取消</el-button>
        <el-button type="primary" @click="sendFriendRequest" :loading="requestLoading">发送好友申请</el-button>
      </template>
    </el-dialog>

    <!-- 分享笔记弹窗 -->
    <el-dialog v-model="showShareDialog" title="分享笔记" width="400px">
      <el-select v-model="shareNoteId" placeholder="选择要分享的笔记" style="width: 100%">
        <el-option v-for="note in myNotes" :key="note.id" :label="note.title" :value="note.id" />
      </el-select>
      <el-select v-model="sharePermission" placeholder="权限类型" style="width: 100%; margin-top: 12px">
        <el-option label="仅查看" :value="1" />
        <el-option label="可编辑" :value="2" />
      </el-select>
      <template #footer>
        <el-button @click="showShareDialog = false">取消</el-button>
        <el-button type="primary" @click="doShareNote">确认分享</el-button>
      </template>
    </el-dialog>

    <!-- 修改分组弹窗 -->
    <el-dialog v-model="showGroupDialog" title="修改分组" width="400px">
      <el-input v-model="newGroupName" placeholder="请输入新分组名" />
      <template #footer>
        <el-button @click="showGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="updateGroup">确认修改</el-button>
      </template>
    </el-dialog>
    <!-- 好友分享的笔记弹窗 -->
    <el-dialog v-model="showFriendNotesDialog" :title="`${selectedFriend?.friendName} 分享的笔记`" width="700px">
      <div class="friend-notes-list">
        <div v-for="note in friendNotes" :key="note.id" class="friend-note-item" @click="viewNoteDetail(note)">
          <div class="note-header">
            <h3>{{ note.title }}</h3>
            <el-tag :type="note.permissionType === 2 ? 'success' : 'info'" size="small">
              {{ note.permissionType === 2 ? '可编辑' : '仅查看' }}
            </el-tag>
          </div>
          <p class="note-preview">{{ note.content?.substring(0, 100) }}...</p >
          <div class="note-meta">
            <span>分享时间：{{ formatDate(note.shareTime) }}</span>
          </div>
        </div>
        <el-empty v-if="friendNotes.length === 0" description="该好友没有分享笔记给你" />
      </div>
    </el-dialog>
    
    <!-- 查看笔记详情弹窗 -->
    <el-dialog v-model="showNoteDetailDialog" :title="currentNote.title" width="80%">
      <div class="note-detail-content">
        <div class="markdown-body" v-html="renderedNoteContent"></div>
      </div>
      <template #footer>
        <el-button @click="showNoteDetailDialog = false">关闭</el-button>
        <el-button v-if="currentNote.canEdit" type="primary" @click="editSharedNote">编辑笔记</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑分享笔记弹窗 -->
    <el-dialog v-model="showEditNoteDialog" title="编辑笔记" width="80%">
      <el-form :model="editNoteForm">
        <el-form-item label="标题">
          <el-input v-model="editNoteForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editNoteForm.content" type="textarea" :rows="12" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditNoteDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSharedNote" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted,computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getFriendList, getRequestList, handleRequest, searchFriend, addFriendRequest, shareToUser, getNotesByTime,
   updateFriendGroup, canEditNote, getFriendNotes,canSeeNote } from '@/api/index'
import { marked } from 'marked'

const router = useRouter()
const activeTab = ref('friends')
const friendGroups = ref([])
const requests = ref([])
const myNotes = ref([])

const showSearchDialog = ref(false)
const showResultDialog = ref(false)
const showShareDialog = ref(false)
const showGroupDialog = ref(false)
const searchLoading = ref(false)
const requestLoading = ref(false)
const searchKeyword = ref('')
const searchEmail = ref('')
const searchPhone = ref('')
const searchResult = ref(null)
const currentFriend = ref(null)
const newGroupName = ref('')
const shareNoteId = ref(null)
const sharePermission = ref(1)
const friendList = ref([])
const showFriendNotesDialog = ref(false)
const showNoteDetailDialog = ref(false)
const showEditNoteDialog = ref(false)
const selectedFriend = ref(null)
const friendNotes = ref([])
const currentNote = ref({})
const editNoteForm = ref({ title: '', content: '' })
const saving = ref(false)

// 渲染 Markdown
const renderedNoteContent = computed(() => {
  return currentNote.value.content ? marked(currentNote.value.content) : ''
})

// 查看好友分享的笔记
const viewFriendNotes = async (friend) => {
  selectedFriend.value = friend
  try {
    const res = await getFriendNotes(friend.friendId)  // 调用后端接口
    friendNotes.value = res.data || []
    showFriendNotesDialog.value = true
  } catch (error) {
    ElMessage.error('获取好友分享笔记失败')
  }
}

// 查看笔记详情
const viewNoteDetail = async (note) => {
  try {
    const res = await canSeeNote(note.id)
    currentNote.value = {
      ...res.data,
      canEdit: note.permissionType === 2,  // 只有可编辑权限才能编辑
      aiAnalysis: res.data.aiAnalysis
    }
    showNoteDetailDialog.value = true
    showFriendNotesDialog.value = false
  } catch (error) {
    ElMessage.error('获取笔记详情失败')
  }
}

// 编辑分享的笔记
const editSharedNote = () => {
  editNoteForm.value = {
    id: currentNote.value.id,
    title: currentNote.value.title,
    content: currentNote.value.content
  }
  showEditNoteDialog.value = true
  showNoteDetailDialog.value = false
}

// 保存编辑
const saveSharedNote = async () => {
  saving.value = true
  try {
    await canEditNote({
      noteId: editNoteForm.value.id,
      title: editNoteForm.value.title,
      content: editNoteForm.value.content
    })
    ElMessage.success('保存成功')
    showEditNoteDialog.value = false
    
    // 刷新当前显示的笔记
    currentNote.value.title = editNoteForm.value.title
    currentNote.value.content = editNoteForm.value.content
    
    // 刷新好友笔记列表
    await viewFriendNotes(selectedFriend.value)
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()}`
}

const fetchFriends = async () => {
  try {
    const res = await getFriendList()
    console.log(res.data)
    friendList.value = res.data || []
    friendGroups.value = res.data || []
  } catch (error) {
    ElMessage.error('获取好友列表失败')
  }
}

const fetchRequests = async () => {
  try {
    const res = await getRequestList()
    requests.value = res.data || []
  } catch (error) {
    ElMessage.error('获取申请列表失败')
  }
}

const fetchMyNotes = async () => {
  try {
    const res = await getNotesByTime(1, 50)
    myNotes.value = res.data?.records || []
  } catch (error) {}
}

const searchMyFriend = () => {
  showSearchDialog.value = true
}

const doSearch = async () => {
  if (!searchEmail.value && !searchPhone.value) {
    ElMessage.warning('请填写邮箱或手机号')
    return
  }
  searchLoading.value = true
  try {
    const res = await searchFriend(searchEmail.value, searchPhone.value)
    if (res.data) {
      searchResult.value = res.data
      showResultDialog.value = true
    } else {
      ElMessage.warning('未找到该用户')
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    searchLoading.value = false
  }
}

const sendFriendRequest = async () => {
  if (!searchResult.value) return
  requestLoading.value = true
  try {
    await addFriendRequest(searchResult.value.id)
    ElMessage.success('好友申请已发送')
    showResultDialog.value = false
    searchEmail.value = ''
    searchPhone.value = ''
    searchResult.value = null
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    requestLoading.value = false
  }
}

const handleMyRequest = async (requestId, status) => {
  try {
    await handleRequest(requestId, status)
    ElMessage.success(status === 1 ? '已添加好友' : '已拒绝')
    fetchRequests()
    fetchFriends()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleFriendAction = (command, friend) => {
  console.log('选中的好友', friend)
  currentFriend.value = friend
  if (command === 'chat') {
    ElMessage.info('聊天功能开发中...')
  } else if (command === 'share') {
    showShareDialog.value = true
  } else if (command === 'group') {
    showGroupDialog.value = true
  } else if (command === 'remove') {
    ElMessageBox.confirm(`确定删除好友 ${friend.friendName} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      // 调用删除好友接口
      ElMessage.success('删除成功')
      fetchFriends()
    }).catch(() => {})
  }
}

const renameGroup = (oldName) => {
  ElMessageBox.prompt('请输入新分组名', '重命名分组', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: oldName
  }).then(({ value }) => {
    // 调用重命名分组接口
    ElMessage.success('修改成功')
    fetchFriends()
  }).catch(() => {})
}

const updateGroup = async () => {
  if (!newGroupName.value) {
    ElMessage.warning('请输入分组名')
    return
  }
  try {
    await updateFriendGroup(currentFriend.value.friendId, newGroupName.value)
    ElMessage.success('修改成功')
    showGroupDialog.value = false
    fetchFriends()
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

const doShareNote = async () => {
  if (!shareNoteId.value) {
    ElMessage.warning('请选择笔记')
    return
  }
  try {
    await shareToUser(shareNoteId.value, currentFriend.value.friendId, sharePermission.value)
    ElMessage.success('分享成功')
    showShareDialog.value = false
  } catch (error) {
    ElMessage.error('分享失败')
  }
}

onMounted(() => {
  fetchFriends()
  fetchRequests()
  fetchMyNotes()
})
</script>

<style scoped>
.friend-container { min-height: 100vh; background: #f5f7fa; padding: 20px; }
.friend-card { max-width: 800px; margin: 0 auto; }
.header { display: flex; align-items: center; gap: 16px; }
.header .title { font-size: 18px; font-weight: bold; }
.search-bar { display: flex; gap: 12px; margin-bottom: 24px; }
.friend-group { margin-bottom: 24px; }
.group-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid #e4e7ed; }
.friend-list { display: flex; flex-direction: column; gap: 12px; }
.friend-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: #fafafa; border-radius: 8px; }
.friend-info { flex: 1; }
.friend-info .name { font-weight: bold; margin-bottom: 4px; }
.friend-info .email { font-size: 12px; color: #909399; }
.request-item { display: flex; align-items: center; gap: 12px; padding: 12px; border-bottom: 1px solid #e4e7ed; }
.request-info { flex: 1; }
.request-info .name { font-weight: bold; margin-bottom: 4px; }
.request-info .message { font-size: 12px; color: #909399; }
.request-actions { display: flex; gap: 8px; }
.search-result { display: flex; align-items: center; gap: 16px; padding: 16px; background: #f5f7fa; border-radius: 8px; }
.result-info .name { font-weight: bold; font-size: 16px; }
.result-info .account { font-size: 12px; color: #909399; margin-top: 4px; }
</style>