<template>
  <div class="seller-items">
    <div class="toolbar">
      <el-button text class="back-btn" @click="goBack">← 返回</el-button>
      <el-button type="primary" @click="$router.push('/sell')">发布新商品</el-button>
      <el-tabs v-model="status" class="tabs" @tab-click="onTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="上架中" name="listed" />
        <el-tab-pane label="已下架" name="archived" />
        <el-tab-pane label="已售出" name="sold" />
      </el-tabs>
      <div class="search">
        <el-input v-model="q" placeholder="标题关键词" @keyup.enter="search">
          <template #append><el-button type="primary" @click="search">搜索</el-button></template>
        </el-input>
      </div>
    </div>

    <div v-if="loading" class="loading-wrap"><div class="spinner"></div></div>
    <el-empty v-else-if="list.length===0" description="暂无发布" />
    <div v-else>
      <div v-for="row in list" :key="row.id" class="item-card">
        <div class="i-head">
          <div class="time-info">
            <span class="time-item">
              <span class="time-label">创建时间：</span>
              <span class="time-value">{{ formatDate(row.createdAt) }}</span>
            </span>
            <span v-if="row.updatedAt && row.updatedAt !== row.createdAt" class="time-item">
              <span class="time-label">更新时间：</span>
              <span class="time-value">{{ formatDate(row.updatedAt) }}</span>
            </span>
          </div>
          <span class="no">商品ID：{{ row.id }}</span>
          <span class="status">{{ statusText(row.status) }}</span>
        </div>
        <div class="i-body">
          <div class="thumb-section">
            <div class="image-gallery">
              <img :src="row.image || placeholder(row.id)" class="thumb-img" alt="" />
              <div v-if="row.images && row.images.length > 1" class="image-count">
                共{{ row.images.length }}张
              </div>
            </div>
            <div class="thumb-info">
              <div class="t-title">{{ row.title }}</div>
              <div class="t-desc">{{ row.description }}</div>
              <div class="t-meta">
                <div class="meta-row">
                  <span class="t-price">{{ formatCurrency(row.currency) }}{{ row.price }}</span>
                  <span class="t-qty">库存：{{ row.quantity || 0 }}</span>
                  <span v-if="row.soldQuantity !== undefined && row.soldQuantity > 0" class="t-sold">已售：{{ row.soldQuantity }}</span>
                </div>
                <div class="meta-row">
                  <el-tag v-if="row.condition" size="small" :type="conditionType(row.condition)">{{ conditionText(row.condition) }}</el-tag>
                  <el-tag v-if="row.categoryName" size="small">{{ row.categoryName }}</el-tag>
                  <span v-if="row.location" class="location">📍 {{ row.location }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="summary">
            <div class="ops">
              <el-button size="small" @click="edit(row)">编辑</el-button>
              <el-button 
                size="small" 
                type="warning" 
                @click="toggle(row)"
                :disabled="row.status !== 'listed' && (!row.quantity || row.quantity === 0)"
                :title="row.status !== 'listed' && (!row.quantity || row.quantity === 0) ? '库存为0，无法上架。请先编辑商品设置库存' : ''">
                {{ row.status==='listed'?'下架':'上架' }}
              </el-button>
              <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="pager">
        <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          @current-change="onPage"
        />
      </div>
    </div>

    <el-dialog v-model="show" title="编辑商品" width="800px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left">
        <el-form-item label="商品标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入商品标题，建议10-50字"
            maxlength="100"
            show-word-limit
            clearable />
          <div class="form-tip">简洁明了的标题能吸引更多买家</div>
        </el-form-item>
        
        <el-form-item label="商品描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4"
            placeholder="请详细描述商品的外观、成色、来源、配件、保修等信息"
            maxlength="500"
            show-word-limit
            clearable />
          <div class="form-tip">详细描述有助于提高商品成交率</div>
        </el-form-item>
        
        <el-form-item label="价格" prop="price">
          <el-input-number 
            v-model="form.price" 
            :min="0" 
            :precision="2"
            :step="1"
            :max="999999"
            controls-position="right"
            style="width:200px" />
          <span style="margin-left:8px;color:#666">元</span>
          <div class="form-tip">请填写合理的价格，支持小数点后两位</div>
        </el-form-item>
        
        <el-form-item label="库存数量" prop="quantity">
          <el-input-number 
            v-model="form.quantity" 
            :min="0" 
            :step="1"
            :max="9999"
            controls-position="right"
            style="width:200px" />
          <span style="margin-left:8px;color:#666">件</span>
          <div class="form-tip">
            <span v-if="form.quantity === 0" style="color:#ff4d4f;font-weight:500">⚠️ 库存为0时，商品将自动下架</span>
            <span v-else>库存为0时商品将自动下架</span>
          </div>
        </el-form-item>
        
        <el-form-item label="商品成色" prop="condition">
          <el-select v-model="form.condition" placeholder="请选择商品成色" style="width:200px">
            <el-option label="全新" value="new" />
            <el-option label="95新" value="like_new" />
            <el-option label="良好" value="good" />
            <el-option label="一般" value="fair" />
            <el-option label="较差" value="poor" />
          </el-select>
          <div class="form-tip">如实描述商品成色，避免交易纠纷</div>
        </el-form-item>
        
        <el-form-item label="所在位置" prop="location">
          <el-input 
            v-model="form.location" 
            placeholder="例如：北京/朝阳区 或 上海/浦东新区"
            maxlength="50"
            clearable
            style="width:300px" />
          <div class="form-tip">填写详细位置有助于同城交易</div>
        </el-form-item>
        
        <el-form-item label="商品图片" prop="images">
          <div class="upload-section">
            <el-upload
              :action="uploadAction"
              list-type="picture-card"
              :headers="headers"
              accept="image/*"
              :on-success="imgSuccess"
              :on-remove="imgRemove"
              :on-error="imgError"
              :file-list="fileList"
              :limit="9"
              :before-upload="beforeUpload">
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="upload-tip">支持 JPG、PNG 格式，单张不超过 5MB，最多上传 9 张</div>
              </template>
            </el-upload>
            <div v-if="form.images?.length > 0" class="reorder-hint">
              <div class="hint-title">图片排序（第一张为封面图）</div>
              <div class="reorder">
                <div v-for="(u,idx) in form.images" :key="u" class="row">
                  <div class="img-wrap">
                    <img :src="u" />
                    <div class="img-index">{{ idx + 1 }}</div>
                  </div>
                  <div class="ops">
                    <el-button size="small" @click="move(idx,-1)" :disabled="idx===0">↑ 上移</el-button>
                    <el-button size="small" @click="move(idx,1)" :disabled="idx===form.images.length-1">↓ 下移</el-button>
                    <el-button size="small" type="danger" @click="removeImg(idx)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick } from 'vue'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import http from '../api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const list = ref<any[]>([])
const loading = ref(true)
const status = ref<'all'|'listed'|'archived'|'sold'>('all')
const q = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const show = ref(false)
const saving = ref(false)
const editing = ref<any>(null)
const formRef = ref<FormInstance>()
const form = ref<any>({ title:'', description:'', price:0, quantity:0, condition:'good', location:'', images:[] as string[] })
const fileList = ref<any[]>([])
const auth = useAuthStore()
const headers = auth.token ? { Authorization: `Bearer ${auth.token}` } : {}
const uploadAction = `${(http.defaults.baseURL || '').replace(/\/$/, '')}/api/files/upload`

// 表单验证规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在2-100个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度应在10-500个字符之间', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入商品价格'))
        } else if (typeof value === 'number' && value <= 0) {
          callback(new Error('价格必须大于0'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  quantity: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入库存数量'))
        } else if (typeof value === 'number' && value < 0) {
          callback(new Error('库存数量不能小于0'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  condition: [
    { required: true, message: '请选择商品成色', trigger: 'change' }
  ],
  location: [
    { max: 50, message: '位置信息不能超过50个字符', trigger: 'blur' }
  ],
  images: [
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (!value || !Array.isArray(value) || value.length === 0) {
          callback(new Error('请至少上传一张商品图片'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
})

async function load(){
  loading.value = true
  const offset = (page.value-1)*pageSize.value
  const params:any = { limit: pageSize.value, offset, status: status.value==='all'? undefined: status.value, q: q.value||undefined }
  const { data } = await http.get('/api/items/seller', { params })
  if (data.code===0){ list.value = data.data||[]; total.value = data.data?.total || list.value.length }
  loading.value = false
}
function onTab(pane?: any){ if (pane?.paneName) status.value = pane.paneName as any; page.value=1; load() }
function search(){ page.value=1; load() }
function onPage(p:number){ page.value=p; load() }

async function edit(row:any){
  editing.value=row;
  // fetch detail to get images and extra fields
  const d = await http.get(`/api/items/${row.id}`)
  if (d.data.code===0){
    const it = d.data.data
    form.value={ title:it.title, description:it.description, price:it.price, quantity:it.quantity, condition:it.condition||'good', location:it.location||'', images:it.images||[] }
    fileList.value = (it.images||[]).map((u:string)=>({ name:'img', url:u }))
  }
  show.value=true
  // 重置表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

async function handleSave(){
  if (!formRef.value) return
  // 表单验证
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }
    try {
      saving.value = true
      // 构建更新数据
      const updateData: any = { 
        title:form.value.title, 
        description:form.value.description, 
        price:form.value.price, 
        quantity:form.value.quantity, 
        condition:form.value.condition, 
        location:form.value.location, 
        images: form.value.images 
      }
      
      // 如果库存为0，自动设置为已下架状态
      if (form.value.quantity === 0) {
        updateData.status = 'archived'
      }
      
      await http.put(`/api/items/${editing.value.id}`, updateData)
      
      // 根据库存状态给出不同的提示
      if (form.value.quantity === 0) {
        ElMessage.success('商品信息已保存，库存为0已自动下架')
      } else {
        ElMessage.success('商品信息已保存')
      }
      
      show.value=false
      await load()
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message || '保存失败，请重试')
    } finally {
      saving.value = false
    }
  })
}

function handleCancel(){
  if (saving.value) return
  show.value = false
  formRef.value?.resetFields()
}
async function toggle(row:any){
  // 如果要上架，检查库存
  if (row.status !== 'listed') {
    // 检查库存是否为0
    if (!row.quantity || row.quantity === 0) {
      ElMessage.warning('库存为0，无法上架。请先编辑商品设置库存后再上架')
      return
    }
  }
  
  const status = row.status==='listed'?'archived':'listed'
  try {
    await http.put(`/api/items/${row.id}`, { status })
    ElMessage.success(status==='listed'?'已上架':'已下架')
    await load()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败，请重试')
  }
}
async function remove(row:any){
  await ElMessageBox.confirm('确定删除该商品？','提示')
  await http.delete(`/api/items/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

// 图片上传前验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

function imgSuccess(res:any){ 
  if (res?.code===0 && res.data?.url) { 
    form.value.images.push(res.data.url); 
    fileList.value.push({ name:'img', url:res.data.url })
    // 触发图片字段验证
    formRef.value?.validateField('images')
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res?.message || '图片上传失败')
  }
}

function imgRemove(file:any){ 
  const url=file?.url; 
  if (!url) return
  form.value.images = form.value.images.filter((u:string)=>u!==url)
  fileList.value = fileList.value.filter((f:any)=>f.url!==url)
  // 触发图片字段验证
  formRef.value?.validateField('images')
}

function imgError(error:any){
  ElMessage.error('图片上传失败，请重试')
  console.error('Upload error:', error)
}

function removeImg(idx:number){
  if (idx < 0 || idx >= form.value.images.length) return
  const url = form.value.images[idx]
  form.value.images.splice(idx, 1)
  fileList.value = fileList.value.filter((f:any)=>f.url!==url)
  // 触发图片字段验证
  formRef.value?.validateField('images')
}

function move(idx:number, delta:number){
  const arr = form.value.images
  const ni = idx + delta
  if (ni<0 || ni>=arr.length) return
  const tmp = arr[idx]
  arr[idx] = arr[ni]
  arr[ni] = tmp
  // 更新 fileList 顺序
  const fileTmp = fileList.value[idx]
  fileList.value[idx] = fileList.value[ni]
  fileList.value[ni] = fileTmp
}

onMounted(load)

function goBack(){ if (window.history.length>1) history.back(); else location.assign('/') }

function placeholder(id:any){ return `https://picsum.photos/seed/seller${id}/400/300` }

function statusText(s:string){
  const map:any = { listed:'上架中', archived:'已下架', sold:'已售出' }
  return map[s] || s
}

function conditionText(c:string){
  const map:any = { new:'全新', like_new:'95新', good:'良好', fair:'一般', poor:'较差' }
  return map[c] || c
}

function conditionType(c:string){
  const map:any = { 
    new:'success',      // 全新 - 绿色
    like_new:'success', // 95新 - 绿色
    good:'primary',     // 良好 - 蓝色
    fair:'warning',     // 一般 - 黄色/橙色
    poor:'danger'       // 较差 - 红色
  }
  return map[c] || 'info'
}

function formatCurrency(currency?: string){
  if (!currency) return '¥'
  if (currency === 'CNY') return '¥'
  return currency
}

function formatDate(iso:string){
  if(!iso) return '--'
  const date = new Date(iso)
  if(isNaN(date.getTime())) return iso.replace('T',' ').slice(0,19)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
</script>

<style scoped>
.seller-items{max-width:1200px;margin:0 auto;padding:12px 16px}
.toolbar{display:flex;align-items:center;gap:12px}
.back-btn{color:#666}
.toolbar .tabs :deep(.el-tabs__header){margin-bottom:0}
.toolbar .search{flex:1}
.item-card{border:1px solid #eee;border-radius:8px;background:#fff;margin-top:12px}
.i-head{display:flex;align-items:center;gap:12px;padding:10px 12px;border-bottom:1px solid #f5f5f5;background:#fafafa;flex-wrap:wrap}
.i-head .time-info{display:flex;align-items:center;gap:16px;flex:1}
.i-head .time-item{display:flex;align-items:center;gap:4px;font-size:14px;color:#666;font-weight:600}
.i-head .time-label{color:#999;font-weight:600}
.i-head .time-value{color:#333;font-weight:600}
.i-head .no{font-size:12px;color:#666}
.i-head .status{margin-left:auto;color:#ff4d4f;font-weight:600}
.i-body{display:flex;gap:12px;padding:12px}
.thumb-section{display:flex;gap:12px;flex:1}
.image-gallery{position:relative;flex-shrink:0}
.thumb-img{width:140px;height:90px;object-fit:cover;border:1px solid #f0f0f0;border-radius:6px}
.image-count{position:absolute;bottom:4px;right:4px;background:rgba(0,0,0,0.6);color:#fff;padding:2px 6px;border-radius:4px;font-size:11px}
.thumb-info{flex:1;display:flex;flex-direction:column;gap:6px;min-width:0}
.t-title{font-size:15px;font-weight:600;color:#333;line-height:1.4;word-break:break-word}
.t-desc{color:#666;font-size:13px;line-height:1.5;word-break:break-word;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden}
.t-meta{display:flex;flex-direction:column;gap:6px;margin-top:4px}
.meta-row{display:flex;align-items:center;gap:8px;flex-wrap:wrap}
.t-price{color:#f04848;font-weight:700;font-size:16px}
.t-qty{color:#666;font-size:12px}
.t-sold{color:#52c41a;font-size:12px;font-weight:500}
.location{color:#666;font-size:12px;display:flex;align-items:center;gap:2px}
.summary{display:flex;flex-direction:column;align-items:flex-end;gap:8px;min-width:220px}
.summary .ops{display:flex;gap:8px;flex-wrap:wrap;justify-content:flex-end}
.pager{display:flex;justify-content:center;padding:12px}
.loading-wrap{display:flex;align-items:center;justify-content:center;min-height:200px}
.spinner{width:26px;height:26px;border:3px solid #e5e5e5;border-top-color:#409eff;border-radius:50%;animation:spin 1s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
.form-tip{font-size:12px;color:#999;margin-top:4px;line-height:1.4}
.upload-section{margin-top:8px}
.upload-tip{font-size:12px;color:#999;margin-top:8px}
.reorder-hint{margin-top:16px;padding-top:16px;border-top:1px solid #eee}
.hint-title{font-size:13px;color:#666;margin-bottom:12px;font-weight:600}
.reorder{display:grid;grid-template-columns:repeat(auto-fill, minmax(140px,1fr));gap:12px;margin-top:8px}
.reorder .row{display:flex;flex-direction:column;align-items:center;gap:8px;border:1px solid #e0e0e0;padding:10px;border-radius:8px;background:#fafafa;transition:all 0.3s}
.reorder .row:hover{border-color:#409eff;box-shadow:0 2px 8px rgba(64,158,255,0.1)}
.img-wrap{position:relative;width:100px;height:100px}
.reorder .row img{width:100px;height:100px;object-fit:cover;border-radius:6px;border:1px solid #e0e0e0}
.img-index{position:absolute;top:-8px;left:-8px;width:24px;height:24px;background:#409eff;color:#fff;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600;box-shadow:0 2px 4px rgba(0,0,0,0.2)}
.reorder .ops{display:flex;flex-direction:column;gap:4px;width:100%}
.reorder .ops .el-button{width:100%;font-size:11px;padding:4px 8px}
.dialog-footer{display:flex;justify-content:flex-end;gap:12px}
</style>


