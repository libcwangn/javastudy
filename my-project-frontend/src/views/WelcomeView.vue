<script setup>

</script>

<template>
  <div style="width:100vw;height:100vh;overflow:hidden;display:flex">
     <div class="left-side" style="flex:1;background: black">
        <el-image style="width: 100%;height: 100%" fit="cover"
            src="20241103100334173059941468502.jpg" ></el-image>
       <div class="welcome-title">
         <div style=" font-family:'Huawei Sans',sans-serif;text-align: center; font-size: 30px;font-weight: bold">欢迎来到我的个人项目</div>
         <div style="text-align: center;margin-top: 10px">我在这里展示学习java的成果</div>
       </div>
        </div>

    <div class="right-side">
<router-view/>
    </div>
  </div>
</template>

<style scoped>
.right-side{
  width:400px;
  z-index: 1;
  background:white;
}
.welcome-title{
  position:absolute;
  bottom:750px;
  left:30%;
  color: #000000;
  text-shadow:  0 0 10px #fff3f3;
}
</style>
