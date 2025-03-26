<script setup xmlns="http://www.w3.org/1999/html">
import {ElMessage} from "element-plus";

const active = ref(0)
import {reactive ,ref,computed} from "vue";
import {Message, EditPen, Lock} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import {get, post} from "../../../net/index.js";
const formRef = ref()
const form = reactive({
  email: "",
  code: "",
  password: "",
  password_repeat: "",
})
const coldTime=ref(0)
const  isEmailValid = computed(() => /^\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}/.test(form.email))
function askCode(){
  if(isEmailValid){
    coldTime.value=60
    get(`/api/auth/ask-code?email=${form.email}&type=register`,()=>{
      ElMessage.success(`验证码已经成功发送到邮箱:${form.email},请查收`)
      setInterval(()=>coldTime.value--,1000)
    },()=>{
      ElMessage.warning(message)
      coldTime.value=0
    })
  }else{
    ElMessage.warning("请输入正确的电子邮件")
  }
}
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}
const rules = {
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
}
const confirmReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('api/auth/reset-confirm',{
        email: form.email,
        code: form.code,
      },()=> active.value++)
    }
  })
}
const doReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('api/auth/reset-password',{...form
      },()=> {
        ElMessage.success('重置成功')
        router.push('/')
      })
    }
  })
}
</script>

<template>
  <div>
  <div style="margin-top: 30px ">
    <el-steps :active="active" finish-status="success" align-center>
      <el-step title="验证电子邮件" />
      <el-step title="重新设定密码" />
    </el-steps>
  </div>
    <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 0">
      <div style="margin-top: 80px">
        <div style="font-size: 25px;font-weight: bold">重置密码</div>
        <div style="font-size: 14px;color: grey">请输入需要重置密码的电子邮件地址</div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="email">
            <el-input v-model="form.email" type="text" placeholder="电子邮件地址">
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-row :gutter="10" style="width: 100%">
              <el-col :span="17">
                <el-input v-model="form.code" maxlength="6" minlength="6" type="text" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon>
                      <EditPen />
                    </el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="5">
                <el-button @click="askCode" :disabled="!isEmailValid||coldTime>0" type="success" >
                  {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </div>
      <div style="text-align: center;margin-top: 20px">
        <el-button @click="confirmReset" style="width: 75%" type="warning" plain>开始重置密码</el-button>
      </div>
    </div>

    <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 1">
      <div style="margin-top: 80px">
        <div style="font-size: 25px;font-weight: bold">重置密码</div>
        <div style="font-size: 14px;color: grey">请填写你的新密码,务必牢记</div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="password">
            <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
              <template #prefix>
                <el-icon>
                  <Lock />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password_repeat">
            <el-input v-model="form.password_repeat" maxlength="20" type="password" placeholder="重复密码">
              <template #prefix>
                <el-icon>
                  <Lock />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

        </el-form>
      </div>
      <div style="text-align: center;margin-top: 20px">
        <el-button @click="doReset" style="width: 75%" type="danger" plain>立即重置密码</el-button>
      </div>
    </div>

  </div>
</template>

<style scoped>

</style>