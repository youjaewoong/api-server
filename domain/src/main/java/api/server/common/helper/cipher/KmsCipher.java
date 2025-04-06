package api.server.common.helper.cipher;


import api.server.payment.gateway.AbstractApp;
import com.exim.kms.client.KmsClient;

import java.util.Base64;


/**
 * KMSBuilder 와 통신하는 KmsClient 라이브러리에 대한 유틸
 *
 */
public class KmsCipher implements AbstractCipher {

    private final String keyIndex;

    /**
     * DB 프로퍼티 설정 암호화 시, keyIndex 값은 사용하지 않음
     */
    public KmsCipher() {
        this.keyIndex = "";
    }

    public KmsCipher(String keyIndex) {
        this.keyIndex = keyIndex;
    }

    @Override
    public String getKeyIndex() {
        return this.keyIndex;
    }

    /**
     * 암호화된 property 값을 복호화하여 리턴하는 함수
     *
     * @param encPropertyValue
     * @return
     * @throws Exception
     */
    public String decryptProperty(String encPropertyValue) throws Exception{
        String kmsServiceKeyId = AbstractApp.g_appCfg.getKmsPropSvcId();

        KmsClient kmsClient = getKmsClient();
        byte[] encKey = kmsClient.getEncKey(kmsServiceKeyId);
        byte[] base64DecBytes = Base64.getDecoder().decode(encPropertyValue);

        return new String(kmsClient.decrypt(base64DecBytes, encKey));
    }

    @Override
    public byte[] encryptData(byte[] data) throws Exception {
        String keyIndex = this.keyIndex;
        String kmsServiceKeyId = AbstractApp.g_appCfg.getKmsCekSvcId(keyIndex);

        KmsClient kmsClient = getKmsClient();
        byte[] encKey = kmsClient.getEncKey(kmsServiceKeyId);

        return kmsClient.encrypt(data, encKey);
    }

    /**
     * 암호화된 byte 배열을 복호화한 byte배열 리턴
     *
     * @param encBytes
     * @return
     * @throws Exception
     */
    @Override
    public byte[] decryptData(byte[] encBytes) throws Exception {
        String kmsServiceKeyId = AbstractApp.g_appCfg.getKmsCekSvcId(this.keyIndex);

        KmsClient kmsClient = getKmsClient();
        byte[] encKey = kmsClient.getEncKey(kmsServiceKeyId);

        return kmsClient.decrypt(encBytes, encKey);
    }

    @Override
    public String decryptData(String encData) throws Exception {
        String kmsServiceKeyId = AbstractApp.g_appCfg.getKmsCekSvcId(this.keyIndex);

        KmsClient kmsClient = getKmsClient();
        byte[] encKey = kmsClient.getEncKey(kmsServiceKeyId);

        byte[] decBytes = kmsClient.decrypt(encData.getBytes(), encKey);

        return decBytes != null ? new String(decBytes) : "";

    }

    private KmsClient getKmsClient() {
        String kmsIp = AbstractApp.g_appCfg.getKmsIp();
        int kmsPort = AbstractApp.g_appCfg.getKmsPort();

        return KmsClient.getInstance(kmsIp, kmsPort);
    }

}