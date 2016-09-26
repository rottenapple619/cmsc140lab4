/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public class Initiator {
    private final int initiatorID;
    private final int initiatorPort;
//    private final int initiatorObjPort;
//    private final String initiatorAddress;
    
    
    public Initiator(int id, int port){
        this.initiatorID = id;
        this.initiatorPort = port;
    }
    
    public int getID(){
        return this.initiatorID;
    }
    
    public int getPort(){
        return this.initiatorPort;
    }
}
