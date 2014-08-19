package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 14-6-6.
 */
public class RegisterRequestBean implements Parcelable {
    // SIM序列号(作废)
    private String imsi;

    // 用户标识:邮箱地址\电话号码
    private String userMark;

    public RegisterRequestBean() {
    }

    public RegisterRequestBean(Parcel pl) {
        imsi = pl.readString();
        userMark = pl.readString();
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getUserMark() {
        return userMark;
    }

    public void setUserMark(String userMark) {
        this.userMark = userMark;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imsi);
        parcel.writeString(userMark);
    }

    public static final Creator<RegisterRequestBean> CREATOR = new Creator<RegisterRequestBean>() {

        @Override
        public RegisterRequestBean createFromParcel(Parcel source) {
            return new RegisterRequestBean(source);
        }

        @Override
        public RegisterRequestBean[] newArray(int size) {
            return new RegisterRequestBean[size];
        }
    };
}
