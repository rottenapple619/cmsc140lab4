/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
class FileReference {

    private final int ID;
    private final String fileName;
    private final PeerReference publisher;
    private final PeerReference initiator;
    
    FileReference(int fileID, String fileName, PeerReference peerReference, PeerReference initiator) {
        this.ID = fileID;
        this.fileName = fileName;
        this.publisher = peerReference;
        this.initiator = initiator;
    }
    
    public int getID(){
        return this.ID;
    }
    
    public String getFileName(){
        return this.fileName;
    }
    
    public int getPublisherID(){
        return this.publisher.getID();
    }
    
    public int getPublisherPort(){
        return this.publisher.getPort();
    }
    
    public String getPublisherAddress(){
        return this.publisher.getAddress();
    }
    
    public PeerReference getPublisher(){
        return this.publisher;
    }
    
    public PeerReference getIniator(){
        return this.initiator;
    }
}
