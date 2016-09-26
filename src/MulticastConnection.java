
import java.io.IOException;
import java.net.InetAddress;
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
public class MulticastConnection {

    public static final String MULTICAST_ADDRESS = "230.0.0.1";
    public static final int MULTICAST_PORT = 4444;
    
    private final InetAddress address;
    private final MulticastSocket mcSocket;
    
    //private boolean isRunning = false;
    
    private final MulticastIncomingListener incoming;
    private final MulticastOutgoingListener outgoing;
    
    MulticastConnection() throws IOException{
        mcSocket = new MulticastSocket(MULTICAST_PORT);
        address = InetAddress.getByName(MULTICAST_ADDRESS);
        mcSocket.joinGroup(address);
        
        incoming = new MulticastIncomingListener(mcSocket);
        outgoing = new MulticastOutgoingListener(mcSocket);
        
        //isRunning = true;
    }
    
    void startConnection() {
        System.out.println("Connected to the MULTICAST ADDRESS: "+MULTICAST_ADDRESS);
        incoming.start();
        outgoing.start();
        System.out.println("Listening for new connections at PORT: "+MULTICAST_PORT);
        System.out.println("Type 'COMMAND' for a list of available commands.");
    }

    MulticastOutgoingListener getOutgoing() {
        return outgoing;
    }
    
}
