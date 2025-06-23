/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import app.MessageType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import model.Model_File;
import model.Model_File_Receive;
import model.Model_Package_Sender;
import model.Model_Receive_Image;
import model.Model_Send_Message;
import swing.blurHash.BlurHash;

/**
 *
 * @author Admin
 */
public class ServiceFile {
    public ServiceFile(){
        this.fileReceivers = new HashMap<>();
        
        // Create client_data directory if it doesn't exist
        File dataDir = new File(PATH_FILE);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            System.out.println("Created client_data directory: " + created);
        }
    }
    
 
    
    public void initFile(Model_File file) throws IOException {
        // lưu vào hashmap với key là id file value là Model_File_Receive
       fileReceivers.put(file.getFileID(), new Model_File_Receive(null, toFileObject(file)));
    
    }
    public File receiveFile(Model_Receive_Image dataImage) throws IOException{
      
           // lấy class model_file_receive trong list hashmap theo key = id trỏ đến phương thức writeFile truyền vào byte[] data đọc dữ liệu từ file đã chọn 
      File file = fileReceivers.get(dataImage.getFileID()).writeFile(dataImage.getData());
      
       return file;
       
    }
     
   
    public File toFileObject(Model_File file) {
       return new File(PATH_FILE + file.getFileID() + file.getFileExtensions()); //tạo file mới với đường dẫn kèm phần mở rộng dc gán
    }
    
    private final String PATH_FILE = "client_data/";
    
    //instance
    private final Map<Integer, Model_File_Receive> fileReceivers;
}
