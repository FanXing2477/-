import axios from 'axios';
import { ElMessage } from 'element-plus';
import { clearToken } from './jwt';

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 120000
});

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.token = token;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 成功
    if (res.code === 200) {
      return res;
    }
    
    // 业务错误
    ElMessage.error(res.msg || '操作失败');
    
    // Token相关错误：未登录、token过期、token无效
    const tokenErrors = ['token', '未登录', '过期', '无效', '401'];
    const isTokenError = tokenErrors.some(keyword => 
      res.msg?.toLowerCase().includes(keyword)
    ) || res.code === 401;
    
    if (isTokenError) {
      clearToken();
      // 使用 Vue Router 跳转，而不是 location.href
      // 注意：这里需要导入 router，或者使用事件总线
      setTimeout(() => {
        window.location.href = '/login'; // 简单方案，刷新页面清空状态
      }, 3000);
    }
    
    return Promise.reject(res);
  },
  error => {
    // 网络错误或服务器错误
    const status = error.response?.status;
    const message = error.response?.data?.msg;
    
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录');
      clearToken();
      window.location.href = '/login';
    } else if (status === 403) {
      ElMessage.error('没有权限访问');
    } else if (status === 404) {
      ElMessage.error('请求的资源不存在');
    } else if (status === 500) {
      ElMessage.error('服务器内部错误');
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试');
    } else if (error.message?.includes('Network Error')) {
      ElMessage.error('网络连接失败，请检查网络');
    } else {
      ElMessage.error(message || '服务器异常，请稍后重试');
    }
    
    return Promise.reject(error);
  }
);

export default request;