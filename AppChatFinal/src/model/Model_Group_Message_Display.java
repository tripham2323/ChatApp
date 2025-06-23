package model;

import java.util.Date;

/**
 * Model for displaying group messages on client side
 */
public class Model_Group_Message_Display {
    private int messageID;
    private int groupID;
    private int senderID;
    private String messageType;
    private String messageContent;
    private Date sentAt;
    private String senderName;
    private byte[] senderAvatar;
    
    public Model_Group_Message_Display() {
    }
    
    public Model_Group_Message_Display(int messageID, int groupID, int senderID, String messageType, 
                                     String messageContent, Date sentAt, String senderName, byte[] senderAvatar) {
        this.messageID = messageID;
        this.groupID = groupID;
        this.senderID = senderID;
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.sentAt = sentAt;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
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

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public byte[] getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(byte[] senderAvatar) {
        this.senderAvatar = senderAvatar;
    }
}
