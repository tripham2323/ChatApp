package model;

import java.sql.Timestamp;

/**
 * Model for group messages with sender information
 */
public class Model_Group_Message {
    private int messageID;
    private int groupID;
    private int senderID;
    private String messageType;
    private String messageContent;
    private Timestamp sentAt;
    private String senderName;
    private byte[] senderImage;
    
    public Model_Group_Message() {
    }
    
    public Model_Group_Message(int messageID, int groupID, int senderID, String messageType, 
                              String messageContent, Timestamp sentAt, String senderName, byte[] senderImage) {
        this.messageID = messageID;
        this.groupID = groupID;
        this.senderID = senderID;
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.sentAt = sentAt;
        this.senderName = senderName;
        this.senderImage = senderImage;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public byte[] getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(byte[] senderImage) {
        this.senderImage = senderImage;
    }
}
