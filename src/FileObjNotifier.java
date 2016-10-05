
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
public class FileObjNotifier extends Thread{

    private static final int delay = 1000;
    private boolean isRunning = false;
    private final Node node;
    private final FileObj publishedFile;
    
    FileObjNotifier(Node node,FileObj publishedFile/*InitiatorConnection initiator*/){
        this.node = node;
        this.isRunning = true;
        this.publishedFile = publishedFile;
    }
    
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run(){

        while(isRunning){
            try {
                Thread.sleep(delay);
                Connections.getInstance().getMulticastConnection().getOutgoing().send(Messages.PUBLISH//broadcast to multicast that a file has been
                                                                                                    //published
                    +Messages.REGEX+node.getReference().getInitiatorID()
                    +Messages.REGEX+node.getReference().getInitiatorPort()
                    +Messages.REGEX+node.getReference().getInitiatorAddress()
                        
                    +Messages.REGEX+node.getID()
                    +Messages.REGEX+node.getPort()
                    +Messages.REGEX+node.getAddress()
                        
                    +Messages.REGEX+publishedFile.getFileName()
                    +Messages.REGEX+publishedFile.getID());
            } catch (InterruptedException ex) {
                
            }

        }
    }
    
    public synchronized void stopThread(){
        try {
            this.isRunning = false;
            this.wait(delay*3);
        } catch (InterruptedException ex) {
            Logger.getLogger(FileObjNotifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FileObj getFileObj(){
        return this.publishedFile;
    }
}
