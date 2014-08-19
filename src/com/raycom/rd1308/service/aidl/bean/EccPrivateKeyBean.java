package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;
import com.raycom.interfaces.algorithm.EccPrivateKey;
import com.raycom.rd1308.certificate.RaycomV1PrivateKey;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 14-3-26.
 */
public class EccPrivateKeyBean implements Parcelable {
    // 私钥
    private EccPrivateKey eccPrivateKey;

    public EccPrivateKeyBean(Parcel pl) {
        int priKeyLen = pl.readInt();
        byte[] priKey = new byte[priKeyLen];
        pl.readByteArray(priKey);
        eccPrivateKey = new RaycomV1PrivateKey();
        eccPrivateKey.decode(ByteBuffer.wrap(priKey));
    }

    public EccPrivateKey getEccPrivateKey() {
        return eccPrivateKey;
    }

    public void setEccPrivateKey(EccPrivateKey eccPrivateKey) {
        this.eccPrivateKey = eccPrivateKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        byte[] priKey = eccPrivateKey.getEncoded();
        parcel.writeInt(priKey.length);
        parcel.writeByteArray(priKey);
    }

    public static final Creator<EccPrivateKeyBean> CREATOR = new Creator<EccPrivateKeyBean>() {

        @Override
        public EccPrivateKeyBean createFromParcel(Parcel source) {
            return new EccPrivateKeyBean(source);
        }

        @Override
        public EccPrivateKeyBean[] newArray(int size) {
            return new EccPrivateKeyBean[size];
        }
    };
}
