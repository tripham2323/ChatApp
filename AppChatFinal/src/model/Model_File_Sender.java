/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import event.EventFileSender;
import io.socket.client.Ack;
import io.socket.client.Socket;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import service.Service;

/**
 *
 * @author Admin
 */
public class Model_File_Sender {


    public Model_File_Sender(File file, Socket socket, Model_Send_Message message) throws IOException {
        accFile = new RandomAccessFile(file, "r");
        this.file = file;
        this.fileName = file.getName()+"!"+convertSize(file.length());
        this.socket = socket;
        this.message = message;
        fileExtensions = getExtensions(file.getName());
        fileSize = accFile.length();
    }
    public Model_File_Sender(){
    
    }
    private String getExtensions(String fileName){
        return fileName.substring(fileName.lastIndexOf("."), fileName.length());
    }
    public synchronized byte[]readFile() throws IOException {
        long filepointer = accFile.getFilePointer();
        if(filepointer != fileSize){// nếu con trỏ index trong file chưa = file size thì tiếp tục đọc
           int max = 2000;
           long length = filepointer + max >= fileSize?fileSize - filepointer : max;
           byte[] data = new byte[(int) length];
           accFile.read(data);//đọc file đã chọn vào biến data theo độ dài byte khởi tạo , the same way as the InputStream.read(byte[]) method of InputStream.
           return data;
        }else{ // đã đọc xong file
           return null;
        }
    }
    public void initSend() throws IOException {
        socket.emit("send_to_user", message.toJSONObject(), new Ack() {
            @Override
            public void call(Object... os) {
                if (os.length > 0){
                   int fileID = (int) os[0]; //nhận id file từ server
                    try {
                        startSend(fileID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                }
        });
       
    }
    public void startSend(int fileID) throws IOException {
        
        this.fileID = fileID;
        if(event != null){
           event.onStartSending();
        }
        sendingFile();
    
    }
    private void sendingFile() throws IOException {
        Model_Package_Sender data = new Model_Package_Sender();
        data.setFileID(fileID);
        data.setFromUserID(Service.getInstance().getUser().getUserID());
        data.setFileName(fileName);
        byte[] bytes = readFile(); //đọc file, trả về biến data chứa dữ liệu đọc từ file đã chọn theo type byte[]
        if (bytes != null){
           data.setData(bytes);
           data.setFinish(false);
        }else{
           data.setFinish(true);
           close();
        
        }
        socket.emit("send_file", data.toJsonObject(), new Ack() {
            @Override
            public void call(Object... os) {
                if(os.length > 0){
                    boolean act = (boolean) os[0];
                    if (act) {
                        try {
                            if(!data.isFinish()){
                                 if(event != null){
                                    event.onSending(getPercentage());
                                 }
                               sendingFile();   
                            }else{
                              //send file finish
                               Service.getInstance().fileSendFinish(Model_File_Sender.this);
                                if(event != null){
                                    event.onFinish();
                                 }
                            }
                        } catch (IOException e) {
                           e.printStackTrace();
                        }
                    }
                
                }
               }
        });
     
    }
    public double getPercentage() throws IOException {
           double percentage;
           long filePointer = accFile.getFilePointer();
           percentage = filePointer * 100/fileSize;
           return percentage;
    }
    public void close() throws IOException {
           accFile.close();
    }
    
    private static final String[] fileSizeUnits = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"}; //chuyển đổi file length sang size
    private static String convertSize(double bytes) {
        String sizeToReturn;
        DecimalFormat df = new DecimalFormat("0.#");
        int index;
        for (index = 0; index < fileSizeUnits.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        System.out.println("Systematic file size: " + bytes + " " + fileSizeUnits[index]);
        sizeToReturn = df.format(bytes) + " " + fileSizeUnits[index];
        return sizeToReturn;
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
     * @return the fileID
     */
    public int getFileID() {
        return fileID;
    }

    /**
     * @param fileID the fileID to set
     */
    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

        /**
     * @return the FileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param FileName the FileName to set
     */
    public void setFileName(String FileName) {
        this.fileName = FileName;
    }

    /**
     * @return the fileExtensions
     */
    public String getFileExtensions() {
        return fileExtensions;
    }

    /**
     * @param fileExtensions the fileExtensions to set
     */
    public void setFileExtensions(String fileExtensions) {
        this.fileExtensions = fileExtensions;
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
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
      
    public void addEvent(EventFileSender event) {
        this.event = event;
    }
    
    private Model_Send_Message message;
    private int fileID;
    private String fileName;
    private String fileExtensions;
    private File file;
    private long fileSize;
    private RandomAccessFile accFile;
    private Socket socket;
    private EventFileSender event;
    
    
    
}
