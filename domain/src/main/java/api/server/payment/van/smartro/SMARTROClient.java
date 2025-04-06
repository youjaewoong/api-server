/*
 * SMARTROClient.java
 *
 * Created on 2008년 10월 7일 (화), 오후 12:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package api.server.payment.van.smartro;

import api.server.payment.van.AppVANClient;
import java.io.InputStream;

/**
 * @author 공현진
 */
public class SMARTROClient extends AppVANClient {

    /**
     * Creates a new instance of SMARTROClient
     */
    public SMARTROClient() {
        super();
    }

    private int read(InputStream in, byte[] b, int off, int len) throws Exception {
        return in.read(b, off, len);
    }

    public byte[] recvData(int type) throws Exception {
        if (type == TYPE_LEN) {
            //전체 데이터 길이 수신
            byte[] bLen = new byte[4];
            int i = read(m_is, bLen, 0, 4);
            System.out.println("bLen : " + new String(bLen));
            if (i == -1) {
                return null;
            }

            int nLen = Integer.parseInt(new String(bLen));

            int nIdx = 0;
            byte[] bData = new byte[nLen];
            byte[] bRecv = new byte[nLen];

            //전체길이만큼 데이터 수신
            while (true) {
                i = read(m_is, bData, 0, nLen - nIdx);
                if (i == -1) {
                    break;
                }

                System.arraycopy(bData, 0, bRecv, nIdx, i);
                nIdx += i;

                if (nIdx == nLen) break;
            }

            return bRecv;
        } else if (type == TYPE_CHAR) {
            int i = 0;
            int nIdx = 0;
            byte[] bTemp = new byte[2];
            byte[] bData = new byte[1024];

            //전체길이만큼 데이터 수신
            while (true) {
                i = read(m_is, bTemp, 0, 1);
                if (i == -1 || bTemp[0] == 0x0D) {
                    break;
                }

                System.arraycopy(bTemp, 0, bData, nIdx, i);
                nIdx += i;
            }

            byte[] bRecv = new byte[nIdx];
            System.arraycopy(bData, 0, bRecv, 0, nIdx);

            return bRecv;
        }

        return null;
    }

    public void sendData(byte[] dataBytes) throws Exception {
        m_os.write(dataBytes);
    }
}
