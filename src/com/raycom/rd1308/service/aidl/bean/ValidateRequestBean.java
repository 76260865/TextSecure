package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 14-6-6.
 */
public class ValidateRequestBean implements Parcelable {
    // 用户标识:邮箱地址\电话号码
    private String userMark;

    // 验证码
    private String validateCode;

    public ValidateRequestBean() {
    }

    public ValidateRequestBean(Parcel pl) {
        userMark = pl.readString();
        validateCode = pl.readString();
    }

    public String getUserMark() {
        return userMark;
    }

    public void setUserMark(String userMark) {
        this.userMark = userMark;
    }

    public String getValidateCode() {
        return validateCode;
    }

    public void setValidateCode(String validateCode) {
        this.validateCode = validateCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userMark);
        parcel.writeString(validateCode);
    }

    public static final Creator<ValidateRequestBean> CREATOR = new Creator<ValidateRequestBean>() {

        @Override
        public ValidateRequestBean createFromParcel(Parcel source) {
            return new ValidateRequestBean(source);
        }

        @Override
        public ValidateRequestBean[] newArray(int size) {
            return new ValidateRequestBean[size];
        }
    };
}
