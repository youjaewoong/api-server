/*
 * DriveConfig.java
 *
 * Created on 2006년 9월 2일 (토), 오후 1:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package api.server.gateway.legacy.config;

import java.util.Hashtable;

/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public class GroupConfig {
    private String name;
    private String code;
    private Hashtable commonConfigMap = new Hashtable();

    /** Creates a new instance of DriveConfig */
    public GroupConfig() {
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setCode(String value) {
        this.code = value;
    }
    public String getCode() {
        return code;
    }

    public void addCommonConfig(CommonConfig config){
        commonConfigMap.put(config.getName(), config);
    }

    public CommonConfig getCommonConfig(String name){
        return (CommonConfig)commonConfigMap.get(name);
    }
}
