
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
public class Connections {

    private int ID;
    private String addressID;
    private boolean isServer;
    
    private PeerConnection pConnect;
    private static Connections connect = null;
    private static MulticastConnection mcConnect = null;
    private static Map<Integer, PeerReference> initiatorList;
    private static Map<Integer, PeerConnection> peerConnectionList;
    private static Map<Integer,FileObj> filesLocal;
    private static Map<Integer,Integer> cacheOfNetworkFiles;
    
    Connections(){
        ID = getRandomID();
        initiatorList = new ConcurrentHashMap<>();
        peerConnectionList = new ConcurrentHashMap<>();
        filesLocal = new ConcurrentHashMap<>();
        cacheOfNetworkFiles = new ConcurrentHashMap<>();
        
        try {
            addressID = InetAddress.getLocalHost().getHostAddress(); //this should be the one being used as ID
        } catch (UnknownHostException ex) {
            Logger.getLogger(Connections.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Your ID: "+ID);
    }

    static Connections getInstance(){
        if(connect == null){
            connect = new Connections();
        }
        return connect;
    }
    
    void initiateMulticastConnection(){
        try {
            mcConnect = new MulticastConnection();
            mcConnect.startConnection();
        } catch (IOException ex) {
            Logger.getLogger(Connections.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void initializePeerConnection(){//initiator
        try {
            pConnect = new PeerConnection();
            pConnect.startConnection(true);
            peerConnectionList.put(pConnect.getReference().getInitiatorID(),pConnect);
            isServer = true;
        } catch (SocketException ex) {
            Logger.getLogger(Connections.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    void initializePeerConnection(PeerReference initRef){
        try {
            pConnect = new PeerConnection(initRef);
            pConnect.startConnection(false);
            peerConnectionList.put(pConnect.getReference().getInitiatorID(),pConnect);
        } catch (SocketException ex) {
            Logger.getLogger(Connections.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static int getRandomID() {
        //return (int)(Math.random()*Integer.MAX_VALUE);
        return (int)(Math.random()*Math.pow(2, FingerTable.KEY));
    }

    
    Map<Integer, PeerReference> getInitiatorList() {
        return initiatorList;
    }
    
    int getID(){
        return this.ID;
    }
    
    String getAddress() {
        return this.addressID;
    }
    
    boolean isServer() {
       return this.isServer;
    }

    MulticastConnection getMulticastConnection(){
        return mcConnect;
    }
    
    Map<Integer, PeerConnection> getPeerConnection(){
        return peerConnectionList;
    }

    Map<Integer, FileObj> getLocalFiles(){
        return filesLocal;
    }
    
    Map<Integer, Integer> getCachedNetworkFiles(){
        return cacheOfNetworkFiles;
    }
    
}
