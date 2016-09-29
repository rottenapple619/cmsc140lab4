import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marz
 */
public class FileObj implements Serializable{

    private final File file;
    private final int ID;
    private final int initiatorID;
    private static final long serialVersionUID = 1L;
    
    FileObj(File file, int initiatorID) {
        this.file = file;
        this.ID = getRandomID();
        this.initiatorID = initiatorID;
    }

    String getFileName() {
        return this.file.getName();
    }
    
    @Override
    public int hashCode(){
        return this.file.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileObj other = (FileObj) obj;
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        return true;
    }

    private int getRandomID() {
        return (int)(Math.random()*Math.pow(2, FingerTable.KEY));
    }
    
    public int getID(){
        return this.ID;
    }
}
