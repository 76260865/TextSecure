package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-3-28.
 */
public class GetEKSResponseBean implements Parcelable {
    // 结果码
    private int result;

    // 加密后的会话密钥列表
    List<EncryptSessionKeyBean> encryptSessionKeys = new ArrayList<EncryptSessionKeyBean>();

    // 会话密钥保护密钥
    private SessionKEKBean sessionKEKBean;

    public GetEKSResponseBean() {
    }

    public GetEKSResponseBean(Parcel pl) {
        result = pl.readInt();
        encryptSessionKeys = pl.readArrayList(EncryptSessionKeyBean.class.getClassLoader());
        sessionKEKBean = pl.readParcelable(SessionKEKBean.class.getClassLoader());
    }

    public SessionKEKBean getSessionKEKBean() {
        return sessionKEKBean;
    }

    public void setSessionKEKBean(SessionKEKBean sessionKEKBean) {
        this.sessionKEKBean = sessionKEKBean;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public List<EncryptSessionKeyBean> getEncryptSessionKeys() {
        return encryptSessionKeys;
    }

    public void setEncryptSessionKeys(List<EncryptSessionKeyBean> encryptSessionKeys) {
        this.encryptSessionKeys = encryptSessionKeys;
    }

    public void addEncryptSessionKeys(EncryptSessionKeyBean encryptSessionKeyBean) {
        this.encryptSessionKeys.add(encryptSessionKeyBean);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(result);
        parcel.writeList(encryptSessionKeys);
        parcel.writeValue(SessionKEKBean.class.getClassLoader());
    }

    public static final Creator<GetEKSResponseBean> CREATOR = new Creator<GetEKSResponseBean>() {

        @Override
        public GetEKSResponseBean createFromParcel(Parcel source) {
            return new GetEKSResponseBean(source);
        }

        @Override
        public GetEKSResponseBean[] newArray(int size) {
            return new GetEKSResponseBean[size];
        }
    };
}
