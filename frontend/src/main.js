import { createApp } from 'vue'//导入vue
import ElementPlus from 'element-plus'//导入element-plus
import 'element-plus/dist/index.css'//导入element-plus的样式
import App from './App.vue'//导入app.vue
//引入 Element Plus 图标库，方便后续使用图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import router from '@/router'

import pinia from './store'

const app = createApp(App)//创建应用实例
app.use(pinia)
app.use(ElementPlus)//使用element-plus
app.use(router)

//注册所有 Element Plus 图标 (全局注册，这样在任何页面都能直接用 <el-icon><User /></el-icon>)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')//控制html元素