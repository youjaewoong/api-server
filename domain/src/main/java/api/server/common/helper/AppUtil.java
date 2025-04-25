package api.server.common.helper;


import java.util.Calendar;
import java.util.Random;

public class AppUtil{
	public final static int LEFT = 0;
	public final static int RIGHT = 1;
	public final static String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	/**
	 *	현재시간을 스트링으로 반환한다. (YYYYMMDDhhmmss)
	 */
	public static String getDateTime(){
		Calendar rightNow = Calendar.getInstance();
		int YYYY = rightNow.get(Calendar.YEAR);
		int MM = rightNow.get(Calendar.MONTH) + 1;
		int DD = rightNow.get(Calendar.DAY_OF_MONTH);
		int hh = rightNow.get(Calendar.HOUR_OF_DAY);
		int mm = rightNow.get(Calendar.MINUTE);
		int ss = rightNow.get(Calendar.SECOND);
		//int W = rightNow.get(Calendar.DAY_OF_WEEK);
		StringBuffer strbuf = new StringBuffer();
		strbuf.append(YYYY);
		if (MM < 10) strbuf.append('0');
		strbuf.append(MM);

		if (DD < 10) strbuf.append('0');
		strbuf.append(DD);

		if (hh < 10) strbuf.append('0');
		strbuf.append(hh);

		if (mm < 10) strbuf.append('0');
		strbuf.append(mm);

		if (ss < 10) strbuf.append('0');
		strbuf.append(ss);
		//strbuf.append(W);

		return strbuf.toString();
	}

    public static String getDateFormat(String dateStr){
		StringBuffer strbuf = new StringBuffer();
		strbuf.append(dateStr.substring(0, 4));
		strbuf.append("-");

		strbuf.append(dateStr.substring(4, 6));
		strbuf.append("-");

		strbuf.append(dateStr.substring(6, 8));

		return strbuf.toString();
	}

    public static String getDateEng(String strDate){
		int YYYY = Integer.parseInt(strDate.substring(0, 4));
		int MM = Integer.parseInt(strDate.substring(4, 6));
		int DD = Integer.parseInt(strDate.substring(6, 8));


		StringBuffer strbuf = new StringBuffer();
		switch(MM){
			case 1: strbuf.append("Jan. "); break;
			case 2: strbuf.append("Feb. "); break;
			case 3: strbuf.append("Mar. "); break;
			case 4: strbuf.append("Apr. "); break;
			case 5: strbuf.append("May. "); break;
			case 6: strbuf.append("Jun. "); break;
			case 7: strbuf.append("Jul. "); break;
			case 8: strbuf.append("Aug. "); break;
			case 9: strbuf.append("Sept. "); break;
			case 10: strbuf.append("Oct. "); break;
			case 11: strbuf.append("Nov. "); break;
			case 12: strbuf.append("Dec. "); break;
		}
		strbuf.append(DD);
		strbuf.append(", ");
		strbuf.append(YYYY);

		return strbuf.toString();
	}

    public static String getDateTimeFormat(String dateTime){
		if(dateTime.length() != 14) return dateTime;

		StringBuffer strbuf = new StringBuffer();
		strbuf.append(dateTime.substring(0, 4));
		strbuf.append("-");

		strbuf.append(dateTime.substring(4, 6));
		strbuf.append("-");

		strbuf.append(dateTime.substring(6, 8));
		strbuf.append(" ");

		strbuf.append(dateTime.substring(8, 10));
		strbuf.append(":");

		strbuf.append(dateTime.substring(10, 12));
		strbuf.append(":");

		strbuf.append(dateTime.substring(12, 14));

		return strbuf.toString();
	}

    public static int getDayOfWeek(){
		Calendar rightNow = Calendar.getInstance();
		int W = rightNow.get(Calendar.DAY_OF_WEEK);

		return W;
	}

	public static int getDayOfWeek(String YYYYMMDD){
		int YYYY = Integer.parseInt(YYYYMMDD.substring(0, 4));
		int MM = Integer.parseInt(YYYYMMDD.substring(4, 6));
		int DD = Integer.parseInt(YYYYMMDD.substring(6, 8));

		Calendar cal = Calendar.getInstance();
		cal.set(YYYY, MM-1, DD);
		int W = cal.get(Calendar.DAY_OF_WEEK);

		return W;
	}

    public static int getCurDayOfMonth(){
		Calendar rightNow = Calendar.getInstance();
		int YYYY = rightNow.get(Calendar.YEAR);
		int MM = rightNow.get(Calendar.MONTH) + 1;

		return getDayOfMonth(YYYY, MM);
	}

    public static int getDayOfMonth(int YYYY, int MM){
		int[] DOMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		int[] lDOMonth = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

		if((YYYY % 4) == 0){
			if((YYYY % 100) == 0 && (YYYY % 400) != 0) return DOMonth[MM-1];

			return lDOMonth[MM-1];

		}else return DOMonth[MM-1];
	}

    /* 몇일을 더해주는 function */
	public static String getDateadd(String dd, int i) throws Exception{
		String ss_date = "";

		try{
			Calendar cal = Calendar.getInstance();
			int year = Integer.parseInt(dd.substring(0,4));
			int month = Integer.parseInt(dd.substring(4,6));
			int day = Integer.parseInt(dd.substring(6,8));
			String mm;
			String day_s;

			cal.set(year, month-1, day);
			cal.add(Calendar.DATE , i);

			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH);
			day = cal.get(Calendar.DATE);

			month = month + 1;

			if ( month == 13 ) month = 1;

			if ( month < 10 ) mm = "0" + String.valueOf(month);
			else mm = String.valueOf(month);

			if ( day < 10 ) day_s = "0" + String.valueOf(day);
			else day_s = String.valueOf(day);

			ss_date = String.valueOf(year) + mm + day_s;

		}catch(Exception e){
			throw e;
		}

		return  ss_date;
	}

	/**
	 *	int 형의 자료를 입력받아 길이만큼 문자를 채워서 리턴하는 method
	 *
	 *	inputData : 원데이타
	 *	align     : 0 - left, 1 - right (어느쪽에 원 데이타를 둘건지...)
	 *	fillSize  : 늘리고자하는 길이
	 *	fillChar  : 채울 문자
	 *
	 *	예) : fmt(35, 1, 5, '0')  =>  "00035"
	 */
	public static String formatData(int value, int align, int fillSize, char fillChar) {
		return formatData(String.valueOf(value), align, fillSize, fillChar);
	}

	/**
	 *	String 형의 자료를 입력받아 길이만큼 문자를 채워서 리턴하는 method
	 *
	 *	inputData : 원데이타
	 *	align     : 0 - left, 1 - right (어느쪽에 원 데이타를 둘건지...)
	 *	fillSize  : 늘리고자하는 길이
	 *	fillChar  : 채울 문자
	 *
	 *	예) : fmt("string", 1, 10, '0')  =>  "0000string"
	 */
	public static String formatData(String data, int align, int fillSize, char fillChar) {
		if (data == null) {
			data = "";
		}
		byte[] bytes = data.getBytes();
		int len = bytes.length;
		if (len < fillSize) {	// 모자라는 길이만큼 채울 문자열을 만든다.
			if (align == LEFT) {
				StringBuffer strbuf = new StringBuffer(data);
				for (int i = len; i < fillSize; i++) {
					strbuf.append(fillChar);
				}
				return strbuf.toString();
			}
			else {
				StringBuffer strbuf = new StringBuffer();
				for (int i = len; i < fillSize; i++) {
					strbuf.append(fillChar);
				}
				strbuf.append(data);
				return strbuf.toString();
			}
		}
		else {	// 원하는 길이보다 크면 잘라 보낸다.
			//20021022 modified by kong,hyun jin
			//한글이 잘리는 경우 잘리는 글자는 제외
			String tmp = "";
			if(align == LEFT){
				int idx = getUniLength(bytes, fillSize, LEFT);
				tmp = new String(bytes, 0, idx);
				if(idx < fillSize){
					StringBuffer strbuf = new StringBuffer(tmp);
					for(int i=idx; i<fillSize; i++){
						strbuf.append(fillChar);
					}
					return strbuf.toString();
				}else{
					if(tmp.equals("")) tmp = new String(bytes, 0, fillSize-1) + " ";
					return tmp;
				}
			}
			else {
				int idx = getUniLength(bytes, fillSize, RIGHT);
				tmp = new String(bytes, idx, bytes.length-idx);
				if(bytes.length-idx < fillSize){
					StringBuffer strbuf = new StringBuffer();
					for(int i=bytes.length-idx; i<fillSize; i++){
						strbuf.append(fillChar);
					}
					strbuf.append(tmp);
					return strbuf.toString();
				}else{
					if(tmp.equals("")) tmp = new String(bytes, len - fillSize + 1, fillSize) + " ";
					return tmp;
				}
			}
		}
	}

	private static int getUniLength(byte[] bytes, int size, int align){
		//size가 한글이 잘리는 경우, 이전 글자까지 길이 반환
		int idx = 0;
		if(align == LEFT){
			for(idx=0; idx<bytes.length && idx<size; idx++){
				if(bytes[idx] > 127 || bytes[idx] < 0){
					idx++;
				}
			}
			if(idx > size) idx -= 2;
		}else{
			for(idx=0; idx<bytes.length-size; idx++){
				if(bytes[idx] > 127 || bytes[idx] < 0){
					idx++;
				}
			}
		}

		return idx;
	}

	public static String chkUniCode(byte[] bytes, int sIdx, int len){
		int idx = 0;
		for(idx=0; idx<len; idx++){
			//System.out.println("chkUniCode:"+(sIdx + idx)+":"+bytes[sIdx + idx]);
			if(bytes[sIdx + idx] > 127 || bytes[sIdx + idx] < 0){
				idx++;
			}
		}

		//System.out.println("idx : " + idx + ", len : " + len);

		if(idx > len) idx -= 2;

		return new String(bytes, sIdx, idx);
	}

	public static int convertByte2Int(byte[] b, int off) {

		if (b.length < off + 4) {
			throw new IllegalArgumentException();
		}

		//Big Endian
		int v = (((b[off+3] & 0xff)) + ((b[off + 2] & 0xff) << 8) + ((b[off + 1] & 0xff) <<  16) + ((b[off ] & 0xff) << 24));
		return v;

	}

	public static byte[] convertInt2Byte(int v) {

		byte[] b = new byte[4];
		b[0] = (byte) (0xff & (v      ));
		b[1] = (byte) (0xff & (v >>  8));
		b[2] = (byte) (0xff & (v >> 16));
		b[3] = (byte) (0xff & (v >> 24));
		return b;
	}

	public static int byteToInt(byte b) {
		return (int) b & 0xFF;
	}

	public static String toHexString(char[] letter){
		StringBuffer sbHex = new StringBuffer();
        for (int j = 0; j <letter.length; j++) {
            String hexValue = Integer.toHexString(letter[j]);
            sbHex.append(hexValue);
        }

        return sbHex.toString();
	}

	public static String toHexString(char ch){
		String hexValue = Integer.toHexString(ch);

        return hexValue;
	}

	public static String toHexString(byte b){
		String hexValue = Integer.toHexString((int)b&0xff);

        return hexValue;
	}

    public static String toHexString(byte[] letter){
        return toHexString(letter, "");
    }

	public static String toHexString(byte[] letter, String split){
		StringBuffer sbHex = new StringBuffer();
        for (int j = 0; j <letter.length; j++) {
            String hexValue = Integer.toHexString((int)letter[j]&0xff);
            sbHex.append(formatData(hexValue.toUpperCase(), RIGHT, 2, '0')+split);
        }

        return sbHex.toString();
	}

    public static byte[] toBytes(String hexStr){

        byte[] bcdBytes = new byte[8];
        byte high = 0x00, low = 0x00;
        for(int i=0; i<hexStr.length(); i+=2){
            if(hexStr.charAt(i) > '9') high = (byte)(hexStr.charAt(i) - 55);
            else high = (byte)(hexStr.charAt(i) - 48);

            if(hexStr.charAt(i+1) > '9') low = (byte)(hexStr.charAt(i+1) - 55);
            else low = (byte)(hexStr.charAt(i+1) - 48);

            bcdBytes[i/2] = (byte)((high << 4) | low);
        }

        return bcdBytes;
    }

    public static String getUniqueId(){
		String uniqueId = "";

		long ms = System.currentTimeMillis();
		long rn = (long)(Math.random() * (double)1000000000);

		String msHex = Long.toHexString(ms).toUpperCase();
		String rnHex = Long.toHexString(rn).toUpperCase();

		uniqueId = rnHex + msHex.substring(msHex.length() - (10 - rnHex.length()), msHex.length());

		return uniqueId;
	}

    public static String checkNull(Object obj){
		if(obj == null) return "";
		else return (String)obj;
	}

	public static int checkNullInt(Object obj){
		if(obj == null) return 0;
		else return Integer.parseInt((String)obj);
	}
    
    public static double checkNullDouble(Object obj){
		if(obj == null) return 0;
		else return Double.parseDouble((String)obj);
	}
    
    public static byte[] getMarkByte(byte[] src, String cardmark, int cardInx, String cvcmark, int cvcInx) {
         byte[] mark = cardmark.getBytes();
         byte[] cvc  = cvcmark.getBytes();
         byte[] dumpsrc = new byte[src.length];
         System.arraycopy(src, 0,  dumpsrc, 0, src.length);   
         System.arraycopy(mark, 0, dumpsrc, cardInx, mark.length);
         if (cvcInx > 0)
            System.arraycopy(cvc,  0, dumpsrc, cvcInx,  cvc.length);
         return dumpsrc;
    }
    
    public static byte[] getMarkByte(byte[] src, String cardmark, int cardInx, String cvcmark, int cvcInx, String ext1mark, int ext1Inx) {
         byte[] mark = cardmark.getBytes();
         byte[] cvc  = cvcmark.getBytes();
         byte[] ext1 = ext1mark.getBytes();
         byte[] dumpsrc = new byte[src.length];
         System.arraycopy(src, 0,  dumpsrc, 0, src.length);   
         System.arraycopy(mark, 0, dumpsrc, cardInx, mark.length);
         if (cvcInx > 0)
            System.arraycopy(cvc,  0, dumpsrc, cvcInx,  cvc.length);
         if (ext1Inx > 0)
            System.arraycopy(ext1, 0, dumpsrc, ext1Inx, ext1.length);
         return dumpsrc;
    }
    
    public static byte[] getMarkByte(byte[] src, String cardmark, int cardInx, String cvcmark, int cvcInx, String ext1mark, int ext1Inx, String ext2mark, int ext2Inx) {
         byte[] mark = cardmark.getBytes();
         byte[] cvc  = cvcmark.getBytes();
         byte[] ext1 = ext1mark.getBytes();
         byte[] ext2 = ext2mark.getBytes();
         byte[] dumpsrc = new byte[src.length];
         System.arraycopy(src, 0,  dumpsrc, 0, src.length);   
         System.arraycopy(mark, 0, dumpsrc, cardInx, mark.length);
         if (cvcInx > 0)
            System.arraycopy(cvc,  0, dumpsrc, cvcInx,  cvc.length);
         if (ext1Inx > 0)
            System.arraycopy(ext1, 0, dumpsrc, ext1Inx, ext1.length);
         if (ext2Inx > 0)
            System.arraycopy(ext2, 0, dumpsrc, ext2Inx, ext2.length);
         return dumpsrc;
    }
    
    public static byte[] getMarkByte(byte[] src, String cardmark, int cardInx, String cvcmark, int cvcInx, String ext1mark, int ext1Inx, String ext2mark, int ext2Inx, String ext3mark, int ext3Inx) {
         byte[] mark = cardmark.getBytes();
         byte[] cvc  = cvcmark.getBytes();
         byte[] ext1 = ext1mark.getBytes();
         byte[] ext2 = ext2mark.getBytes();
         byte[] ext3 = ext3mark.getBytes();
         byte[] dumpsrc = new byte[src.length];
         System.arraycopy(src, 0,  dumpsrc, 0, src.length);   
         System.arraycopy(mark, 0, dumpsrc, cardInx, mark.length);
         if (cvcInx > 0)
            System.arraycopy(cvc,  0, dumpsrc, cvcInx,  cvc.length);
         if (ext1Inx > 0)
            System.arraycopy(ext1, 0, dumpsrc, ext1Inx, ext1.length);
         if (ext2Inx > 0)
            System.arraycopy(ext2, 0, dumpsrc, ext2Inx, ext2.length);
         if (ext3Inx > 0)
            System.arraycopy(ext3, 0, dumpsrc, ext3Inx, ext3.length);
         return dumpsrc;
    }
    
    public static String getCurrencyCode(String currency) {
        if(currency.equals("KRW")) return "410";
        else if(currency.equals("USD")) return "840";
        else if(currency.equals("JPY")) return "392";
        else if(currency.equals("EUR")) return "978";
        else if(currency.equals("AUD")) return "036";
        else if(currency.equals("CAD")) return "124";
        else if(currency.equals("CNY")) return "156";
        else if(currency.equals("HKD")) return "344";
        else if(currency.equals("RUB")) return "643";
        else if(currency.equals("SGD")) return "702";
        else if(currency.equals("THB")) return "764";
        else if(currency.equals("GBP")) return "826";
        else if(currency.equals("TWD")) return "901";
        else if(currency.equals("MYR")) return "458";
        else if(currency.equals("VND")) return "704";
        else if(currency.equals("PHP")) return "608";
        else if(currency.equals("MNT")) return "496";
        else if(currency.equals("NZD")) return "554";
        else if(currency.equals("AED")) return "784";
        else if(currency.equals("MOP")) return "446";
        else if(currency.equals("BRL")) return "986";
        else if(currency.equals("KZT")) return "398";
        else if(currency.equals("NOK")) return "578";
        else if(currency.equals("SAR")) return "682";
        else if(currency.equals("TRY")) return "949";
        return "   "; 
    }
    
    public static String getCurrencyNum(String currency){
        if(currency.equals("KRW")) return "1";
        else if(currency.equals("USD")) return "2";
        else if(currency.equals("JPY")) return "3";
        else if(currency.equals("EUR")) return "4";
        else if(currency.equals("HKD")) return "5";
        else if(currency.equals("GBP")) return "6";
        else if(currency.equals("SGD")) return "7";
        else if(currency.equals("AUD")) return "8";
        else if(currency.equals("THB")) return "9";
        else if(currency.equals("CAD")) return "A";
        else if(currency.equals("RUB")) return "B";
        else if(currency.equals("CNY")) return "C";

        return "   ";
    }

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * 
	* @methodName    : generateKey
	* @return        : String
	* @author        : Edgar
	* @date          : 2025.03.07
	* @description   : table lock 제거하기 위해 유일성 보장 키 생성 추가 
	* @return
	 */
	public static String generateKey() {
        String ret = null;
        Random random = new Random();
    	
        // 타임스탬프 (밀리초) 가져와서 마지막 10자리사용
        long timestamp = System.currentTimeMillis() % 10000000000L;

        // Base62변환 - 최대 6자리
        String base62Timestamp = toBase62(timestamp);
        
        // 랜덤값 생성 (최대9자리 0~999999999) 
        int randomValue = random.nextInt(1000000000);
        
        // Base62변환 - 최대 6자리 
        String base62Random = toBase62(randomValue);
        
        // 결합
        ret = base62Timestamp + base62Random;
        
        // 길이부족시 랜덤문자를 뒤에 패딩하여 채운다
        while(ret.length() < 12) {
        	ret = ret + generateRandomCharacter(random);
        }
        
        return ret;
    }
    
    // 랜덤 문자 생성 메서드
    public static char generateRandomCharacter(Random random) {
        int charType = random.nextInt(3);  // 0: 대문자, 1: 소문자, 2: 숫자
        char randomChar = ' ';
        
        switch (charType) {
            case 0:
                randomChar = (char) ('A' + random.nextInt(26));  // 대문자 A-Z
                break;
            case 1:
                randomChar = (char) ('a' + random.nextInt(26));  // 소문자 a-z
                break;
            case 2:
                randomChar = (char) ('0' + random.nextInt(10));  // 숫자 0-9
                break;
        }
        
        return randomChar;
    }
	
	/**
	 * 
	* @methodName    : toBase62
	* @return        : String
	* @author        : Edgar
	* @date          : 2025.03.07
	* @description   : 
	* @param value
	* @return
	 */
    private static String toBase62(long value) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }

}
