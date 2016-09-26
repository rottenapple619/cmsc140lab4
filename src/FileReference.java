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
    private final int publisherID;
    private final int publisherPort;
    private final String fileName;
    
    FileReference(int ID, String fileName, int pubID, int pubPort){
        this.ID = ID;
        this.fileName = fileName;
        this.publisherID = pubID;
        this.publisherPort = pubPort;
    }
    
    public int getID(){
        return this.ID;
    }
    
    public String getFileName(){
        return this.fileName;
    }
    
    public int getPublisherID(){
        return this.publisherID;
    }
    
    public int getPublisherPort(){
        return this.publisherPort;
    }
}
