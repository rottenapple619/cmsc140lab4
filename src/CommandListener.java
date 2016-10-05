
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
public class CommandListener extends Thread{
    
    private static CommandListener instance = null;
    private boolean isRunning = false;
    private final BufferedReader stdIn;
    
    public CommandListener(){
        this.isRunning = true;
        this.stdIn = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public static CommandListener getInstance(){
        if(instance == null)
            instance = new CommandListener();
        return instance;
    }
    
    @Override
    public void run(){
        try{
            while(isRunning){
                try {
                    parse(stdIn.readLine().replaceAll(" ", Messages.REGEX));
                } catch (IOException ex) {
                    Logger.getLogger(CommandListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }finally{
            if(stdIn!=null){
                try {
                    stdIn.close();
                } catch (IOException ex) {
                    Logger.getLogger(CommandListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
   
    }
    
    
    public void stopThread(){
        this.isRunning = false;
    }

    private void parse(String typedString) throws UnknownHostException {
        String command[] = typedString.split(Messages.REGEX);
        Connections conInstance = Connections.getInstance();
        
        /*********************** C  R  E  A  T  E ************************/
        if(command[0].equalsIgnoreCase(Command.CREATE.toString())){
            
            if(conInstance.isServer()){//check if nagcreate na
                System.err.println("Only a single instance of INITIATOR can be created.");
            }
            else{
                conInstance.initializePeerConnection();
                PeerConnection peer = conInstance.getPeerConnection().get(conInstance.getID());
                System.out.println("Created a new network with ID: "+peer.getID()+"@"+peer.getPort());
                System.out.println("Initially setting parameters:");
                System.out.println("Predecessor: "+peer.getReference().getPredecessorID());
                System.out.println("Successor: "+peer.getReference().getSuccessorID());
            }
        }
        
        /***********************N  E  T  W  O  R  K  S************************/
        else if(command[0].equalsIgnoreCase(Command.NETWORKS.toString())){
            if(conInstance.getInitiatorList().isEmpty()){
                System.out.println("No available P2P Network.");
                return;
            }
            System.out.println();
            System.out.println("List of available P2P Networks:");
            conInstance.getInitiatorList().forEach( (Integer k,PeerReference peer) -> 
                    System.out.println("NetworkID: "+peer.getID()+" Port: "+peer.getPort()));
            System.out.println("-End of List-");
        }//end NETWORKS
        
        /***********************FILESNETWORK************************/
        else if(command[0].equalsIgnoreCase(Command.FILESNETWORK.toString())){
        
            final int initiatorID;
            try{
                initiatorID = Integer.parseInt(command[1]);
            }catch(NumberFormatException ex){
                System.err.println("Invalid initiatorID: "+command[1]);
                return;
            }
            
            PeerReference initiator = conInstance.getInitiatorList().get(initiatorID);

            if(initiator == null){
                System.err.println("Unknown initiator: "+initiatorID);
                return;
            }
            
            Map<Integer, FileReference> publishedFiles = 
                conInstance.getPublishedFiles().entrySet()
                .stream()
                .filter(file -> file.getValue().getInitiator().getID() == initiatorID)
                .collect(Collectors.toMap(file -> file.getKey(), file -> file.getValue()));
            
            if(publishedFiles.isEmpty()){
                System.out.println("No published files for the P2P Network: "+initiator.getID()+"@"+initiator.getPort());
                return;
            }
            
            System.out.println();
            System.out.println("List of available files in the P2P Network: "+initiator.getID()+"@"+initiator.getPort());
            publishedFiles.forEach( (Integer i,FileReference file) -> 
                System.out.println("FileID: "+file.getID()+" Filename: "+file.getFileName()));
            System.out.println("-End of List-");
        }
        
        
        /*********************** J   O   I   N ************************/
        else if(command[0].equalsIgnoreCase(Command.JOIN.toString())){
            int initiatorID;
            try{
                initiatorID = Integer.parseInt(command[1]);
            }catch(NumberFormatException ex){
                System.err.println("Invalid initiatorID: "+command[1]);
                return;
            }
            
            PeerReference initiator = conInstance.getInitiatorList().get(initiatorID);

            if(initiator == null){
                System.err.println("Unknown initiator: "+initiatorID);
                return;
            }
            
            if(conInstance.getPeerConnection().get(initiatorID)!=null){
                System.err.println("Already connected to this network.");
                return;
            }
            
            Connections.getInstance().initializePeerConnection(initiator);
        }
        
        /*********************** P U B L I S H ************************/
        else if(command[0].equalsIgnoreCase(Command.PUBLISH.toString())){
            int initiatorID;
            PeerConnection peer;
            
            try{
                initiatorID = Integer.parseInt(command[1]);
            }catch(ArrayIndexOutOfBoundsException | NumberFormatException ex){
                System.err.println("Invalid initiatorID");
                return;
            }
                        
            if((peer = conInstance.getPeerConnection().get(initiatorID))==null){
                System.err.println("Not connected to this network.");
                return;
            }
            
            JFileChooser fc = new JFileChooser();
            File file;
            FileObj fileObj;
            int actionTaken = fc.showOpenDialog(null);
            if(actionTaken == JFileChooser.APPROVE_OPTION){
                file = fc.getSelectedFile();
                fileObj = new FileObj(file,initiatorID);
                peer.getFilesToPublish().put(fileObj.getID(), fileObj);
            }
            else{
                System.err.println("Publication aborted.");
                return;
            }
            
            System.out.println();
            System.out.println("PUBLISHING A FILE TO THE P2P NETWORK: "+command[1]//+"@"+initiator.getPort()
                    +"\nFileID: "+fileObj.getID()
                    +"\nFilename : '"+fileObj.getFileName()+"'");
            System.out.println();
            
            peer.getOutgoing().send(Messages.PUBLISH
                +Messages.REGEX+initiatorID         //network ID
                
                +Messages.REGEX+peer.getID()        //publisher ID
                +Messages.REGEX+peer.getPort()
                +Messages.REGEX+peer.getAddress()
                +Messages.REGEX+fileObj.getID()     //file ID
                +Messages.REGEX+fileObj.getFileName(),  //file Name  
                InetAddress.getByName(peer.getReference().getInitiatorAddress()), peer.getReference().getInitiatorPort());
            
        }//END PUBLISH
        
        /*********************** D  E   L   E   T   E ************************/
        else if(command[0].equalsIgnoreCase(Command.DELETE.toString())){
            FileReference fileRef;
            int initiatorID;
            PeerConnection peer;
            PeerReference initiator;
            try{
                fileRef = conInstance.getPublishedFiles().get(Integer.parseInt(command[1]));
                initiatorID = Integer.parseInt(command[2]);
                peer = conInstance.getPeerConnection().get(initiatorID);
                initiator = conInstance.getInitiatorList().get(initiatorID);
            }catch(ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException | NumberFormatException ex){
                System.err.println("Invalid address");
                return;
            }
            
            if(fileRef==null){
                System.err.println("File not found!");
                return;
            }
            
            if(initiator == null){
                System.err.println("Unknown initiator: "+initiatorID);
                return;
            }
            
            if(peer==null){
                System.err.println("Not connected to "+initiator.getID()+"@"+initiator.getPort());
                return;
            }

            System.out.println();
            System.out.println("DELETING A FILE TO THE P2P NETWORK: "+initiator.getID()+"@"+initiator.getPort()
                    +"\nFileID: "+fileRef.getID()
                    +"\nFilename : '"+fileRef.getFileName()+"'");
            System.out.println();
            
            peer.getOutgoing().send(Messages.DELETE //send a delete msg to the initiator
                +Messages.REGEX+initiatorID
                
                +Messages.REGEX+peer.getID()
                +Messages.REGEX+peer.getPort()
                +Messages.REGEX+peer.getAddress()
                +Messages.REGEX+fileRef.getID(),
                InetAddress.getByName(initiator.getAddress()), initiator.getPort());
        }//END DELETE
        
        /*********************** R  E  T  R  I  E  V  E ************************/
        else if(command[0].equalsIgnoreCase(Command.RETRIEVE.toString())){
            FileReference fileRef;
            int initiatorID;
            PeerConnection peer;
            PeerReference initiator;
            try{
                fileRef = conInstance.getPublishedFiles().get(Integer.parseInt(command[1]));
                initiatorID = Integer.parseInt(command[2]);
                peer = conInstance.getPeerConnection().get(initiatorID);
                initiator = conInstance.getInitiatorList().get(initiatorID);
            }catch(ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException | NumberFormatException ex){
                System.err.println("Invalid address");
                return;
            }
            
            if(fileRef==null){
                System.err.println("File not found!");
                return;
            }
            
            if(initiator == null){
                System.err.println("Unknown initiator: "+initiatorID);
                return;
            }
            
            if(peer==null){
                System.err.println("Not connected to "+initiator.getID()+"@"+initiator.getPort());
                return;
            }
            
            System.out.println();
            System.out.println("RETRIEVING A FILE IN THE P2P NETWORK: "+initiator.getID()+"@"+initiator.getPort()
                + "\nFileID: "+fileRef.getID()
                /*+ "\nFilename: '"+file.getName()+"'"*/
                + "\nRequested by: "+peer.getID()+"@"+peer.getPort()+"(You)");
            System.out.println();
            
            peer.getOutgoing().send(Messages.RETRIEVE
                    +Messages.REGEX+initiator.getID()
                    
                    +Messages.REGEX+peer.getID()
                    +Messages.REGEX+peer.getPort()
                    +Messages.REGEX+peer.getAddress()
                    +Messages.REGEX+fileRef.getID(),
                InetAddress.getByName(initiator.getAddress()), initiator.getPort());
        }//END RETRIEVE
        
        /*********************** F I L E S L O C A L ************************/
        else if(command[0].equalsIgnoreCase(Command.FILESLOCAL.toString())){
            if(conInstance.getLocalFiles().isEmpty()){
                System.out.println("No files stored.");
                return;
            }
            System.out.println();
            System.out.println("List of files stored locally:");
            conInstance.getLocalFiles().forEach( (Integer id,FileObj file) -> 
                    System.out.println("FileID: "+file.getID()+" Filename: "+file.getFileName()));
            System.out.println("-End of List-");
        }//end FILESLOCAL
        
        /*********************** C O M M A N D ************************/
        else if(command[0].equalsIgnoreCase(Command.COMMAND.toString())){
            System.out.println();
            System.out.println("AVAILABLE COMMANDS:");
            
                
            for(Command c: Command.values()){
                System.out.println();
                System.out.println(c.toString()+"\n\tDescription - "+c.getDescription()
                                    +"\n\tSyntax - "+c.getSyntax());
            }
            System.out.println();
        }//end COMMAND
        
        
        /*********************** I N V A L I D ************************/
        else{
            System.err.println("Invalid command");
        }//end INVALID
   }
}
