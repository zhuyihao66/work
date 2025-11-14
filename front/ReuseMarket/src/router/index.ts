import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
const ItemDetail = () => import('../views/ItemDetail.vue')

const routes = [
  { path: '/', name: 'home', component: Home },
  { path: '/item/:id', name: 'item-detail', component: ItemDetail },
  { path: '/orders', name: 'orders', component: () => import('../views/Orders.vue') },
  { path: '/orders/:id', name: 'order-detail', component: () => import('../views/OrderDetail.vue') }
  ,{ path: '/me', name: 'me', component: () => import('../views/Me.vue') }
  ,{ path: '/seller/orders', name: 'seller-orders', component: () => import('../views/SellerOrders.vue') }
  ,{ path: '/sell', name: 'sell', component: () => import('../views/Sell.vue') }
  ,{ path: '/seller/items', name: 'seller-items', component: () => import('../views/SellerItems.vue') }
  ,{ path: '/favorites', name: 'favorites', component: () => import('../views/Favorites.vue') }
  ,{ path: '/shops/favorites', name: 'favorite-shops', component: () => import('../views/FavoriteShops.vue') }
  ,{ path: '/footprints', name: 'footprints', component: () => import('../views/Footprints.vue') }
  ,{ path: '/store/:sellerId', name: 'seller-store', component: () => import('../views/SellerStore.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router


