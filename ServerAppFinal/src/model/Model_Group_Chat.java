/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;
import java.util.List;

/**
 *
 * @author Admin
 */
public class Model_Group_Chat {
    private int groupID;
    private String groupName;
    private String groupDescription;
    private byte[] groupImage;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String status;
    private List<Model_User_Account> members;
    private String lastMessage;
    private Timestamp lastMessageTime;
    private int unreadCount;

    public Model_Group_Chat() {
    }

    public Model_Group_Chat(int groupID, String groupName, String groupDescription, byte[] groupImage, 
                           int createdBy, Timestamp createdAt, String status) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupImage = groupImage;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public byte[] getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(byte[] groupImage) {
        this.groupImage = groupImage;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Model_User_Account> getMembers() {
        return members;
    }

    public void setMembers(List<Model_User_Account> members) {
        this.members = members;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Timestamp lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
