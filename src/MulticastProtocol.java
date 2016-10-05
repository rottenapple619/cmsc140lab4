
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.JFileChooser;

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
        }
        
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
        }
        
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
        }
    }
        
    
    
    
    static void command(String command) throws FileNotFoundException, IOException{
                

        /*********************** R  E  T  R  I  E  V  E ************************/
        if(command.startsWith("RETRIEVE")||command.startsWith("retrieve")){
            int fileID;
            int initiatorID;
            int initiatorPort;
            PeerConnection peer;
            try{
                fileID = Integer.parseInt(command.substring(command.indexOf(" ")+1,command.lastIndexOf(" ")));
                initiatorID = Integer.parseInt(command.substring(command.lastIndexOf(" ")+1,command.indexOf("@")));
                initiatorPort = Integer.parseInt(command.substring(command.indexOf("@")+1));
                peer = (PeerConnection) Connections.getInstance().getPeerConnection().get(initiatorID);
            }catch(StringIndexOutOfBoundsException | NumberFormatException ex){
                System.err.println("Invalid address");
                return;
            }
            
            if(peer==null){
                System.err.println("Not connected to "+initiatorID+"@"+initiatorPort);
                return;
            }
            
            System.out.println();
            System.out.println("RETRIEVING A FILE IN THE P2P NETWORK: "+initiatorID+"@"+initiatorPort
                + "\nFileID: "+fileID
                /*+ "\nFilename: '"+file.getName()+"'"*/
                + "\nRequested by: "+peer.getID()+"@"+peer.getPort()+"(You)");
            System.out.println();
            
            peer.getOutgoing().send(Messages.RETRIEVE
                    +Messages.REGEX+initiatorID
                    +Messages.REGEX+initiatorPort
                    +Messages.REGEX+peer.getID()
                    +Messages.REGEX+peer.getPort()
                    +Messages.REGEX+fileID,
                InetAddress.getLocalHost(), initiatorPort);
            
            
        }
        
//        /*********************** F I L E S K E P T ************************/
//        else if(command.startsWith("FILESKEPT")||command.startsWith("fileskept")){
//            int initiatorID;
//            int initiatorPort;
//            PeerConnection peer;
//            try{
//                initiatorID = Integer.parseInt(command.substring(10,command.indexOf("@")));
//                initiatorPort = Integer.parseInt(command.substring(command.indexOf("@")+1));
//                peer = Connections.getInstance().getPeerConnection.get(initiatorID);
//            }catch(StringIndexOutOfBoundsException | NumberFormatException ex){
//                System.err.println("Invalid address");
//                return;
//            }
//            
//            if(peer==null){
//                System.err.println("Not connected to "+initiatorID+"@"+initiatorPort);
//                return;
//            }
//            
//            if(peer.getFilesInNetwork().isEmpty()){
//                System.out.println("There are no files that you keep for the P2P network: "+initiatorID+"@"+initiatorPort);
//            }
//            else{
//                System.out.println();
//                System.out.println("FILE(S) YOU KEEP: "+peer.getFilesInNetwork().size()+" FOR THE P2P NETWORK: "+initiatorID+"@"+initiatorPort);
//                for(FileObj file : peer.getFilesInNetwork()){
//                    System.out.println("ID: "+file.getID()+" Filename: "+file.getName());
//                }
//                System.out.println();
//            }
//            
//        }
        
        /*********************** F I L E S L O C A L ************************/
        else if(command.equalsIgnoreCase(Command.FILESLOCAL.toString())){
          if(Connections.getInstance().getLocalFiles().isEmpty()){
                System.out.println("Files empty..");
            }
            else{
                System.out.println();
                System.out.println("Local Files: ");
                Iterator entries = Connections.getInstance().getLocalFiles().entrySet().iterator();
                
                while (entries.hasNext()) {
                    Entry thisEntry = (Entry) entries.next();
                    int key = (int) thisEntry.getKey();
                    FileObj file = (FileObj) thisEntry.getValue();
                    System.out.println("ID: "+key+" Filename: "+file.getFileName());
                }
                System.out.println();
            }
            
        }
        /*********************** F I L E S N E T W O R K ************************/ 
        else if(command.startsWith("FILESNETWORK")||command.startsWith("filesnetwork")){
            int initiatorID;
            int initiatorPort;
            PeerConnection peer;
            try{
                initiatorID = Integer.parseInt(command.substring(13,command.indexOf("@")));
                initiatorPort = Integer.parseInt(command.substring(command.indexOf("@")+1));
                peer = (PeerConnection) Connections.getInstance().getPeerConnection().get(initiatorID);
            }catch(StringIndexOutOfBoundsException | NumberFormatException ex){
                System.err.println("Invalid address");
                return;
            }
            
            if(peer==null){
                System.err.println("Not connected to "+initiatorID+"@"+initiatorPort);
                return;
            }
            
            
//            Connections.getInstance().getCachedNetworkFiles().clear();
            System.out.println();
            System.out.println("FILES IN THE P2P NETWORK: "+initiatorID+"@"+initiatorPort);
            peer.getOutgoing().send(Messages.FILESNETWORK
                +Messages.REGEX+initiatorID     //net ID
                +Messages.REGEX+initiatorPort
                +Messages.REGEX+peer.getID()    //your ID / request ID
                +Messages.REGEX+peer.getPort(), 
                InetAddress.getLocalHost(), initiatorPort); //sent to P2P network Initiator/Creator
        }
        /*********************** C O M M A N D ************************/
        else if(command.equalsIgnoreCase(Command.COMMAND.toString())){
            System.out.println();
            System.out.println("AVAILABLE COMMANDS:");
            
                
            for(Command c: Command.values()){
                System.out.println();
                System.out.println(c.toString()+"\n\tDescription - "+c.getDescription()
                                    +"\n\tSyntax - "+c.getSyntax());
            }
            System.out.println();
        }
        
        else if(command.equalsIgnoreCase(Command.EXIT.toString())){
            System.exit(0);
        }
        /*********************** I N V A L I D ************************/
        else{
            System.err.println("Invalid command");
        }
    }
    
}
