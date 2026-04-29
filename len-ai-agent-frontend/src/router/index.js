import { createRouter, createWebHistory } from 'vue-router'
import IndexApp from '../views/IndexApp.vue'
import MenuView from '@/views/MenuView.vue'
import HomeView from '@/views/HomeView.vue'
import LoveAppView from '@/views/LoveAppView.vue'
import ManusAppView from '../views/ManusAppView.vue'
import HealthAppView from '../views/HealthAppView.vue'
import AgentChatView from '@/views/AgentChatView.vue'
import AgentAdminView from '@/views/AgentAdminView.vue'
import CoffeeShopView from '@/views/CoffeeShopView.vue'


const routes = [
  {
    path: '/',
    component: IndexApp
  },
  {
    path: '/coffee-shop',
    component: CoffeeShopView,
    children: [
  {
    path: 'agent-chat',
    component: AgentChatView
  },
  {
    path: 'agent-admin',
    component: AgentAdminView
  }]
  },
  {
    path: '/menu',
    component: MenuView,
    children: [
      {
        path: 'home',
        component: HomeView
      },
      {
        path: 'love-app',
        component: LoveAppView
      },
      {
        path: 'manus-app',
        component: ManusAppView
      },
      {
        path: 'health-app',
        component: HealthAppView
      }
    ]
  },

]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router 