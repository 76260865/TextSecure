package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;
import com.raycom.interfaces.algorithm.EccSignature;
import com.raycom.rd1308.certificate.RaycomV1Signature;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 14-3-27.
 */
public class EccSignatureBean implements Parcelable {
    // 签名
    private EccSignature signature;

    public EccSignatureBean(EccSignature signature) {
        this.signature = signature;
    }

    public EccSignatureBean(Parcel pl) {
        int signatureLen = pl.readInt();
        byte[] signatureArray = new byte[signatureLen];
        pl.readByteArray(signatureArray);
        signature = new RaycomV1Signature();
        signature.decode(ByteBuffer.wrap(signatureArray));
    }

    public EccSignature getSignature() {
        return signature;
    }

    public void setSignature(EccSignature signature) {
        this.signature = signature;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        byte[] signatureArray = signature.getEncoded();
        parcel.writeInt(signatureArray.length);
        parcel.writeByteArray(signatureArray);

    }

    public static final Creator<EccSignatureBean> CREATOR = new Creator<EccSignatureBean>() {

        @Override
        public EccSignatureBean createFromParcel(Parcel source) {
            return new EccSignatureBean(source);
        }

        @Override
        public EccSignatureBean[] newArray(int size) {
            return new EccSignatureBean[size];
        }
    };
}
