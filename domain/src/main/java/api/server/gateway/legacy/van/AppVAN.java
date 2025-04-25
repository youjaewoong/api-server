/*
 * Created on 2005. 10. 12
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package api.server.gateway.legacy.van;

import api.server.gateway.legacy.my.MyException;
import api.server.gateway.legacy.my.MyException;

import java.util.Map;

/************************************************
 * 
 * @author 케이알파트너스
 */
public interface AppVAN {
	/**
	 * 
	* @methodName    : procVANStressTest
	* @return        : void
	* @author        : Edgar
	* @date          : 2024.03.15
	* @description   : 부하테스트용 default 메소드 선언 (implements한 클래스에서 미구현 하더라도 컴파일 에러 회피위함)
	* @param dataInfo
	* @throws MyException
	 */
	public default void procVANStressTest(Map<String, String> dataInfo) throws MyException {
		
	}
    
    public void procVANTest(Map<String, String> dataInfo) throws MyException, MyException;
    
    public void procVAN(Map<String, String> dataInfo) throws MyException;
    
}
