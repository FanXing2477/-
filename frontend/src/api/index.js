// api/index.js
import request from '@/utils/request'

// ========== 用户认证相关 ==========
export const register = (data) => request.post('/user/register', data)

export const login = (data) => request.post('/user/login', data)

export const changePassword = (data) => request.put('/user/change_password', data)

export const updatePassword = (data) => request.put('/api/update_password', data)

export const getUserInfo = () => request.get("user/info")

// ========== 用户信息相关 ==========
export const updateAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/updateAvatar', formData)
}

export const updateNickname = (nickname) => request.post('/api/updateNickname', null, { params: { nickname } })

export const updateMotto = (motto) => request.post('/api/updateMotto', null, { params: { motto } })

export const deleteUser = () => request.post('/api/delete_user')

// ========== 笔记相关 ==========
export const canSeeNote = (noteId) => request.get(`/api/see_note${noteId}`)

export const canEditNote = (data) => request.post('/api/edit_note', data)

export const createNote = (title, content, tags) => request.post('/api/create_note', null, { params: { title, content, tags } })

export const getNotesByTime = (current = 1, size = 10) => request.get('/api/select_by_time', { params: { current, size } })

export const getNotesByTags = (tagNames, current = 1, size = 10) => 
  request.get('/api/page_by_tags', { params: { tagNames, current, size } })

export const updateNote = (data) => request.post('/api/update_note', data)

export const updateTag = (data) => request.post('/api/update_tag', data)

export const deleteNote = (noteId) => request.post('/api/delete_note', null, { params: { noteId } })

export const getNoteDetail = (noteId) => request.get('/api/note_detail', { params: { noteId } })

export const setNoteVisibility = (noteId, visibility) => 
  request.post('/api/set_visibility', null, { params: { noteId, visibility } })

// ========== 标签相关 ==========
export const createTag = (tagName) => request.post('/api/create_tag', null, { params: { tagName } })

// ========== 浏览记录 ==========
export const addHistory = (noteId) => request.get('/api/add_history', { params: { noteId } })

export const getHistory = (current = 1, size = 10) => request.get('/api/see_history', { params: { current, size } })

export const deleteHistory = (id) => request.post('/api/delete_history', null, { params: { id } })

// ========== 好友相关 ==========
export const getFriendNotes = (friendId) => request.get('/api/friend_notes', {params: {friendId}})

export const searchFriend = (email, phone) => request.get('/api/search_friend', { params: { email, phone } })

export const addFriendRequest = (toUserId) => request.post('/api/add_request', null, { params: { toUserId } })

export const getRequestList = () => request.get('/api/request_list')

export const handleRequest = (requestId, status) => request.post('/api/request_handle', null, { params: { requestId, status } })

export const getFriendList = () => request.get('/api/friend_list')

export const updateFriendGroup = (friendId, groupName) => request.post('/api/update_friend_group', null, { params: { friendId, groupName } })

// ========== AI相关 ==========
export const aiAnalyze = (noteId) => request.post('/api/ai_analyze', null, { params: { noteId } })

// ========== 分享权限 ==========
export const shareToUser = (noteId, targetUserId, permissionType) => 
  request.post('/api/share_to_user', null, { params: { noteId, targetUserId, permissionType } })