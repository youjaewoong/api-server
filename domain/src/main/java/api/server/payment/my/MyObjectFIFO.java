
package api.server.payment.my;

import java.util.Vector;

public class MyObjectFIFO{

	private Vector m_objects;
	private int m_tmpObjects;		//임시 쓰레드 수

	public MyObjectFIFO(int initalCapacity) {
		super();
		initalCapacity = Math.max(1, initalCapacity);
		m_objects = new Vector(initalCapacity);
		m_tmpObjects = 0;
	}

	public MyObjectFIFO(){
		m_objects = new Vector();
		//System.out.println("[MyObjectFIFO]size:"+ m_objects.size() +", capacity:" + m_objects.capacity());
	}

	public synchronized boolean isEmpty() {
		return m_objects.isEmpty();
	}

	public synchronized void add(Object obj) throws InterruptedException {
		while (m_objects.size() == m_objects.capacity()) {
			wait();
		}
		m_objects.addElement(obj);
		notifyAll();
	}

	public synchronized Object remove() throws InterruptedException {
		while (m_objects.size() == 0) {
			wait();
		}
		Object obj = m_objects.firstElement();
		m_objects.removeElementAt(0);
		notifyAll();
		return obj;
	}

	public synchronized Object[] removeAll() throws InterruptedException {
		Object[] objs = m_objects.toArray();
		m_objects.removeAllElements();
		return objs;
	}

	public synchronized int size(){
		return m_objects.size();
	}

	public synchronized void addTmpObjects(){
		m_tmpObjects++;
	}

	public synchronized void removeTmpObjects(){
		m_tmpObjects--;
	}

	public synchronized int sizeTmp(){
		return m_tmpObjects;
	}
}
