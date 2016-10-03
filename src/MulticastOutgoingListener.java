
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
class MulticastOutgoingListener {
    private final MulticastSocket mcSocket;
    
    private byte[] sendData;
    private DatagramPacket sendPacket;
    
    public MulticastOutgoingListener(MulticastSocket mcSocket) {
        this.mcSocket = mcSocket;        
    }
    
    void send(String msg) {
        try {
            sendData = new byte[1024];
            sendData = msg.getBytes();
            sendPacket = new DatagramPacket(sendData,sendData.length,
                    InetAddress.getByName(MulticastConnection.MULTICAST_ADDRESS),MulticastConnection.MULTICAST_PORT);
            mcSocket.send(sendPacket);
        } catch (IOException ex) {
        }
    }
}
