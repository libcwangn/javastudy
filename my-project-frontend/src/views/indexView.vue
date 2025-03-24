<script setup>
import {login, logout} from "../../net/index.js";
import router from "@/router/index.js";

// 试图离开未保存的编辑文本界面

function userLogout(){

  logout(()=>{router.push('/')},()=>{});


}
</script>

<template>
  <router-view/>
  <div>
    <el-button @click="userLogout">退出登录</el-button>
  </div>

</template>

<style scoped>

</style>