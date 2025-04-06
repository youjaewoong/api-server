package api.server.common.helper.cipher;


import api.server.payment.gateway.AbstractApp;

import static api.server.common.helper.AppUtil.isNumeric;

public class CipherFactory {

    private CipherFactory() {}

    /**
     * keyIndex 값에 따른 암복호화 구현체 리턴
     *
     * @param keyIndex
     * @return
     */
    public static AbstractCipher getCipher(String keyIndex) {

        // 잘못된 keyIndex가 넘어올 경우, config.xml에 설정된 keyIndex 값으로 세팅
        keyIndex = isNumeric(keyIndex) ? keyIndex : AbstractApp.g_appCfg.getKmsCekLastKeyIndex();

        if (Integer.parseInt(keyIndex) >= 1) {
            return new KmsCipher(keyIndex);
        }

        return new AppCipher();
    }

}
