
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private void parse(String command) {
        System.out.println(command);
   }
}
