import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
class ObjReceiver extends Thread{

    private final PeerConnection node;
    private final int senderID;
//    private final int fileID;
    private final int senderPORT;
    private final int objPORT;

    private Socket cSocket;
    private ObjectInput in;
    private final String transferType;
    
    ObjReceiver(String type, PeerConnection node, int sID, int sPORT, int objPORT) {
        this.transferType = type;
        this.node = node;
        this.senderID = sID;
        this.senderPORT = sPORT;
        this.objPORT = objPORT;
        //this.fileID = fileID;
    }
    
    @Override
    public void run(){

        try {
            cSocket = new Socket(InetAddress.getLocalHost(), objPORT);
            System.out.println();
            System.out.println("Connected to: "+senderID);
            System.out.println("Receiving file(s) from: "+senderID+"...");
            in = new ObjectInputStream(cSocket.getInputStream());
            FileObj f = (FileObj) in.readObject(); 
            System.out.println("Received file: "+f.getFileName()+" with ID: "+f.getID());
            System.out.println("Receiving file(s) from: "+senderID+" completed.");
            
//            if(this.transferType.equalsIgnoreCase(Messages.PUBLISH)){
//            //    node.getFilesInNetwork().addToList(f);
//            
//                Connections.getConnection().getMulticastConnection().getOutgoing().send(Messages.PUBLISH//broadcast to Multicast Network
//                +Messages.REGEX+node.getID()                                                            //that a new file is published
//                +Messages.REGEX+node.getPort()
//                +Messages.REGEX+node.getInitiatorID()
//                +Messages.REGEX+node.getInitiatorPort()
//                +Messages.REGEX+f.getFileName()
//                +Messages.REGEX+f.getID());
//            }
//            else{
                Connections.getConnection().getLocalFiles().put(f.getID(), f);
                System.out.println("'"+f.getFileName()+"' has been retrieved and saved to local files.");
//            }
            
            
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ObjReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {//closing all sockets and streams
            if(this.in!=null){
                try {
                    this.in.close();
                } catch (IOException ex) {
                    Logger.getLogger(ObjReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(this.cSocket!=null){
                try {
                    this.in.close();
                } catch (IOException ex) {
                    Logger.getLogger(ObjReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }

    void startReceiving() {
        this.start();
    }
    
}
