import instance from './index';
const preUrlPath = '';
//登录
const Login = {
    p: ['get,/rest/api/auth/jwt'],
    r: params => {
        return instance.post(`${preUrlPath}/rest/api/auth/jwt`, params)
    }
};
//注销
const Logout = {
    p: ['post,/rest/api/auth/logout'],
    r: params => {
        return instance.get(`${preUrlPath}/test/user/2`, params)
    }
};
const ResetPassword = {
    p: ['post,/rest/api/auth/reset'],
    r: params => {
        return instance.post(`${preUrlPath}/rest/api/auth/resetPassword`, params)
    }
};

export {
    Login,Logout,instance,ResetPassword
}
