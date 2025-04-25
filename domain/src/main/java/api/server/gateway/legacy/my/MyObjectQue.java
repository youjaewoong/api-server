/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.server.gateway.legacy.my;
import java.util.Vector;

/**
 *
 * @author PC-2038
 */
public class MyObjectQue {
    
        private Vector m_objects;
        
        public MyObjectQue() {
            m_objects = new Vector();
        }
        
        public synchronized boolean isEmpty() {
                return m_objects.isEmpty();
        }

        public synchronized void add(Object obj) throws InterruptedException {
                m_objects.addElement(obj);
                
        }

        public synchronized Object remove() throws InterruptedException {
                Object obj = m_objects.firstElement();
                m_objects.removeElementAt(0);
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
    
}
