/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.server.payment.my;

import java.net.Socket;
/**
 *
 * @author PC-2038
 */
public class MyObject {
    
    Socket m_socket;
    long start_time;
    
    public void setSocket(Socket m_socket) {
        this.m_socket = m_socket;
    }
    
    public Socket getSocket() {
        return m_socket;
    }
            
    public void setStartTime(long start_time) {
        this.start_time = start_time;
    }
    
    public long getStartTime() {
        return start_time;
    }
    
}
