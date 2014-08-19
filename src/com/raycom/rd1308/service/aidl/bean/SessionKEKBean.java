package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 14-3-27.
 */
public class SessionKEKBean implements Parcelable {
    // 结果码
    private int result;

    // 用户标识：邮箱地址\电话号码.....
    private String userMark;

    // 会话密钥保护客钥版本号
    private int sessionKEKVersion;

    // 会话密钥
    private byte[] sessionKEK;

    public SessionKEKBean() {
    }

    public SessionKEKBean(Parcel pl) {
        result = pl.readInt();
        userMark = pl.readString();
        sessionKEKVersion = pl.readInt();
        int sKEKLen = pl.readInt();
        sessionKEK = new byte[sKEKLen];
        pl.readByteArray(sessionKEK);
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getUserMark() {
        return userMark;
    }

    public void setUserMark(String userMark) {
        this.userMark = userMark;
    }

    public int getSessionKEKVersion() {
        return sessionKEKVersion;
    }

    public void setSessionKEKVersion(int sessionKEKVersion) {
        this.sessionKEKVersion = sessionKEKVersion;
    }

    public byte[] getSessionKEK() {
        return sessionKEK;
    }

    public void setSessionKEK(byte[] sessionKEK) {
        this.sessionKEK = sessionKEK;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(result);
        parcel.writeString(userMark);
        parcel.writeInt(sessionKEKVersion);
        parcel.writeInt(sessionKEK.length);
        parcel.writeByteArray(sessionKEK);
    }

    public static final Creator<SessionKEKBean> CREATOR = new Creator<SessionKEKBean>() {

        @Override
        public SessionKEKBean createFromParcel(Parcel source) {
            return new SessionKEKBean(source);
        }

        @Override
        public SessionKEKBean[] newArray(int size) {
            return new SessionKEKBean[size];
        }
    };
}
