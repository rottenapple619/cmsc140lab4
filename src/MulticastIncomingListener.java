
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
class MulticastIncomingListener extends Thread {

    private byte[] receiveData;
    private DatagramPacket receivePacket;
    private boolean isRunning = false;
    private final MulticastSocket mcSocket;
        
    public MulticastIncomingListener(MulticastSocket mcSocket) {
        isRunning = true;
        this.mcSocket = mcSocket;
    }


    @Override
    public void run(){
        try{
            while(isRunning){
                receiveData = new byte[1024];
                receivePacket = new DatagramPacket(receiveData,receiveData.length);
                mcSocket.receive(receivePacket);
                String incomingMessage = new String(receivePacket.getData(),0,receivePacket.getLength())
                        +Messages.REGEX+receivePacket.getAddress().getHostAddress();
                        //+"_"+receivePacket.getPort();
                MulticastProtocol.check(incomingMessage);
            }
        } catch (IOException ex) {
        }
    }
    
}
