package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-3-28.
 */
public class GetKEKResponseBean implements Parcelable {
    // 结果码
    private int result;

    // 会话密钥保护密钥列表
    private List<SessionKEKBean> sessionKEKs = new ArrayList<SessionKEKBean>();

    public GetKEKResponseBean() {
    }

    public GetKEKResponseBean(Parcel pl) {
        result = pl.readInt();
        sessionKEKs = pl.readArrayList(SessionKEKBean.class.getClassLoader());
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

    public void setSessionKEKs(List<SessionKEKBean> sessionKEKs) {
        this.sessionKEKs = sessionKEKs;
    }

    public void addSessionKEKs(SessionKEKBean sessionKEKBean) {
        this.sessionKEKs.add(sessionKEKBean);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(result);
        parcel.writeList(sessionKEKs);
    }

    public static final Creator<GetKEKResponseBean> CREATOR = new Creator<GetKEKResponseBean>() {

        @Override
        public GetKEKResponseBean createFromParcel(Parcel source) {
            return new GetKEKResponseBean(source);
        }

        @Override
        public GetKEKResponseBean[] newArray(int size) {
            return new GetKEKResponseBean[size];
        }
    };
}
