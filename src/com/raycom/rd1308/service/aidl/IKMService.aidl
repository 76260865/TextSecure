package com.raycom.rd1308.service.aidl;

import com.raycom.rd1308.service.aidl.bean.RegisterRequestBean;
import com.raycom.rd1308.service.aidl.bean.ValidateRequestBean;
import com.raycom.rd1308.service.aidl.bean.LoginRequestBean;
import com.raycom.rd1308.service.aidl.bean.LoginResponseBean;
import com.raycom.rd1308.service.aidl.bean.LogoutRequestBean;
import com.raycom.rd1308.service.aidl.bean.GetKEKRequestBean;
import com.raycom.rd1308.service.aidl.bean.GetKEKResponseBean;
import com.raycom.rd1308.service.aidl.bean.GetEKSRequestBean;
import com.raycom.rd1308.service.aidl.bean.GetEKSResponseBean;
import com.raycom.rd1308.service.aidl.bean.EccPublicKeyBean;
import com.raycom.rd1308.service.aidl.bean.EccPrivateKeyBean;
import com.raycom.rd1308.service.aidl.bean.EccSignatureBean;
import com.raycom.rd1308.service.aidl.bean.EccKeyPairBean;
import com.raycom.rd1308.service.aidl.bean.EncryptSessionKeyBean;
import com.raycom.rd1308.service.aidl.bean.SessionKEKBean;
import com.raycom.rd1308.service.aidl.bean.SessionKEKInfoBean;
import com.raycom.rd1308.service.aidl.ICallBack;

interface IKMService
{
    //注册申请
    void registerRequest(String appID, in ICallBack cb,in RegisterRequestBean bean);
     // 验证号码申请(用于验证用户电话号码)
    void validateRequest(String appID, in ICallBack cb,in ValidateRequestBean bean);
    // 登录申请
    void loginRequest(String appID, in ICallBack cb, in LoginRequestBean bean);
    // 注销申请
    void logoutRequest(String appID, in ICallBack cb,in LogoutRequestBean bean);
    // 获取邮箱地址相应版本KEK申请
    void getKEKRequest(String appID, in ICallBack cb, in GetKEKRequestBean bean);
    //向密钥管理服务器请求接收方会话密钥申请
    void getEKSRequest(String appID, in ICallBack cb, in GetEKSRequestBean bean);
    // 远程控制申请
    void remoteControlRequest(String appID, in ICallBack cb, int type);
    // 获取证书状态,测试使用
    void getCertificateStatus();
}