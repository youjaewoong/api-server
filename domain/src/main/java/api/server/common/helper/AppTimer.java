package api.server.common.helper;

import java.util.Timer;
import java.util.TimerTask;

public class AppTimer{
    
	private AppTask appTask;
	private AppTimerListener appEvt;
	private Timer timer;		
	private int cmd;
	
	public AppTimer(AppTimerListener appEvt){
		this.appEvt = appEvt;
	} 
	
	public void setAppTimer(long period){
		setAppTimer(-1, period);
	}
	
	public void setAppTimer(int cmd, long period){
		this.cmd = cmd;
		
		cancelAppTimer();
		
		appTask = new AppTask();
		timer = new Timer();
		timer.schedule(appTask, period);	
	}
	
	public void releaseAppTimer(boolean bReturn){
		cancelAppTimer();
		
		if(!bReturn) return;
		
		if(cmd == -1) appEvt.releaseTimer();
		else appEvt.releaseTimer(cmd);
	}
	
	public void cancelAppTimer(){
		if(timer != null){
			timer.cancel();
			timer = null;
		}	
		if(appTask != null){
			appTask.cancel();
			appTask = null;
		}
	}
	
	class AppTask extends TimerTask{
		public void run(){
			cancelAppTimer();
			
			if(cmd == -1) appEvt.expireTimer();
			else appEvt.expireTimer(cmd);
		}
	}
}