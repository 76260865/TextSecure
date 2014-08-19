package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-3-28.
 */
public class GetEKSRequestBean implements Parcelable {
    // SIM序列号
    private String imsi;

    // 欠费标志
    private int flag;

    // 发送方用户标识：邮箱地址\电话号码.....
    private String senderUserMark;

    // 会话密钥保护密钥版本号
    private int sessionKEKVersion;

    // 接收方用户标识：邮箱地址\电话号码.....
    private List<String> receiverUserMarks = new ArrayList<String>();

    // 会话密钥
    private byte[] sessionKey;

    private byte[] iv;

    public GetEKSRequestBean(){}
    public GetEKSRequestBean(Parcel pl){
        imsi = pl.readString();
        flag = pl.readInt();
        senderUserMark = pl.readString();
//        int reLen = pl.readInt();
//        receiverUserMarks = new String[reLen];
//        pl.readStringArray(receiverUserMarks);
        sessionKEKVersion = pl.readInt();
        receiverUserMarks = pl.readArrayList(String.class.getClassLoader());
        int skLen = pl.readInt();
        sessionKey = new byte[skLen];
        pl.readByteArray(sessionKey);
        int msgKeyLen = pl.readInt();
        iv = new byte[msgKeyLen];
        pl.readByteArray(iv);
    }

    public int getSessionKEKVersion() {
        return sessionKEKVersion;
    }

    public void setSessionKEKVersion(int sessionKEKVersion) {
        this.sessionKEKVersion = sessionKEKVersion;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getSenderUserMark() {
        return senderUserMark;
    }

    public void setSenderUserMark(String senderUserMark) {
        this.senderUserMark = senderUserMark;
    }

    public List<String> getReceiverUserMarks() {
        return receiverUserMarks;
    }

    public void addReceiverUserMarks(List<String> receiverUserMarks) {
        this.receiverUserMarks.addAll(receiverUserMarks);
    }

    public void addReceiverUserMarks(String receiverUserMarks){
        this.receiverUserMarks.add(receiverUserMarks);
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imsi);
        parcel.writeInt(flag);
        parcel.writeString(senderUserMark);
        parcel.writeInt(sessionKEKVersion);
//        parcel.writeInt(receiverUserMarks.length);
//        parcel.writeStringArray(receiverUserMarks);
        parcel.writeList(receiverUserMarks);
        parcel.writeInt(sessionKey.length);
        parcel.writeByteArray(sessionKey);
        parcel.writeInt(iv.length);
        parcel.writeByteArray(iv);
    }
    public static final Creator<GetEKSRequestBean> CREATOR = new Creator<GetEKSRequestBean>() {

        @Override
        public GetEKSRequestBean createFromParcel(Parcel source) {
            return new GetEKSRequestBean(source);
        }

        @Override
        public GetEKSRequestBean[] newArray(int size) {
            return new GetEKSRequestBean[size];
        }
    };
}
