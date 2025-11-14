<template>
  <HeaderBar @show-login="showLogin = true" />
  <router-view />
  <LoginModal v-model:visible="showLogin" />
  <PaymentModal />
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import HeaderBar from './components/HeaderBar.vue'
import LoginModal from './components/LoginModal.vue'
import PaymentModal from './components/PaymentModal.vue'
import { useAuthStore } from './store/auth'
import { useRealtime } from './hooks/useRealtime'

const showLogin = ref(false)
const auth = useAuthStore()
useRealtime()
const handleShowLogin = () => { showLogin.value = true }

// 监听 auth store 中的 shouldShowLogin 状态，自动显示登录弹窗
watch(() => auth.shouldShowLogin, (shouldShow) => {
  if (shouldShow) {
    showLogin.value = true
    auth.clearLoginRequirement()
  }
})

// 监听登录弹窗关闭，清除标记
watch(showLogin, (visible) => {
  if (!visible) {
    auth.clearLoginRequirement()
  }
})

onMounted(() => {
  // Soft prompt: if not logged, show login modal once, allow close
  if (!auth.isLoggedIn && !sessionStorage.getItem('login_prompt_shown')) {
    showLogin.value = true
    sessionStorage.setItem('login_prompt_shown', '1')
  }

  window.addEventListener('show-login', handleShowLogin)
})

onBeforeUnmount(() => {
  window.removeEventListener('show-login', handleShowLogin)
})
</script>

<style>
body { margin: 0; font-family: -apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial; }
</style>
