
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
            RECEIVER then sends a TELLPREDECESSOR MESSAGE to the FORMER SUCCESSOR OF THE SENDER
        */
        if(msg[0].equalsIgnoreCase(TELLSUCCESSOR)){
            
            int netID = Integer.parseInt(msg[1]);
            
            int predecessorID = Integer.parseInt(msg[2]);
            int predecessorPORT = Integer.parseInt(msg[3]);
            String predecessorADD = msg[4];
            
            int successorID = Integer.parseInt(msg[5]);
            int successorPORT = Integer.parseInt(msg[6]);
            String successorADD = msg[7];
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);
            peer.getReference().updatePredID(predecessorID+"");
            peer.getReference().updatePredPort(predecessorPORT+"");
            peer.getReference().updatePredAddress(predecessorADD);
            peer.getReference().updateSucID(successorID+"");
            peer.getReference().updateSucPort(successorPORT+"");
            peer.getReference().updateSucAddress(successorADD);
            
            peer.getOutgoing().send(TELLPREDECESSOR //tell the successor of the sender you are the "new" predecessor
                    +REGEX+peer.getReference().getInitiatorID()        //network ID
                    +REGEX+peer.getID()                 //receiver's predecessor
                    +REGEX+peer.getPort()
                    +REGEX+peer.getAddress(),
                    InetAddress.getByName(successorADD), successorPORT);
            printStatus(peer);
            
            peer.getFingerTable().update(peer.getID(), new PeerReference(predecessorID+"",predecessorPORT+"",predecessorADD));
            peer.getFingerTable().update(peer.getID(), new PeerReference(successorID+"",successorPORT+"",successorADD));
            peer.getFingerTable().printFingerTable();
        }//end TELLSUCCESSOR
        
        /*
            Received a TELLPREDECESSOR MESSAGE
            RECEIVER is the SUCCESSOR of the SENDER of this MESSAGE
        */
        else if(msg[0].equalsIgnoreCase(TELLPREDECESSOR)){
            
            int netID = Integer.parseInt(msg[1]);
                        
            int predecessorID = Integer.parseInt(msg[2]);
            int predecessorPORT = Integer.parseInt(msg[3]);
            String predecessorADD = msg[4];
            
            PeerConnection peer = (PeerConnection) Connections.getInstance().getPeerConnection().get(netID);
            peer.getReference().updatePredID(predecessorID+"");
            peer.getReference().updatePredPort(predecessorPORT+"");
            peer.getReference().updatePredAddress(predecessorADD);
            
            printStatus(peer);
            
            peer.getFingerTable().update(peer.getID(), new PeerReference(predecessorID+"",predecessorPORT+"",predecessorADD));
            peer.getFingerTable().printFingerTable();
        }//end TELLPREDECESSOR
        
        /*
            Received TRANS_REG Message
        */
        else if(msg[0].equalsIgnoreCase(TRANS_REG)){
            int netID = Integer.parseInt(msg[1]);
            
            
            int publisherID  = Integer.parseInt(msg[2]);
            int publisherPORT = Integer.parseInt(msg[3]);
            String publisherADD = msg[4];
            
            int fileID = Integer.parseInt(msg[5]);
            String fileName = msg[6];
            
            PeerConnection peer = (PeerConnection) Connections.getInstance().getPeerConnection().get(netID);
            
            peer.addToReferencedFiles(new FileReference(fileID,fileName,
                    new PeerReference(publisherID+"",publisherPORT+"",publisherADD),
                    Connections.getInstance().getInitiatorList().get(netID)));
        }//end TRANS_REG
        
        /*
            Received a FINDSUCCESSOR MESSAGE
        */
        else if(msg[0].equalsIgnoreCase(FINDSUCCESSOR)){    
            
            int netID = Integer.parseInt(msg[1]);
            
            int joinID = Integer.parseInt(msg[2]);
            int joinPORT  = Integer.parseInt(msg[3]);
            String joinAddress  = msg[4];

            String senderAddress = msg[5];
            int senderPORT = Integer.parseInt(msg[6]);
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);

            PeerReference routeToPeer = peer.getFingerTable().getNearestPeer
                (new PeerReference(peer.getID()+"",peer.getPort()+"",peer.getAddress()), joinID);
            System.out.println("Message routed to: "+routeToPeer.getID());
            if(routeToPeer.getID()==peer.getID()){
                peer.getOutgoing().send(TELLSUCCESSOR //tell your "new successor" about your old successor
                    +REGEX+netID                         //network id
                    +REGEX+peer.getID()                 //receiver's predecessor
                    +REGEX+peer.getPort()
                    +REGEX+peer.getAddress()
                    +REGEX+peer.getReference().getSuccessorID()        //receiver's successor
                    +REGEX+peer.getReference().getSuccessorPort()
                    +REGEX+peer.getReference().getSuccessorAddress(),
                    InetAddress.getByName(joinAddress), joinPORT);
                
                peer.getReference().updateSucID(joinID+"");//update successor
                peer.getReference().updateSucPort(joinPORT+"");
                peer.getReference().updateSucAddress(joinAddress);
                printStatus(peer);

                
                //TRANSFER CUSTODY OF REGISTERED/REFERENCED FILES
                Iterator entries = peer.getReferencedFiles().entrySet().iterator();
                while (entries.hasNext()) {//iterate through the files that are registered to you
                    Entry thisEntry = (Entry) entries.next();
                    int key = (int) thisEntry.getKey();
                    FileReference file = (FileReference) thisEntry.getValue();
                        
                    if(((peer.getID()<joinID) && ((joinID < file.getID()) || (file.getID()<peer.getID())))//check if there is a registered 
                        || ((peer.getID()>joinID) && (file.getID()<peer.getID() && file.getID()>joinID))){//file that will change custody 
                        peer.getOutgoing().send(TRANS_REG
                            +REGEX+netID                         //network id
                
                            +REGEX+file.getPublisherID()        //file publisher
                            +REGEX+file.getPublisherPort()
                            +REGEX+file.getPublisherAddress()
                            +REGEX+file.getID()                 //file metadata
                            +REGEX+file.getFileName(),
                        InetAddress.getByName(joinAddress), joinPORT);
                        entries.remove();
                    }
                }//end while
                
                
                peer.getFingerTable().update(peer.getID(), new PeerReference(joinID+"",joinPORT+"",joinAddress));//update finger table
                peer.getFingerTable().printFingerTable();
            }
            else{
                peer.getOutgoing().send(FINDSUCCESSOR   
                    +REGEX+netID
                    +REGEX+joinID                   
                    +REGEX+joinPORT
                    +REGEX+joinAddress,
                    InetAddress.getByName(routeToPeer.getAddress()), routeToPeer.getPort());

                peer.getFingerTable().update(peer.getID(), new PeerReference(joinID+"",joinPORT+"",joinAddress));
                peer.getFingerTable().printFingerTable();
            }

             

        }//end FINDSUCCESSOR
        
        /*
            Received PUBLISH Message
        */
        else if(msg[0].equalsIgnoreCase(PUBLISH)){

            int netID = Integer.parseInt(msg[1]);

            int publisherID  = Integer.parseInt(msg[2]);
            int publisherPORT = Integer.parseInt(msg[3]);
            String publisherADD = msg[4];
            
            int fileID = Integer.parseInt(msg[5]);
            String fileName = msg[6];
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);

            PeerReference routeToPeer = peer.getFingerTable().getNearestPeer
                (new PeerReference(peer.getID()+"",peer.getPort()+"",peer.getAddress()), fileID);
            
            System.out.println("Message routed to: "+routeToPeer.getID());
            if(routeToPeer.getID()==peer.getID()){
                peer.addToReferencedFiles(new FileReference(fileID,fileName,
                        new PeerReference(publisherID+"",publisherPORT+"",publisherADD),
                        Connections.getInstance().getInitiatorList().get(netID)));
                if(publisherID==peer.getID()){
                    peer.publishFile(fileID);
                }
                else{
                    peer.getOutgoing().send(TELLPUBLISHER 
                        +REGEX+netID                //network id

                        +REGEX+peer.getID()         //the one to keep the reference for the file
                        +REGEX+peer.getPort()
                        +REGEX+peer.getAddress()
                        +REGEX+fileID              //file ID
                        +REGEX+fileName,           //file Name
                        InetAddress.getByName(publisherADD), publisherPORT); //send to the publisher of the file
                }
            }
            else{
                peer.getOutgoing().send(PUBLISH   
                    +REGEX+netID  
             
                    +REGEX+publisherID                   
                    +REGEX+publisherPORT
                    +REGEX+publisherADD
                    +REGEX+fileID
                    +REGEX+fileName,
                    InetAddress.getByName(routeToPeer.getAddress()), routeToPeer.getPort());
            }
        }//end PUBLISH
        
        /*
            Received TELLPUBLISHER MESSAGE
            sender is the one who keep the reference to the to-be-published file
            receiver is the publisher of the file
            sender tells the publisher to "publish the file" to the network
        */
        else if(msg[0].equalsIgnoreCase(TELLPUBLISHER)){

            int netID = Integer.parseInt(msg[1]);
            
            int referencedID  = Integer.parseInt(msg[2]);
            int referencedPORT = Integer.parseInt(msg[3]);
            String referencedADD = msg[5];
            
            int fileID = Integer.parseInt(msg[5]);
            String fileName = msg[6];
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);
            
            System.out.println();
            System.out.println("A new file has been successfully registered to  "+referencedID+"@"+referencedPORT+"\n"+
                "For the P2P Network: " +netID+"\n"+
                "FileID: "+fileID+ "\n"+
                "FileName: "+fileName+ "\n"+
                "Published by: "+peer.getID()+"@"+peer.getPort());
            System.out.println();
            
            peer.publishFile(fileID);
        }//end TELLPUBLISHER

        /*
            Received DELETE Message
        */
        else if(msg[0].equalsIgnoreCase(DELETE)){
            System.out.println(incomingMessage);
            int netID = Integer.parseInt(msg[1]);
            
            int requestID  = Integer.parseInt(msg[2]);//the one who requested to delete the file
            int requestPORT = Integer.parseInt(msg[3]);
            String requestADD = msg[4];
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);
            FileReference fileRef;
            
            PeerReference routeToPeer = peer.getFingerTable().getNearestPeer
                (new PeerReference(peer.getID()+"",peer.getPort()+"",peer.getAddress()), fileID);
            System.out.println("Message routed to: "+routeToPeer.getID());
            if(routeToPeer.getID()==peer.getID()){//check if fileID immediately follows peerID
                if((fileRef=peer.deleteReference(fileID)) != null){//check if there is a "fileReference" associated with the fileID and deletes 
                                                                    //the reference if it exist
                    if(fileRef.getPublisherID()==peer.getID()){//check if publisher is the peer itself
                        peer.deleteNetworkFile(fileID);
                        Connections.getInstance().getMulticastConnection().getOutgoing().send(DELETE
                            +Messages.REGEX+netID
                            +Messages.REGEX+fileRef.getPublisherID()
                            +Messages.REGEX+fileRef.getPublisherPort()
                            +Messages.REGEX+fileRef.getPublisherAddress()
                            +Messages.REGEX+fileRef.getID()
                            +Messages.REGEX+fileRef.getFileName());
                    }
                    else{//tell the publisher to delete the file associated with the fileID
                        peer.getOutgoing().send(DELETEFILEPUBLISHER
                            +REGEX+netID
                            +REGEX+requestID
                            +REGEX+requestPORT
                            +REGEX+requestADD
                            +REGEX+fileRef.getFileName()
                            +REGEX+fileRef.getID(),
                            InetAddress.getByName(fileRef.getPublisherAddress()), fileRef.getPublisherPort());
                    }
                }
                else{//fileReference not found
                    peer.getOutgoing().send(FAILDELETE
                        +REGEX+netID

                        +REGEX+peer.getID()
                        +REGEX+peer.getPort()
                        +REGEX+peer.getAddress()
                        +REGEX+fileID,
                        InetAddress.getByName(requestADD), requestPORT);
                }
                
            }
            else{//forward to peer with ID nearest fileID
                peer.getOutgoing().send(DELETE   
                    +REGEX+netID

                    +REGEX+requestID                   
                    +REGEX+requestPORT
                    +REGEX+requestADD
                    +REGEX+fileID,
                    InetAddress.getByName(routeToPeer.getAddress()), routeToPeer.getPort());
            }
        }//end DELETE
        
        /*
            The publisher received a DELETEFILEPUBLISHER Message
            and deletes the file associated with the fileID
        */
        else if(msg[0].equalsIgnoreCase(DELETEFILEPUBLISHER)){

            int netID = Integer.parseInt(msg[1]);
            
            int senderID  = Integer.parseInt(msg[2]); 
            int senderPORT = Integer.parseInt(msg[3]);
            String senderADD = msg[4];
            
            String fileName = msg[5];
            int fileID = Integer.parseInt(msg[6]);
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);
            
            peer.deleteNetworkFile(fileID);
            
            Connections.getInstance().getMulticastConnection().getOutgoing().send(DELETE
                +Messages.REGEX+netID
                +Messages.REGEX+peer.getID()
                +Messages.REGEX+peer.getPort()
                +Messages.REGEX+peer.getAddress()
                +Messages.REGEX+fileID
                +Messages.REGEX+fileName);
        }//end DELETEFILEPUBLISHER

        /*
            Received FAILDELETE Message
        */
        else if(msg[0].equalsIgnoreCase(FAILDELETE)){
            int netID = Integer.parseInt(msg[1]);

            int senderID  = Integer.parseInt(msg[2]);
            int senderPORT = Integer.parseInt(msg[3]);
            String senderADD = msg[4];
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = (PeerConnection) Connections.getInstance().getPeerConnection().get(netID);
            
            System.out.println();
            System.out.println("FAILED TO DELETE A FILE IN THE P2P NETWORK: "+netID//+"@"+netPORT
                    +"\nFileID: "+fileID
                    +"\nMessage from: "+senderID+"@"+senderPORT
                    +"\nError! File Not Found!");
            System.out.println();
        }//end FAILDELETE
        
        /*
            Received RETRIEVE Message
        */
        else if(msg[0].equalsIgnoreCase(RETRIEVE)){
            int netID = Integer.parseInt(msg[1]);
            
            
            int requestID  = Integer.parseInt(msg[2]); //address or ID of the one who retrieves the file
            int requestPORT = Integer.parseInt(msg[3]);
            String requestADD = msg[4];
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = (PeerConnection) Connections.getInstance().getPeerConnection().get(netID);
            FileReference fileRef;
            
            PeerReference routeToPeer = peer.getFingerTable().getNearestPeer
                (new PeerReference(peer.getID()+"",peer.getPort()+"",peer.getAddress()), fileID);
            System.out.println("Message routed to: "+routeToPeer.getID());
            if(routeToPeer.getID()==peer.getID()){//check if fileID immediately follows peerID
                if((fileRef=peer.getReferencedFiles().get(fileID)) != null){//check if there is a "fileReference" associated with the fileID 
                    if(fileRef.getPublisherID()==peer.getID()&&fileRef.getPublisherID()==requestID){//check if the one who requested is the publisher itself
                        FileObj file = peer.getPublishedFile(fileID);
                        Connections.getInstance().getLocalFiles().put(file.getID(), file);
                        System.out.println("'"+file.getFileName()+"' has been retrieved and saved to local files.");
                    }
                    else{//tell the publisher that someone Requests a file associated with the fileID
                        peer.getOutgoing().send(INIT_SEND 
                            +REGEX+netID                //network id
                        
                            +REGEX+requestID         
                            +REGEX+requestPORT
                            +REGEX+requestADD
                            +REGEX+fileID,              //file ID
                            InetAddress.getByName(fileRef.getPublisherAddress()), fileRef.getPublisherPort()); //send to the one who published the file
                    }
                }
                else{//fileReference not found
                    peer.getOutgoing().send(FAILRETRIEVE
                        +REGEX+netID

                        +REGEX+peer.getID()
                        +REGEX+peer.getPort()
                        +REGEX+peer.getAddress()
                        +REGEX+fileID,
                        InetAddress.getByName(requestADD), requestPORT);
                }
                
            }
            else{//forward to peer with ID nearest fileID
                peer.getOutgoing().send(RETRIEVE   
                    +REGEX+netID

                    +REGEX+requestID                   
                    +REGEX+requestPORT
                    +REGEX+requestADD
                    +REGEX+fileID,
                    InetAddress.getByName(routeToPeer.getAddress()), routeToPeer.getPort());
            }
        }//END RETRIEVE
            
        /*
            Received INIT_SEND Message
        */
        else if(msg[0].equalsIgnoreCase(INIT_SEND)){
            int netID = Integer.parseInt(msg[1]);
        
            
            int requestID  = Integer.parseInt(msg[2]); //address or ID of the one who retrieves the file
            int requestPORT = Integer.parseInt(msg[3]);
            String requestADD = msg[4];
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);
            peer.openObjSender(RETRIEVE, requestID, requestPORT, requestADD, fileID);//tcp send
        }//END INIT_SEND
        
        else if(msg[0].equalsIgnoreCase(INIT_RECEIVE)){
            int netID = Integer.parseInt(msg[1]);

            
            int senderID  = Integer.parseInt(msg[2]); //address or ID of the one who keeps the file
            int senderPORT = Integer.parseInt(msg[3]);
            String senderADD = msg[4];
            
            int objPORT = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);
            peer.openObjReceiver(RETRIEVE,senderID,senderPORT,senderADD,objPORT);//tcp receive
        }//END INIT_RECEIVE
        
        else if(msg[0].equalsIgnoreCase(FAILRETRIEVE)){
            int netID = Integer.parseInt(msg[1]);

            int senderID  = Integer.parseInt(msg[2]);
            int senderPORT = Integer.parseInt(msg[3]);
            String senderADD = msg[4];
            
            int fileID = Integer.parseInt(msg[5]);
            
            PeerConnection peer = Connections.getInstance().getPeerConnection().get(netID);
            
            System.out.println();
            System.out.println("FAILED TO RETRIEVE A FILE IN THE P2P NETWORK: "+netID//+"@"+netPORT
                    +"\nFileID: "+fileID
                    +"\nMessage from: "+senderID+"@"+senderPORT
                    +"\nError! File Not Found!");
            System.out.println();
        }//end FAILRETRIEVE
        


        
//        else if(msg[0].equalsIgnoreCase(SUCCESSDELETE)){
//            int netID = Integer.parseInt(msg[1]);
//            int netPORT = Integer.parseInt(msg[2]);
//            
//            int publisherID  = Integer.parseInt(msg[3]);
//            int publisherPORT = Integer.parseInt(msg[4]);
//            
//            String fileName = msg[5];
//            int fileID = Integer.parseInt(msg[6]);
//            
//            PeerConnection peer = (PeerConnection) Connections.getInstance().getPeerConnection().get(netID);
//            
//            System.out.println();
//            System.out.println("FILE SUCCESSFULLY DELETED IN THE P2P NETWORK: "+netID+"@"+netPORT
//                    +"\nFileID: "+fileID
//                    +"\nFilename: '"+fileName+"'"
//                    +"\nPublished by: "+publisherID+"@"+publisherPORT);
//            System.out.println();
//        }

        

//        else if(msg[0].equalsIgnoreCase(FILE)){
//            int netID = Integer.parseInt(msg[1]);
//            int netPORT = Integer.parseInt(msg[2]);
//            
//            int senderID  = Integer.parseInt(msg[3]);
//            int senderPORT = Integer.parseInt(msg[4]);
//            
//            int fileID = Integer.parseInt(msg[5]);
//            String fileName = msg[6];
//            
//            PeerConnection peer = (PeerConnection) Connections.getInstance().getPeerConnection().get(netID);
//            
//            if(!Connections.getInstance().getCachedNetworkFiles().containsKey(fileID)){
//                System.out.println("FileID: "+fileID
//                                +" Filename: "+fileName
//                                +" Registered to: "+senderID+"@"+senderPORT);
//                Connections.getInstance().getCachedNetworkFiles().put(fileID, senderID);
//            }
//        }
      
    }

    private static void printStatus(PeerConnection peer) {
        
        System.out.println();
        System.out.println("Your successor: "+      peer.getReference().getSuccessorID()+" "
                        + "Your successor port: "+  peer.getReference().getSuccessorPort()+"\n"
                        + "Your predecessor: "+     peer.getReference().getPredecessorID()+" "
                        + "Your predecessor port: "+peer.getReference().getPredecessorPort());
        
    }

    
    
}
