
package api.server.gateway.legacy.my;

import java.util.EventListener;

public interface MyEventListener extends EventListener{
	
	public void onEvent(MyEvent event);
	
}
