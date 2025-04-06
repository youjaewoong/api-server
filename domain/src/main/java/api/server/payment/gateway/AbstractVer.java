package api.server.payment.gateway;


import api.server.payment.my.MyException;

public abstract class AbstractVer {
    public AppWorker appWorker;

    public void setAppWorker(AppWorker appWorker){
        this.appWorker = appWorker;

        setAppParameter();
    }

    protected abstract void setAppParameter();
    protected abstract void procTransaction() throws MyException;
}


