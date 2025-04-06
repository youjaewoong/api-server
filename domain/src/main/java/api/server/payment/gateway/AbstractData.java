/*
 * Created on 2004. 9. 10.
 *
 */
package api.server.payment.gateway;

import api.server.common.exception.custom.BusinessException;

import java.util.HashMap;
import java.util.Map;

/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public abstract class AbstractData {
	
	protected Map<String, String> dataInfo;
	
    AbstractData(){
		dataInfo = new HashMap<>();
	}
    
    public Map<String, String> getRecvInfo(){
		return dataInfo;
	}
	
	public abstract Map<String, String> parseData(byte[] recvBytes) throws BusinessException;

	public abstract byte[] makeData() throws BusinessException;
    
}
