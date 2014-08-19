package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 14-3-28.
 */
public class LogoutRequestBean implements Parcelable {
    // SIM序列号,作废
    private String imsi;

    // 用户标识
    private String userMark;

    public LogoutRequestBean(){}
    public LogoutRequestBean(Parcel pl){
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
    public static final Creator<LogoutRequestBean> CREATOR = new Creator<LogoutRequestBean>() {

        @Override
        public LogoutRequestBean createFromParcel(Parcel source) {
            return new LogoutRequestBean(source);
        }

        @Override
        public LogoutRequestBean[] newArray(int size) {
            return new LogoutRequestBean[size];
        }
    };
}
