package api.server.payment.gateway;


import api.server.payment.my.MyException;

import java.util.Map;

public abstract class AbstractVer {
    public AppWorker appWorker;

    public void setAppWorker(AppWorker appWorker){
        this.appWorker = appWorker;

        setAppParameter();
    }

    protected abstract void setAppParameter();
    protected abstract void procTransaction() throws MyException;
    protected abstract void procTransaction(Map<String, String> dataInfo) throws MyException;
}


