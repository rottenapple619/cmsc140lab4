/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public class FingerTable {
    
    public static final int KEY = 5;
    
    private final PeerReference entries[];
    
    FingerTable(){
        this.entries = new PeerReference[KEY];
    }
    
    PeerReference getEntry(int index){
        return this.entries[index];
    }
    
    void update(int basePeerID, PeerReference peerRef){
        
        int j;
        for(j=0;j<KEY;j++){
            int compareID = (int) ((basePeerID+Math.pow(2, j))%Math.pow(2, KEY));
            PeerReference peerStored = this.entries[j];
            if(compareID == peerRef.getID()){
                this.entries[j]=peerRef;
                continue;
            }
            if(peerStored==null){
                if((compareID > basePeerID && compareID <= peerRef.getID()) //amazing Lyle
                    || (basePeerID > peerRef.getID() && (compareID > basePeerID || compareID < peerRef.getID())))
                        this.entries[j]=peerRef;
            }
            else{
                
//                int joinID_diff = Math.abs(compareID-peerRef.getID());
//                int storedID_diff = Math.abs(compareID-peerStored.getID());
//                if(joinID_diff == 0 || joinID_diff<storedID_diff)
//                    this.entries[j]=peerRef;
                if((peerRef.getID() > compareID && peerRef.getID() <= peerStored.getID()) //amazing Lyle
                    || (compareID > peerStored.getID() && (peerRef.getID() > compareID || peerRef.getID() < peerStored.getID())))
                    this.entries[j]=peerRef;
            }
        }
    }
    
    void printFingerTable(){
        System.out.println("Finger Table Update:");
        for(int j=0; j<KEY;j++){
            if(!(this.entries[j]==null)){
                System.out.println("Entry "+j+": "+this.entries[j].getID());
            }
            else{
                System.out.println("Entry "+j+": null");
            }
        }
    }
    
    PeerReference getNearestPeer(PeerReference basePeer, int requestID){
        PeerReference nearestPeer = basePeer;
        PeerReference storedPeer = null;
        for(int j=0;j<KEY;j++){
            storedPeer = this.entries[j];
           
            if(storedPeer==null)
                break;

            if((requestID > nearestPeer.getID() && requestID <= storedPeer.getID()) //amazing Lyle
                || (nearestPeer.getID() > storedPeer.getID() && (requestID > nearestPeer.getID() || requestID < storedPeer.getID()))){
                break;
            }
            
            nearestPeer = storedPeer;
        }
        return nearestPeer;
    }
}
