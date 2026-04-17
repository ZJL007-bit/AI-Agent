import { createRouter, createWebHistory } from 'vue-router'
import IndexApp from '../views/IndexApp.vue'
import MenuView from '@/views/MenuView.vue'
import HomeView from '@/views/HomeView.vue'
import LoveAppView from '@/views/LoveAppView.vue'
import ManusAppView from '../views/ManusAppView.vue'
import ExamAppView from '@/views/ExamAppView.vue'
import HealthAppView from '../views/HealthAppView.vue'


const routes = [
  {
    path: '/',
    component: IndexApp
  },
  {
    path: '/menu',
    component: MenuView,
        // 当子路由路径以 / 开头时，它会被当做根路径。
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
        path: 'exam-app',
        component: ExamAppView
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