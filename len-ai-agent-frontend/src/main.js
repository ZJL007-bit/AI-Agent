import { createApp } from 'vue'
import router from './router'
import ArcoVue from '@arco-design/web-vue'
import '@arco-design/web-vue/dist/arco.css'
import { Message } from '@arco-design/web-vue'
import './assets/global-styles.css'
import App from './App.vue'

const app = createApp(App)

app.use(router)
app.use(ArcoVue)
app.config.globalProperties.$message = Message

app.mount('#app')
