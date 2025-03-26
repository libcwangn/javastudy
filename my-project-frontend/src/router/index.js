import {createRouter, createWebHistory} from "vue-router";
import {unauthorized} from "../../net/index.js";

const router=createRouter({
    history:createWebHistory(import.meta.env.BASE_URL),
    routes:[
        {
            path:'/',
            name:'Welcome',
            component:() => import('@/views/WelcomeView.vue'),
            children:[
                {
                    path:'',
                    name:'welcome-login',
                    component:()=>import('@/views/welcome/LoginPage.vue')
                },{
                    path:'register',
                    name:'welcome-register',
                    component:()=>import('@/views/welcome/RegisterPage.vue')
                },{
                    path: 'reset-password',
                    name: 'welcome-reset-password',
                    component: () => import('@/views/welcome/ResetPage.vue')
                }
            ]
        },{
            path: '/index',
            name:'index',
            component: ()=>import('@/views/indexView.vue')
        }
    ]
});
router.beforeEach((to, from, next)=>{
    const isUnauthorized = unauthorized()
    if(to.name.startsWith('welcome-')&& !isUnauthorized){
        next('/index')
    }else if(to.fullPath.startsWith('/index')&&isUnauthorized){
        next('/')
    }else {
        next()
    }
})

export default router;