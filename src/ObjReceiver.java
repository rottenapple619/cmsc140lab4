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
    private final int senderPORT;
    private final String senderADD;
    private final int objPORT;

    private Socket cSocket;
    private ObjectInput in;
    private final String transferType;
    
    ObjReceiver(String type, PeerConnection node, int sID, int sPORT, String sADD, int objPORT) {
        this.transferType = type;
        this.node = node;
        this.senderID = sID;
        this.senderPORT = sPORT;
        this.senderADD = sADD;
        this.objPORT = objPORT;
    }
    
    @Override
    public void run(){

        try {
            cSocket = new Socket(InetAddress.getByName(senderADD), objPORT);
            System.out.println();
            System.out.println("Connected to: "+senderID);
            System.out.println("Receiving file(s) from: "+senderID+"...");
            in = new ObjectInputStream(cSocket.getInputStream());
            FileObj f = (FileObj) in.readObject(); 
            System.out.println("Received file: "+f.getFileName()+" with ID: "+f.getID());
            System.out.println("Receiving file(s) from: "+senderID+" completed.");
            

            Connections.getInstance().getLocalFiles().put(f.getID(), f);
            System.out.println("'"+f.getFileName()+"' has been retrieved and saved to local files.");

            
            
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
