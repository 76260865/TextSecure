package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class LoginRequestBean implements Parcelable {
    // 用户标识列表
    private List<String> userMark = new ArrayList<String>();
    // IMSI号码
    private String imsi;

    public LoginRequestBean() {
    }

    public LoginRequestBean(Parcel pl) {
//		int ksLen = pl.readInt();
//		ks = new byte[ksLen];
//		pl.readByteArray(ks);
//        int addressLen = pl.readInt();
//        address = new String[addressLen];
//        pl.readStringArray(address);
        userMark = pl.readArrayList(String.class.getClassLoader());
        imsi = pl.readString();
//		int mkLen = pl.readInt();
//		mk = new byte[mkLen];
//		pl.readByteArray(mk);
    }

    //	public byte[] getKs() {
//		return ks;
//	}
//	public void setKs(byte[] ks) {
//		this.ks = ks;
//	}
    public List<String> getUserMark() {
        return userMark;
    }

    public void addUserMarks(List<String> userMarks) {
        this.userMark.addAll(userMarks);
    }

    public void addUserMark(String address) {
        this.userMark.add(address);
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    //	public byte[] getMk() {
//		return mk;
//	}
//	public void setMk(byte[] mk) {
//		this.mk = mk;
//	}
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//		dest.writeInt(CommUtil.KSLEN);
//		dest.writeByteArray(ks);
        //        dest.writeInt(address.length);
        //        dest.writeStringArray(address);
        dest.writeList(userMark);
        dest.writeString(imsi);
//		dest.writeInt(CommUtil.MKLEN);
//		dest.writeByteArray(mk);
    }

    public static final Creator<LoginRequestBean> CREATOR = new Creator<LoginRequestBean>() {

        @Override
        public LoginRequestBean createFromParcel(Parcel source) {
            return new LoginRequestBean(source);
        }

        @Override
        public LoginRequestBean[] newArray(int size) {
            return new LoginRequestBean[size];
        }
    };
}
