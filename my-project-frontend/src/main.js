import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js";
import axios from "axios";
import 'element-plus/theme-chalk/dark/css-vars.css'
const app = createApp(App)
app.use(router)

app.mount('#app')
axios.defaults.baseURL='http://localhost:8080'