
package api.server.payment.my;

import java.util.EventListener;

public interface MyEventListener extends EventListener{
	
	public void onEvent(MyEvent event);
	
}
