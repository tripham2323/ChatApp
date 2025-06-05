/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import app.MessageType;
import connection.DatabaseConnection;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
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
        this.con = DatabaseConnection.getInstance().getConnection();
        this.fileReceivers = new HashMap<>();
    }
    
    public Model_File addFileReceiver(String fileExtension) throws SQLException {
        Model_File data;
        PreparedStatement p = con.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
        p.setString(1, fileExtension);
        p.execute();
        ResultSet r = p.getGeneratedKeys();
        r.first();
        int fileID = r.getInt(1);
        data = new Model_File(fileID, fileExtension);
        r.close();
        p.close();
        return data;
    
    }
    
    public void updateBlurHashDone(int fileID, String blurhash) throws SQLException {
       PreparedStatement p = con.prepareStatement(UPDATE_BLUR_HASH_DONE);
       p.setString(1, blurhash);
       p.setInt(2, fileID);
       p.execute();
       p.close();
        
    }
    
    public void updateDone(int fileID) throws SQLException {
       PreparedStatement p = con.prepareStatement(UPDATE_DONE);
       p.setInt(1, fileID);
       p.execute();
       p.close();
    
    }
    public void updateAvatarData(byte[] data, int userID) throws SQLException {
       PreparedStatement p = con.prepareStatement(UPDATE_IMAGE_AVATAR);
       InputStream fis = new ByteArrayInputStream(data);
       p.setBinaryStream(1, fis);
       p.setInt(2, userID);
       p.execute();
       p.close();
    
    }
    
    public void initFile(Model_File file, Model_Send_Message message) throws IOException {
        // lưu vào hashmap với key là id file value là Model_File_Receive
       fileReceivers.put(file.getFileID(), new Model_File_Receive(message, toFileObject(file)));
    
    }
    public void receiveFile(Model_Package_Sender dataPackage) throws IOException{
       if(!dataPackage.isFinish()){
           // lấy class model_file_receive trong list hashmap theo key = id trỏ đến phương thức writeFile truyền vào byte[] data đọc dữ liệu từ file đã chọn 
           fileReceivers.get(dataPackage.getFileID()).writeFile(dataPackage.getData());
       }else{
           fileReceivers.get(dataPackage.getFileID()).close();
       
       }
    }
     public void receiveVoice(Model_Package_Sender dataPackage, ByteArrayOutputStream ByteOutputStream) throws IOException{
       if(!dataPackage.isFinish()){
                ByteOutputStream.write(dataPackage.getData());
       }else{
                ByteOutputStream.close();
                
       
       }
    }
    
    public Model_Send_Message closeFile(Model_Receive_Image dataImage) throws IOException, SQLException{
       Model_File_Receive file = fileReceivers.get(dataImage.getFileID());
       if(file.getMessage().getMessageType() == MessageType.IMAGE.getValue()) {
           
           //image file
           //create blurhash image string
           file.getMessage().setText("");
           String blurhash = ConvertFileToBlurHash(file.getFile(), dataImage);
           updateBlurHashDone(dataImage.getFileID(), blurhash);
           
           
       }else if(file.getMessage().getMessageType() == MessageType.FILE.getValue()){
           file.getMessage().setText("");
 
       }
       else{
           updateDone(dataImage.getFileID());
       }
      
         // lấy message để gửi đến client đích khi nhận file hoàn tất
       return file.getMessage();
    }
    public void getFile(Model_Package_Sender data){
       Model_File_Receive file = fileReceivers.get(data.getFileID());
        try {
            FileInputStream in = new FileInputStream(file.getFile());
            byte dataFile[] = new byte[in.available()];
            in.read(dataFile);
            in.close();
            data.setData(dataFile);
            data.setFinish(true);
            
        } catch (IOException ex) {
          
        }
    }
    
    private String ConvertFileToBlurHash(File file, Model_Receive_Image dataImage) throws IOException{
        try {
        FileInputStream in = new FileInputStream(file);
        byte data[] = new byte[in.available()];
        in.read(data);
        in.close();
        dataImage.setData(data);
        } catch (IOException e) {
            System.err.println(e);
        }
        BufferedImage img = ImageIO.read(file);
        Dimension size = getAutoSize(new Dimension(img.getWidth(), img.getHeight()), new Dimension(200, 200));
        // chuyển image sang size nhỏ
        BufferedImage newImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        g2.drawImage(img, 0, 0, size.width, size.height, null);
        String blurhash = BlurHash.encode(newImage);    
        dataImage.setWidth(size.width);
        dataImage.setHeight(size.height);
        dataImage.setImage(blurhash);
        
        return blurhash;
        
        
    
    }
   
    private Dimension getAutoSize(Dimension fromSize, Dimension toSize){
        int w = toSize.width;
        int h = toSize.height;
        int iw = fromSize.width;
        int ih = fromSize.height;
        double xScale = (double) w / iw;
        double yScale = (double) h / ih;
        double scale = Math.min(xScale, yScale);
        int width = (int) (scale * iw);
        int height = (int) (scale * ih);
        return new Dimension(width, height);
    
    }

    public File toFileObject(Model_File file) {
       return new File(PATH_FILE + file.getFileID() + file.getFileExtensions()); //tạo file mới với đường dẫn kèm phần mở rộng dc gán
    }
    
    //truy vấn sql
    private final String PATH_FILE = "server_data/";
    private final String INSERT = "insert into files (FileExtension) values (?)";
    private final String UPDATE_BLUR_HASH_DONE = "update files set BlurHash=?, `Status`='1' where FileID=? limit 1";
    private final String UPDATE_DONE = "update files set `Status`='1' where FileID=? limit 1";
    private final String UPDATE_IMAGE_AVATAR = "update user_account set Image=? where UserID=? limit 1";
    
    //instance
    private final Connection con;
    private final Map<Integer, Model_File_Receive> fileReceivers;
}
