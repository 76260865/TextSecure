package com.raycom.rd1308.service.aidl.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.raycom.interfaces.certificate.ICertificate;
import com.raycom.rd1308.certificate.RaycomV1Certificate;

import java.nio.ByteBuffer;


/**
 * Created by Administrator on 14-3-27.
 */
public class RaycomCertificateBean implements Parcelable {
    // RCC证书
    private ICertificate certificate;

    public RaycomCertificateBean(ICertificate certificate) {
        this.certificate = certificate;
    }

    public RaycomCertificateBean(Parcel pl) {
        int cerLen = pl.readInt();
        byte[] signatureArray = new byte[cerLen];
        pl.readByteArray(signatureArray);
        certificate = new RaycomV1Certificate();
        certificate.decode(ByteBuffer.wrap(signatureArray));
    }

    public ICertificate getCertificate() {
        return certificate;
    }

    public void setCertificate(ICertificate certificate) {
        this.certificate = certificate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        byte[] data = certificate.getEncoded();
        parcel.writeInt(data.length);
        parcel.writeByteArray(data);

    }

    public static final Creator<RaycomCertificateBean> CREATOR = new Creator<RaycomCertificateBean>() {

        @Override
        public RaycomCertificateBean createFromParcel(Parcel source) {
            return new RaycomCertificateBean(source);
        }

        @Override
        public RaycomCertificateBean[] newArray(int size) {
            return new RaycomCertificateBean[size];
        }
    };
}
