package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 14-3-28.
 */
public class EncryptSessionKeyBean implements Parcelable {
    // 结果码
    private int result;

    // 用户标识
    private String receiverUserMark;

    // 会话密钥保护密钥版本号
    private int sessionKEKVersion;

    // 加密后的会话密钥
    private byte[] encryptSessionKey;

    public EncryptSessionKeyBean() {
    }

    public EncryptSessionKeyBean(Parcel pl) {
        result = pl.readInt();
        receiverUserMark = pl.readString();
        sessionKEKVersion = pl.readInt();
        int skLen = pl.readInt();
        encryptSessionKey = new byte[skLen];
        pl.readByteArray(encryptSessionKey);
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getReceiverUserMark() {
        return receiverUserMark;
    }

    public void setReceiverUserMark(String receiverUserMark) {
        this.receiverUserMark = receiverUserMark;
    }

    public int getSessionKEKVersion() {
        return sessionKEKVersion;
    }

    public void setSessionKEKVersion(int sessionKEKVersion) {
        this.sessionKEKVersion = sessionKEKVersion;
    }

    public byte[] getEncryptSessionKey() {
        return encryptSessionKey;
    }

    public void setEncryptSessionKey(byte[] encryptSessionKey) {
        this.encryptSessionKey = encryptSessionKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(result);
        parcel.writeString(receiverUserMark);
        parcel.writeInt(sessionKEKVersion);
        parcel.writeInt(encryptSessionKey.length);
        parcel.writeByteArray(encryptSessionKey);
    }

    public static final Creator<EncryptSessionKeyBean> CREATOR = new Creator<EncryptSessionKeyBean>() {

        @Override
        public EncryptSessionKeyBean createFromParcel(Parcel source) {
            return new EncryptSessionKeyBean(source);
        }

        @Override
        public EncryptSessionKeyBean[] newArray(int size) {
            return new EncryptSessionKeyBean[size];
        }
    };
}
