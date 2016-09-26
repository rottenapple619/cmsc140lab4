
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
class ObjSender extends Thread{
    
    private final int receiverID;
    private final int receiverPORT;
    private final int objPORT;
    private final FileObj file;
    private final PeerConnection node;
    
    private ServerSocket sSocket;
    private Socket cSocket;
    private ObjectOutput out;
    private final String transferType;
    
    ObjSender(String type,PeerConnection node, int rID, int rPORT, FileObj file) {
        this.transferType = type;
        this.node = node;
        this.receiverID = rID;
        this.receiverPORT = rPORT;
        this.objPORT = AvailablePort.getAvailablePort();
        this.file = file;
    }

    
    void startSending() {
        try {
            sSocket = new ServerSocket(objPORT);
            node.getOutgoing().send(Messages.INIT_RECEIVE
                    /*+Messages.REGEX+this.transferType*/
                    +Messages.REGEX+node.getReference().getInitiatorID()
                    +Messages.REGEX+node.getReference().getInitiatorPort()
                    +Messages.REGEX+node.getID()
                    +Messages.REGEX+node.getPort()
                    +Messages.REGEX+objPORT
                    /*+Messages.REGEX+fileID*/,
                    InetAddress.getLocalHost(), receiverPORT);
            this.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ObjSender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ObjSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run(){
        try {
            
            this.cSocket = sSocket.accept();
            System.out.println();
            System.out.println("Connected to: "+receiverID);
            System.out.println("Sending file(s) to: "+receiverID+"...");
            
            out = new ObjectOutputStream(cSocket.getOutputStream());   
            out.writeObject(file);
            System.out.println("Sending file(s) to: "+receiverID+" finished");
            System.out.println();
            
        } catch (IOException ex) {
            Logger.getLogger(ObjSender.class.getName()).log(Level.SEVERE, null, ex);
        } finally {//closing all sockets and streams
            if(this.out!=null){
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(ObjSender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(this.cSocket!=null){
                try {
                    this.cSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(ObjSender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(this.sSocket!=null){
                try {
                    this.sSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(ObjSender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
    }
    
}
