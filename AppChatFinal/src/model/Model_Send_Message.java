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
public class Model_Send_Message {

 

    /**
     * @return the messageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @param messageType the messageType to set
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

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

    /**
     * @return the toUserID
     */
    public int getToUserID() {
        return toUserID;
    }

    /**
     * @param toUserID the toUserID to set
     */
    public void setToUserID(int toUserID) {
        this.toUserID = toUserID;
    }    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the file
     */
    public Model_File_Sender getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(Model_File_Sender file) {
        this.file = file;
    }
    
    private MessageType messageType;
    private int fromUserID;
    private int toUserID;
    private String text;
    private Model_File_Sender file;

    public Model_Send_Message(MessageType messageType, int fromUserID, int toUserID, String text) {
        this.messageType = messageType;
        this.fromUserID = fromUserID;
        this.toUserID = toUserID;
        this.text = text;
    }    public JSONObject toJSONObject(){
        try {
            JSONObject json = new JSONObject();
            json.put("messageType", messageType.getValue());
            json.put("fromUserID", fromUserID);
            json.put("toUserID", toUserID);
            if(messageType ==  MessageType.FILE||messageType == MessageType.IMAGE){
                if (file != null) {
                    json.put("text", file.getFileExtensions());
                } else {
                    json.put("text", ""); // Default empty text if file is null
                }
            }else if(messageType == MessageType.AVATAR){
                if (file != null) {
                    json.put("text", file.getFileName());
                } else {
                    json.put("text", ""); // Default empty text if file is null
                }
            }
            else{
                json.put("text", text);
            }
            return json;
            
        } catch (JSONException e) {
            System.err.println(e);
            return null;
        }
    
    }
    
}
