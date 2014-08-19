package com.raycom.rd1308.service.aidl;
import com.raycom.rd1308.service.aidl.bean.ValidateResponseBean;
import com.raycom.rd1308.service.aidl.bean.LoginResponseBean;
import com.raycom.rd1308.service.aidl.bean.GetKEKResponseBean;
import com.raycom.rd1308.service.aidl.bean.GetEKSResponseBean;
import com.raycom.rd1308.service.aidl.bean.SessionKEKBean;
import com.raycom.rd1308.service.aidl.bean.SessionKEKInfoBean;
interface ICallBack{
    //注册回调
    void registerResponse(int result);
    //验证号码回调
    void validateResponse(in ValidateResponseBean bean);
    //登录回调
    void loginResponse(in LoginResponseBean bean);
    //退出回调
    void logoutResponse(int result);
    //获取邮箱地址相应版本KEK申请回调
    void getKEKResponse(in GetKEKResponseBean bean);
    //向密钥管理服务器请求接收方会话密钥申请回调
    void getEKSResponseBean(in GetEKSResponseBean bean);
    //// 远程控制回调
    void remoteControlResponse(int result);
}