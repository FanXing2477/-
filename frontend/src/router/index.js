import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, getUserIdFromToken } from '../utils/jwt'

const routes = [
  { path: '/', component: () => import('../views/Index.vue') },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  {
    path: '/home',
    component: () => import('../views/Home.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/note/create',
    component: () => import('../views/NoteCreate.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/note/detail/:id',
    component: () => import('../views/NoteDetail.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/user/profile',
    component: () => import('../views/UserProfile.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/friend',
    component: () => import('../views/Friend.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/history',
    component: () => import('../views/History.vue'),
    meta: { requireAuth: true }
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：认证鉴权
router.beforeEach((to, from) => {
    const isLogin = isLoggedIn()

    if (to.meta.requireAuth && !isLogin) {
        // 未登录，跳转到登录页
        return '/login'
    } else if (to.path === '/login' && isLogin) {
        // 已登录，访问登录页时跳转到主页
        return '/home'
    }
    // 放行
    return true
})

export default router