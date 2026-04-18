<template>
  <div class="create-container">
    <el-card class="create-card">
      <template #header>
        <div class="header">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <span class="title">新建笔记</span>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入笔记标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-select v-model="form.tags" multiple filterable allow-create default-first-option placeholder="选择或创建标签" style="width: 100%">
            <el-option v-for="tag in tagList" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <div class="editor-toolbar">
            <el-button-group>
              <el-button size="small" @click="insertMarkdown('# ')" title="标题">H1</el-button>
              <el-button size="small" @click="insertMarkdown('## ')" title="二级标题">H2</el-button>
              <el-button size="small" @click="insertMarkdown('**加粗**')" title="加粗">B</el-button>
              <el-button size="small" @click="insertMarkdown('*斜体*')" title="斜体">I</el-button>
              <el-button size="small" @click="insertMarkdown('- 列表项')" title="列表">列表</el-button>
              <el-button size="small" @click="insertMarkdown('[链接](url)')" title="链接">链接</el-button>
              <el-button size="small" @click="insertMarkdown('```\n代码\n```')" title="代码块">代码</el-button>
            </el-button-group>
          </div>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="支持Markdown语法..."
            resize="vertical"
          />
        </el-form-item>

        <el-divider />
        
        <div class="preview-section">
          <h4>预览</h4>
          <div class="markdown-preview" v-html="renderedContent"></div>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createNote, createTag } from '@/api/index'
import { marked } from 'marked'

const router = useRouter()
const formRef = ref()
const saving = ref(false)
const tagList = ref(['学习', '工作', '生活', '灵感'])

const form = reactive({
  title: '',
  content: '',
  tags: []
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const renderedContent = computed(() => {
  return form.content ? marked(form.content) : '<span style="color:#999;">预览内容将显示在这里...</span>'
})

const insertMarkdown = (markdown) => {
  form.content += markdown + '\n'
}

const handleSave = async () => {
  const tags = form.tags.join(' , ')

  const valid = await formRef.value?.validate()
  if (!valid) return

  saving.value = true
  try {
    const res = await createNote(form.title, form.content, tags)
    if (res.code === 200) {
      ElMessage.success('笔记创建成功')
      router.push('/home')
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败，请重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.create-container { min-height: 100vh; background: #f5f7fa; padding: 20px; }
.create-card { max-width: 1200px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; }
.header .title { font-size: 18px; font-weight: bold; }
.editor-toolbar { margin-bottom: 10px; }
.markdown-preview { min-height: 200px; padding: 16px; background: #fafafa; border-radius: 8px; border: 1px solid #e4e7ed; overflow: auto; }
.markdown-preview :deep(h1) { font-size: 24px; margin: 16px 0; }
.markdown-preview :deep(h2) { font-size: 20px; margin: 14px 0; }
.markdown-preview :deep(code) { background: #f0f0f0; padding: 2px 6px; border-radius: 4px; }
.markdown-preview :deep(pre) { background: #2d2d2d; color: #f8f8f2; padding: 16px; border-radius: 8px; overflow-x: auto; }
</style>