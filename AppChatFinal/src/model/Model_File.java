/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import app.MessageType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
public class Model_File {

    public Model_File(int fileID, String fileExtensions) {
        this.fileID = fileID;
        this.fileExtensions = fileExtensions;
    }
    public Model_File(Object json){
         JSONObject obj = (JSONObject) json;
        try {
            fileID = obj.getInt("fileID");
            fileExtensions = obj.getString("fileExtensions");
        } catch (JSONException e) {
            System.err.println(e);
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
    private int fileID;
    private String fileExtensions;
    
}
