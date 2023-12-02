import Vue from 'vue';
import VueRouter from 'vue-router';

Vue.use(VueRouter)

import DataDictionary from "../components/DataDictionary";

const routes = [
    {path: "/DataDictionary", component: DataDictionary},
    //可以配置重定向
    {path: '', redirect: "DataDictionary"},
    //或者重新写个路径为空的路由
    {path: '', component: DataDictionary}
]

const router = new VueRouter({
    routes
});


let AUTH_URL = [];

router.beforeEach(async (to, from, next) => {
    if (to.fullPath === from.fullPath) { //防止页面死循环
        next()
    } else if (AUTH_URL.includes(to.fullPath)) {
        if (!sessionStorage.getItem("token")) {
            next({path: "login"})
        } else {
            next()
        }
    } else {
        next()
    }
});

export default router