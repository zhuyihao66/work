import { onBeforeUnmount, onMounted, watch } from 'vue'
import { useAuthStore } from '../store/auth'
import { usePaymentStore } from '../store/payment'
import { ElNotification } from 'element-plus'

function buildWsUrl(token: string) {
  const base = import.meta.env.VITE_API_BASE || window.location.origin
  let url: URL
  try {
    url = new URL(base)
  } catch (e) {
    url = new URL(window.location.origin)
  }
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
  url.pathname = '/ws/notifications'
  url.search = `token=${encodeURIComponent(token)}`
  return url.toString()
}

export function useRealtime() {
  const auth = useAuthStore()
  const paymentStore = usePaymentStore()
  let socket: WebSocket | null = null
  let reconnectTimer: number | null = null

  const cleanup = () => {
    if (socket) {
      socket.onmessage = null
      socket.onopen = null
      socket.onclose = null
      socket.onerror = null
      socket.close()
      socket = null
    }
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  const scheduleReconnect = () => {
    if (reconnectTimer || !auth.isLoggedIn) return
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      connect()
    }, 4000)
  }

  const handleMessage = (event: MessageEvent) => {
    try {
      const payload = JSON.parse(event.data)
      const { type, data } = payload
      switch (type) {
        case 'order_countdown':
          if (data?.orderId && data?.remainingSeconds !== undefined) {
            paymentStore.syncCountdown(Number(data.orderId), Number(data.remainingSeconds))
          }
          break
        case 'order_timeout':
          paymentStore.markExpired(Number(data?.orderId))
          ElNotification({
            title: '订单已超时',
            message: `订单 ${data?.orderNo || ''} 已自动取消`,
            type: 'warning'
          })
          window.dispatchEvent(new CustomEvent('orders:refresh'))
          break
        case 'seller_new_order':
          ElNotification({
            title: '新的订单提醒',
            message: `有买家下单了《${data?.itemTitle || '未知商品'}》，等待付款`,
            position: 'top-right'
          })
          break
        case 'seller_order_paid':
          ElNotification({
            title: '买家已付款',
            message: `订单 ${data?.orderNo || ''} 已付款，请尽快发货`,
            type: 'success',
            position: 'top-right'
          })
          window.dispatchEvent(new CustomEvent('seller-orders:refresh'))
          break
        default:
          break
      }
    } catch (error) {
      // ignore malformed payload
    }
  }

  const connect = () => {
    cleanup()
    if (!auth.isLoggedIn || !auth.token) return
    try {
      socket = new WebSocket(buildWsUrl(auth.token))
      socket.onmessage = handleMessage
      socket.onclose = scheduleReconnect
      socket.onerror = scheduleReconnect
    } catch (error) {
      scheduleReconnect()
    }
  }

  onMounted(() => {
    if (auth.isLoggedIn) {
      connect()
    }
  })

  watch(
    () => auth.token,
    (token) => {
      if (!token) {
        cleanup()
      } else {
        connect()
      }
    }
  )

  onBeforeUnmount(() => {
    cleanup()
  })
}


