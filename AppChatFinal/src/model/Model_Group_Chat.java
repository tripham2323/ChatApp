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
    }    // Constructor for creating from server response
    public Model_Group_Chat(Object serverData) {
        System.out.println("=== MODEL_GROUP_CHAT CONSTRUCTOR ===");
        System.out.println("Server data type: " + (serverData != null ? serverData.getClass().getName() : "null"));
        System.out.println("Server data: " + serverData);
        
        if (serverData instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) serverData;
            
            System.out.println("=== PARSING MAP DATA ===");
            System.out.println("Available keys: " + data.keySet());
            for (String key : data.keySet()) {
                System.out.println("Key: " + key + " = " + data.get(key) + " (" + 
                                 (data.get(key) != null ? data.get(key).getClass().getSimpleName() : "null") + ")");
            }
            
            try {
                Object groupIDObj = data.get("groupID");
                System.out.println("Raw groupID object: " + groupIDObj + " (type: " + 
                                 (groupIDObj != null ? groupIDObj.getClass().getName() : "null") + ")");
                
                if (groupIDObj != null) {
                    if (groupIDObj instanceof Number) {
                        this.groupID = ((Number) groupIDObj).intValue();
                    } else if (groupIDObj instanceof String) {
                        this.groupID = Integer.parseInt((String) groupIDObj);
                    } else {
                        this.groupID = Integer.parseInt(groupIDObj.toString());
                    }
                    System.out.println("Set groupID: " + this.groupID);
                } else {
                    System.out.println("groupID is null in data!");
                }
                
                this.groupName = (String) data.get("groupName");
                System.out.println("Set groupName: " + this.groupName);
                
                this.groupDescription = (String) data.get("groupDescription");
                
                // Handle group image - ENHANCED DEBUG
                Object groupImageObj = data.get("groupImage");
                System.out.println("=== PARSING GROUP IMAGE ===");
                System.out.println("GroupImage object: " + groupImageObj);
                System.out.println("GroupImage type: " + (groupImageObj != null ? groupImageObj.getClass().getName() : "null"));
                
                if (groupImageObj != null) {
                    if (groupImageObj instanceof byte[]) {
                        this.groupImage = (byte[]) groupImageObj;
                        System.out.println("✅ Set groupImage directly from byte[], size: " + this.groupImage.length);
                    } else if (groupImageObj instanceof java.util.List) {
                        // Convert List<Integer> back to byte[]
                        @SuppressWarnings("unchecked")
                        java.util.List<?> rawList = (java.util.List<?>) groupImageObj;
                        System.out.println("Converting from List, raw size: " + rawList.size());
                        
                        if (rawList.size() > 0) {
                            this.groupImage = new byte[rawList.size()];
                            for (int i = 0; i < rawList.size(); i++) {
                                Object item = rawList.get(i);
                                if (item instanceof Number) {
                                    this.groupImage[i] = ((Number) item).byteValue();
                                } else if (item instanceof String) {
                                    this.groupImage[i] = (byte) Integer.parseInt((String) item);
                                } else {
                                    this.groupImage[i] = 0;
                                    System.out.println("⚠️ Unknown item type at index " + i + ": " + (item != null ? item.getClass().getName() : "null"));
                                }
                            }
                            System.out.println("✅ Successfully converted groupImage from List to byte[], size: " + this.groupImage.length);
                            // Verify first few bytes
                            if (this.groupImage.length >= 4) {
                                System.out.println("First 4 bytes: " + 
                                    (this.groupImage[0] & 0xFF) + ", " + 
                                    (this.groupImage[1] & 0xFF) + ", " + 
                                    (this.groupImage[2] & 0xFF) + ", " + 
                                    (this.groupImage[3] & 0xFF));
                            }
                        } else {
                            this.groupImage = null;
                            System.out.println("❌ List is empty, setting groupImage to null");
                        }
                    } else {
                        System.out.println("❌ groupImage is unknown type: " + groupImageObj.getClass().getName());
                        System.out.println("String representation: " + groupImageObj.toString());
                        this.groupImage = null;
                    }
                } else {
                    this.groupImage = null;
                    System.out.println("❌ groupImage key is null in server data");
                }
                
                Object createdByObj = data.get("createdBy");
                System.out.println("Raw createdBy object: " + createdByObj + " (type: " + 
                                 (createdByObj != null ? createdByObj.getClass().getName() : "null") + ")");
                
                if (createdByObj != null) {
                    if (createdByObj instanceof Number) {
                        this.createdBy = ((Number) createdByObj).intValue();
                    } else if (createdByObj instanceof String) {
                        this.createdBy = Integer.parseInt((String) createdByObj);
                    } else {
                        this.createdBy = Integer.parseInt(createdByObj.toString());
                    }
                    System.out.println("Set createdBy: " + this.createdBy);
                } else {
                    System.out.println("createdBy is null in data!");
                }
                
                this.lastMessage = (String) data.get("lastMessage");
                this.status = (String) data.get("status");
                
                Object unreadCountObj = data.get("unreadCount");
                if (unreadCountObj != null) {
                    if (unreadCountObj instanceof Number) {
                        this.unreadCount = ((Number) unreadCountObj).intValue();
                    } else {
                        this.unreadCount = Integer.parseInt(unreadCountObj.toString());
                    }
                }
                
                // Parse timestamps if they exist
                if (data.get("createdAt") != null) {
                    String createdAtStr = data.get("createdAt").toString();
                    try {
                        this.createdAt = Timestamp.valueOf(createdAtStr);
                    } catch (Exception e) {
                        System.out.println("Error parsing createdAt: " + e.getMessage());
                    }
                }
                
                if (data.get("lastMessageTime") != null) {
                    String lastMessageTimeStr = data.get("lastMessageTime").toString();
                    try {
                        this.lastMessageTime = Timestamp.valueOf(lastMessageTimeStr);
                    } catch (Exception e) {
                        System.out.println("Error parsing lastMessageTime: " + e.getMessage());
                    }
                }
                
                System.out.println("=== FINAL PARSED VALUES ===");
                System.out.println("Final groupID: " + this.groupID);
                System.out.println("Final groupName: " + this.groupName);
                System.out.println("Final createdBy: " + this.createdBy);
                
            } catch (Exception e) {
                System.out.println("Error parsing group data: " + e.getMessage());
                e.printStackTrace();
            }        } else if (serverData.getClass().getName().contains("JSONObject")) {
            System.out.println("=== PARSING JSON OBJECT DATA ===");
            
            try {
                // Use reflection to access JSONObject methods
                Object jsonData = serverData;
                Class<?> jsonClass = jsonData.getClass();
                
                // Get methods
                java.lang.reflect.Method hasMethod = jsonClass.getMethod("has", String.class);
                java.lang.reflect.Method isNullMethod = jsonClass.getMethod("isNull", String.class);
                java.lang.reflect.Method getIntMethod = jsonClass.getMethod("getInt", String.class);
                java.lang.reflect.Method getStringMethod = jsonClass.getMethod("getString", String.class);
                java.lang.reflect.Method keysMethod = jsonClass.getMethod("keys");
                
                Object keys = keysMethod.invoke(jsonData);
                System.out.println("Available keys: " + keys);
                
                // Parse groupID
                if ((Boolean) hasMethod.invoke(jsonData, "groupID") && !(Boolean) isNullMethod.invoke(jsonData, "groupID")) {
                    this.groupID = (Integer) getIntMethod.invoke(jsonData, "groupID");
                    System.out.println("Set groupID from JSON: " + this.groupID);
                } else {
                    System.out.println("groupID not found in JSON data!");
                }
                
                // Parse groupName
                if ((Boolean) hasMethod.invoke(jsonData, "groupName") && !(Boolean) isNullMethod.invoke(jsonData, "groupName")) {
                    this.groupName = (String) getStringMethod.invoke(jsonData, "groupName");
                    System.out.println("Set groupName from JSON: " + this.groupName);
                }
                
                // Parse groupDescription
                if ((Boolean) hasMethod.invoke(jsonData, "groupDescription") && !(Boolean) isNullMethod.invoke(jsonData, "groupDescription")) {
                    this.groupDescription = (String) getStringMethod.invoke(jsonData, "groupDescription");
                }
                
                // Handle group image - PARSE FROM JSON
                if ((Boolean) hasMethod.invoke(jsonData, "groupImage") && !(Boolean) isNullMethod.invoke(jsonData, "groupImage")) {
                    try {
                        // Get the groupImage as JSONArray
                        java.lang.reflect.Method getMethod = jsonClass.getMethod("get", String.class);
                        Object groupImageObj = getMethod.invoke(jsonData, "groupImage");
                        
                        System.out.println("=== PARSING GROUP IMAGE FROM JSON ===");
                        System.out.println("GroupImage object: " + groupImageObj);
                        System.out.println("GroupImage type: " + (groupImageObj != null ? groupImageObj.getClass().getName() : "null"));
                        
                        if (groupImageObj != null && groupImageObj.getClass().getName().contains("JSONArray")) {
                            // Parse JSONArray to byte[]
                            Class<?> jsonArrayClass = groupImageObj.getClass();
                            java.lang.reflect.Method lengthMethod = jsonArrayClass.getMethod("length");
                            java.lang.reflect.Method getIntAtMethod = jsonArrayClass.getMethod("getInt", int.class);
                            
                            int arrayLength = (Integer) lengthMethod.invoke(groupImageObj);
                            System.out.println("JSON Array length: " + arrayLength);
                            
                            if (arrayLength > 0) {
                                this.groupImage = new byte[arrayLength];
                                for (int i = 0; i < arrayLength; i++) {
                                    int value = (Integer) getIntAtMethod.invoke(groupImageObj, i);
                                    this.groupImage[i] = (byte) value;
                                }
                                System.out.println("✅ Successfully parsed groupImage from JSON, size: " + this.groupImage.length);
                                
                                // Verify first few bytes
                                if (this.groupImage.length >= 4) {
                                    System.out.println("First 4 bytes: " + 
                                        (this.groupImage[0] & 0xFF) + ", " + 
                                        (this.groupImage[1] & 0xFF) + ", " + 
                                        (this.groupImage[2] & 0xFF) + ", " + 
                                        (this.groupImage[3] & 0xFF));
                                }
                            } else {
                                this.groupImage = null;
                                System.out.println("❌ JSON Array is empty, setting groupImage to null");
                            }
                        } else {
                            this.groupImage = null;
                            System.out.println("❌ groupImage is not a JSONArray or is null");
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Error parsing groupImage from JSON: " + e.getMessage());
                        e.printStackTrace();
                        this.groupImage = null;
                    }
                } else {
                    this.groupImage = null;
                    System.out.println("❌ groupImage not found in JSON data");
                }
                
                // Parse createdBy
                if ((Boolean) hasMethod.invoke(jsonData, "createdBy") && !(Boolean) isNullMethod.invoke(jsonData, "createdBy")) {
                    this.createdBy = (Integer) getIntMethod.invoke(jsonData, "createdBy");
                    System.out.println("Set createdBy from JSON: " + this.createdBy);
                } else {
                    System.out.println("createdBy not found in JSON data!");
                }
                
                // Parse other fields
                if ((Boolean) hasMethod.invoke(jsonData, "lastMessage") && !(Boolean) isNullMethod.invoke(jsonData, "lastMessage")) {
                    this.lastMessage = (String) getStringMethod.invoke(jsonData, "lastMessage");
                }
                
                if ((Boolean) hasMethod.invoke(jsonData, "status") && !(Boolean) isNullMethod.invoke(jsonData, "status")) {
                    this.status = (String) getStringMethod.invoke(jsonData, "status");
                }
                
                // Parse timestamps
                if ((Boolean) hasMethod.invoke(jsonData, "createdAt") && !(Boolean) isNullMethod.invoke(jsonData, "createdAt")) {
                    String createdAtStr = (String) getStringMethod.invoke(jsonData, "createdAt");
                    try {
                        this.createdAt = Timestamp.valueOf(createdAtStr);
                    } catch (Exception e) {
                        System.out.println("Error parsing createdAt from JSON: " + e.getMessage());
                    }
                }
                
                System.out.println("=== FINAL PARSED VALUES FROM JSON ===");
                System.out.println("Final groupID: " + this.groupID);
                System.out.println("Final groupName: " + this.groupName);
                System.out.println("Final createdBy: " + this.createdBy);
                
            } catch (Exception e) {
                System.out.println("Error parsing JSON data: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Server data is not a Map or JSONObject! Type: " + (serverData != null ? serverData.getClass().getName() : "null"));
        }
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
