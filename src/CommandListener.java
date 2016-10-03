
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
        }
        
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
            publishedFiles.forEach( (Integer i,FileReference file) -> System.out.println("FileID: "+file.getID()+" Filename: "+file.getFileName()));
            System.out.println("-End of List-");
        }
        
        
        /*********************** J   O   I   N ************************/
        else if(command[0].equalsIgnoreCase(Command.JOIN.toString())){
            int initiatorID = 0;
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
            PeerConnection peer = null;
            
            try{
                initiatorID = Integer.parseInt(command[1]);
            }catch(NumberFormatException ex){
                System.err.println("Invalid initiatorID: "+command[1]);
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
            
        }
   }
}
