
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private static ArrayList<Initiator> initiatorList;
    private static ArrayList<PeerConnection> peerConnectionList;
    private static HashMap<Integer,FileObj> filesLocal;
    private static HashMap<Integer,Integer> cacheOfNetworkFiles;
    
    Connections(){
        ID = getRandomID();
        initiatorList = new ArrayList<>();
        peerConnectionList = new ArrayList<>();
        filesLocal = new HashMap<>();
        cacheOfNetworkFiles = new HashMap<>();
        
        try {
            addressID = InetAddress.getLocalHost().getHostAddress(); //this should be the one being used as ID
        } catch (UnknownHostException ex) {
            Logger.getLogger(Connections.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Your ID: "+ID);
    }

    static Connections getConnection(){
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
            peerConnectionList.add(pConnect);
            isServer = true;
        } catch (SocketException ex) {
            Logger.getLogger(Connections.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    void initializePeerConnection(int id, int port){
        try {
            pConnect = new PeerConnection(id,port);
            pConnect.startConnection(false);
            peerConnectionList.add(pConnect);
        } catch (SocketException ex) {
            Logger.getLogger(Connections.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static int getRandomID() {
        //return (int)(Math.random()*Integer.MAX_VALUE);
        return (int)(Math.random()*1000)+1;
    }

    

    
    ArrayList<Initiator> getInitiatorList() {
        return initiatorList;
    }

    boolean contains(int id) {
        Iterator<Initiator> it = initiatorList.iterator();
        while(it.hasNext()){
            Initiator i = it.next();
            if(i.getID()==id)
                return true;
        }
        return false;
    }
    
    int getID(){
        return this.ID;
    }
    /*String getID() {
        return this.addressID;
    }*/
    
    boolean isServer() {
       return this.isServer;
    }

    MulticastConnection getMulticastConnection(){
        return mcConnect;
    }
    
    PeerConnection getPeerConnection(int initiatorID/*String initiatorID*/){
        for(PeerConnection peer:peerConnectionList){
            if(peer.getInitiatorID() == initiatorID){
                return peer;
            }
        }
        return null;
    }
    
    ArrayList<PeerConnection> getPeerConnection(){
        return peerConnectionList;
    }

    HashMap getLocalFiles(){
        return filesLocal;
    }
    
    HashMap getCachedNetworkFiles(){
        return cacheOfNetworkFiles;
    }
    
}
