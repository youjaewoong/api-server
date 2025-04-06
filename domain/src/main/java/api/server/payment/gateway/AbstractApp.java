package api.server.payment.gateway;


import api.server.common.config.datasource.DBConnectionManager;
import api.server.payment.config.Configuration;
import api.server.payment.van.AppVANManager;

public abstract class AbstractApp {

	public static Configuration g_appCfg;
	public static DBConnectionManager g_dbcm;
	public static AppVANManager g_van;
}