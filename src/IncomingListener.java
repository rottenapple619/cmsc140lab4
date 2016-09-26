
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
class IncomingListener extends Thread{
    private byte[] receiveData;
    private DatagramPacket receivePacket;

    private boolean isRunning = false;
    private final DatagramSocket socket;
    private final Node node;

    public IncomingListener(Node node, DatagramSocket socket) {
        isRunning = true;
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run(){
        try{
            while(isRunning){
                receiveData = new byte[1024];
                receivePacket = new DatagramPacket(receiveData,receiveData.length);
                socket.receive(receivePacket);
                String incomingMessage = new String(receivePacket.getData(),0,receivePacket.getLength())
                        +Messages.REGEX+receivePacket.getAddress().getHostAddress()
                        +Messages.REGEX+receivePacket.getPort();
//                if(node instanceof InitiatorConnection){
//                    InitiatorProtocol.check(incomingMessage);
//                }
//                else{
                    PeerProtocol.check(incomingMessage);
//                }
            }
        } catch (IOException ex) {

//            }finally{
//                mcSocket.close();
//            }
        }
    }

}
