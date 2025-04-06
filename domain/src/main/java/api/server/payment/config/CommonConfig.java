/*
 * CommonConfig.java
 *
 * Created on 2006년 9월 1일 (금), 오후 5:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package api.server.payment.config;

/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public class CommonConfig {
    private String name;
    private String value;

    /** Creates a new instance of CommonConfig */
    public CommonConfig() {
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
