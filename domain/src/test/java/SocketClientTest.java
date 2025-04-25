
import java.io.*;
import java.net.*;


public class SocketClientTest {
    public static void main(String[] args) {
        // 서버 IP 및 포트 설정
        String serverIp = "127.0.0.1"; // 테스트용 로컬 서버 IP
        int port = 14201;             // 서버가 열려 있는 포트 번호

        try (Socket socket = new Socket(serverIp, port)) {
            // 서버에 연결된 후 입출력 스트림 준비
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            // 서버로 데이터 전송
            writer.println("01931000KICC01100000202504011801171113DA79FAC2FA1F5848W0000089978620USD00000000010000000000000000000000000000C000411111******1111    3312**********          KR109611      202504011801173ojZigvnd0Dj");

            // 서버로부터 응답 수신
            String response = reader.readLine();
            System.out.println("서버 응답: " + response);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
