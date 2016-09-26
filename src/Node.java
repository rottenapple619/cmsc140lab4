
import java.util.HashMap;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
class Node{
    public boolean requestForFiles = false;
    
    private final HashMap<Integer, FileObj> filesNetwork;
    private final HashMap<Integer, FileReference> referencedFiles;
    
    private ObjSender objSender;
    private ObjReceiver ojbReceiver;
        
    private final NodeReference reference;
    
    
//    private int successorID;
//    private int successorPort;
//    
//    private int predecessorID;
//    private int predecessorPort;
//
//    private int initiatorID;
//    private int initiatorPort;
    
    private final int port;
    
    private final boolean isServer;
    
    Node(boolean isServer){

        this.referencedFiles = new HashMap<>();
        this.filesNetwork = new HashMap<>();
        
        this.isServer = isServer;
        this.port = AvailablePort.getAvailablePort();
        
        this.reference = new NodeReference();
        
        this.reference.updatePredID(0+"");
        this.reference.updatePredPort(0+"");
        
        if(isServer){
            this.reference.updateSucID(getID()+"");
            this.reference.updateSucPort(port+"");
            
        }
    }
    
    NodeReference getReference(){
        return this.reference;
    }
    
    HashMap getReferencedFiles(){
        return this.referencedFiles;
    }
    
    HashMap getFilesInNetwork(){
        return this.filesNetwork;
    }
    
    int getPort(){
        return port;
    }
    
    final int getID(){
        /*if(isServer) //remove this
            return 9999;
        else*/
            return Connections.getConnection().getID();
    }
    
//    int getPredecessorID(){
//        return predecessorID;
//    }
//    
//    int getPredecessorPort(){
//        return predecessorPort;
//    }
//    
//    int getSuccessorID(){
//        return successorID;
//    }
//    
//    int getSuccessorPort(){
//        return successorPort;
//    }
//
//    int getInitiatorID(){
//        return initiatorID;
//    }
//    
//    int getInitiatorPort(){
//        return initiatorPort;
//    }
    
    boolean isServer(){
        return isServer;
    }
//    void setPID(int id){
//        predecessorID = id;
//    }
//    
//    void setSID(int id){
//        successorID = id;
//    }
//    
//    void setPredecessorPort(int port){
//        predecessorPort = port;
//    }
//    
//    void setSuccessorPort(int port){
//        successorPort = port;
//    }
//    
//    void setInitiatorID(int id){
//        initiatorID = id;
//    }
//    
//    void setInitiatorPort(int port){
//        initiatorPort = port;
//    }
    
    void addToReferencedFiles(FileReference fileReference) {
        this.referencedFiles.put(fileReference.getID(), fileReference);
        System.out.println();
        System.out.print("A new file has been REGISTERED to you for the P2P Network: " +this.reference.getInitiatorID() +"@"+ this.reference.getInitiatorPort() +"\n"+
            "FileID: "+fileReference.getID()+ "\n"+
            "FileName: "+fileReference.getFileName()+ "\n"+
            "Published by: "+fileReference.getPublisherID()+"@"+fileReference.getPublisherPort());
        if(fileReference.getPublisherID()==this.getID())
            System.out.print("(You)\n");
        else
            System.out.println();

    }
    
    FileReference deleteReference(int fileID){
        FileReference r = null;
        if(this.referencedFiles.containsKey(fileID)){
            r = this.referencedFiles.get(fileID);
            this.referencedFiles.remove(fileID);
            System.out.println();
            System.out.print("A file has been UNREGISTERED to you for the P2P Network: " +this.reference.getInitiatorID() +"@"+ this.reference.getInitiatorPort() +"\n"+
                "FileID: "+r.getID()+ "\n"+
                "FileName: "+r.getFileName()+ "\n"+
                "Published by: "+r.getPublisherID()+"@"+r.getPublisherPort());
            if(r.getPublisherID()==this.getID())
                System.out.print("(You)\n");
            else
                System.out.println();
        }
        return r;
    }
    
    FileObj deleteNetworkFile(int fileID){
        FileObj r = null;
        if(this.filesNetwork.containsKey(fileID)){
            r = this.filesNetwork.get(fileID);
            this.filesNetwork.remove(fileID);
            System.out.println();
            System.out.print("A file has been DELETED to you for the P2P Network: " +this.reference.getInitiatorID() +"@"+ this.reference.getInitiatorPort() +"\n"+
                "FileID: "+r.getID()+ "\n"+
                "FileName: "+r.getFileName()+ "\n"+
                "Published by: "+this.getID()+"@"+this.getPort());
            System.out.print("(You)\n");
            
        }
        return r;
    }
}
