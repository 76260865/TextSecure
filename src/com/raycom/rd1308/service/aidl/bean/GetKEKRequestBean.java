package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

//import com.raycom.rd1308.proto.KMTerminalProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-3-28.
 */
public class GetKEKRequestBean implements Parcelable {
    // SIM序列号(作废)
    private String imsi;

    // 会话密钥保护密钥版本号
    private List<SessionKEKInfoBean> sessionKEKInfos = new ArrayList<SessionKEKInfoBean>();

    public GetKEKRequestBean() {
    }

    public GetKEKRequestBean(Parcel pl) {
        imsi = pl.readString();
        sessionKEKInfos = pl.readArrayList(SessionKEKInfoBean.class.getClassLoader());
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public void addSessionKEKInfo(SessionKEKInfoBean bean) {
        this.sessionKEKInfos.add(bean);
    }

    public List<SessionKEKInfoBean> getSessionKEKInfos() {
        return sessionKEKInfos;
    }

    public void addSessionKEKInfos(List<SessionKEKInfoBean> sessionKEKInfos) {
        this.sessionKEKInfos.addAll(sessionKEKInfos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imsi);
        parcel.writeList(sessionKEKInfos);
    }

    public static final Creator<GetKEKRequestBean> CREATOR = new Creator<GetKEKRequestBean>() {

        @Override
        public GetKEKRequestBean createFromParcel(Parcel source) {
            return new GetKEKRequestBean(source);
        }

        @Override
        public GetKEKRequestBean[] newArray(int size) {
            return new GetKEKRequestBean[size];
        }
    };
}
