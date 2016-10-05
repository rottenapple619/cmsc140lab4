
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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
    
    private final Map<Integer, FileObj> filesToPublish;
    private final Map<Integer, FileReference> referencedFiles;
    private final Map<Integer, FileObjNotifier> publishedFiles;
    
    private ObjSender objSender;
    private ObjReceiver ojbReceiver;
        
    private final NodeReference reference;
    private final FingerTable fingerTable;
    
    private final int port;
    private final boolean isServer;
    
    Node(boolean isServer){

        this.referencedFiles = new ConcurrentHashMap<>();
        this.filesToPublish = new ConcurrentHashMap<>();
        this.publishedFiles = new ConcurrentHashMap<>();
        
        this.isServer = isServer;
        this.port = AvailablePort.getAvailablePort();
        
        this.reference = new NodeReference();
        this.fingerTable = new FingerTable();
        
        this.reference.updatePredID(0+"");
        this.reference.updatePredPort(0+"");
        
        if(isServer){
            this.reference.updateSucID(getID()+"");
            this.reference.updateSucPort(port+"");
            this.reference.updateSucAddress(getAddress());
            
        }
    }
    
    FingerTable getFingerTable(){
        return this.fingerTable;
    }
    
    NodeReference getReference(){
        return this.reference;
    }
    
    Map<Integer, FileReference> getReferencedFiles(){
        return this.referencedFiles;
    }
    
    Map<Integer, FileObj> getFilesToPublish(){
        return this.filesToPublish;
    }
    
    int getPort(){
        return port;
    }
    
    final int getID(){
        /*if(isServer) //remove this
            return 9999;
        else*/
            return Connections.getInstance().getID();
    }
    final String getAddress() {
        return Connections.getInstance().getAddress();
    }

    
    boolean isServer(){
        return isServer;
    }
    
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
        FileReference fr;
        if((fr=this.referencedFiles.remove(fileID))!=null){
            System.out.println();
            System.out.print("A file has been UNREGISTERED to you for the P2P Network: " +this.reference.getInitiatorID() +"@"+ this.reference.getInitiatorPort() +"\n"+
                "FileID: "+fr.getID()+ "\n"+
                "FileName: "+fr.getFileName()+ "\n"+
                "Published by: "+fr.getPublisherID()+"@"+fr.getPublisherPort());
            if(fr.getPublisherID()==this.getID())
                System.out.print("(You)\n");
            else
                System.out.println();
        }
        return fr;
    }
    
    void publishFile(int fileID){
        FileObj fileToPublish = this.filesToPublish.remove(fileID);
        FileObjNotifier fileNotifier = new FileObjNotifier(this,fileToPublish);
        fileNotifier.start();
        this.publishedFiles.put(fileID, fileNotifier);
    }
    
    FileObjNotifier deleteNetworkFile(int fileID){
        FileObjNotifier fon;
        if((fon = this.publishedFiles.remove(fileID))!=null){
            fon.stopThread();
            System.out.println();
            System.out.print("A file has been DELETED to you for the P2P Network: " +this.reference.getInitiatorID() +"@"+ this.reference.getInitiatorPort() +"\n"+
                "FileID: "+fon.getFileObj().getID()+ "\n"+
                "FileName: "+fon.getFileObj().getFileName()+ "\n"+
                "Published by: "+this.getID()+"@"+this.getPort());
            System.out.print("(You)\n");
            
        }
        return fon;
    }

    
}
