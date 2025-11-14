<template>
  <div class="toolbar">
    <el-button text @click="goBack">← 返回</el-button>
    <div class="spacer"></div>
    <el-button text @click="$router.push('/')">首页</el-button>
  </div>
  <div class="me">
    <div class="content-wrapper">
      <!-- 个人资料 -->
      <el-card class="card profile">
        <template #header>
          <div class="card-header">
            <span class="section-title">个人资料</span>
            <el-button size="small" type="primary" @click="save">保存</el-button>
          </div>
        </template>
        <div class="profile-row">
          <el-avatar :size="72" v-if="form.avatarUrl" :src="form.avatarUrl" />
          <el-avatar :size="72" v-else>{{ (form.displayName||'U').slice(0,1).toUpperCase() }}</el-avatar>
          <div class="profile-fields">
            <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
              <el-form-item label="昵称" prop="displayName">
                <el-input v-model="form.displayName" placeholder="填写昵称" maxlength="20" />
              </el-form-item>
              <el-form-item label="头像上传">
                <UploadImage v-model="form.avatarUrl" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="11位手机号" maxlength="11" />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>

      <!-- 我的收藏 -->
      <el-card class="card favorites">
        <template #header>
          <div class="card-header">
            <span class="section-title">⭐ 我的收藏</span>
            <el-button size="small" text type="primary" @click="$router.push('/favorites')">
              查看更多 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div v-if="loadingFavorites" class="loading-wrap"><div class="spinner"></div></div>
        <el-empty v-else-if="favoriteItems.length===0" description="暂无收藏的商品" :image-size="80" />
        <div v-else class="favorites-grid">
          <div v-for="item in favoriteItems" :key="item.id" class="favorite-item" @click="$router.push(`/item/${item.id}`)">
            <div class="item-image">
              <img :src="item.image || placeholder(item.id)" :alt="item.title" />
            </div>
            <div class="item-info">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-price">¥{{ item.price }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 我的足迹 -->
      <el-card class="card footprints">
        <template #header>
          <div class="card-header">
            <span class="section-title">🧭 我的足迹</span>
            <div class="header-actions">
              <el-button size="small" text type="danger" @click="clearAllHistory" :disabled="loadingHistory || historyItems.length===0">清空</el-button>
              <el-button size="small" text type="primary" @click="$router.push('/footprints')">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
        <div v-if="loadingHistory" class="loading-wrap"><div class="spinner"></div></div>
        <el-empty v-else-if="historyItems.length===0" description="暂无浏览记录" :image-size="80" />
        <div v-else class="history-list">
          <div v-for="item in historyItems" :key="item.id" class="history-item" @click="$router.push(`/item/${item.id}`)">
            <div class="item-image">
              <img :src="item.image || placeholder(item.id)" :alt="item.title" />
            </div>
            <div class="item-info">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-footer">
                <span class="item-price">¥{{ item.price }}</span>
                <span class="view-time">{{ formatDate(item.viewedAt) }}</span>
              </div>
            </div>
            <el-button 
              size="small" 
              text 
              type="danger" 
              @click.stop="deleteHistoryItem(item.id)"
              class="delete-btn"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 我的地址 -->
      <el-card class="card addresses">
        <template #header>
          <div class="card-header">
            <span class="section-title">📍 我的地址</span>
            <el-button size="small" type="primary" @click="openDialog()">新增地址</el-button>
          </div>
        </template>
        <el-table :data="addresses" style="width:100%" size="small">
          <el-table-column label="默认" width="80">
            <template #default="{row}">
              <el-tag v-if="row.isDefault===1" type="success">默认</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="receiverName" label="收件人" width="120" />
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column label="地址">
            <template #default="{row}">
              {{ row.country }} {{ row.province }} {{ row.city }} {{ row.district }} {{ row.addressLine }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260">
            <template #default="{row}">
              <el-button link @click="openDialog(row)">编辑</el-button>
              <el-button link type="primary" @click="setDefault(row)" :disabled="row.isDefault===1">设为默认</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="show" :title="editing?.id ? '编辑地址' : '新增地址'" width="560px">
      <el-form ref="addrRef" :model="addr" :rules="addrRules" label-width="90px" class="addr-form">
        <div class="addr-grid">
          <el-form-item label="收件人" prop="receiverName"><el-input v-model="addr.receiverName" maxlength="20" /></el-form-item>
          <el-form-item label="电话" prop="phone"><el-input v-model="addr.phone" maxlength="11" /></el-form-item>
          <el-form-item label="国家/地区"><el-input v-model="addr.country" maxlength="20" /></el-form-item>
          <el-form-item label="省"><el-input v-model="addr.province" maxlength="20" /></el-form-item>
          <el-form-item label="市"><el-input v-model="addr.city" maxlength="20" /></el-form-item>
          <el-form-item label="区/县"><el-input v-model="addr.district" maxlength="20" /></el-form-item>
          <el-form-item label="详细地址" class="full" prop="addressLine"><el-input v-model="addr.addressLine" maxlength="100" /></el-form-item>
          <el-form-item label="邮编"><el-input v-model="addr.postalCode" maxlength="10" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="show=false">取消</el-button>
        <el-button type="primary" @click="saveAddr">保存</el-button>
      </template>
    </el-dialog>
  </div>
  </template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import UploadImage from '../components/UploadImage.vue'
import { ArrowRight, Delete } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()
const form = ref<any>({ displayName: '', avatarUrl: '', phone: '' })
const addresses = ref<any[]>([])
const favoriteItems = ref<any[]>([])
const historyItems = ref<any[]>([])
const loadingFavorites = ref(false)
const loadingHistory = ref(false)

function placeholder(seed: any) {
  return `https://picsum.photos/seed/item${seed}/300/200`
}

function formatDate(iso: string) {
  if (!iso) return ''
  const date = new Date(iso)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

async function load(){
  const me = await http.get('/api/me')
  if (me.data.code === 0) form.value = { ...form.value, ...me.data.data }
  const addr = await http.get('/api/me/addresses')
  if (addr.data.code === 0) addresses.value = addr.data.data
  
  // 加载收藏和足迹（如果已登录）
  if (auth.isLoggedIn) {
    await loadFavorites()
    await loadHistory()
  }
}

async function loadFavorites() {
  if (!auth.isLoggedIn) return
  loadingFavorites.value = true
  try {
    const { data } = await http.get('/api/favorites/items', {
      params: { limit: 6, offset: 0 }
    })
    if (data.code === 0) {
      favoriteItems.value = data.data?.items || []
    }
  } catch (error: any) {
    // 静默失败
    console.error('Failed to load favorites:', error)
  }
  loadingFavorites.value = false
}

async function loadHistory() {
  if (!auth.isLoggedIn) return
  loadingHistory.value = true
  try {
    const { data } = await http.get('/api/favorites/history', {
      params: { limit: 6, offset: 0 }
    })
    if (data.code === 0) {
      historyItems.value = data.data?.history || []
    }
  } catch (error: any) {
    // 静默失败
    console.error('Failed to load history:', error)
  }
  loadingHistory.value = false
}

async function deleteHistoryItem(itemId: number) {
  try {
    await http.delete(`/api/favorites/history/${itemId}`)
    ElMessage.success('已删除')
    await loadHistory()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}

async function clearAllHistory() {
  try {
    await ElMessageBox.confirm('确定要清空所有浏览记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await http.delete('/api/favorites/history')
    ElMessage.success('已清空')
    await loadHistory()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '清空失败')
    }
  }
}

const formRef = ref()
const formRules = {
  displayName: [
    { max: 20, message: '昵称长度不能超过20个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function save(){
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  
  try {
    await http.put('/api/me', { displayName: form.value.displayName, avatarUrl: form.value.avatarUrl, phone: form.value.phone })
    // 更新auth store中的用户信息
    if (auth.user) {
      auth.setAuth(auth.token, {
        ...auth.user,
        displayName: form.value.displayName,
        avatarUrl: form.value.avatarUrl,
        phone: form.value.phone
      })
    }
    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '保存失败')
  }
}

const show = ref(false)
const editing = ref<any>(null)
const addr = ref<any>({ receiverName:'', phone:'', country:'CN', province:'', city:'', district:'', addressLine:'', postalCode:'' })
const addrRef = ref()
const addrRules = {
  receiverName: [
    { required: true, message: '请填写收件人', trigger: 'blur' },
    { max: 20, message: '收件人长度不能超过20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请填写电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  addressLine: [
    { required: true, message: '请填写详细地址', trigger: 'blur' },
    { max: 100, message: '详细地址长度不能超过100个字符', trigger: 'blur' }
  ]
}

function openDialog(row?:any){
  editing.value = row || null
  addr.value = row ? { ...row } : { receiverName:'', phone:'', country:'CN', province:'', city:'', district:'', addressLine:'', postalCode:'' }
  show.value = true
  // 重置表单验证状态
  setTimeout(() => {
    addrRef.value?.clearValidate()
  }, 100)
}

async function saveAddr(){
  if (!addrRef.value) return
  const ok = await addrRef.value.validate().catch(() => false)
  if (!ok) return
  
  try {
    if (editing.value) {
      await http.put(`/api/me/addresses/${editing.value.id}`, addr.value)
      ElMessage.success('已更新')
    } else {
      await http.post('/api/me/addresses', addr.value)
      ElMessage.success('已新增')
    }
    show.value = false
    await load()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  }
}

async function remove(row:any){
  await ElMessageBox.confirm('确认删除该地址？','提示')
  await http.delete(`/api/me/addresses/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

async function setDefault(row:any){
  await http.post(`/api/me/addresses/${row.id}/default`)
  ElMessage.success('已设为默认')
  await load()
}

onMounted(load)

function goBack(){ if (window.history.length>1) history.back(); else location.assign('/') }
</script>

<style scoped>
.toolbar{
  display:flex;
  align-items:center;
  padding:8px 16px;
  border-bottom:1px solid #eee;
  background:#fff;
  position:sticky;
  top:0;
  z-index:10;
}
.spacer{flex:1}
.me{
  padding:24px 16px;
  min-height:calc(100vh - 48px);
  background:#f5f5f5;
}
.content-wrapper{
  max-width:1000px;
  margin:0 auto;
  display:flex;
  flex-direction:column;
  gap:20px;
}
.card{
  border-radius:12px;
  box-shadow:0 2px 8px rgba(0,0,0,0.08);
}
.card-header{
  display:flex;
  align-items:center;
  justify-content:space-between;
}
.section-title{
  font-size:16px;
  font-weight:600;
  color:#333;
}
.header-actions{
  display:flex;
  gap:8px;
  align-items:center;
}
.profile-row{
  display:flex;
  gap:20px;
  align-items:flex-start;
}
.profile-fields{flex:1}

/* 收藏区域 */
.favorites-grid{
  display:grid;
  grid-template-columns:repeat(auto-fill, minmax(140px, 1fr));
  gap:12px;
}
.favorite-item{
  cursor:pointer;
  border:1px solid #f0f0f0;
  border-radius:8px;
  overflow:hidden;
  transition:all 0.3s;
  background:#fff;
}
.favorite-item:hover{
  box-shadow:0 2px 8px rgba(0,0,0,0.1);
  transform:translateY(-2px);
}
.favorite-item .item-image{
  width:100%;
  height:100px;
  overflow:hidden;
  background:#f5f5f5;
}
.favorite-item .item-image img{
  width:100%;
  height:100%;
  object-fit:cover;
}
.favorite-item .item-info{
  padding:8px;
}
.favorite-item .item-title{
  font-size:12px;
  color:#333;
  overflow:hidden;
  text-overflow:ellipsis;
  white-space:nowrap;
  margin-bottom:4px;
}
.favorite-item .item-price{
  font-size:14px;
  font-weight:700;
  color:#ff4d4f;
}

/* 足迹区域 */
.history-list{
  display:flex;
  flex-direction:column;
  gap:12px;
}
.history-item{
  display:flex;
  gap:12px;
  padding:12px;
  border:1px solid #f0f0f0;
  border-radius:8px;
  background:#fff;
  cursor:pointer;
  transition:all 0.3s;
  align-items:center;
}
.history-item:hover{
  box-shadow:0 2px 8px rgba(0,0,0,0.1);
  border-color:#ddd;
}
.history-item .item-image{
  width:80px;
  height:80px;
  border-radius:6px;
  overflow:hidden;
  background:#f5f5f5;
  flex-shrink:0;
}
.history-item .item-image img{
  width:100%;
  height:100%;
  object-fit:cover;
}
.history-item .item-info{
  flex:1;
  min-width:0;
}
.history-item .item-title{
  font-size:14px;
  font-weight:500;
  color:#333;
  margin-bottom:8px;
  overflow:hidden;
  text-overflow:ellipsis;
  display:-webkit-box;
  -webkit-line-clamp:2;
  -webkit-box-orient:vertical;
}
.history-item .item-footer{
  display:flex;
  justify-content:space-between;
  align-items:center;
}
.history-item .item-price{
  font-size:16px;
  font-weight:700;
  color:#ff4d4f;
}
.history-item .view-time{
  font-size:12px;
  color:#999;
}
.history-item .delete-btn{
  flex-shrink:0;
  margin-left:8px;
}

.loading-wrap{
  display:flex;
  align-items:center;
  justify-content:center;
  padding:40px;
}
.spinner{
  width:24px;
  height:24px;
  border:3px solid #e5e5e5;
  border-top-color:#409eff;
  border-radius:50%;
  animation:spin 1s linear infinite;
}
@keyframes spin{
  to{transform:rotate(360deg)}
}

.addr-form .addr-grid{
  display:grid;
  grid-template-columns:1fr 1fr;
  gap:12px;
}
.addr-form .full{
  grid-column:1 / span 2;
}

@media (max-width: 768px){
  .content-wrapper{
    gap:16px;
  }
  .favorites-grid{
    grid-template-columns:repeat(auto-fill, minmax(120px, 1fr));
    gap:8px;
  }
  .history-item{
    flex-wrap:wrap;
  }
  .history-item .item-image{
    width:60px;
    height:60px;
  }
}
</style>


