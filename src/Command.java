/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public enum Command {
    COMMAND         ("Shows the list of available commands", "COMMAND"),
    CREATE          ("Creates a new P2P network", "CREATE"),
    DELETE          ("Deletes a file from the P2P Network", "DELETE<space>FILEID<space>InitiatorID"),
    EXIT            ("Exit", "EXIT"),
    /*FILESKEPT       ("Displays the list of files that you keep in the P2P Network","FILESKEPT<space>InitiatorID+'@'+InitiatorPORT"),*/
    FILESLOCAL      ("Displays the list of files in your local machine","FILESLOCAL"),
    FILESNETWORK    ("Request & display the list of files in the P2P Network","FILESNETWOK<space>InitiatorID"),
    JOIN            ("Joins a P2P Network", "JOIN<space>InitiatorID"),
    PUBLISH         ("Publish a file to the P2P Network", "PUBLISH<space>InitiatorID"),
    RETRIEVE        ("Retrieves a file from the P2P Network", "RETRIEVE<space>FILEID<space>InitiatorID"),
    NETWORKS        ("Displays list of available P2P networks", "NETWORKS");
    
    
    private final String description,syntax;
    
    Command(String description, String syntax){
        this.description = description;
        this.syntax = syntax;
    }
    
    String getDescription(){
        return this.description;
    }
    String getSyntax(){
        return this.syntax;
    }
}
