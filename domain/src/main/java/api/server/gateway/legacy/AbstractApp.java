package api.server.gateway.legacy;


import api.server.common.helper.DBConnectionManager;
import api.server.gateway.legacy.config.Configuration;
import api.server.gateway.legacy.van.AppVANManager;

public abstract class AbstractApp {

	public static Configuration g_appCfg;
	public static DBConnectionManager g_dbcm;
	public static AppVANManager g_van;
}