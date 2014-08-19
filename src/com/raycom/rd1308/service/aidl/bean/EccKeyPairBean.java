package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.raycom.interfaces.algorithm.EccPrivateKey;
import com.raycom.interfaces.algorithm.EccPublicKey;
import com.raycom.rd1308.certificate.RaycomV1PrivateKey;
import com.raycom.rd1308.certificate.RaycomV1PublicKey;

import java.nio.ByteBuffer;


/**
 * Created by Administrator on 14-3-26.
 */
public class EccKeyPairBean implements Parcelable {
    // 私钥
    private EccPrivateKey privateKey;

    // 公钥
    private EccPublicKey publicKey;

    public EccKeyPairBean(EccPrivateKey privateKey, EccPublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public EccKeyPairBean(Parcel pl) {
        int priKeyLen = pl.readInt();
        byte[] priKey = new byte[priKeyLen];
        pl.readByteArray(priKey);
        int pubKeyLen = pl.readInt();
        byte[] pubKey = new byte[pubKeyLen];
        pl.readByteArray(pubKey);
        privateKey = new RaycomV1PrivateKey();
        privateKey.decode(ByteBuffer.wrap(priKey));
        publicKey = new RaycomV1PublicKey();
        publicKey.decode(ByteBuffer.wrap(pubKey));
    }

    public EccPrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(EccPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public EccPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(EccPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        byte[] priKey = privateKey.getEncoded();
        parcel.writeInt(priKey.length);
        parcel.writeByteArray(priKey);
        byte[] pubKey = publicKey.getEncoded();
        parcel.writeInt(pubKey.length);
        parcel.writeByteArray(pubKey);
    }

    public static final Creator<EccKeyPairBean> CREATOR = new Creator<EccKeyPairBean>() {

        @Override
        public EccKeyPairBean createFromParcel(Parcel source) {
            return new EccKeyPairBean(source);
        }

        @Override
        public EccKeyPairBean[] newArray(int size) {
            return new EccKeyPairBean[size];
        }
    };
}
