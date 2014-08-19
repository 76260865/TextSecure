package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 14-6-6.
 */
public class ValidateResponseBean implements Parcelable {
    // 结果码
    private int result;

    // 会话密钥保护密钥
    private SessionKEKBean sessionKEK;
    // 随机数种子
    private byte[] randomSeed;

    public ValidateResponseBean() {
    }

    public ValidateResponseBean(Parcel pl) {
        result = pl.readInt();
        sessionKEK = pl.readParcelable(SessionKEKBean.class.getClassLoader());
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public SessionKEKBean getSessionKEK() {
        return sessionKEK;
    }

    public void setSessionKEK(SessionKEKBean sessionKEK) {
        this.sessionKEK = sessionKEK;
    }

    public byte[] getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(byte[] randomSeed) {
        this.randomSeed = randomSeed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(result);
        parcel.writeValue(SessionKEKBean.class.getClassLoader());
    }

    public static final Creator<ValidateResponseBean> CREATOR = new Creator<ValidateResponseBean>() {

        @Override
        public ValidateResponseBean createFromParcel(Parcel source) {
            return new ValidateResponseBean(source);
        }

        @Override
        public ValidateResponseBean[] newArray(int size) {
            return new ValidateResponseBean[size];
        }
    };
}
