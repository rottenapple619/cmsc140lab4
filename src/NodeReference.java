/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public class NodeReference {

    //reference to INITIATOR
    public static final int INIT_ID = 0;
    public static final int INIT_PORT = 1;
    public static final int INIT_ADD = 2;
    
    public static final int PRED_ID = 3;
    public static final int PRED_PORT = 4;
    public static final int PRED_ADD = 5;
    
    //
    public static final int SUC_ID = 6;
    public static final int SUC_PORT = 7;
    public static final int SUC_ADD = 8;
    
    private final String reference[];
    
    public NodeReference(){
        this.reference = new String[9];
    }
    
    public NodeReference(String ID, String port, String address){
        this.reference = new String[3];
        this.reference[INIT_ID] = ID;
        this.reference[INIT_PORT] = port;
        this.reference[INIT_ADD] = address;
    }
    
    //getters and setters
    public int getInitiatorID(){
        return Integer.parseInt(getReference(INIT_ID));
    }
    
    public int getInitiatorPort(){
        return Integer.parseInt(getReference(INIT_PORT));
    }
    
    public String getInitiatorAddress(){
        return getReference(INIT_ADD);
    }
    
    public int getPredecessorID(){
        return Integer.parseInt(getReference(PRED_ID));
    }
    
    public String getPredecessorAddress(){
        return getReference(PRED_ADD);
    }
    
    public int getPredecessorPort(){
        return Integer.parseInt(getReference(PRED_PORT));
    }
    
    public int getSuccessorID(){
        return Integer.parseInt(getReference(SUC_ID));
    }
    
    public int getSuccessorPort(){
        return Integer.parseInt(getReference(SUC_PORT));
    }
    
    public String getSuccessorAddress(){
        return getReference(SUC_ADD);
    }
    
    public void updateInitID(String ID){
        updateReference(INIT_ID, ID);
    }
    
    public void updateInitPort(String port){
        updateReference(INIT_PORT, port);
    }

    public void updateInitAddress(String address){
        updateReference(INIT_ADD, address);
    }
    
    public void updatePredID(String ID){
        updateReference(PRED_ID, ID);
    }
    
    public void updatePredPort(String port){
        updateReference(PRED_PORT, port);
    }

    public void updatePredAddress(String address){
        updateReference(PRED_ADD, address);
    }
    
    public void updateSucID(String ID){
        updateReference(SUC_ID, ID);
    }
    
    public void updateSucPort(String port){
        updateReference(SUC_PORT, port);
    }

    public void updateSucAddress(String address){
        updateReference(SUC_ADD, address);
    }
    
    protected void updateReference(int index, String newReference) {
        this.reference[index] = newReference;
    }
    
    protected String getReference(int index) {
        return this.reference[index];
    }
}
