/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;



/**
 *
 * @author Admin
 */
public class Model_Receive_Message {

    public Model_Receive_Message(int MessageType, int fromUserID, String text, Model_Receive_Image dataImage) {
        this.MessageType = MessageType;
        this.fromUserID = fromUserID;
        this.text = text;
        this.dataImage = dataImage;
    }

    /**
     * @return the MessageType
     */
    public int getMessageType() {
        return MessageType;
    }

    /**
     * @param MessageType the MessageType to set
     */
    public void setMessageType(int MessageType) {
        this.MessageType = MessageType;
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

   

   
    public Model_Receive_Message(){
        
    }
    
    private int MessageType;
    private int fromUserID;
    private String text;
    private Model_Receive_Image dataImage;


    
}
