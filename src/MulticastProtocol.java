

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public class MulticastProtocol {

    static void check(String incomingMessage) {
        String msg[] = incomingMessage.split(Messages.REGEX);
        Connections currConnection = Connections.getInstance();
        
        /*********************** RECEIVED CREATE MSG ************************/
        if(msg[0].equalsIgnoreCase(Command.CREATE.toString())){
            if(currConnection.getInitiatorList().containsKey(Integer.parseInt(msg[1]))){//check if added or equal to own id
                return;
            }
            
            currConnection.getInitiatorList().put(Integer.parseInt(msg[1]),new PeerReference(msg[1],msg[2],msg[3]));
            
            if(currConnection.getID() == Integer.parseInt(msg[1])){
                return;
            }
            
            System.out.println("Received Message: A network by "+msg[1]+"@"+msg[2]+" has been created");
        }//end CREATE
        
        /*********************** RECEIVED PUBLISH MSG ************************/
        else if(msg[0].equalsIgnoreCase(Messages.PUBLISH)){
            
            int netID = Integer.parseInt(msg[1]);//P2P initiator
            int netPORT = Integer.parseInt(msg[2]);
            String netADD = msg[3];
            
            int publisherID = Integer.parseInt(msg[4]);//file publisher
            int publisherPORT = Integer.parseInt(msg[5]);
            String publisherADD = msg[6];
            
            String fileName = msg[7];
            int fileID = Integer.parseInt(msg[8]);
            
            if(Connections.getInstance().getPublishedFiles().containsKey(fileID)){
                return;
            }
            
            Connections.getInstance().getPublishedFiles().put(fileID, 
                    new FileReference(fileID,fileName,
                    new PeerReference(publisherID+"",publisherPORT+"",publisherADD),
                    Connections.getInstance().getInitiatorList().get(netID)));
            
            System.out.println();
            System.out.println("FILE PUBLISHED TO THE P2P NETWORK: "+netID+"@"+netPORT
                    +"\nFileID: "+fileID
                    +"\nFilename: '"+fileName+"'"
                    +"\nPublished by: "+publisherID+"@"+publisherPORT);
            System.out.println();
        }//END PUBLISH
        
        /*********************** RECEIVED DELETE MSG ************************/
        else if(msg[0].equalsIgnoreCase(Messages.DELETE)){
            
            int netID = Integer.parseInt(msg[1]);
            
            int publisherID = Integer.parseInt(msg[2]);
            int publisherPORT = Integer.parseInt(msg[3]);
            String publisherADD = msg[4];
            
            int fileID = Integer.parseInt(msg[5]);
            String filename = msg[6];
            
            Connections.getInstance().getPublishedFiles().remove(fileID);
            PeerReference initiator = Connections.getInstance().getInitiatorList().get(netID);
            
            System.out.println();
            System.out.println("A file has been successfully DELETED from "+publisherID+"@"+publisherPORT+"\n"+
                "For the P2P Network: " +initiator.getID() +"@"+ initiator.getPort()+"\n"+
                "FileID: "+fileID+ "\n"+
                "FileName: "+filename);
//                "Deleted by: "+peer.getID()+"@"+peer.getPort()+"(You)");
            System.out.println();
        }//end DELETE
    }
    
}
