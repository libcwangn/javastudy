import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js";
import axios from "axios";
const app = createApp(App)
app.use(router)

app.mount('#app')
axios.defaults.baseURL='http://localhost:8080'