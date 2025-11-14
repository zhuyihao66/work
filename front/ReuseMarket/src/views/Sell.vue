<template>
  <div class="toolbar">
    <el-button text @click="goBack">← 返回</el-button>
    <div class="spacer"></div>
    <el-button text @click="$router.push('/')">首页</el-button>
  </div>
  <div class="sell">
    <el-card class="wrap" shadow="never" v-loading="submitting">
      <template #header>
        <div class="head">
          <div class="title-wrapper">
            <el-button circle @click="goBack" class="back-btn" :icon="ArrowLeft" />
            <div class="title">发布二手商品</div>
          </div>
          <el-steps :active="currentStep" align-center class="steps" finish-status="success">
            <el-step title="填写信息" />
            <el-step title="确认预览" />
            <el-step title="发布成功" />
          </el-steps>
        </div>
      </template>
      <el-alert type="info" show-icon title="提示：尽量填写完整信息和清晰图片，能提升成交率" class="mb12" />
    <div class="grid">
    <el-form ref="fRef" :model="form" :rules="rules" label-width="90px" class="editor">
      <el-form-item label="标题" prop="title"><el-input v-model="form.title" maxlength="200" show-word-limit /></el-form-item>
      <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="外观/成色/来源/配件/保修等" maxlength="500" show-word-limit /></el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number v-model="form.price" :min="0" :step="10" controls-position="right" />
        <el-select v-model="form.currency" style="margin-left:8px;width:100px">
          <el-option label="¥" value="CNY" />
        </el-select>
      </el-form-item>
      <el-form-item label="成色" prop="condition">
        <el-select v-model="form.condition" style="width:200px">
          <el-option label="全新" value="new" />
          <el-option label="95新" value="like_new" />
          <el-option label="良好" value="good" />
          <el-option label="一般" value="fair" />
          <el-option label="较差" value="poor" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-cascader v-if="catTree.length" v-model="catPath" :options="catTree" :props="{ value:'id', label:'name', children:'children', checkStrictly:true }" placeholder="选择分类" style="width:280px" @change="onCatChange" />
        <el-skeleton v-else :rows="1" animated style="width:280px" />
      </el-form-item>
      <el-form-item label="数量" prop="quantity"><el-input-number v-model="form.quantity" :min="1" /></el-form-item>
      <el-form-item label="位置" prop="location"><el-input v-model="form.location" placeholder="城市/区域" maxlength="50" /></el-form-item>
      <el-form-item label="图片" prop="images">
        <el-upload
          :action="uploadAction"
          list-type="picture-card"
          :headers="headers"
          drag
          accept="image/*"
          :on-success="imgSuccess"
          :on-remove="imgRemove"
          :limit="9">
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit">发布</el-button>
      </el-form-item>
    </el-form>

    <div class="preview">
      <el-card shadow="hover">
        <div class="p-img">
          <img :src="(form.images[0]||placeholder('preview'))" alt=""/>
        </div>
        <div class="p-title">{{ form.title || '商品标题' }}</div>
        <div class="p-desc">{{ form.description || '商品描述' }}</div>
        <div class="p-meta"><span class="price">¥{{ form.price || 0 }}</span><el-tag size="small" :type="condType(form.condition)" class="cond">{{ condText(form.condition) }}</el-tag></div>
      </el-card>
    </div>
    </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watchEffect } from 'vue'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'
import http from '../api/http'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const headers = auth.token ? { Authorization: `Bearer ${auth.token}` } : {}
const uploadAction = `${(http.defaults.baseURL || '').replace(/\/$/, '')}/api/files/upload`
const form = ref<any>({ title:'', description:'', price:0, currency:'CNY', condition:'good', quantity:1, location:'', images:[] as string[], categoryId: undefined })
const flatCats = ref<any[]>([])
const catTree = ref<any[]>([])
const catPath = ref<any[]>([])
const rules = {
  title: [
    { required: true, message: '请填写标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度应在2-200个字符之间', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请填写价格', trigger: 'blur' }, 
    { validator: (_:any, v:any, cb:any)=>{ if (v>=0) cb(); else cb(new Error('价格需≥0')) }, trigger:'blur' }
  ],
  condition: [
    { required: true, message: '请选择商品成色', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请填写数量', trigger: 'blur' }, 
    { validator: (_:any, v:any, cb:any)=>{ if (v>=1) cb(); else cb(new Error('数量需≥1')) }, trigger:'blur' }
  ],
  location: [
    { max: 50, message: '位置信息不能超过50个字符', trigger: 'blur' }
  ],
  images: [
    { validator: (_:any, v:any, cb:any)=>{ if (Array.isArray(v) && v.length>0) cb(); else cb(new Error('请至少上传一张图片')) }, trigger:'change' }
  ]
}
const fRef = ref()
const submitting = ref(false)
const currentStep = ref(0)

// 计算表单是否填写完整（必填项）
const isFormValid = computed(() => {
  return form.value.title && 
         form.value.price > 0 && 
         form.value.quantity >= 1 && 
         form.value.images && 
         form.value.images.length > 0
})

// 自动更新步骤状态
watchEffect(() => {
  if (submitting.value) {
    currentStep.value = 2 // 提交中/提交成功
  } else if (isFormValid.value) {
    currentStep.value = 1 // 表单填写完整，进入确认预览阶段
  } else {
    currentStep.value = 0 // 填写信息阶段
  }
})

function imgSuccess(res:any){ if (res?.code===0 && res.data?.url) form.value.images.push(res.data.url) }
function imgRemove(file:any){ const url = file?.url; if (!url) return; form.value.images = form.value.images.filter((u:string)=>u!==url) }

function condText(v:string){
  return ({ new:'全新', like_new:'95新', good:'良好', fair:'一般', poor:'较差' } as any)[v] || v
}

function placeholder(seed:any){ return `https://picsum.photos/seed/${seed}/600/400` }

function condType(v:string){
  const map:any = { new:'success', like_new:'success', good:'primary', fair:'warning', poor:'danger' }
  return map[v] || 'info'
}

async function submit(){
  const ok = await fRef.value.validate().catch(()=>false)
  if (!ok) return
  submitting.value = true
  currentStep.value = 2 // 进入发布成功阶段
  try{
    const { data } = await http.post('/api/items', form.value)
    if (data.code===0){ 
      ElMessage.success('发布成功')
      // 延迟跳转，让用户看到成功状态
      setTimeout(() => {
        location.assign(`/item/${data.data}`)
      }, 1000)
    } else {
      ElMessage.error(data.message)
      currentStep.value = 1 // 失败时回到预览阶段
      submitting.value = false
    }
  }catch(e:any){ 
    ElMessage.error(e?.response?.data?.message || '发布失败')
    currentStep.value = 1 // 失败时回到预览阶段
    submitting.value = false
  }
}

function goBack(){ if (window.history.length>1) history.back(); else location.assign('/') }

async function loadCats(){
  const { data } = await http.get('/api/items/categories')
  if (data.code===0){
    const res:any[] = []
    function walk(nodes:any[], prefix:string[]){
      for (const n of nodes){
        const label = [...prefix, n.name].join(' / ')
        res.push({ id:n.id, label })
        if (n.children) walk(n.children, [...prefix, n.name])
      }
    }
    const tree = data.data||[]
    walk(tree, [])
    flatCats.value = res
    catTree.value = tree
  }
}

function onCatChange(path:any[]){ form.value.categoryId = path && path.length ? path[path.length-1] : undefined }

onMounted(loadCats)
</script>

<style scoped>
.toolbar{display:flex;align-items:center;padding:8px 16px;border-bottom:1px solid #eee;background:#fff}
.spacer{flex:1}
.sell{padding:16px}
.wrap{max-width:1200px;margin:0 auto}
.head{display:flex;align-items:flex-start;justify-content:space-between;gap:24px;flex-wrap:wrap}
.title-wrapper{display:flex;align-items:center;gap:12px}
.back-btn{flex-shrink:0}
.title{font-weight:700;font-size:18px;white-space:nowrap}
.steps{flex:1;min-width:400px;max-width:600px}
.steps :deep(.el-steps--horizontal){padding:16px 0;display:flex;justify-content:space-between}
.steps :deep(.el-step){flex:1;position:relative;padding-right:0}
.steps :deep(.el-step__head){width:36px;height:36px;position:relative;margin:0 auto}
.steps :deep(.el-step__icon){width:36px;height:36px;line-height:36px;font-size:14px;font-weight:600;display:flex;align-items:center;justify-content:center}
.steps :deep(.el-step__icon-inner){width:100%;height:100%;display:flex;align-items:center;justify-content:center}
.steps :deep(.el-step__title){font-size:14px;line-height:1.5;padding:0;margin-top:8px;margin-left:0;margin-right:0;text-align:center}
.steps :deep(.el-step__main){margin:0;padding:0;text-align:center}
.steps :deep(.el-step__line){top:18px;left:calc(50% + 18px);right:calc(-50% + 18px);position:absolute}
.steps :deep(.el-step__line-inner){border-width:2px;transition:border-color 0.3s}
.steps :deep(.el-step:last-child .el-step__line){display:none}
.mb12{margin-bottom:12px}
.grid{display:grid;grid-template-columns:1.2fr .8fr;gap:16px}
.editor{background:#fff;padding:8px 12px;border-radius:8px}
.preview .p-img{width:100%;height:200px;overflow:hidden;border-radius:8px}
.preview .p-img img{width:100%;height:100%;object-fit:cover}
.preview .p-title{font-weight:700;margin-top:8px}
.preview .p-desc{color:#888;font-size:12px;margin:4px 0}
.preview .p-meta{display:flex;align-items:center;gap:8px}
.preview .price{color:#f04848;font-weight:800}
.preview .cond{margin-left:auto}
</style>


