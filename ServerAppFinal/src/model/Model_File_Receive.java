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
         this.accFile = new RandomAccessFile(file, "rw");// mode 'w' có thể write vào file null vừa khởi tạo
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

    /**
     * @return the accFile
     */
    public RandomAccessFile getAccFile() {
        return accFile;
    }

    /**
     * @param accFile the accFile to set
     */
    public void setAccFile(RandomAccessFile accFile) {
        this.accFile = accFile;
    }
    private Model_Send_Message message;
    private File file;
    private RandomAccessFile accFile;

    
    public synchronized long writeFile(byte[] data) throws IOException{
          accFile.seek(accFile.length());//write từ vị trí của con trỏ bắt đầu của file null là 0
          accFile.write(data);//write data đọc từ file chọn vào file khởi tạo null 
          return accFile.length();
   
    }
    public void close() throws IOException {
          accFile.close();//kết thúc việc write data vào file khởi tạo
    }
}
