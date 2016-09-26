/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Thread that notifies everyone in the Multicast Network that the Initiator has created 
 * a new Peer-to-Peer Network
 * @author Marz
 */
public class PeerNotifier extends Thread{
    private static final int delay = 5000;
    private boolean isRunning = false;
    //private final InitiatorConnection initiator;
    private final Node node;
    
    PeerNotifier(Node node/*InitiatorConnection initiator*/){
        //this.initiator = initiator;
        this.node = node;
        isRunning = true;
    }
    
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run(){

        while(isRunning){
            try {
                Thread.sleep(delay);
                Connections.getConnection().getMulticastConnection().getOutgoing().send(Command.CREATE.toString()
                        +Messages.REGEX+node.getID()/*initiator.getID()*/
                        +Messages.REGEX+node.getPort()/*initiator.getPort()*/);
            } catch (InterruptedException ex) {
            }

        }
    }
        
}
