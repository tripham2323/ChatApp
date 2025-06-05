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
public class Model_Receive_Message {

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
     * @return the dataImage
     */
    public Model_Receive_Image getDataImage() {
        return dataImage;
    }

    /**
     * @param dataImage the dataImage to set
     */
    public void setDataImage(Model_Receive_Image dataImage) {
        this.dataImage = dataImage;
    }



    public Model_Receive_Message(MessageType messageType, int fromUserID, String text) {
        this.messageType = messageType;
        this.fromUserID = fromUserID;
        this.text = text;
    }
    public Model_Receive_Message(Object json){
        JSONObject obj = (JSONObject) json;
        try {
            messageType = MessageType.toMessageType(obj.getInt("messageType"));
            fromUserID = obj.getInt("fromUserID");
            text = obj.getString("text");
            if(!obj.isNull("dataImage")){
                dataImage = new Model_Receive_Image(obj.get("dataImage"));
            
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }
    
    private MessageType messageType;
    private int fromUserID;
    private String text;
    private Model_Receive_Image dataImage;

    public JSONObject toJSONObject(){
        try {
            JSONObject json = new JSONObject();
            json.put("messageType", getMessageType().getValue());
            json.put("fromUserID", getFromUserID());
            json.put("text", getText());
            if(dataImage != null){
               json.put("dataImage", dataImage.ToJsonObject());
            }
            return json;
            
        } catch (JSONException e) {
            System.err.println(e);
            return null;
        }
    
    }
    
}
