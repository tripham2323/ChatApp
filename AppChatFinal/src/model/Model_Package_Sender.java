/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import app.MessageType;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
public class Model_Package_Sender {

    /**
     * @return the fromUserID
     */
    public int getFromUserID() {
        return fromUserID;
    }

    /**
     * @param fromUserID the fromUserID to set
     */
    public void setFromUserID(int fromUserID) {
        this.fromUserID = fromUserID;
    }


    public Model_Package_Sender(int fileID, byte[] data, boolean finish) {
        this.fileID = fileID;
        this.data = data;
        this.finish = finish;
    }
    public Model_Package_Sender(){
    
    }
    public Model_Package_Sender(Object json){
        JSONObject obj = (JSONObject) json;
        try {
            fileID = obj.getInt("fileID");
            if(!obj.isNull("fileName")){
               fileName = obj.getString("fileName");
            }
            data = (byte[]) obj.get("data");
            finish = obj.getBoolean("finish");
 
        } catch (JSONException e) {
            System.err.println(e);
        }
    
    }
    
    public JSONObject toJsonObject(){
        try {
            JSONObject json = new JSONObject();
           
            json.put("fileID", fileID);
            json.put("fromUserID", fromUserID);
            json.put("fileName", fileName);         
            json.put("data", data);
            json.put("finish", finish);           
            return json;
        } catch (JSONException e) {
            return null;
        }
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
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the finish
     */
    public boolean isFinish() {
        return finish;
    }

    /**
     * @param finish the finish to set
     */
    public void setFinish(boolean finish) {
        this.finish = finish;
    }
 
    private int fileID;
    private int fromUserID;
    private String fileName;
    private byte[] data;
    private boolean finish;
    
}
