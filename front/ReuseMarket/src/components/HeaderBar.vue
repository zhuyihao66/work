<template>
  <div class="header">
    <div class="brand" @click="goToHome">ReuseMarket</div>
    <div class="spacer"></div>
    <div class="nav">
      <el-link 
        :type="isActive('/seller/items') ? 'primary' : 'default'" 
        :class="{ active: isActive('/seller/items') }"
        @click="go('/seller/items')">我发布的</el-link>
      <el-link 
        v-if="auth.isLoggedIn && auth.user?.userId"
        :type="isActive(`/store/${auth.user.userId}`) ? 'primary' : 'default'" 
        :class="{ active: isActive(`/store/${auth.user.userId}`) }"
        @click="goToMyStore">我的店铺</el-link>
      <el-link 
        :type="isActive('/orders') ? 'primary' : 'default'" 
        :class="{ active: isActive('/orders') }"
        @click="go('/orders')">我的购买</el-link>
      <el-link 
        :type="isActive('/seller/orders') ? 'primary' : 'default'" 
        :class="{ active: isActive('/seller/orders') }"
        @click="go('/seller/orders')">我的出售</el-link>
      <el-link 
        :type="isActive('/me') ? 'primary' : 'default'" 
        :class="{ active: isActive('/me') }"
        @click="go('/me')">个人中心</el-link>
    </div>
    <!-- 顶部不再展示登录按钮，登录入口放在首页右侧卡片 -->
    <div v-if="auth.isLoggedIn" class="user">
      <el-avatar :size="32" v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" />
      <el-avatar :size="32" v-else>{{ initials }}</el-avatar>
      <span class="name">{{ auth.user?.displayName }}</span>
      <el-button link type="danger" @click="handleLogout">退出</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import http from '../api/http'
const auth = useAuthStore()
const emit = defineEmits(['show-login'])
const initials = computed(() => (auth.user?.displayName || 'U').slice(0,1).toUpperCase())
const router = useRouter()
const route = useRoute()

function isActive(path: string): boolean {
  const currentPath = route.path
  // 精确匹配或路径前缀匹配
  if (path === '/seller/items') {
    // 特殊处理：/seller/items 应该匹配 /seller/items，但不匹配 /store/:id
    return currentPath === '/seller/items'
  }
  if (path.startsWith('/store/')) {
    // /store/:sellerId 匹配当前用户的店铺页面
    return currentPath === path
  }
  if (path === '/orders') {
    // /orders 应该匹配 /orders 和 /orders/:id
    return currentPath === '/orders' || currentPath.startsWith('/orders/')
  }
  if (path === '/seller/orders') {
    return currentPath === '/seller/orders'
  }
  if (path === '/me') {
    return currentPath === '/me'
  }
  return currentPath.startsWith(path)
}

function goToMyStore() {
  if (!auth.isLoggedIn || !auth.user?.userId) {
    // @ts-ignore
    emit('show-login')
    return
  }
  router.push(`/store/${auth.user.userId}`)
}

function go(path: string){
  if (!auth.isLoggedIn) {
    // 触发外层弹出登录
    // @ts-ignore
    emit('show-login')
    return
  }
  router.push(path)
}
function goToHome() {
  router.push('/')
}

async function handleLogout() {
  try {
    await http.post('/api/auth/logout').catch(() => {})
  } finally {
    auth.logout()
    router.replace('/').finally(() => {
      window.location.reload()
    })
  }
}
</script>

<style scoped>
.header{display:flex;align-items:center;padding:10px 16px;border-bottom:1px solid #eee;background:#fff;position:sticky;top:0;z-index:10}
.brand{
  font-weight:700;
  font-size:18px;
  cursor:pointer;
  background: linear-gradient(90deg, #ff6b6b, #4ecdc4, #45b7d1, #f9ca24, #f0932b, #eb4d4b, #ff6b6b);
  background-size: 300% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientShift 3s ease infinite;
  transition: transform 0.2s;
}
.brand:hover {
  transform: scale(1.05);
}
@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
.spacer{flex:1}
.nav{display:flex;gap:12px;margin-right:12px}
.nav .el-link.active {
  font-weight: 600;
  color: #409eff;
}
.user{display:flex;align-items:center;gap:8px}
.name{margin:0 8px}
</style>


