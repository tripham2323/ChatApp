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
import org.json.JSONObject;
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
    }    public void initSend() throws IOException {
        System.out.println("*** FILE_SENDER: initSend() called ***");
        System.out.println("Message: " + message.getText());
        System.out.println("Message Type: " + message.getMessageType());
        System.out.println("From User: " + message.getFromUserID());
        System.out.println("To User: " + message.getToUserID());
        System.out.println("File extensions: " + fileExtensions);
        
        // Ensure the message has this file sender set
        if (message.getFile() == null) {
            System.out.println("*** WARNING: Message file is null, setting it ***");
            message.setFile(this);
        }
        
        JSONObject messageJson = message.toJSONObject();
        System.out.println("*** MESSAGE JSON: " + messageJson.toString() + " ***");
        
        socket.emit("send_to_user", messageJson, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("*** FILE_SENDER: initSend callback received ***");
                System.out.println("Callback args length: " + os.length);
                if (os.length > 0){
                   System.out.println("Response object type: " + os[0].getClass().getName());
                   System.out.println("Response value: " + os[0]);
                   
                   // Handle both Boolean (error) and Integer (success) responses
                   if (os[0] instanceof Boolean) {
                       boolean success = (Boolean) os[0];
                       if (success) {
                           System.out.println("*** FILE_SENDER: Server returned success but no file ID ***");
                       } else {
                           System.err.println("*** FILE_SENDER: Server returned error! ***");
                       }
                   } else if (os[0] instanceof Integer) {
                       try {
                           int fileID = (Integer) os[0]; //nhận id file từ server
                           System.out.println("*** FILE_SENDER: Received file ID: " + fileID + " ***");
                           startSend(fileID);
                       } catch (IOException e) {
                           System.err.println("Error in startSend: " + e.getMessage());
                           e.printStackTrace();
                       }
                   } else {
                       System.err.println("*** FILE_SENDER: Unexpected response type: " + os[0].getClass().getName() + " ***");
                   }
                } else {
                    System.err.println("*** FILE_SENDER: No callback data received! ***");
                }
                
                }
        });
       
    }    public void startSend(int fileID) throws IOException {
        
        this.fileID = fileID;
        if(event != null){
           event.onStartSending();
        }
        sendingFile();
    }
    
    private void sendingFile() throws IOException {
        System.out.println("*** FILE_SENDER: sendingFile() called ***");
        System.out.println("FileID: " + fileID);
        System.out.println("File pointer: " + accFile.getFilePointer() + "/" + fileSize);
        
        Model_Package_Sender data = new Model_Package_Sender();
        data.setFileID(fileID);
        data.setFromUserID(Service.getInstance().getUser().getUserID());
        data.setFileName(fileName);
        byte[] bytes = readFile(); //đọc file, trả về biến data chứa dữ liệu đọc từ file đã chọn theo type byte[]
        if (bytes != null){
           System.out.println("*** FILE_SENDER: Read " + bytes.length + " bytes ***");
           data.setData(bytes);
           data.setFinish(false);
        }else{
           System.out.println("*** FILE_SENDER: File reading finished ***");
           data.setFinish(true);
           close();
        
        }
        socket.emit("send_file", data.toJsonObject(), new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("*** FILE_SENDER: send_file callback received ***");
                System.out.println("Callback args length: " + os.length);
                
                try {
                    if(os.length > 0){
                        boolean act = (boolean) os[0];
                        System.out.println("Server response: " + act);
                        if (act) {
                            try {
                                if(!data.isFinish()){
                                     System.out.println("*** FILE_SENDER: Continuing to send next chunk ***");
                                     if(event != null){
                                        event.onSending(getPercentage());
                                     }
                                   sendingFile();   
                                }else{
                                  //send file finish
                                   System.out.println("*** FILE_SENDER: File sending completed ***");
                                   Service.getInstance().fileSendFinish(Model_File_Sender.this);
                                    if(event != null){
                                        event.onFinish();
                                     }
                                }
                            } catch (IOException e) {
                               System.err.println("Error in sendingFile callback: " + e.getMessage());
                               e.printStackTrace();
                            }
                        } else {
                            System.err.println("*** FILE_SENDER: Server rejected the file chunk! ***");
                        }
                    } else {
                        System.err.println("*** FILE_SENDER: No callback data from send_file! ***");
                    }
                } catch (Exception e) {
                    System.err.println("*** FILE_SENDER: Error in callback processing: " + e.getMessage());
                    e.printStackTrace();
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
