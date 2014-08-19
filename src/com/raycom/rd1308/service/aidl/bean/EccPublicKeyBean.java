package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;
import com.raycom.interfaces.algorithm.EccPublicKey;
import com.raycom.rd1308.certificate.RaycomV1PublicKey;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 14-3-26.
 */
public class EccPublicKeyBean implements Parcelable {
    // 公钥
    private EccPublicKey eccPublicKey;

    public EccPublicKeyBean(EccPublicKey eccPublicKey) {
        this.eccPublicKey = eccPublicKey;
    }

    public EccPublicKeyBean(Parcel pl) {
        int pubKeyLen = pl.readInt();
        byte[] pubKey = new byte[pubKeyLen];
        pl.readByteArray(pubKey);
        eccPublicKey = new RaycomV1PublicKey();
        eccPublicKey.decode(ByteBuffer.wrap(pubKey));
    }

    public EccPublicKey getEccPublicKey() {
        return eccPublicKey;
    }

    public void setEccPublicKey(EccPublicKey eccPublicKey) {
        this.eccPublicKey = eccPublicKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        byte[] pubKey = eccPublicKey.getEncoded();
        parcel.writeInt(pubKey.length);
        parcel.writeByteArray(pubKey);
    }

    public static final Creator<EccPublicKeyBean> CREATOR = new Creator<EccPublicKeyBean>() {

        @Override
        public EccPublicKeyBean createFromParcel(Parcel source) {
            return new EccPublicKeyBean(source);
        }

        @Override
        public EccPublicKeyBean[] newArray(int size) {
            return new EccPublicKeyBean[size];
        }
    };
}
