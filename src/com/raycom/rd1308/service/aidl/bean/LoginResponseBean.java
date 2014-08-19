package com.raycom.rd1308.service.aidl.bean;

/**
 * Created by Administrator on 14-3-28.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
public class LoginResponseBean implements Parcelable {
    // 结果码
    private int result;
    // 会话密钥保护密钥版列表
    private List<SessionKEKBean> sessionKEKs = new ArrayList<SessionKEKBean>();
    // 随机数种子
    private byte[] randomSeed;

    public LoginResponseBean() {

    }

    public LoginResponseBean(Parcel pl) {
        result = pl.readInt();
        sessionKEKs = pl.readArrayList(SessionKEKBean.class.getClassLoader());
        int randomLen = pl.readInt();
        randomSeed = new byte[randomLen];
        pl.readByteArray(randomSeed);
    }

    public byte[] getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(byte[] randomSeed) {
        this.randomSeed = randomSeed;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public List<SessionKEKBean> getSessionKEKs() {
        return sessionKEKs;
    }

    public void addSessionKEKS(SessionKEKBean kekBean) {
        this.sessionKEKs.add(kekBean);
    }

    public void setSessionKEKs(List<SessionKEKBean> sessionKEKs) {
        this.sessionKEKs = sessionKEKs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result);
        dest.writeList(sessionKEKs);
        dest.writeInt(randomSeed.length);
        dest.writeByteArray(randomSeed);
    }

    public static final Creator<LoginResponseBean> CREATOR = new Creator<LoginResponseBean>() {

        @Override
        public LoginResponseBean createFromParcel(Parcel source) {
            return new LoginResponseBean(source);
        }

        @Override
        public LoginResponseBean[] newArray(int size) {
            return new LoginResponseBean[size];
        }
    };
}

