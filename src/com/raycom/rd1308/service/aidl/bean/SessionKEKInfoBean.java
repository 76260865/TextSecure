package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 14-3-28.
 */
public class SessionKEKInfoBean implements Parcelable {
    // 用户标识：邮箱地址\电话号码.....
    private String userMar;

    // SessionKEK的版本号,如果需要获取最新的版本,则不填写本字段
    private int sessionKEKVersion;

    public SessionKEKInfoBean() {
    }

    public SessionKEKInfoBean(Parcel pl) {
        userMar = pl.readString();
        sessionKEKVersion = pl.readInt();
    }

    public int getSessionKEKVersion() {
        return sessionKEKVersion;
    }

    public void setSessionKEKVersion(int sessionKEKVersion) {
        this.sessionKEKVersion = sessionKEKVersion;
    }

    public String getUserMar() {
        return userMar;
    }

    public void setUserMar(String userMar) {
        this.userMar = userMar;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userMar);
        parcel.writeInt(sessionKEKVersion);
    }

    public static final Creator<SessionKEKInfoBean> CREATOR = new Creator<SessionKEKInfoBean>() {

        @Override
        public SessionKEKInfoBean createFromParcel(Parcel source) {
            return new SessionKEKInfoBean(source);
        }

        @Override
        public SessionKEKInfoBean[] newArray(int size) {
            return new SessionKEKInfoBean[size];
        }
    };
}
