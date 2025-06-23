/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Admin
 */
public class Model_File_Receive {

    public Model_File_Receive(Model_Send_Message message, File file) throws IOException{
        this.message = message;
        this.file = file;
    }
    public Model_File_Receive() {
    
    }

    /**
     * @return the message
     */
    public Model_Send_Message getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(Model_Send_Message message) {
        this.message = message;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }


    private Model_Send_Message message;
    private File file;

      public File writeFile(byte[] data) throws IOException{
        // Ensure parent directory exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            System.out.println("Created directory: " + parentDir.getPath() + " - Success: " + created);
        }
        
        FileOutputStream out = new FileOutputStream(getFile());
        out.write(data);
        out.close();
        return file;
    }
    
}
