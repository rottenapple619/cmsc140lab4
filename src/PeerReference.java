/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public class PeerReference extends NodeReference{
    
    public final static int PEER_ID = INIT_ID;
    public final static int PEER_PORT = INIT_PORT;
    public final static int PEER_ADD = INIT_ADD;
    
    public PeerReference(String ID, String port, String address){
        super(ID,port,address);
    }
    
    public int getID(){
        return Integer.parseInt(this.getReference(PEER_ID));
    }
    
    public int getPort(){
        return Integer.parseInt(this.getReference(PEER_PORT));
    }
    
    public String getAddress(){
        return this.getReference(PEER_ADD);
    }
    
    public void updateID(String ID){
        this.updateReference(PEER_ID, ID);
    }
    
    public void updatePort(String port){
        this.updateReference(PEER_PORT, port);
    }
    
    public void updateAddress(String address){
        this.updateReference(PEER_ADD, address);
    }
    
}
