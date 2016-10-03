
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
public class OutgoingListener extends Thread{

    private byte[] sendData;
    private DatagramPacket sendPacket;
    private final DatagramSocket socket;
    private final Node node;
        
    OutgoingListener(Node node,DatagramSocket socket) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run(){
        //if(!(node instanceof InitiatorConnection)){
        if(!node.isServer()){
            try {
                send(Messages.FINDSUCCESSOR
                    +Messages.REGEX+node.getReference().getInitiatorID()
//                    +Messages.REGEX+node.getReference().getInitiatorPort()
                    +Messages.REGEX+node.getID()
                    +Messages.REGEX+node.getPort()
                    +Messages.REGEX+node.getAddress(),
                    InetAddress.getByName(node.getReference().getInitiatorAddress())/*InetAddress.getByName(node.getInitiatorID())*/,node.getReference().getInitiatorPort());
            } catch (UnknownHostException ex) {
                Logger.getLogger(OutgoingListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void send(String msg,InetAddress address,int port) {
        try {
            sendData = new byte[1024];
            sendData = msg.getBytes();
            sendPacket = new DatagramPacket(sendData,sendData.length,address,port);
            socket.send(sendPacket);
        } catch (IOException ex) {

        }
    }
}