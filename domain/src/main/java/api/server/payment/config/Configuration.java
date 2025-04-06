package api.server.payment.config;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * 설정 정보를 저장하고 있는 객체
 */
public class Configuration implements java.io.Serializable {
    /**
     * <name, value> : server-config에 포함되어 있는 어플리케이션 속성을 저장하고 있다.
     */
    private Hashtable serverCommonConfig = new Hashtable();
    private Hashtable serverConfig = new Hashtable();

    private Hashtable mailConfigMap = new Hashtable();

    /**
     * <name, value> : db-config/db-common에 포함되어 있는 DB 속성을 저장하고 있다.
     */
    private Hashtable dbCommonConfig = new Hashtable();

    /**
     * <name, value> : db-config/db-system에 포함되어 있는 DB 속성을 저장하고 있다.
     */
    private Hashtable dbConfig = new Hashtable();

    /**
     * <name, value> : van-config에 포함되어 있는 VAN 속성을 저장하고 있다.
     */
    private Hashtable vanCommonConfig = new Hashtable();
    private Hashtable vanConfig = new Hashtable();

    /**
     * <name, value> : settle-config에 포함되어 있는 매입서버 속성을 저장하고 있다.
     */
    private Hashtable settleCommonConfig = new Hashtable();
    private Hashtable settleConfig = new Hashtable();

    /**
     * <name, value> : kms-config에 포함되어 있는 kms 시스템 속성을 저장하고 있다.
     *
     */
    private Hashtable kmsCommonConfig = new Hashtable();
    private Hashtable kmsConfig = new Hashtable();


    /**************************************************************************
     * <server-config>관련
     *************************************************************************/
    public void addServerCommonConfig(CommonConfig config) {
        serverCommonConfig.put(config.getName(), config);
    }

    public Hashtable getServerCommonConfig() {
        return serverCommonConfig;
    }

    public String getServerEnable(){
        CommonConfig config = (CommonConfig)serverCommonConfig.get("enable");
        return config.getValue();
    }

    public void addServerConfig(GroupConfig config) {
        serverConfig.put(config.getName(), config);
    }

    public Hashtable getServerConfig() {
        return serverConfig;
    }

    public String[] getServerNames(){
        String[] names = new String[serverConfig.size()];
        int i = 0;
        Enumeration en = serverConfig.keys();
        while(en.hasMoreElements()){
            names[i++] = (String)en.nextElement();
        }

        return names;
    }

    public GroupConfig getServerConfig(String name){
        return (GroupConfig)serverConfig.get(name);
    }

    public String getServerHost(String name){
        GroupConfig group = (GroupConfig)serverConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("host");
        return config.getValue();
    }

    public int getServerPort(String name){
        GroupConfig group = (GroupConfig)serverConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("port");
        return Integer.parseInt(config.getValue());
    }

    public int getServerThreads(String name){
        GroupConfig group = (GroupConfig)serverConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("threads");
        return Integer.parseInt(config.getValue());
    }

    public int getServerTimeout(String name){
        GroupConfig group = (GroupConfig)serverConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("timeout");
        return Integer.parseInt(config.getValue());
    }

    public String getServerRun(String name){
        GroupConfig group = (GroupConfig)serverConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("run");
        return config.getValue();
    }
    /**************************************************************************
     * <mail-config>
     *************************************************************************/
    public void addMailConfig(CommonConfig config) {
        mailConfigMap.put(config.getName(), config);
    }

    public Hashtable getMailConfig(){
        return mailConfigMap;
    }

    public String getSMTP(){
        //CommonConfig config = (CommonConfig)mailConfigMap.get("smtp");
        //return config.getValue();
        //return "mx.eximbay.com";
        return "email-smtp.ap-northeast-2.amazonaws.com";
    }

    public String getFrom(){
        //CommonConfig config = (CommonConfig)mailConfigMap.get("from");
        //return config.getValue();
        return "webmaster@eximbay.com";
    }

    public String getId(){
        //CommonConfig config = (CommonConfig)mailConfigMap.get("id");
        //return config.getValue();
        //return "webmaster@eximbay.com";
        return "AKIA4FNNFTBJMTFV25DF";
    }

    public String getPassword(){
        //CommonConfig config = (CommonConfig)mailConfigMap.get("password");
        //return config.getValue();
        return "BA6sHonM8NuOCbRUDqvOpQ7l/kv591j8RsTsyDmWCFnB";
    }

    public String getMailer(){
        //CommonConfig config = (CommonConfig)mailConfigMap.get("mailer");
        //return config.getValue();
        return "EXIMBAY";
    }


    /**************************************************************************
     * <db-config>관련
     *************************************************************************/
    public void addDBCommonConfig(CommonConfig config) {
        dbCommonConfig.put(config.getName(), config);
    }

    public Hashtable getDBCommonConfig() {
        return dbCommonConfig;
    }

    public String getDBDrivers(){
        //CommonConfig config = (CommonConfig)dbCommonConfig.get("drivers");
        //return config.getValue();
        return "com.mysql.jdbc.Driver";
    }

    public String getDBLogFile(){
        //CommonConfig config = (CommonConfig)dbCommonConfigMap.get("logfile");
        //return config.getValue();

        String sp = System.getProperty("file.separator");

        return "."+sp+"log"+sp+"DBConnectionManager.log";
    }

    public void addDBConfig(GroupConfig config){
        dbConfig.put(config.getName(), config);
    }

    public Hashtable getDBConfig(){
        return dbConfig;
    }

    public String[] getDBConfigNames(){
        String[] names = new String[dbConfig.size()];
        int i = 0;
        Enumeration en = dbConfig.keys();
        while(en.hasMoreElements()){
            names[i++] = (String)en.nextElement();
        }

        return names;
    }

    public String getDBUrl(String name){
        GroupConfig group = (GroupConfig)dbConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("url");
        System.out.println("[Configuration::getDBUrl]"+name+":"+config.getValue());
        return config.getValue();
    }

    public String getDBUser(String name){
        GroupConfig group = (GroupConfig)dbConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("user");
//System.out.println("[Configuration::getDBUser]"+name+":"+config.getValue());
        return config.getValue();
    }

    public String getDBPassword(String name){
        GroupConfig group = (GroupConfig)dbConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("password");
//System.out.println("[Configuration::getDBPassword]"+name+":"+config.getValue());
        return config.getValue();
    }

    public String getDBInitConn(String name){
        GroupConfig group = (GroupConfig)dbConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("initconn");
        System.out.println("[Configuration::getDBInitConn]"+name+":"+config.getValue());
        return config.getValue();
    }

    public String getDBMaxConn(String name){
        GroupConfig group = (GroupConfig)dbConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("maxconn");
        System.out.println("[Configuration::getDBMaxConn]"+name+":"+config.getValue());
        return config.getValue();
    }

    public String getDBLimitMax(String name){
        GroupConfig group = (GroupConfig)dbConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("limitmax");
        System.out.println("[Configuration::getDBLimitMax]"+name+":"+config.getValue());
        return config.getValue();
    }


    /**************************************************************************
     * <kms-config>관련
     *************************************************************************/
    public void addKMSCommonConfig(CommonConfig config) {
        kmsCommonConfig.put(config.getName(), config);
    }

    public void addKMSConfig(GroupConfig config){
        kmsConfig.put(config.getName(), config);
    }

    public String getKmsIp() {
        GroupConfig group = (GroupConfig) kmsConfig.get("kms");
        CommonConfig config =  (CommonConfig) group.getCommonConfig("kmsHost");
        System.out.println("[Configuration::getKmsIp]"+"kms"+":"+config.getValue());
        return config.getValue();
    }

    public int getKmsPort() {
        GroupConfig group = (GroupConfig) kmsConfig.get("kms");
        CommonConfig config =  (CommonConfig) group.getCommonConfig("kmsPort");
        System.out.println("[Configuration::getKmsPort]"+"kms"+":"+config.getValue());
        return Integer.parseInt(config.getValue());
    }

    public String getKmsPropSvcId() {
        GroupConfig group = (GroupConfig) kmsConfig.get("kms");
        CommonConfig config =  (CommonConfig) group.getCommonConfig("kmsPropSvcId");
        System.out.println("[Configuration::getKmsPropSvcId]"+"kms"+":"+config.getValue());
        return config.getValue();
    }

    public String getKmsCekLastKeyIndex() {
        GroupConfig group = (GroupConfig) kmsConfig.get("kms");
        CommonConfig config =  (CommonConfig) group.getCommonConfig("kmsCekLastKeyIndex");
        System.out.println("[Configuration::getKmsCekLastKeyIndex]"+"kms"+":"+config.getValue());
        return config.getValue();
    }

    public String getKmsCekSvcId(String keyIndex) {
        GroupConfig group = (GroupConfig) kmsConfig.get("kms");
        CommonConfig config =  (CommonConfig) group.getCommonConfig("kmsCekSvcId-" + keyIndex);
        System.out.println("[Configuration::getKmsCekSvcId]"+"kms"+":"+config.getValue());
        return config.getValue();
    }

    /**************************************************************************
     * <lu-config>관련
     *************************************************************************/
    public void addVANCommonConfig(CommonConfig config) {
        vanCommonConfig.put(config.getName(), config);
    }

    public Hashtable getVANCommonConfig() {
        return vanCommonConfig;
    }

    public String getVANEnable(){
        CommonConfig config = (CommonConfig)vanCommonConfig.get("enable");
        return config.getValue();
    }

    public void addVANConfig(GroupConfig config) {
        vanConfig.put(config.getName(), config);
    }

    public Hashtable getVANConfig() {
        return vanConfig;
    }

    public String[] getVANNames(){
        String[] names = new String[vanConfig.size()];
        int i = 0;
        Enumeration en = vanConfig.keys();
        while(en.hasMoreElements()){
            names[i++] = (String)en.nextElement();
        }

        return names;
    }

    public String getVANHost(String name){
        GroupConfig group = (GroupConfig)vanConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("host");
        return config.getValue();
    }

    public int getVANPort(String name){
        GroupConfig group = (GroupConfig)vanConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("port");
        return Integer.parseInt(config.getValue());
    }

    public int getVANThreads(String name){
        GroupConfig group = (GroupConfig)vanConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("threads");
        return Integer.parseInt(config.getValue());
    }

    public int getVANTimeout(String name){
        GroupConfig group = (GroupConfig)vanConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("timeout");
        return Integer.parseInt(config.getValue());
    }

    public String getVANClass(String name){
        GroupConfig group = (GroupConfig)vanConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("class");
        return config.getValue();
    }

    public String getVANType(String name){
        GroupConfig group = (GroupConfig)vanConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("type");
        return config.getValue();
    }

    /**************************************************************************
     * <db-config>관련
     *************************************************************************/
    public void addSettleCommonConfig(CommonConfig config) {
        settleCommonConfig.put(config.getName(), config);
    }

    public Hashtable getSettleCommonConfig() {
        return settleCommonConfig;
    }

    public String getSettleEnable(){
        CommonConfig config = (CommonConfig)settleCommonConfig.get("enable");
        return config.getValue();
    }

    public int getSettlePeroid(){
        CommonConfig config = (CommonConfig)settleCommonConfig.get("period");
        return Integer.parseInt(config.getValue());
    }

    public void addSettleConfig(GroupConfig config) {
        settleConfig.put(config.getName(), config);
    }

    public Hashtable getSettleConfig() {
        return settleConfig;
    }

    public ArrayList getSettleConfigNames(){
        ArrayList names = new ArrayList();
        int i = 0;
        Enumeration en = settleConfig.keys();
        while(en.hasMoreElements()){
            names.add((String)en.nextElement());
        }

        return names;
    }

    public String getSettleSystemHost(String name){
        GroupConfig group = (GroupConfig)settleConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("host");
        return config.getValue();
    }

    public int getSettleSystemPort(String name){
        GroupConfig group = (GroupConfig)settleConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("port");
        return Integer.parseInt(config.getValue());
    }

    public int getSettleSystemTimeout(String name){
        GroupConfig group = (GroupConfig)settleConfig.get(name);
        CommonConfig config = (CommonConfig)group.getCommonConfig("timeout");
        return Integer.parseInt(config.getValue());
    }


    /**************************************************************************
     * 시스템 고정
     *************************************************************************/
    private final String appName 	= "EXIMGW Ver1.0.0";
    private final int acceptQue 	= 1000;
    private final int appPriority	= 5;
    private final int vanTimeout	= 40;
    private final String dbName     = "eximgw";

    public String getAppName(){
        return appName;
    }

    public int getAppAcceptQue(){
        return acceptQue;
    }

    public int getAppPriority(){
        return appPriority;
    }

    public int getVanTimeout(){
        return vanTimeout;
    }

    public String getDBName(){
        return dbName;
    }
}