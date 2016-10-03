
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Iterator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public class PeerConnection extends Node{

    private final DatagramSocket socket;

    private final OutgoingListener outgoing;
    private final IncomingListener incoming;
    private PeerNotifier notifier = null;
    private ObjReceiver objReceiver;
    private ObjSender objSender;
    
    PeerConnection(PeerReference initRef) throws SocketException {
        super(false);
        System.out.println("Your ID: "+getID()+" Your Port: "+getPort());
        this.getReference().updateInitID(initRef.getID()+"");
        this.getReference().updateInitPort(initRef.getPort()+"");
        this.socket = new DatagramSocket(this.getPort());
        
      
        this.incoming = new IncomingListener(this, socket);
        this.outgoing = new OutgoingListener(this, socket);
    }
    
    PeerConnection() throws SocketException{
        super(true);
        this.getReference().updateInitID(getID()+"");
        this.getReference().updateInitPort(getPort()+"");
        this.notifier = new PeerNotifier(this);
        this.socket = new DatagramSocket(this.getPort());
        
        this.incoming = new IncomingListener(this, socket);
        this.outgoing = new OutgoingListener(this, socket);
    }
    

    void startConnection(boolean isInitiator) {
        incoming.start();
        outgoing.start();
        if(isInitiator){
            notifier.start();
        }
    }
    
    OutgoingListener getOutgoing(){
        return this.outgoing;
    }
    
    ObjSender getObjSender(){
        return this.objSender;
    }

    void openObjReceiver(String type, int sID, int sPORT, int objPORT) {
        objReceiver = new ObjReceiver(type, this, sID, sPORT, objPORT);
        objReceiver.startReceiving();
    }

    
    void openObjSender(String type,int rID, int rPORT, int fID) {
        FileObj file = null;
        
        if(type.equalsIgnoreCase(Messages.RETRIEVE)){
            file = (FileObj) this.getFilesInNetwork().get(fID);
            objSender = new ObjSender(Messages.RETRIEVE, this, rID, rPORT, file);
        }
        /*else if(type.equalsIgnoreCase(Messages.PUBLISH)){
            file = this.getFilesToPublish().getFile(fID);
            objSender = new ObjSender(Messages.PUBLISH, this, rID, rPORT, file);
        }*/
        
        objSender.startSending();
    }

    

    
}
