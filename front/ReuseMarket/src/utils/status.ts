export function orderStatusType(s: string): string {
  switch (s) {
    case 'pending': return 'warning'
    case 'paid': return 'primary'
    case 'shipped': return 'primary'
    case 'completed': return 'success'
    case 'cancelled': return 'info'
    case 'refunded': return 'danger'
    default: return ''
  }
}

export function orderStatusText(s: string): string {
  const map: Record<string, string> = {
    pending: '待付款',
    paid: '已付款',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return map[s] || s
}


