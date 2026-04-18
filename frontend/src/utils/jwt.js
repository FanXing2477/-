/**
 * JWT Token 工具类
 * 功能：存储、获取、清除、解析Token
 * 符合PDF要求：Token机制、用户隔离、认证鉴权
 */

// ==================== 存储相关 ====================

/**
 * 存储Token及用户信息
 * @param {string} token - JWT字符串
 * @param {object} userInfo - 用户信息（可选）
 */
export const setToken = (token, userInfo = null) => {
  if (!token) return false
  
  // 存储Token
  localStorage.setItem('token', token)
  
  // 存储用户信息（用于前端展示，不用于鉴权）
  if (userInfo) {
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  }
  
  return true
}

/**
 * 获取Token
 * @returns {string|null}
 */
export const getToken = () => {
  return localStorage.getItem('token') || null
}

/**
 * 清除所有登录信息（退出登录时调用）
 */
export const clearToken = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userId')
}

// ==================== Token解析相关 ====================

/**
 * 从Token中解析Payload
 * @param {string} token - JWT字符串
 * @returns {object|null}
 */
export const parseToken = (token) => {
  if (!token || typeof token !== 'string') return null
  
  try {
    // JWT格式：header.payload.signature
    const parts = token.split('.')
    if (parts.length !== 3) return null
    
    // Base64解码payload（JWT使用Base64Url编码，需要转换）
    let payload = parts[1]
    // 替换Base64Url为标准Base64
    payload = payload.replace(/-/g, '+').replace(/_/g, '/')
    // 补齐末尾的=号
    while (payload.length % 4) payload += '='
    
    const decoded = window.atob(payload)
    return JSON.parse(decoded)
  } catch (error) {
    console.error('Token解析失败:', error)
    return null
  }
}

/**
 * 从Token中获取用户ID（核心：用于用户隔离）
 * 根据PDF要求：每个人只能看见自己的笔记
 * @returns {number|null}
 */
export const getUserIdFromToken = () => {
  const token = getToken()
  if (!token) return null
  
  const payload = parseToken(token)
  if (!payload) return null
  
  // 根据后端实际返回的字段名，支持多种可能
  // 建议后端JWT中存入 { userId: xxx, username: xxx, exp: xxx }
  if (payload.userId) return payload.userId
  if (payload.id) return payload.id
  if (payload.sub) return payload.sub
  if (payload.user_id) return payload.user_id
  
  return null
}

/**
 * 获取用户ID的别名（兼容你路由中用的函数名）
 * @returns {number|null}
 */
export const getPersonIdFromStorage = () => {
  return getUserIdFromToken()
}

/**
 * 从Token中获取用户名
 * @returns {string|null}
 */
export const getUsernameFromToken = () => {
  const token = getToken()
  if (!token) return null
  
  const payload = parseToken(token)
  if (!payload) return null
  
  return payload.username || payload.name || payload.userName || null
}

/**
 * 检查Token是否过期
 * @returns {boolean} true=已过期, false=未过期
 */
export const isTokenExpired = () => {
  const token = getToken()
  if (!token) return true
  
  const payload = parseToken(token)
  if (!payload) return true
  
  // 检查exp字段（JWT标准过期时间，单位：秒）
  if (payload.exp) {
    const now = Math.floor(Date.now() / 1000)
    return payload.exp < now
  }
  
  return false
}

/**
 * 判断是否已登录（Token存在且未过期）
 * @returns {boolean}
 */
export const isLoggedIn = () => {
  const token = getToken()
  if (!token) return false
  return !isTokenExpired()
}

// ==================== 请求拦截器辅助 ====================

/**
 * 获取请求头中的Authorization（用于axios拦截器）
 * @returns {object}
 */
export const getAuthHeader = () => {
  const token = getToken()
  if (token) {
    return { Authorization: `Bearer ${token}` }
  }
  return {}
}

/**
 * 获取Token字符串（用于自定义请求头，如你后端的token头）
 * @returns {string|null}
 */
export const getTokenForHeader = () => {
  return getToken()
}