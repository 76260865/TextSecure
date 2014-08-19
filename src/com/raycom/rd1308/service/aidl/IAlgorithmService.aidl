package com.raycom.rd1308.service.aidl;
import com.raycom.rd1308.service.aidl.bean.EccPublicKeyBean;
import com.raycom.rd1308.service.aidl.bean.EccSignatureBean;
import com.raycom.rd1308.service.aidl.bean.EccKeyPairBean;
import com.raycom.rd1308.service.aidl.bean.RaycomCertificateBean;

interface IAlgorithmService
{
    /**
     * 产生内部Ecc密钥对
     */
    EccKeyPairBean genEccKeyPair();

    /**
     * 产生外部Ecc密钥对
     */
    EccKeyPairBean genExpEccKeyPair();

    /**
     * 导入密钥对
     */
    void importEccKeyPair(in byte[] priKey, in byte[] pubKey);

    /**
     * 导出公钥
     */
    EccPublicKeyBean exportPublicKey();

    /**
     * 使用外部公钥加密数据
     */
    byte[] extEccEncrypt(in byte[] pubKey, in byte[] data);

    /**
     * 使用外部私钥解密数据
     */
    byte[] extEccDecrypt(in byte[] priKey, in byte[] encryptedData);

    /**
     * 使用外部私钥签名
     */
    EccSignatureBean extEccSign(in byte[] priKey, in byte[] data);

    /**
     * 使用外部公钥验证签名
     */
    boolean extEccVerify(in byte[] pubKey, in byte[] data, in byte[] signature);

    /**
     * 使用内部私钥签名
     */
    EccSignatureBean eccSignData(in byte[] data);

    /**
     * 使用内部公钥验证
     */
    boolean eccVerify(in byte[] data, in byte[] signature);

    /**
     * 使用内部公钥加密数据
     */
    byte[] eccEncrypt(in byte[] data);

    /**
     * 使用内部私钥解密数据
     */
    byte[] eccDecrypt(in byte[] encryptedData);

    /**
     * 内部私钥点积
     * @param pubKey 公钥
     * @return 点积结果
     */
    byte[] eccDotProduct(in byte[] pubKey);

     /**
      * 外来私钥点积
      * @param priKey 私钥
      * @param pubKey 公钥
      * @return 点积结果
      */
     byte[] extEccDotProduct(in byte[] priKey, in byte[] pubKey);

     /**
      * 内部生成会话密钥,并导出会话密钥密文
      * @param publicKey     加密会话密钥的公钥
      * @param encryptedKey  加密后的会话密钥
      * @return 会话密钥句柄
      */
     long genSessionKey(in byte[] publicKeyData, out byte[] encryptedKey);

     /**
      * 导入公钥加密后的会话密钥,用于密钥交换
      * @param keyData 会话密钥
      * @return 会话密钥句柄
      */
     long importSessionKey(in byte[] keyData);

    /**
     * 导入明文会话密钥
     * @param keyData 会话密钥
     * @return 会话密钥句柄
     */
    long setSymmKey(in byte[] keyData);

    /**
     * 根据密钥句柄删除会话密钥
     * @param hKey 密钥句柄
     */
    void deleteSessionKey(long hKey);

    /**
     * 对称加密初始化
     * @param ulAlgId       算法标识
     * @param hKey          会话密钥句柄
     * @param iv            初始向量
     * @param paddingType   填充方式，0表示不填充，1表示按照PKCS#5方式进行填充
     */
    void encryptInit(int ulAlgId, long hKey, in byte[] iv, int paddingType);

    /**
     * 对称加密数据
     */
    byte[] encrypt(long hKey, in byte[] data);

    /**
     * 对称分组加密更新
     */
    byte[] encryptUpdate(long hKey, in byte[] data);

    /**
     * 对称分组加密结束
     */
    byte[] encryptFinal(long hKey);

     /**
      * 对称解密数据初始化
      */
    void decryptInit(int ulAlgId, long hKey, in byte[] iv, int paddingType);
     /**
      * 对称解密数据
      */
    byte[] decrypt(long hKey, in byte[] encryptedData);
     /**
      * 对称分组解密数据更新
      */
    byte[] decryptUpdate(long hKey, in byte[] encryptedData);

    /**
     * 对称分组解密数据结束
     * hKey             [IN] 解密密钥句柄
     * bDecryptedData  [OUT] 指向解密结果缓冲区,如果此参数为NULL时,由pulDecryptedDataLen返回解密结果长度.
     * bDecryptedData  [IN, OUT] 输入时表示bDecryptedData缓冲区的长度,输出时表示解密结果长度.
     */
    byte[] decryptFinal(long hKey);

    // 消息鉴别码算法
    long macInit(long hKey, in byte[] iv);

    byte[] mac(long hMac, in byte[] data);

    void macUpdate(long hMac, in byte[] data);

    byte[] macFinal(long hMac);

    // 摘要算法
    /**
     * 摘要初始化
     */
    long digestInit(int ulAlgID, long hKey);

    /**
     * 对数据摘要
     */
    byte[] digest(long hHash, in byte[] data);

    /**
     * 对数据分组摘要更新
     */
    void digestUpdate(long hHash, in byte[] data);

    /**
     * 对数据分组摘要结束
     */
    byte[] digestFinal(long hHash);

    /**
     * 获取随机数
     * @param random 根据参数长度生成随机数,并输出
     */
    void getRandom(out byte[] random);

    /**
     * 获取工作密钥长度
     */
    int getWkLen();

    int getIvLen();

    int getPublicKeyLen();

    int getPrivateKeyLen();

    int getSignatureLen();

    int getHashLen();

    boolean login(String pin);

    void logout();

    boolean modifyPin(String pin, String newPin);

    /**
     * 读取证书链
     */
    List<RaycomCertificateBean> readCertificateChain();

    byte[] readHashKey();

    void saveSessionKEK(String keyName, in byte[] keyValue);

    byte[] readSessionKey(String keyName);

    /**
     * 验证证书
     */
    int verifyCertificate(in RaycomCertificateBean cerBean);
}