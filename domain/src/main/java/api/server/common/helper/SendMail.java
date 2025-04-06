
package api.server.common.helper;


import api.server.payment.my.MyException;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class SendMail {
	//private final String SMTP = "webmail.eximbay.com";
	//private final String FROM = "webmaster@eximbay.com";
	//private final String PASSWORD = "eximbay2006";
	//private final String MAILER = "EXIMBAY";

	public SendMail(){

	}

	public String getMailFormat(String fileName) throws MyException {
		StringBuffer message = new StringBuffer();
        InputStream is = null;
        BufferedInputStream in = null;
		try{
            is = ClassLoader.getSystemResourceAsStream(fileName);

			in = new BufferedInputStream(is);
			while(true){
                byte[] data = new byte[1024];
				int temp = in.read(data, 0, 1024);
				if(temp == -1) break;

				message.append(new String(data, 0, temp));
			}
		}catch(Exception e){
			System.out.println("[SendMail::getMailFormat]Exception : " + e);
			throw new MyException("[SendMail::getMailFormat]", "SM01", "Exception:"+e, fileName + " reading is failed.");
		}finally{
            try{
                if(in != null) in.close();
                if(is != null) is.close();
            }catch(Exception ie){}
        }

		return message.toString();
	}

	public void send(String to, String from, String subject, String contents) throws MyException{
            send(to, from, subject, contents, "en");
	}

	public void send(String to, String from, String subject, String contents, String encoding) throws MyException{


	}

}