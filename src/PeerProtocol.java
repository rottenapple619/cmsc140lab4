
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map.Entry;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public class PeerProtocol implements Messages{

    static void check(String incomingMessage) throws UnknownHostException {
        String msg[];
        msg = incomingMessage.split(REGEX);
        
        
        
        /*
            Received a TELLSUCCESSOR MESSAGE
            RECEIVER is the SUCCESSOR of the SENDER of this MESSAGE
            RECEIVER then sends a TELLPREDECESSOR MESSAGE to the SENDER
        */
        if(msg[0].equalsIgnoreCase(TELLSUCCESSOR)){
            
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int predecessorID = Integer.parseInt(msg[3]);
            int predecessorPORT = Integer.parseInt(msg[4]);
            
            int successorID = Integer.parseInt(msg[5]);
            int successorPORT = Integer.parseInt(msg[6]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            peer.setPID(predecessorID);
            peer.setPredecessorPort(predecessorPORT);
            peer.setSID(successorID);
            peer.setSuccessorPort(successorPORT);
            
            peer.getOutgoing().send(TELLPREDECESSOR
                    +REGEX+peer.getInitiatorID()        //network ID
                    +REGEX+peer.getInitiatorPort()
                    +REGEX+peer.getID()                 //receiver's predecessor
                    +REGEX+peer.getPort(),
                    InetAddress.getLocalHost(), successorPORT);
            printStatus(peer);
        }
        
        /*
            Received a TELLPREDECESSOR MESSAGE
            RECEIVER is the PREDECESSOR of the SENDER of this MESSAGE
        */
        else if(msg[0].equalsIgnoreCase(TELLPREDECESSOR)){
            
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int predecessorID = Integer.parseInt(msg[3]);
            int predecessorPORT = Integer.parseInt(msg[4]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            peer.setPID(predecessorID);
            peer.setPredecessorPort(predecessorPORT);
            
            printStatus(peer);
        }
        else if(msg[0].equalsIgnoreCase(TRANS_REG)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int publisherID  = Integer.parseInt(msg[3]);
            int publisherPORT = Integer.parseInt(msg[4]);
            
            int fileID = Integer.parseInt(msg[5]);
            String fileName = msg[6];
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            peer.addToReferencedFiles(new FileReference(fileID,fileName,publisherID,publisherPORT));
        }
        /*
            Received a FINDSUCCESSOR MESSAGE
        */
        else if(msg[0].equalsIgnoreCase(FINDSUCCESSOR)){    
            
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int joinID  = Integer.parseInt(msg[3]);
            int joinPORT = Integer.parseInt(msg[4]);
            
            
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);

            if(peer.getPredecessorID() == 0){ //P2P Network in initial state
                peer.setPID(joinID);
                peer.setPredecessorPort(joinPORT);
                peer.setSID(joinID);
                peer.setSuccessorPort(joinPORT);
                peer.getOutgoing().send(TELLSUCCESSOR 
                    +REGEX+netID                //net ID
                    +REGEX+netPORT
                    +REGEX+peer.getID()         //receiver's predecessor
                    +REGEX+peer.getPort()
                    +REGEX+peer.getID()         //receiver's successor
                    +REGEX+peer.getPort(),
                    InetAddress.getLocalHost(), joinPORT);
                
                //TRANSFER CUSTODY OF REGISTERED FILES//
                Iterator entries = peer.getReferencedFiles().entrySet().iterator();
                while (entries.hasNext()) {//iterate through the files that are registered to you
                    Entry thisEntry = (Entry) entries.next();
                    int key = (int) thisEntry.getKey();
                    FileReference file = (FileReference) thisEntry.getValue();

                    if((joinID>peer.getID()&&((file.getID()<peer.getID())||(file.getID()>joinID)))
                            ||((joinID<peer.getID())&&(file.getID()>joinID))){//check if there is a registered file that will change custody
                        
                        peer.getOutgoing().send(TRANS_REG
                            +REGEX+netID                         //network id
                            +REGEX+netPORT
                            +REGEX+file.getPublisherID()        //file publisher
                            +REGEX+file.getPublisherPort()
                            +REGEX+file.getID()                 //file metadata
                            +REGEX+file.getFileName(),
                        InetAddress.getLocalHost(), joinPORT);
                        
                        entries.remove();
                    }
                }//end while
                
            }
            else{//P2P Network in populated state
                if((joinID > peer.getID() && joinID <= peer.getSuccessorID()) //amazing Lyle
                    || (peer.getID() > peer.getSuccessorID() && (joinID > peer.getID() || joinID < peer.getSuccessorID()))){
                    peer.getOutgoing().send(TELLSUCCESSOR 
                        +REGEX+netID                         //network id
                        +REGEX+netPORT
                        +REGEX+peer.getID()                 //receiver's predecessor
                        +REGEX+peer.getPort()
                        +REGEX+peer.getSuccessorID()        //receiver's successor
                        +REGEX+peer.getSuccessorPort(),
                        InetAddress.getLocalHost(), joinPORT);
                    peer.setSID(joinID);
                    peer.setSuccessorPort(joinPORT);
                    printStatus(peer);
                    
                    //TRANSFER CUSTODY OF REGISTERED FILES//
                    Iterator entries = peer.getReferencedFiles().entrySet().iterator();
                    while (entries.hasNext()) {//iterate through the files that are registered to you
                        Entry thisEntry = (Entry) entries.next();
                        int key = (int) thisEntry.getKey();
                        FileReference file = (FileReference) thisEntry.getValue();

                        if((joinID>peer.getID()&&((file.getID()<peer.getID())||(file.getID()>joinID)))
                                ||((joinID<peer.getID())&&(file.getID()>joinID))){//check if there is a registered file that will change custody

                            peer.getOutgoing().send(TRANS_REG
                                +REGEX+netID                         //network id
                                +REGEX+netPORT
                                +REGEX+file.getPublisherID()        //file publisher
                                +REGEX+file.getPublisherPort()
                                +REGEX+file.getID()                 //file metadata
                                +REGEX+file.getFileName(),
                            InetAddress.getLocalHost(), joinPORT);

                            entries.remove();
                        }
                    }//end while
                }
                else{
                    peer.getOutgoing().send(FINDSUCCESSOR   //find successor message sent to successor
                        +REGEX+netID
                        +REGEX+netPORT
                        +REGEX+joinID                   
                        +REGEX+joinPORT,
                        InetAddress.getLocalHost(), peer.getSuccessorPort());

                } 
            }

            
        }
        /*
            Received PUBLISH Message
        */
        else if(msg[0].equalsIgnoreCase(PUBLISH)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int publisherID  = Integer.parseInt(msg[3]);
            int publisherPORT = Integer.parseInt(msg[4]);
            
            int fileID = Integer.parseInt(msg[5]);
            String fileName = msg[6];
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            if(peer.getPredecessorID() == 0){ //P2P Network in initial state
                peer.addToReferencedFiles(new FileReference(fileID,fileName,publisherID,publisherPORT));
                
                Connections.getConnection().getMulticastConnection().getOutgoing().send(Messages.PUBLISH//broadcast to multicast that a file has been
                    +Messages.REGEX+peer.getID()                                                        //published
                    +Messages.REGEX+peer.getPort()
                    +Messages.REGEX+peer.getInitiatorID()
                    +Messages.REGEX+peer.getInitiatorPort()
                    +Messages.REGEX+fileName
                    +Messages.REGEX+fileID);

            }
            else{//P2P Network in populated state
                if((fileID > peer.getID() && fileID <= peer.getSuccessorID()) //amazing Lyle
                    || (peer.getID() > peer.getSuccessorID() && (fileID > peer.getID() || fileID < peer.getSuccessorID()))){
                    
                    peer.addToReferencedFiles(new FileReference(fileID,fileName,publisherID,publisherPORT));
                    
                    if(peer.getID()!=publisherID){
                        peer.getOutgoing().send(TELLPUBLISHER 
                            +REGEX+netID                //network id
                            +REGEX+netPORT
                            +REGEX+peer.getID()         //successor ID (the one to keep the file)
                            +REGEX+peer.getPort()
                            +REGEX+fileID              //file ID
                            +REGEX+fileName,           //file Name
                            InetAddress.getLocalHost(), publisherPORT); //send to the publisher of the file
                    }
                    else{
                        Connections.getConnection().getMulticastConnection().getOutgoing().send(Messages.PUBLISH//broadcast to multicast that a file has been
                            +Messages.REGEX+peer.getID()                                                        //published
                            +Messages.REGEX+peer.getPort()
                            +Messages.REGEX+peer.getInitiatorID()
                            +Messages.REGEX+peer.getInitiatorPort()
                            +Messages.REGEX+fileName
                            +Messages.REGEX+fileID);
                    }
                }
                else{
                    peer.getOutgoing().send(PUBLISH
                        +REGEX+netID                //network ID
                        +REGEX+netPORT
                        +REGEX+publisherID          //publisher ID
                        +REGEX+publisherPORT
                        +REGEX+fileID               //file ID
                        +REGEX+fileName,            //file Name
                        InetAddress.getLocalHost(), peer.getSuccessorPort()); //send to successor

                } 
            }
        }
        else if(msg[0].equalsIgnoreCase(TELLPUBLISHER)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int referencedID  = Integer.parseInt(msg[3]);
            int referencedPORT = Integer.parseInt(msg[4]);
            
            int fileID = Integer.parseInt(msg[5]);
            String fileName = msg[6];
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            System.out.println();
            System.out.println("A new file has been successfully registered to  "+referencedID+"@"+referencedPORT+"\n"+
                "For the P2P Network: " +netID +"@"+ netPORT +"\n"+
                "FileID: "+fileID+ "\n"+
                "FileName: "+fileName+ "\n"+
                "Published by: "+peer.getID()+"@"+peer.getPort());
            System.out.println();
            
            Connections.getConnection().getMulticastConnection().getOutgoing().send(Messages.PUBLISH//broadcast to multicast that a file has been
                +Messages.REGEX+referencedID                                                          //published
                +Messages.REGEX+referencedPORT
                +Messages.REGEX+peer.getInitiatorID()
                +Messages.REGEX+peer.getInitiatorPort()
                +Messages.REGEX+fileName
                +Messages.REGEX+fileID);
            
            
        }

        else if(msg[0].equalsIgnoreCase(DELETE)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int senderID  = Integer.parseInt(msg[3]);//the one who requested to delete the file
            int senderPORT = Integer.parseInt(msg[4]);
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            FileReference fr;
            
            if(peer.getPredecessorID() == 0){ //P2P Network in initial state
               
                if((fr = peer.deleteReference(fileID)) != null){
                    peer.deleteNetworkFile(fr.getID());
                    System.out.println();
                    System.out.println("A file has been successfully DELETED from "+peer.getID()+"@"+peer.getPort()+"\n"+
                        "For the P2P Network: " +netID +"@"+ netPORT +"\n"+
                        "FileID: "+fr.getID()+ "\n"+
                        "FileName: "+fr.getFileName()+ "\n"+
                        "Deleted by: "+peer.getID()+"@"+peer.getPort()+"(You)");
                    System.out.println();
                }
                else{
                    System.out.println();
                    System.out.println("Failed to DELETE. File not found!");
                    System.out.println();
                }
             

            }
            else{//P2P Network in populated state
                if((fileID > peer.getID() && fileID <= peer.getSuccessorID()) //amazing Lyle
                    || (peer.getID() > peer.getSuccessorID() && (fileID > peer.getID() || fileID < peer.getSuccessorID()))){
                    
                    if((fr = peer.deleteReference(fileID)) != null){//successful delete
                        
                        if(fr.getPublisherID()!=peer.getID()){
                            peer.getOutgoing().send(DELETEFILEPUBLISHER
                                +REGEX+netID
                                +REGEX+netPORT
                                +REGEX+senderID
                                +REGEX+senderPORT
                                +REGEX+fr.getFileName()
                                +REGEX+fr.getID(),
                                InetAddress.getLocalHost(), fr.getPublisherPort());
                        }
                        else{
                            peer.deleteNetworkFile(fr.getID());
                            System.out.println();
                            System.out.println("A file has been successfully DELETED from "+peer.getID()+"@"+peer.getPort()+"\n"+
                                "For the P2P Network: " +netID +"@"+ netPORT +"\n"+
                                "FileID: "+fr.getID()+ "\n"+
                                "FileName: "+fr.getFileName()+ "\n"+
                                "Deleted by: "+senderID+"@"+senderPORT);
                            System.out.println();
                            if(peer.getID()!=senderID)//send delete confirmation to the one who requested to delete the file
                                peer.getOutgoing().send(SUCCESSDELETE
                                    +REGEX+netID
                                    +REGEX+netPORT
                                    +REGEX+peer.getID()
                                    +REGEX+peer.getPort()
                                    +REGEX+fr.getFileName()
                                    +REGEX+fr.getID(),
                                    InetAddress.getLocalHost(), senderPORT);
                        }
                        
                    }
                    else{//file not found
                        peer.getOutgoing().send(FAILDELETE
                            +REGEX+netID
                            +REGEX+netPORT
                            +REGEX+peer.getID()
                            +REGEX+peer.getPort()
                            +REGEX+fileID,
                            InetAddress.getLocalHost(), senderPORT);
                    }
                }
                else{
                    peer.getOutgoing().send(Messages.DELETE
                        +REGEX+netID
                        +REGEX+netPORT
                        +REGEX+senderID
                        +REGEX+senderPORT
                        +REGEX+fileID,
                        InetAddress.getLocalHost(), peer.getSuccessorPort());
                }
            }
        }
        else if(msg[0].equalsIgnoreCase(DELETEFILEPUBLISHER)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int requestID  = Integer.parseInt(msg[3]); 
            int requestPORT = Integer.parseInt(msg[4]);
            
            String fileName = msg[5];
            int fileID = Integer.parseInt(msg[6]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            peer.deleteNetworkFile(fileID);
            
            peer.getOutgoing().send(SUCCESSDELETE
                                +REGEX+netID
                                +REGEX+netPORT
                                +REGEX+peer.getID()
                                +REGEX+peer.getPort()
                                +REGEX+fileName
                                +REGEX+fileID,
                                InetAddress.getLocalHost(), requestPORT);
        }
        else if(msg[0].equalsIgnoreCase(SUCCESSDELETE)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int publisherID  = Integer.parseInt(msg[3]);
            int publisherPORT = Integer.parseInt(msg[4]);
            
            String fileName = msg[5];
            int fileID = Integer.parseInt(msg[6]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            System.out.println();
            System.out.println("FILE SUCCESSFULLY DELETED IN THE P2P NETWORK: "+netID+"@"+netPORT
                    +"\nFileID: "+fileID
                    +"\nFilename: '"+fileName+"'"
                    +"\nPublished by: "+publisherID+"@"+publisherPORT);
            System.out.println();
        }
        else if(msg[0].equalsIgnoreCase(FAILDELETE)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int senderID  = Integer.parseInt(msg[3]);
            int senderPORT = Integer.parseInt(msg[4]);
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            System.out.println();
            System.out.println("FAILED TO DELETE A FILE IN THE P2P NETWORK: "+netID+"@"+netPORT
                    +"\nFileID: "+fileID
                    +"\nMessage from: "+senderID+"@"+senderPORT
                    +"\nError! File Not Found!");
            System.out.println();
        }
        
        else if(msg[0].equalsIgnoreCase(FILESNETWORK)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int senderID  = Integer.parseInt(msg[3]);
            int senderPORT = Integer.parseInt(msg[4]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            if(peer.getPredecessorID() == 0){ //P2P Network in initial state
                
                Iterator entries = peer.getReferencedFiles().entrySet().iterator();
                while (entries.hasNext()) {//iterate through the files that are registered to you
                    Entry thisEntry = (Entry) entries.next();
                    int key = (int) thisEntry.getKey();
                    FileReference file = (FileReference) thisEntry.getValue();

                    System.out.println("FileID: "+file.getID()
                        +" Filename: "+file.getFileName()
                        +" Registered to: "+peer.getID()+"@"+peer.getPort()+"(You)");
                }//end while

            }
            else{//P2P Network in populated state

                if(senderID==peer.getID()){
                    if(!peer.requestForFiles){
                        peer.requestForFiles = true;
                        Iterator entries = peer.getReferencedFiles().entrySet().iterator();
                        while (entries.hasNext()) {
                            Entry thisEntry = (Entry) entries.next();
                            int key = (int) thisEntry.getKey();
                            FileReference file = (FileReference) thisEntry.getValue();

                            System.out.println("FileID: "+file.getID()
                                +" Filename: "+file.getFileName()
                                +" Registered to: "+peer.getID()+"@"+peer.getPort()+"(You)");
                        }//end while

                        peer.getOutgoing().send(FILESNETWORK //send FILESNETWORK Message
                            +REGEX+netID
                            +REGEX+netPORT
                            +REGEX+senderID
                            +REGEX+senderPORT, 
                            InetAddress.getLocalHost(), peer.getSuccessorPort());//sent to your SUCCESSOR

                    }
                    else{
                        peer.requestForFiles = false;
                    }
                }
                else{
                    Iterator entries = peer.getReferencedFiles().entrySet().iterator();
                    while (entries.hasNext()) {//iterate through the files that are registered to you
                        Entry thisEntry = (Entry) entries.next();
                        int key = (int) thisEntry.getKey();
                        FileReference file = (FileReference) thisEntry.getValue();
                        peer.getOutgoing().send(FILE    //send FILE Message
                            +REGEX+netID                //netID
                            +REGEX+netPORT
                            +REGEX+peer.getID()         //your ID
                            +REGEX+peer.getPort()
                            +REGEX+file.getID()         //file ID
                            +REGEX+file.getFileName(), 
                            InetAddress.getLocalHost(), senderPORT);//sent to the one who requested the list of files in the P2P Network
                    }//end while
                    
                    
                    peer.getOutgoing().send(FILESNETWORK //send FILESNETWORK Message
                        +REGEX+netID
                        +REGEX+netPORT
                        +REGEX+senderID
                        +REGEX+senderPORT, 
                        InetAddress.getLocalHost(), peer.getSuccessorPort());//sent to your SUCCESSOR
                }

            }

            
        }
        else if(msg[0].equalsIgnoreCase(FILE)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int senderID  = Integer.parseInt(msg[3]);
            int senderPORT = Integer.parseInt(msg[4]);
            
            int fileID = Integer.parseInt(msg[5]);
            String fileName = msg[6];
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            
            if(!Connections.getConnection().getCachedNetworkFiles().containsKey(fileID)){
                System.out.println("FileID: "+fileID
                                +" Filename: "+fileName
                                +" Registered to: "+senderID+"@"+senderPORT);
                Connections.getConnection().getCachedNetworkFiles().put(fileID, senderID);
            }
        }
        else if(msg[0].equalsIgnoreCase(Messages.RETRIEVE)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int requestID  = Integer.parseInt(msg[3]); //address or ID of the one who retrieves the file
            int requestPORT = Integer.parseInt(msg[4]);
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            FileReference f;
            
            if(peer.getPredecessorID() == 0){ //P2P Network in initial state
                
                if((f =(FileReference) peer.getReferencedFiles().get(fileID))==null){
                        System.err.println("Error! File Not Found!");
                } 
                else {
                    System.out.println();
                    System.out.println("FILE HAS BEEN FOUND FOR THE P2P NETWORK: "+netID+"@"+netPORT
                            +"\nFileID: "+fileID
                            +"\nFilename : '"+f.getFileName()
                            +"\nRegistered to: "+peer.getID()+"@"+peer.getPort()+"(You)");
                    System.out.println();
                    FileObj file = (FileObj) peer.getFilesInNetwork().get(fileID);
                    Connections.getConnection().getLocalFiles().put(fileID, file);
                    System.out.println("'"+file.getFileName()+"' has been retrieved and saved to local files.");
                }
            }
            else{//P2P Network in populated state
                if((fileID > peer.getID() && fileID <= peer.getSuccessorID()) //amazing Lyle
                    || (peer.getID() > peer.getSuccessorID() && (fileID > peer.getID() || fileID < peer.getSuccessorID()))){
            
                    f =(FileReference) peer.getReferencedFiles().get(fileID); ///////////////////////////////////////////////////////
                    //////add here if file exists
                    
                    if(requestID!=f.getPublisherID())
                        peer.getOutgoing().send(INIT_SEND 
                            +REGEX+netID                //network id
                            +REGEX+netPORT
                            +REGEX+requestID         
                            +REGEX+requestPORT
                            +REGEX+fileID,              //file ID
                            InetAddress.getLocalHost(), f.getPublisherPort()); //send to the one who published the file
                    else{//the file that is requested is published by the one who requested the file
                        peer.getOutgoing().send(INIT_COPY 
                            +REGEX+netID                //network id
                            +REGEX+netPORT
                            +REGEX+fileID,              //file ID
                            InetAddress.getLocalHost(), requestPORT); //send to the one who published the file
                    }
                }
                else{
                    peer.getOutgoing().send(RETRIEVE
                        +REGEX+netID                //network ID
                        +REGEX+netPORT
                        +REGEX+requestID            //request ID
                        +REGEX+requestPORT
                        +REGEX+fileID,              //file ID
                        InetAddress.getLocalHost(), peer.getSuccessorPort()); //send to successor

                } 
            }
        }
        else if(msg[0].equalsIgnoreCase(INIT_SEND)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int requestID  = Integer.parseInt(msg[3]); //address or ID of the one who retrieves the file
            int requestPORT = Integer.parseInt(msg[4]);
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            peer.openObjSender(RETRIEVE, requestID, requestPORT, fileID);
        }
        else if(msg[0].equalsIgnoreCase(INIT_RECEIVE)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int senderID  = Integer.parseInt(msg[3]); //address or ID of the one who keeps the file
            int senderPORT = Integer.parseInt(msg[4]);
            
            int objPORT = Integer.parseInt(msg[5]);
            //int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            peer.openObjReceiver(RETRIEVE,senderID,senderPORT,objPORT); 
        }
        else if(msg[0].equalsIgnoreCase(INIT_COPY)){
            int netID = Integer.parseInt(msg[1]);
            int netPORT = Integer.parseInt(msg[2]);
            
            int fileID = Integer.parseInt(msg[3]);
            
            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
            System.out.println("RECEIVED A INIT_RECEIVE");
            
            FileObj file = (FileObj) peer.getFilesInNetwork().get(fileID);
            Connections.getConnection().getLocalFiles().put(fileID, file);
            System.out.println("'"+file.getFileName()+"' has been retrieved and saved to local files.");
        }
        //        else if(msg[0].equalsIgnoreCase(READYTOPUBLISH)){
//            String transferType = msg[1];
//            int netID = Integer.parseInt(msg[2]);
//            int netPORT = Integer.parseInt(msg[3]);
//            
//            int senderID  = Integer.parseInt(msg[4]);
//            int senderPORT = Integer.parseInt(msg[5]);
//            
//            int objPORT = Integer.parseInt(msg[6]);
//            //int fileID = Integer.parseInt(msg[7]);
//            
//            PeerConnection peer = Connections.getConnection().getPeerConnection(netID);
//            peer.openObjReceiver(transferType,senderID,senderPORT,objPORT); 
//        }
       
    }

    private static void printStatus(PeerConnection peer) {
        
        System.out.println();
        System.out.println("Your successor: "+      peer.getSuccessorID()+" "
                        + "Your successor port: "+  peer.getSuccessorPort()+"\n"
                        + "Your predecessor: "+     peer.getPredecessorID()+" "
                        + "Your predecessor port: "+peer.getPredecessorPort());
        
    }

    
    
}
