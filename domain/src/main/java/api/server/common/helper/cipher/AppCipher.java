package api.server.common.helper.cipher;

/*
 * AppCipher.java
 *
 * Created on 2007년 12월 28일 (금), 오후 12:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.Key;
/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public class AppCipher implements AbstractCipher {
	static private AppCipher instance;       // The single instance

    private Cipher cipher;
    private Key key;

    /*static synchronized public AppCipher getInstance(){
        if(instance == null){
            instance = new AppCipher();
        }
        return instance;
    }*/

    /** Creates a new instance of AppCipher */
    public AppCipher() {
        try{
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream("EXIMBAY.DAT")); //암호화 키값을 읽어오기
            key = (Key)oin.readObject();
            oin.close();

            //System.out.println("키파일 리딩 완료되었음");

            // for (int i = 0; i < key.getEncoded().length ; i++)    {
            //    System.out.print((key.getEncoded())[i] + " ");
            // }
            
            // Cipher를 생성, 사용할 키로 초기화
            cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        }catch(Exception e){
            System.out.println("[AppCipher]Key Exception : " + e.toString());
        }
    }

    @Override
    public byte[] encryptData(byte[] data){
        byte[] encBytes = null;

        try{
            //암호화 모드로서 다시 초기화
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // 암호화 시작
            encBytes = cipher.doFinal(data);

        }catch(Exception e){
            System.out.println("[AppCipher::encryptData]Exception : " + e + ":" + new String(data));
        }

        return encBytes;
    }


    @Override
    public byte[] decryptData(byte[] data){
        byte[] decBytes = null;

        try{
            //암호화 모드로서 다시 초기화
            cipher.init(Cipher.DECRYPT_MODE, key);

            // 암호화 시작
            decBytes = cipher.doFinal(data);

        }catch(Exception e){
            System.out.println("[AppCipher::decryptData]Exception : " + e + ":" + new String(data));
        }

        return decBytes;
    }

    @Override
    public String decryptData(String encData) throws Exception {
        byte[] decBytes = null;

        try{
            //암호화 모드로서 다시 초기화
            cipher.init(Cipher.DECRYPT_MODE, key);

            // 암호화 시작
            decBytes = cipher.doFinal(encData.getBytes());

        }catch(Exception e){
            System.out.println("[AppCipher::decryptData]Exception : " + e + ":" + encData);
        }

        return new String(decBytes);
    }

    @Override
    public String getKeyIndex() {
        return "00";
    }

}
