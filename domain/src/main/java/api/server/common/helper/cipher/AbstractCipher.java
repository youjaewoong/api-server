package api.server.common.helper.cipher;

/**
 * 암,복호화 기능을 제공하는 클래스.
 *
 */
public interface AbstractCipher {


    /**
     * 카드정보 등 평문 문자열을 암호화하여 byte 배열로 리턴
     *
     * @param data
     * @return
     * @throws Exception
     */
    byte[] encryptData(byte[] data) throws Exception;


    /**
     * 암호화된 byte 배열을 복호화한 byte배열 리턴
     *
     * @param encBytes
     * @return
     */
    byte[] decryptData(byte[] encBytes) throws Exception;


    /**
     * 암호화된 String 값 복호화
     *
     * @param encData
     * @return
     * @throws Exception
     */
    String decryptData(String encData) throws Exception;

    String getKeyIndex();

}
