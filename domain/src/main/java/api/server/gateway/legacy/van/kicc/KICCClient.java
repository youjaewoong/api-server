
package api.server.gateway.legacy.van.kicc;


import api.server.gateway.legacy.van.AppVANClient;

import java.io.InputStream;

/**********************************************
 * 
 * @author krpartners
 */

public class KICCClient extends AppVANClient {

    /** Creates a new instance of KICCClient */
    public KICCClient() {
        super();
    }

     private int read(InputStream in, byte[] b, int off, int len) throws Exception{
		return in.read(b, off, len);
	}

    public byte[] recvData(int type) throws Exception{
		if(type == TYPE_LEN){
            //전체 데이터 길이 수신
            byte[] bLen = new byte[4];
            int i = read(m_is, bLen, 0, 4);

            if(i == -1){
                return null;
            }

            int nLen = Integer.parseInt(new String(bLen));

            int nIdx = 0;
            byte[] bData = new byte[nLen];
            byte[] bRecv = new byte[nLen];

            //전체길이만큼 데이터 수신
            while(true){
                i = read(m_is, bData, 0, nLen-nIdx);
                if(i == -1){
                    break;
                }

                System.arraycopy(bData, 0, bRecv, nIdx, i); nIdx += i;

                if(nIdx == nLen) break;
            }

            return bRecv;
        }else if(type == TYPE_CHAR){
            int i = 0;
            int nIdx = 0;
            byte[] bTemp = new byte[2];
            byte[] bData = new byte[1024];
            
            //전체길이만큼 데이터 수신
            while(true){
                i = read(m_is, bTemp, 0, 1);
                if(i == -1 || bTemp[0] == 0x0D){
                    break;
                }

                System.arraycopy(bTemp, 0, bData, nIdx, i); nIdx += i;
            }

            byte[] bRecv = new byte[nIdx];
            System.arraycopy(bData, 0, bRecv, 0, nIdx);

            return bRecv;
        }

        return null;
	}

     public void sendData(byte[] dataBytes) throws Exception{
        m_os.write(dataBytes);
    }
}
