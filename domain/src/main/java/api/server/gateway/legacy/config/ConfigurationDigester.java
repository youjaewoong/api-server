package api.server.gateway.legacy.config;

import org.apache.commons.digester.Digester;

import java.io.FileInputStream;
import java.io.InputStream;

/************************************************
 * 
 * @author 케이알파트너스
 * 
 */
public class ConfigurationDigester {
    public static final String FILE_NAME = "D:\\dev\\money\\backand\\kicc-gateway\\config.xml";

    public static Configuration digest() throws Exception {
        return ConfigurationDigester.digest(FILE_NAME);
    }

    public static Configuration digest(String fileName) throws Exception {
        Digester digester = new Digester();
        digester.setValidating(false);

        digester.addObjectCreate("configuration", Configuration.class);

        digester.addObjectCreate("configuration/server-config/server-common", CommonConfig.class);
        digester.addSetProperties("configuration/server-config/server-common", "name", "name");
        digester.addSetProperties("configuration/server-config/server-common", "value", "value");
        digester.addSetNext("configuration/server-config/server-common", "addServerCommonConfig");

        digester.addObjectCreate("configuration/server-config/server-system", GroupConfig.class);
        digester.addSetProperties("configuration/server-config/server-system", "name", "name");
        digester.addObjectCreate("configuration/server-config/server-system/attr", CommonConfig.class);
        digester.addSetProperties("configuration/server-config/server-system/attr", "name", "name");
        digester.addSetProperties("configuration/server-config/server-system/attr", "value", "value");
        digester.addSetNext("configuration/server-config/server-system/attr", "addCommonConfig");
        digester.addSetNext("configuration/server-config/server-system", "addServerConfig");

        //DB
        digester.addObjectCreate("configuration/db-config/db-common", CommonConfig.class);
        digester.addSetProperties("configuration/db-config/db-common", "name", "name");
        digester.addSetProperties("configuration/db-config/db-common", "value", "value");
        digester.addSetNext("configuration/db-config/db-common", "addDBCommonConfig");

        digester.addObjectCreate("configuration/db-config/db-system", GroupConfig.class);
        digester.addSetProperties("configuration/db-config/db-system", "name", "name");
        digester.addObjectCreate("configuration/db-config/db-system/attr", CommonConfig.class);
        digester.addSetProperties("configuration/db-config/db-system/attr", "name", "name");
        digester.addSetProperties("configuration/db-config/db-system/attr", "value", "value");
        digester.addSetNext("configuration/db-config/db-system/attr", "addCommonConfig");
        digester.addSetNext("configuration/db-config/db-system", "addDBConfig");

        //KMS
        digester.addObjectCreate("configuration/kms-config/kms-common", CommonConfig.class);
        digester.addSetProperties("configuration/kms-config/kms-common", "name", "name");
        digester.addSetProperties("configuration/kms-config/kms-common", "value", "value");
        digester.addSetNext("configuration/kms-config/kms-common", "addKMSCommonConfig");

        digester.addObjectCreate("configuration/kms-config/kms-system", GroupConfig.class);
        digester.addSetProperties("configuration/kms-config/kms-system", "name", "name");
        digester.addObjectCreate("configuration/kms-config/kms-system/attr", CommonConfig.class);
        digester.addSetProperties("configuration/kms-config/kms-system/attr", "name", "name");
        digester.addSetProperties("configuration/kms-config/kms-system/attr", "value", "value");
        digester.addSetNext("configuration/kms-config/kms-system/attr", "addCommonConfig");
        digester.addSetNext("configuration/kms-config/kms-system", "addKMSConfig");

        //VAN
        digester.addObjectCreate("configuration/van-config/van-common", CommonConfig.class);
        digester.addSetProperties("configuration/van-config/van-common", "name", "name");
        digester.addSetProperties("configuration/van-config/van-common", "value", "value");
        digester.addSetNext("configuration/van-config/van-common", "addVANCommonConfig");

        digester.addObjectCreate("configuration/van-config/van-system", GroupConfig.class);
        digester.addSetProperties("configuration/van-config/van-system", "name", "name");
        digester.addObjectCreate("configuration/van-config/van-system/attr", CommonConfig.class);
        digester.addSetProperties("configuration/van-config/van-system/attr", "name", "name");
        digester.addSetProperties("configuration/van-config/van-system/attr", "value", "value");
        digester.addSetNext("configuration/van-config/van-system/attr", "addCommonConfig");
        digester.addSetNext("configuration/van-config/van-system", "addVANConfig");

        //Settlement
        digester.addObjectCreate("configuration/settle-config/settle-common", CommonConfig.class);
        digester.addSetProperties("configuration/settle-config/settle-common", "name", "name");
        digester.addSetProperties("configuration/settle-config/settle-common", "value", "value");
        digester.addSetNext("configuration/settle-config/settle-common", "addSettleCommonConfig");

        digester.addObjectCreate("configuration/settle-config/settle-system", GroupConfig.class);
        digester.addSetProperties("configuration/settle-config/settle-system", "name", "name");
        digester.addObjectCreate("configuration/settle-config/settle-system/attr", CommonConfig.class);
        digester.addSetProperties("configuration/settle-config/settle-system/attr", "name", "name");
        digester.addSetProperties("configuration/settle-config/settle-system/attr", "value", "value");
        digester.addSetNext("configuration/settle-config/settle-system/attr", "addCommonConfig");
        digester.addSetNext("configuration/settle-config/settle-system", "addSettleConfig");




        //File configurationFile = new File(fileName);
        try {

            InputStream inputStream = new FileInputStream(FILE_NAME);
            Configuration configuration = (Configuration) digester.parse(inputStream);
            return configuration;
        } catch(Exception e) {
            System.out.println("[ConfigurationDigester::digest]Exception : " + e);
            throw e;
        }
    }
}
