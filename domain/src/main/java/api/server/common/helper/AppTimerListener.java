package api.server.common.helper;

public interface AppTimerListener{
	
	public void releaseTimer();
	public void releaseTimer(int cmd);
	
	public void expireTimer();
	public void expireTimer(int cmd);	
}