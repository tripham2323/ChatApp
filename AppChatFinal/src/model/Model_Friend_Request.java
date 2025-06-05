package model;

import java.sql.Timestamp;

/**
 * Model for friend request data
 */
public class Model_Friend_Request {
    private int requestID;
    private int fromUserID;
    private int toUserID;
    private String status;
    private Timestamp createdAt;
    private Model_User_Account senderInfo;

    public Model_Friend_Request() {
    }

    public Model_Friend_Request(int requestID, int fromUserID, int toUserID, String status, Timestamp createdAt) {
        this.requestID = requestID;
        this.fromUserID = fromUserID;
        this.toUserID = toUserID;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public int getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(int fromUserID) {
        this.fromUserID = fromUserID;
    }

    public int getToUserID() {
        return toUserID;
    }

    public void setToUserID(int toUserID) {
        this.toUserID = toUserID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Model_User_Account getSenderInfo() {
        return senderInfo;
    }

    public void setSenderInfo(Model_User_Account senderInfo) {
        this.senderInfo = senderInfo;
    }
}
