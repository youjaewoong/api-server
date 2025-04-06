
package api.server.payment.my;

public class MyEvent extends Object {
	
	protected Object m_oSource;
	protected int m_iID;
	protected Object m_oParam;
	protected long m_lTime;
	
	public MyEvent(Object source) {
		this(source, 0, null);
	}
	
	public MyEvent(Object source, int id) {
		this(source, id, null);
	}
	
	public MyEvent(Object source, int id, Object param) {
		super();
		m_oSource = source;
		m_iID = id;
		m_oParam = param;
		m_lTime = System.currentTimeMillis();
	}
	
	public Object getSource() {
		return m_oSource;
	}
	
	public int getID() {
		return m_iID;
	}
	
	public Object getParam() {
		return m_oParam;
	}
	
	public long getTime() {
		return m_lTime;
	}
	
}
