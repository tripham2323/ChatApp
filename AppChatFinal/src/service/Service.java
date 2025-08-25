/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import app.MessageType;
import event.PublicEvent;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import model.Model_File;
import model.Model_File_Sender;
import model.Model_Group_Message_Display;
import model.Model_Package_Sender;
import model.Model_Receive_Message;
import model.Model_Send_Message;
import model.Model_User_Account;
import model.Model_Voice_Receive;
import model.Model_Friend_Request;
import model.Model_Group_Chat;
import org.json.JSONObject;
import org.json.JSONException;
import swing.Recoder;

public class Service {

    private static Service instance;    private Socket client;
    private final int PORT_NUMBER = 9999;
//    private final String IP = "192.168.224.134"; //ip nối đến máy ảo 
    private final String IP = "localhost"; 
    private Model_User_Account user;
    private List<Model_File_Sender> fileSender;
    private ServiceFile serviceFile;
    private Recoder recoder;
    private boolean hasPendingFriendRequestNotification = false;


    public static Service getInstance() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    private Service() {
       fileSender = new ArrayList<>();
       serviceFile = new ServiceFile();
       recoder = new Recoder();
    }

//    public void startServer() {
//            //xóa file cache trong folder client_data
//             File f = new File("client_data");
//        for (File fs : f.listFiles()) {
//            fs.delete();
//        }
        
           public void startServer(){
         File f = new File("client_data");
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File fs : files) {
                    fs.delete();
                }
            }
        }
        
        try {
            client = IO.socket("http://" + IP + ":" + PORT_NUMBER);//kết nối đến server với ip và cổng đã khai báo
            client.on("list_user", new Emitter.Listener() {//bắt đầu chạy program thì gửi request : list_user đến server và chờ phản hồi từ server
                @Override
                public void call(Object... os) {
                    //list user nhận từ server
                    List<Model_User_Account> users = new ArrayList<>();
                    for (Object o : os){
                        Model_User_Account u = new Model_User_Account(o);
                        if (u.getUserID() != user.getUserID()) {// lấy all list info client khác đang có trong db gán vào array list trừ info của client hiện tại
                            users.add(u);
                        }
                       
                    }
                    PublicEvent.getInstance().getEventMenuLeft().newUser(users);
                    
                   }
            });
          
            client.on("user_status", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
               
                    boolean status = (Boolean) os[1];
                    if(status){
                          //connect
                          Model_User_Account user = new Model_User_Account(os[0]);
                          PublicEvent.getInstance().getEventMenuLeft().userConnect(user);
                    }else{
                        //disconnect
                        int userID = (Integer) os[0];
                        PublicEvent.getInstance().getEventMenuLeft().userDisconnect(userID);
                    
                    }
                    }
            });
            client.on("receive_data", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    Model_File dataFile = new Model_File(os[0]);
                    try {
                        serviceFile.initFile(dataFile);
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    }
            });
              client.on("receive_ms", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    System.out.println("=== CLIENT RECEIVED MESSAGE EVENT ===");
                    System.out.println("Received " + os.length + " objects");
                    if (os.length > 0) {
                        System.out.println("Message object: " + os[0]);
                        System.out.println("Object type: " + os[0].getClass().getName());
                    }
                    
                    Model_Receive_Message message = new Model_Receive_Message(os[0]);
                    System.out.println("Parsed message - From: " + message.getFromUserID() + 
                                     ", Type: " + message.getMessageType() + 
                                     ", Text: " + message.getText());
           
                    if (message.getMessageType().getValue() == MessageType.IMAGE.getValue()) {
                        try {
                       File file =  serviceFile.receiveFile(message.getDataImage());
                       PublicEvent.getInstance().getEventChat().receiveMessage(message, file);
                       
                    } catch (IOException ex) {
                        Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    }else if(message.getMessageType().getValue() == MessageType.FILE.getValue()){
                        PublicEvent.getInstance().getEventChat().receiveMessage(message, null);
                    }
                    else{
                    
                        PublicEvent.getInstance().getEventChat().receiveMessage(message, null);
                    }
                  }
                    
            });            client.on("receive_voice", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    Model_Voice_Receive voice = new Model_Voice_Receive(os[0]);
                    PublicEvent.getInstance().getEventChat().receiveMessage(voice);
                    }
            });            client.on("GetFile", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    Model_Package_Sender data = new Model_Package_Sender(os[0]);
                    saveFileIntoFolder(data);
                }
            });            // Event listener for friend request notifications
            client.on("friend_request_received", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    try {
                        System.out.println("=== FRIEND REQUEST NOTIFICATION RECEIVED ===");
                        Model_User_Account senderInfo = new Model_User_Account(os[0]);
                        System.out.println("Received friend request from: " + senderInfo.getUserName());
                        
                        // Trigger UI refresh for friend requests with retry mechanism
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("=== TRIGGERING UI REFRESH FOR FRIEND REQUEST ===");
                            triggerFriendRequestRefreshWithRetry(0);
                        });
                    } catch (Exception e) {
                        System.err.println("Error handling friend request notification: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            
            // Event listener for group messages with avatar info
            client.on("receive_group_message", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    System.out.println("=== CLIENT RECEIVED GROUP MESSAGE EVENT ===");
                    System.out.println("Received " + os.length + " objects");
                    if (os.length > 0) {
                        System.out.println("Group message object: " + os[0]);
                        System.out.println("Object type: " + os[0].getClass().getName());
                          try {
                            // Parse group message data - handle both Map and JSONObject
                            if (os[0] instanceof java.util.Map) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> messageData = (java.util.Map<String, Object>) os[0];
                                processGroupMessageData(messageData);
                            } else if (os[0].getClass().getName().contains("JSONObject")) {
                                // Handle JSONObject using reflection to avoid import
                                try {
                                    Object jsonObj = os[0];
                                    Class<?> jsonClass = jsonObj.getClass();
                                    
                                    // Extract data using reflection
                                    int messageType = (Integer) jsonClass.getMethod("getInt", String.class).invoke(jsonObj, "messageType");
                                    int fromUserID = (Integer) jsonClass.getMethod("getInt", String.class).invoke(jsonObj, "fromUserID");
                                    String text = (String) jsonClass.getMethod("getString", String.class).invoke(jsonObj, "text");
                                    boolean isGroupMessage = (Boolean) jsonClass.getMethod("getBoolean", String.class).invoke(jsonObj, "isGroupMessage");
                                    int groupID = (Integer) jsonClass.getMethod("getInt", String.class).invoke(jsonObj, "groupID");
                                    String senderName = (String) jsonClass.getMethod("getString", String.class).invoke(jsonObj, "senderName");
                                    
                                    // Handle avatar array
                                    byte[] senderAvatar = null;
                                    try {
                                        Object avatarArray = jsonClass.getMethod("get", String.class).invoke(jsonObj, "senderAvatar");
                                        if (avatarArray != null && avatarArray.getClass().isArray()) {
                                            senderAvatar = (byte[]) avatarArray;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Could not extract avatar: " + e.getMessage());
                                    }
                                    
                                    System.out.println("=== PARSING JSON GROUP MESSAGE DATA ===");
                                    System.out.println("Group message details:");
                                    System.out.println("  MessageType: " + messageType);
                                    System.out.println("  FromUserID: " + fromUserID);
                                    System.out.println("  Text: " + text);
                                    System.out.println("  GroupID: " + groupID);
                                    System.out.println("  SenderName: " + senderName);
                                    System.out.println("  SenderAvatar: " + (senderAvatar != null ? senderAvatar.length + " bytes" : "null"));
                                    
                                    // Create Model_Receive_Message with sender info
                                    MessageType msgType = MessageType.toMessageType(messageType);
                                    Model_Receive_Message message = new Model_Receive_Message(msgType, fromUserID, text);
                                    
                                    // Create a fake user account for sender info (for avatar display)
                                    Model_User_Account senderInfo = new Model_User_Account();
                                    senderInfo.setUserID(fromUserID);
                                    senderInfo.setUserName(senderName);
                                    senderInfo.setImage(senderAvatar);
                                    
                                    // Pass the message to the chat UI with sender info
                                    PublicEvent.getInstance().getEventChat().receiveGroupMessage(message, senderInfo, groupID);
                                    
                                } catch (Exception e) {
                                    System.err.println("Error parsing JSONObject group message: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("Group message data is unsupported type: " + os[0].getClass().getName());
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing group message: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            });
            
            client.on("group_avatar_updated", new Emitter.Listener() {
                @Override
                public void call(Object... os) {
                    System.out.println("=== RECEIVED GROUP AVATAR UPDATED NOTIFICATION ===");
                    if (os.length > 0) {
                        try {
                            System.out.println("Notification data type: " + os[0].getClass().getName());
                            System.out.println("Notification data: " + os[0]);
                            
                            // Handle both JSONObject and Map
                            java.util.Map<String, Object> data = null;
                            
                            if (os[0] instanceof org.json.JSONObject) {
                                org.json.JSONObject jsonObj = (org.json.JSONObject) os[0];
                                data = new java.util.HashMap<>();
                                
                                // Convert JSONObject to Map
                                java.util.Iterator<String> keys = jsonObj.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    data.put(key, jsonObj.get(key));
                                }
                            } else if (os[0] instanceof java.util.Map) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> mapData = (java.util.Map<String, Object>) os[0];
                                data = mapData;
                            }
                            
                            if (data != null) {
                                Object groupIDObj = data.get("groupID");
                                Object avatarDataObj = data.get("avatarData");
                                
                                final int groupID = (groupIDObj instanceof Number) ? 
                                    ((Number) groupIDObj).intValue() : 
                                    Integer.parseInt(groupIDObj.toString());
                                
                                final byte[] newAvatar;
                                if (avatarDataObj instanceof java.util.List) {
                                    // Convert List<Integer> to byte[]
                                    @SuppressWarnings("unchecked")
                                    java.util.List<Integer> avatarList = (java.util.List<Integer>) avatarDataObj;
                                    newAvatar = new byte[avatarList.size()];
                                    for (int i = 0; i < avatarList.size(); i++) {
                                        newAvatar[i] = avatarList.get(i).byteValue();
                                    }
                                } else {
                                    newAvatar = null;
                                }
                                
                                System.out.println("Group " + groupID + " avatar updated, size: " + 
                                                 (newAvatar != null ? newAvatar.length : 0) + " bytes");
                                
                                // Update local group data and refresh UI
                                SwingUtilities.invokeLater(() -> {
                                    PublicEvent.getInstance().getEventMain().updateGroupAvatar(groupID, newAvatar);
                                    PublicEvent.getInstance().getEventMain().refreshGroupsList();
                                });
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing group avatar update: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            });
            
            client.open();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
      // Method to process group message data from Map
    private void processGroupMessageData(java.util.Map<String, Object> messageData) {
        try {
            System.out.println("Processing group message data: " + messageData);
            
            // Extract message data
            int messageType = (Integer) messageData.get("messageType");
            int fromUserID = (Integer) messageData.get("fromUserID");
            String text = (String) messageData.get("text");
            boolean isGroupMessage = (Boolean) messageData.get("isGroupMessage");
            int groupID = (Integer) messageData.get("groupID");
            
            // Extract sender info
            String senderName = (String) messageData.get("senderName");
            String senderAvatar = (String) messageData.get("senderAvatar");
            
            // Create sender info
            Model_User_Account senderInfo = new Model_User_Account();
            senderInfo.setUserID(fromUserID);
            senderInfo.setUserName(senderName != null ? senderName : "Unknown");
            // Skip setting image for now since it expects byte[] but we have String
            
            // Create message object
            Model_Receive_Message message = new Model_Receive_Message(MessageType.values()[messageType], fromUserID, text);
            
            // Pass the message to the chat UI with sender info
            PublicEvent.getInstance().getEventChat().receiveGroupMessage(message, senderInfo, groupID);
            
        } catch (Exception e) {
            System.err.println("Error processing group message data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Model_File_Sender addFile(File file, Model_Send_Message message) throws IOException {
        // Check if client is connected
        if (client == null || !client.connected()) {
            System.err.println("*** CLIENT NOT CONNECTED! Cannot send file ***");
            throw new IOException("Client is not connected to server");
        }
        
        System.out.println("*** FILE_SENDER: Adding file: " + file.getName() + " ***");
        System.out.println("*** CLIENT CONNECTION STATUS: " + client.connected() + " ***");
        
        Model_File_Sender data = new Model_File_Sender(file, client, message);
        message.setFile(data);
        fileSender.add(data);
        // cho send file từng cái một
        if(fileSender.size() == 1){
           data.initSend();
        }
        return data;
    }
    public void fileSendFinish(Model_File_Sender data) throws IOException {
        fileSender.remove(data);
        if (!fileSender.isEmpty()){
            // bắt đầu gửi file mới khi file cũ đã gửi hoàn thành
            fileSender.get(0).initSend();
        }
    }
    private void saveFileIntoFolder(Model_Package_Sender data){
        try {
            File file = new File(data.getFileName());
            FileOutputStream out = new FileOutputStream(file);
            out.write(data.getData());
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }

    public Socket getClient() {
        return client;
    }
     public Model_User_Account getUser() {
        return user;
    }

    public void setUser(Model_User_Account user) {
        this.user = user;
    }    public Recoder getRecoder(){
        return recoder;
    }    
    
    public void setRecoder(Recoder aRecoder) {
        recoder = aRecoder;
    }    // Method to search users by username
    public void searchUsers(String searchText, component.FriendSearchPanel.SearchCallback callback) {
        System.out.println("Đang tìm kiếm người dùng: " + searchText);
        
        // Create request data with searchText and currentUserID
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put("searchText", searchText);
        requestData.put("currentUserID", user != null ? user.getUserID() : 0);
        
        client.emit("search_users", requestData, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("Nhận phản hồi từ server, số lượng tham số: " + os.length);
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    System.out.println("Kết quả tìm kiếm: success=" + success);
                    if (success && os.length > 1) {
                        // Parse the search results
                        List<Model_User_Account> users = new ArrayList<>();

                        try {
                            // Handle JSONArray using reflection to avoid import issues
                            if (os[1].getClass().getName().contains("JSONArray")) {
                                // Use reflection to handle JSONArray
                                Object jsonArray = os[1];
                                int length = (Integer) jsonArray.getClass().getMethod("length").invoke(jsonArray);
                                System.out.println("Số lượng kết quả: " + length);                                for (int i = 0; i < length; i++) {
                                    try {
                                        Object userObj = jsonArray.getClass().getMethod("get", int.class).invoke(jsonArray, i);
                                        Model_User_Account user = new Model_User_Account(userObj);
                                        
                                        // Skip current user from search results
                                        if (Service.this.user != null && user.getUserID() == Service.this.user.getUserID()) {
                                            System.out.println("Bỏ qua người dùng hiện tại: " + user.getUserName());
                                            continue;
                                        }
                                        
                                        System.out.println("Đã tìm thấy người dùng: " + user.getUserName());
                                        users.add(user);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.err.println("Lỗi phân tích dữ liệu người dùng: " + e.getMessage());
                                    }
                                }
                            } else if (os[1] instanceof Object[]) {
                                // Handle Object[] case (fallback)
                                Object[] results = (Object[]) os[1];
                                System.out.println("Số lượng kết quả (Object[]): " + results.length);                                for (Object o : results) {
                                    try {
                                        Model_User_Account user = new Model_User_Account(o);
                                        
                                        // Skip current user from search results
                                        if (Service.this.user != null && user.getUserID() == Service.this.user.getUserID()) {
                                            System.out.println("Bỏ qua người dùng hiện tại: " + user.getUserName());
                                            continue;
                                        }
                                        
                                        System.out.println("Đã tìm thấy người dùng: " + user.getUserName());
                                        users.add(user);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.err.println("Lỗi phân tích dữ liệu người dùng: " + e.getMessage());
                                    }
                                }
                            } else {
                                System.err.println("Kết quả không phải là mảng, loại thực tế: " + os[1].getClass().getName());
                            }
                        } catch (Exception e) {
                            System.err.println("Lỗi xử lý kết quả tìm kiếm: " + e.getMessage());
                            e.printStackTrace();
                        }

                        // Send search results to callback
                        callback.onResult(users);
                    } else if (!success && os.length > 1) {
                        // Error message returned from server
                        String errorMsg = os[1].toString();
                        System.err.println("Lỗi từ server: " + errorMsg);
                        callback.onError(errorMsg);
                    } else {
                        System.out.println("Không có kết quả");
                        callback.onResult(new ArrayList<>());
                    }
                } else {
                    System.err.println("Không nhận được phản hồi từ server");
                    callback.onError("No response from server");
                }
            }
        });
    }    // Method to send friend request
    public void sendFriendRequest(int toUserID, component.FriendSearchItem.FriendRequestCallback callback) {
        // Create friend request data using Map instead of JSONObject
        try {
            java.util.Map<String, Object> requestData = new java.util.HashMap<>();
            requestData.put("fromUserID", user.getUserID());
            requestData.put("toUserID", toUserID);
            
            client.emit("send_friend_request", requestData, new Ack() {
                @Override
                public void call(Object... os) {
                    if (os.length > 0) {
                        boolean success = (Boolean) os[0];
                        if (success) {
                            callback.onSuccess();
                        } else {
                            String error = os.length > 1 ? (String) os[1] : "Failed to send friend request";
                            callback.onError(error);
                        }
                    } else {
                        callback.onError("No response from server");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Error creating request: " + e.getMessage());
        }
    }        // Method to accept friend request
    public void acceptFriendRequest(int requestID, component.FriendRequestItem.FriendRequestCallback callback) {
        try {
            // Create a simple Map instead of JSONObject to avoid import issues
            java.util.Map<String, Object> requestData = new java.util.HashMap<>();
            requestData.put("requestID", requestID);
            
            client.emit("accept_friend_request", requestData, new Ack() {
                @Override
                public void call(Object... os) {
                    if (os.length > 0) {
                        boolean success = (Boolean) os[0];
                        if (success) {
                            callback.onSuccess();
                        } else {
                            String error = os.length > 1 ? (String) os[1] : "Failed to accept friend request";
                            callback.onError(error);
                        }
                    } else {
                        callback.onError("No response from server");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Error accepting request: " + e.getMessage());
        }
    }      // Method to reject friend request
    public void rejectFriendRequest(int requestID, component.FriendRequestItem.FriendRequestCallback callback) {
        try {
            // Create a simple Map instead of JSONObject to avoid import issues
            java.util.Map<String, Object> requestData = new java.util.HashMap<>();
            requestData.put("requestID", requestID);
            
            client.emit("reject_friend_request", requestData, new Ack() {
                @Override
                public void call(Object... os) {
                    if (os.length > 0) {
                        boolean success = (Boolean) os[0];
                        if (success) {
                            callback.onSuccess();
                        } else {
                            String error = os.length > 1 ? (String) os[1] : "Failed to reject friend request";
                            callback.onError(error);
                        }
                    } else {
                        callback.onError("No response from server");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Error rejecting request: " + e.getMessage());
        }
    }    // Method to get pending friend requests
    public void getFriendRequests(FriendRequestListCallback callback) {
        try {
            if (user == null) {
                System.err.println("ERROR: User is null when calling getFriendRequests!");
                callback.onError("User not logged in");
                return;
            }
            
            System.out.println("=== CLIENT REQUESTING FRIEND REQUESTS ===");
            System.out.println("Client requesting friend requests for UserID: " + user.getUserID());
            System.out.println("Current thread: " + Thread.currentThread().getName());
            
            client.emit("get_friend_requests", user.getUserID(), new Ack() {
                @Override
                public void call(Object... os) {
                    System.out.println("=== CLIENT RECEIVED RESPONSE ===");
                    System.out.println("Response thread: " + Thread.currentThread().getName());
                    System.out.println("Response data: " + java.util.Arrays.toString(os));
                    System.out.println("Response length: " + os.length);                        if (os.length >= 1 && (Boolean) os[0]) {
                        try {
                            List<Model_Friend_Request> requests = new ArrayList<>();
                              // Check if there's actual data (length > 1)
                            if (os.length > 1) {
                                for (int i = 1; i < os.length; i++) {                        Object requestData = os[i];
                        System.out.println("Processing request data: " + requestData);
                        System.out.println("Request data type: " + requestData.getClass().getName());
                        
                        java.util.Map<String, Object> requestMap = null;
                        
                        if (requestData instanceof java.util.Map) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> mapData = (java.util.Map<String, Object>) requestData;
                            requestMap = mapData;
                            System.out.println("Used Map directly");                        } else {
                            // Handle JSONObject manually
                            try {
                                System.out.println("Attempting to parse as JSONObject...");
                                // Convert JSONObject to Map manually
                                String jsonString = requestData.toString();
                                System.out.println("JSON String: " + jsonString);
                                
                                // Parse manually - this is a quick fix
                                requestMap = new java.util.HashMap<>();
                                
                                // Extract values manually from JSON string (temporary solution)
                                if (jsonString.contains("\"requestID\":")) {
                                    int reqId = extractIntFromJson(jsonString, "requestID");
                                    int fromId = extractIntFromJson(jsonString, "fromUserID");
                                    int toId = extractIntFromJson(jsonString, "toUserID");
                                    int senderId = extractIntFromJson(jsonString, "senderUserID");
                                    String senderName = extractStringFromJson(jsonString, "senderUserName");
                                    String senderGender = extractStringFromJson(jsonString, "senderGender");
                                    boolean senderStatus = extractBooleanFromJson(jsonString, "senderStatus");
                                    String createdAt = extractStringFromJson(jsonString, "createdAt");
                                    
                                    requestMap.put("requestID", reqId);
                                    requestMap.put("fromUserID", fromId);
                                    requestMap.put("toUserID", toId);
                                    requestMap.put("senderUserID", senderId);                                    requestMap.put("senderUserName", senderName);
                                    requestMap.put("senderGender", senderGender);
                                    requestMap.put("senderStatus", senderStatus);
                                    requestMap.put("createdAt", createdAt);
                                    requestMap.put("status", "pending");
                                    
                                    // Extract image data from JSON string
                                    byte[] senderImage = extractImageFromJson(jsonString, "senderImage");
                                    requestMap.put("senderImage", senderImage);
                                    
                                    System.out.println("Manual JSON parsing successful with image data: " + 
                                                     (senderImage != null ? senderImage.length : 0) + " bytes");
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to parse JSONObject manually: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        if (requestMap != null) {
                            try {
                                System.out.println("Parsing request map: " + requestMap.keySet());
                                            // Create Model_Friend_Request from map data
                                            Model_Friend_Request request = new Model_Friend_Request();
                                            request.setRequestID(((Number) requestMap.get("requestID")).intValue());
                                            request.setFromUserID(((Number) requestMap.get("fromUserID")).intValue());
                                            request.setToUserID(((Number) requestMap.get("toUserID")).intValue());
                                            request.setStatus((String) requestMap.get("status"));
                                            System.out.println("Basic request data set successfully");
                                            // Create sender info using the constructor with parameters
                                            int senderUserID = ((Number) requestMap.get("senderUserID")).intValue();
                                            String senderUserName = (String) requestMap.get("senderUserName");
                                            String senderGender = (String) requestMap.get("senderGender");
                                            System.out.println("Sender info: " + senderUserName + " (ID: " + senderUserID + ")");
                                            // Handle senderImage - it might come as List<Integer> from Socket.IO
                                            byte[] senderImage = null;
                                            Object imageData = requestMap.get("senderImage");
                                            if (imageData != null) {
                                                System.out.println("Processing image data: " + imageData.getClass().getSimpleName());
                                                if (imageData instanceof List) {
                                                    @SuppressWarnings("unchecked")
                                                    List<Number> imageList = (List<Number>) imageData;
                                                    senderImage = new byte[imageList.size()];
                                                    for (int j = 0; j < imageList.size(); j++) {
                                                        senderImage[j] = imageList.get(j).byteValue();
                                                    }
                                                    System.out.println("Converted List to byte array: " + senderImage.length + " bytes");
                                                } else if (imageData instanceof byte[]) {
                                                    senderImage = (byte[]) imageData;
                                                    System.out.println("Used existing byte array: " + senderImage.length + " bytes");
                                                } else {
                                                    System.out.println("Unknown image data type: " + imageData.getClass());
                                                }
                                            }
                                            Boolean senderStatus = (Boolean) requestMap.get("senderStatus");
                                            System.out.println("Creating Model_User_Account...");
                                            Model_User_Account senderInfo = new Model_User_Account(
                                                senderUserID, senderUserName, senderGender, senderImage, senderStatus
                                            );
                                            System.out.println("Model_User_Account created successfully");
                                            request.setSenderInfo(senderInfo);
                                            requests.add(request);
                                            System.out.println("Added friend request from: " + senderUserName + " (ID: " + request.getRequestID() + ")");
                                        } catch (Exception e) {
                                            System.err.println("Error parsing individual request: " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    } else {
                                        System.out.println("Request data is not a Map: " + requestData.getClass());
                                    }
                                }
                            } else {
                                System.out.println("No request data found in response");
                            }
                            
                            System.out.println("=== PARSING COMPLETED ===");
                            System.out.println("Total friend requests parsed: " + requests.size());
                            System.out.println("Request details:");
                            for (int i = 0; i < requests.size(); i++) {
                                Model_Friend_Request req = requests.get(i);
                                System.out.println("  [" + i + "] ID=" + req.getRequestID() + 
                                                 ", From=" + req.getFromUserID() + 
                                                 ", To=" + req.getToUserID() +
                                                 ", SenderName=" + (req.getSenderInfo() != null ? req.getSenderInfo().getUserName() : "null"));
                            }
                            
                            System.out.println("=== CALLING CALLBACK.ONRESULT ===");
                            System.out.println("About to call callback.onResult with " + requests.size() + " requests");
                            System.out.println("Callback thread: " + Thread.currentThread().getName());
                            
                            // Call callback immediately without additional SwingUtilities.invokeLater
                            callback.onResult(requests);
                            System.out.println("=== CALLBACK.ONRESULT COMPLETED ===");                        } catch (Exception e) {
                            System.err.println("=== PARSING ERROR ===");
                            System.err.println("Error parsing friend requests: " + e.getMessage());
                            e.printStackTrace();
                            callback.onError("Error parsing friend requests: " + e.getMessage());
                            System.out.println("=== ERROR CALLBACK COMPLETED ===");
                        }
                    } else {
                        System.out.println("=== SERVER RETURNED FALSE OR NO DATA ===");
                        // No friend requests or error
                        if (os.length > 0 && !(Boolean) os[0]) {
                            String errorMsg = os.length > 1 ? (String) os[1] : "Failed to get friend requests";
                            System.out.println("Server returned false, calling callback.onError: " + errorMsg);
                            callback.onError(errorMsg);
                        } else {
                            System.out.println("No friend requests found, calling callback.onResult with empty list");
                            callback.onResult(new ArrayList<>());
                        }
                        System.out.println("=== NO DATA CALLBACK COMPLETED ===");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Error getting friend requests: " + e.getMessage());
        }
    }      // Interface for getting friend requests with results
    public interface FriendRequestListCallback {
        void onResult(List<Model_Friend_Request> requests);
        void onError(String error);
    }
      // Method to trigger friend request refresh with retry mechanism
    private void triggerFriendRequestRefreshWithRetry(int retryCount) {
        if (PublicEvent.getInstance().getEventFriendRequest() != null) {
            System.out.println("Event handler found, calling onFriendRequestReceived()");
            PublicEvent.getInstance().getEventFriendRequest().onFriendRequestReceived();
        } else {
            System.err.println("No EventFriendRequest handler registered, retry attempt: " + (retryCount + 1));
            if (retryCount < 20) { // Retry up to 20 times (10 seconds total)
                // Wait 500ms and retry
                javax.swing.Timer timer = new javax.swing.Timer(500, e -> {
                    triggerFriendRequestRefreshWithRetry(retryCount + 1);
                });
                timer.setRepeats(false);
                timer.start();            } else {
                System.err.println("ERROR: EventFriendRequest handler not found after 20 retries!");
                System.err.println("Will try to manually trigger refresh when handler becomes available...");
                // Store notification for later processing
                hasPendingFriendRequestNotification = true;
            }
        }    }
    
    // Helper methods for manual JSON parsing
    private int extractIntFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            String value = json.substring(start, end).trim();
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private String extractStringFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }
    
    private boolean extractBooleanFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            String value = json.substring(start, end).trim();
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return false;
        }    }
    
    private byte[] extractImageFromJson(String json, String key) {
        try {
            String pattern = "\"" + key + "\":[";
            int start = json.indexOf(pattern);
            if (start == -1) {
                System.out.println("Image key not found in JSON");
                return new byte[0];
            }
            
            start += pattern.length();
            int end = json.indexOf("]", start);
            if (end == -1) {
                System.out.println("Image array end not found in JSON");
                return new byte[0];
            }
            
            String arrayContent = json.substring(start, end);
            if (arrayContent.trim().isEmpty()) {
                System.out.println("Image array is empty");
                return new byte[0];
            }
            
            // Parse comma-separated integer values
            String[] values = arrayContent.split(",");
            byte[] imageBytes = new byte[values.length];
            
            for (int i = 0; i < values.length; i++) {
                String value = values[i].trim();
                imageBytes[i] = (byte) Integer.parseInt(value);
            }
            
            System.out.println("Extracted image data: " + imageBytes.length + " bytes");
            return imageBytes;
            
        } catch (Exception e) {
            System.err.println("Error extracting image from JSON: " + e.getMessage());
            return new byte[0];
        }
    }
    
    // Method to check and process pending friend request notifications
    public void checkAndProcessPendingNotifications() {
        if (hasPendingFriendRequestNotification && PublicEvent.getInstance().getEventFriendRequest() != null) {
            System.out.println("=== PROCESSING PENDING FRIEND REQUEST NOTIFICATION ===");
            hasPendingFriendRequestNotification = false;
            PublicEvent.getInstance().getEventFriendRequest().onFriendRequestReceived();
        }
    }
    
    // Method to get friends list
    public void getFriendsList(FriendsListCallback callback) {
        try {
            if (user == null) {
                System.err.println("ERROR: User is null when calling getFriendsList!");
                callback.onError("User not logged in");
                return;
            }
            
            System.out.println("=== CLIENT REQUESTING FRIENDS LIST ===");
            System.out.println("Client requesting friends list for UserID: " + user.getUserID());
            
            client.emit("get_friends_list", user.getUserID(), new Ack() {
                @Override
                public void call(Object... os) {
                    System.out.println("=== CLIENT RECEIVED FRIENDS LIST RESPONSE ===");
                    System.out.println("Response data: " + java.util.Arrays.toString(os));
                    System.out.println("Response length: " + os.length);
                    
                    if (os.length >= 1 && (Boolean) os[0]) {
                        try {
                            List<Model_User_Account> friends = new ArrayList<>();
                              // Check if there's actual data (length > 1)
                            if (os.length > 1) {
                                for (int i = 1; i < os.length; i++) {
                                    Object friendData = os[i];
                                    System.out.println("Processing friend data: " + friendData);
                                    System.out.println("Friend data type: " + friendData.getClass().getName());
                                    
                                    Model_User_Account friend = null;
                                      // Try parsing as different data types
                                    if (friendData instanceof java.util.Map) {
                                        System.out.println("Parsing as Map");
                                        @SuppressWarnings("unchecked")
                                        java.util.Map<String, Object> friendMap = (java.util.Map<String, Object>) friendData;
                                        friend = parseUserFromMap(friendMap);                                    } else if (friendData instanceof JSONObject) {
                                        System.out.println("Parsing as JSONObject");
                                        JSONObject jsonObj = (JSONObject) friendData;
                                        friend = parseUserFromJSONObject(jsonObj);
                                    } else {
                                        System.out.println("Data is not a Map or JSONObject, trying to debug its structure");
                                        System.out.println("Friend data toString: " + friendData.toString());
                                        System.out.println("Friend data class: " + friendData.getClass().getName());
                                        
                                        // Skip this friend since we can't parse it
                                        System.out.println("Skipping this friend data");  
                                        continue;
                                    }
                                    
                                    if (friend != null) {
                                        friends.add(friend);
                                        System.out.println("Added friend: " + friend.getUserName() + " (ID: " + friend.getUserID() + ")");
                                    }
                                }
                            }
                            
                            System.out.println("=== CALLING CALLBACK.ONRESULT ===");
                            System.out.println("About to call callback.onResult with " + friends.size() + " friends");
                            callback.onResult(friends);
                            System.out.println("=== CALLBACK.ONRESULT COMPLETED ===");
                        } catch (Exception e) {
                            System.err.println("Error parsing friends list: " + e.getMessage());
                            e.printStackTrace();
                            callback.onError("Error parsing friends list: " + e.getMessage());
                        }
                    } else {
                        System.out.println("=== SERVER RETURNED FALSE OR NO DATA ===");
                        if (os.length > 0 && !(Boolean) os[0]) {
                            String errorMsg = os.length > 1 ? (String) os[1] : "Failed to get friends list";
                            System.out.println("Server returned false, calling callback.onError: " + errorMsg);
                            callback.onError(errorMsg);
                        } else {
                            System.out.println("No friends found, calling callback.onResult with empty list");
                            callback.onResult(new ArrayList<>());
                        }
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Error getting friends list: " + e.getMessage());
        }
    }

    // Interface for getting friends list with results
    public interface FriendsListCallback {
        void onResult(List<Model_User_Account> friends);
        void onError(String error);
    }

    // Helper method to parse user from Map
    private Model_User_Account parseUserFromMap(java.util.Map<String, Object> friendMap) {
        try {
            Model_User_Account friend = new Model_User_Account();
            friend.setUserID(((Number) friendMap.get("userID")).intValue());
            friend.setUserName((String) friendMap.get("userName"));
            friend.setGender((String) friendMap.get("gender"));
            friend.setStatus((Boolean) friendMap.get("status"));
            
            // Handle image data
            Object imageData = friendMap.get("image");
            if (imageData != null) {
                if (imageData instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Number> imageList = (List<Number>) imageData;
                    byte[] friendImage = new byte[imageList.size()];
                    for (int j = 0; j < imageList.size(); j++) {
                        friendImage[j] = imageList.get(j).byteValue();
                    }
                    friend.setImage(friendImage);
                } else if (imageData instanceof byte[]) {
                    friend.setImage((byte[]) imageData);
                }
            } else {
                friend.setImage(new byte[0]);
            }
            
            return friend;
        } catch (Exception e) {
            System.err.println("Error parsing user from Map: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }    // Helper method to parse user from JSONObject
    private Model_User_Account parseUserFromJSONObject(JSONObject jsonObj) {
        try {
            Model_User_Account friend = new Model_User_Account();
            friend.setUserID(jsonObj.getInt("userID"));
            friend.setUserName(jsonObj.getString("userName"));
            friend.setGender(jsonObj.optString("gender", ""));
            friend.setStatus(jsonObj.optBoolean("status", false));
            
            // Handle image data
            if (jsonObj.has("image") && !jsonObj.isNull("image")) {
                Object imageData = jsonObj.get("image");                System.out.println("Image data type: " + imageData.getClass().getName());
                String imageClassName = imageData.getClass().getName();
                
                if (imageClassName.equals("[B")) {
                    // Image data is already a byte array
                    System.out.println("Image data is already byte array");
                    byte[] friendImage = (byte[]) imageData;
                    friend.setImage(friendImage);
                    System.out.println("Successfully set image, size: " + friendImage.length);
                } else if (imageClassName.equals("org.json.JSONArray")) {
                    System.out.println("Converting JSONArray to byte array");
                    try {
                        Object jsonArray = imageData;
                        // Use reflection to get length and values
                        int length = (Integer) jsonArray.getClass().getMethod("length").invoke(jsonArray);
                        byte[] friendImage = new byte[length];
                        for (int j = 0; j < length; j++) {
                            Object value = jsonArray.getClass().getMethod("get", int.class).invoke(jsonArray, j);
                            friendImage[j] = ((Number) value).byteValue();
                        }
                        friend.setImage(friendImage);
                        System.out.println("Successfully converted image, size: " + friendImage.length);
                    } catch (Exception ex) {
                        System.err.println("Error converting JSONArray: " + ex.getMessage());
                        friend.setImage(new byte[0]);
                    }
                } else if (imageData instanceof java.util.List) {
                    System.out.println("Converting List to byte array");
                    @SuppressWarnings("unchecked")
                    java.util.List<Number> imageList = (java.util.List<Number>) imageData;
                    byte[] friendImage = new byte[imageList.size()];
                    for (int j = 0; j < imageList.size(); j++) {
                        friendImage[j] = imageList.get(j).byteValue();
                    }
                    friend.setImage(friendImage);
                    System.out.println("Successfully converted image from List, size: " + friendImage.length);
                } else {
                    System.out.println("Unknown image data type: " + imageClassName + ", setting empty byte array");
                    friend.setImage(new byte[0]);
                }
            } else {
                System.out.println("No image data found, setting empty byte array");
                friend.setImage(new byte[0]);
            }
            
            return friend;
        } catch (Exception e) {
            System.err.println("Error parsing user from JSONObject: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Group chat methods
    public void createGroup(String groupName, String groupDescription, List<Integer> memberIDs, component.CreateGroupDialog.CreateGroupCallback callback) {
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put("groupName", groupName);
        requestData.put("groupDescription", groupDescription);
        requestData.put("memberIDs", memberIDs);
        requestData.put("createdBy", user != null ? user.getUserID() : 0);
        
        client.emit("create_group", requestData, new Ack() {
            @Override
            public void call(Object... os) {
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    if (success && os.length > 1) {
                        callback.onSuccess((String) os[1]);
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "Unknown error";
                        callback.onError(error);
                    }
                } else {
                    callback.onError("No response from server");
                }
            }
        });
    }
    
    public void getUserGroups(GroupListCallback callback) {
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        client.emit("get_user_groups", user.getUserID(), new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("=== RECEIVED GROUPS RESPONSE ===");
                System.out.println("Response array length: " + os.length);
                
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    System.out.println("Success: " + success);
                    
                    if (success) {
                        List<Model_Group_Chat> groups = new ArrayList<>();
                        
                        // Parse groups data with detailed logging
                        for (int i = 1; i < os.length; i++) {
                            try {
                                System.out.println("\n=== PROCESSING GROUP " + i + " ===");
                                System.out.println("Raw data type: " + (os[i] != null ? os[i].getClass().getName() : "null"));
                                
                                if (os[i] instanceof java.util.Map) {
                                    @SuppressWarnings("unchecked")
                                    java.util.Map<String, Object> groupData = (java.util.Map<String, Object>) os[i];
                                    
                                    System.out.println("Group data keys: " + groupData.keySet());
                                    System.out.println("GroupID: " + groupData.get("groupID"));
                                    System.out.println("GroupName: " + groupData.get("groupName"));
                                    
                                    Object groupImageObj = groupData.get("groupImage");
                                    if (groupImageObj != null) {
                                        System.out.println("GroupImage found! Type: " + groupImageObj.getClass().getName());
                                        if (groupImageObj instanceof java.util.List) {
                                            java.util.List<?> imageList = (java.util.List<?>) groupImageObj;
                                            System.out.println("GroupImage list size: " + imageList.size());
                                            if (imageList.size() > 0) {
                                                System.out.println("First 5 bytes: " + imageList.subList(0, Math.min(5, imageList.size())));
                                            }
                                        }
                                    } else {
                                        System.out.println("GroupImage is NULL in received data!");
                                    }
                                }
                                
                                Model_Group_Chat group = new Model_Group_Chat(os[i]);
                                groups.add(group);
                            } catch (Exception e) {
                                System.err.println("Error parsing group data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        callback.onSuccess(groups);
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "Failed to get groups";
                        callback.onError(error);
                    }
                } else {
                    callback.onError("No response from server");
                }
            }
        });
    }
    
    public interface GroupListCallback {
        void onSuccess(List<Model_Group_Chat> groups);
        void onError(String error);
    }
    
    public void getGroupMembers(int groupID, GroupMembersCallback callback) {
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        System.out.println("=== SERVICE: EMITTING get_group_members FOR GROUP " + groupID + " ===");
        
        client.emit("get_group_members", groupID, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("=== SERVICE: RECEIVED get_group_members RESPONSE ===");
                System.out.println("Response length: " + os.length);
                System.out.println("Response data: " + java.util.Arrays.toString(os));
                
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    System.out.println("Success: " + success);
                    
                    if (success) {
                        List<Model_User_Account> members = new ArrayList<>();
                        
                        // Parse members data
                        for (int i = 1; i < os.length; i++) {
                            try {
                                System.out.println("Parsing member data [" + i + "]: " + os[i]);
                                Model_User_Account member = new Model_User_Account(os[i]);
                                members.add(member);
                                System.out.println("Added member: " + member.getUserName() + " (ID: " + member.getUserID() + ")");
                            } catch (Exception e) {
                                System.err.println("Error parsing member data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        System.out.println("=== SERVICE: CALLING callback.onSuccess WITH " + members.size() + " MEMBERS ===");
                        callback.onSuccess(members);
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "Failed to get group members";
                        System.out.println("=== SERVICE: CALLING callback.onError: " + error + " ===");
                        callback.onError(error);
                    }
                } else {
                    System.out.println("=== SERVICE: CALLING callback.onError: No response from server ===");
                    callback.onError("No response from server");
                }
            }
        });
    }
    
    public interface GroupMembersCallback {
        void onSuccess(List<Model_User_Account> members);
        void onError(String error);
    }
    
    // Load group messages from database
    public void loadGroupMessages(int groupID, GroupMessagesCallback callback) {
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        System.out.println("=== CLIENT REQUESTING GROUP MESSAGES FOR GROUP " + groupID + " ===");
        
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put("groupID", groupID);
        
        client.emit("load_group_messages", requestData, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("=== CLIENT RECEIVED GROUP MESSAGES RESPONSE ===");
                System.out.println("Response objects: " + os.length);
                
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    System.out.println("Success: " + success);
                      if (success && os.length > 1) {
                        try {
                            List<java.util.Map<String, Object>> messagesData = new ArrayList<>();
                              // Handle JSONArray response from server
                            if (os[1].getClass().getName().contains("JSONArray")) {
                                // Use reflection to handle JSONArray to avoid import issues
                                try {
                                    Object jsonArray = os[1];
                                    int length = (Integer) jsonArray.getClass().getMethod("length").invoke(jsonArray);
                                    System.out.println("Processing JSONArray with " + length + " messages");
                                    
                                    for (int i = 0; i < length; i++) {
                                        Object jsonObj = jsonArray.getClass().getMethod("get", int.class).invoke(jsonArray, i);
                                        java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                                        
                                        // Extract data from JSONObject using reflection
                                        Class<?> jsonClass = jsonObj.getClass();
                                        msgMap.put("messageID", jsonClass.getMethod("getInt", String.class).invoke(jsonObj, "messageID"));
                                        msgMap.put("groupID", jsonClass.getMethod("getInt", String.class).invoke(jsonObj, "groupID"));
                                        msgMap.put("senderID", jsonClass.getMethod("getInt", String.class).invoke(jsonObj, "senderID"));
                                        msgMap.put("messageType", jsonClass.getMethod("getString", String.class).invoke(jsonObj, "messageType"));
                                        msgMap.put("messageContent", jsonClass.getMethod("getString", String.class).invoke(jsonObj, "messageContent"));
                                        msgMap.put("sentAt", jsonClass.getMethod("getLong", String.class).invoke(jsonObj, "sentAt"));
                                        msgMap.put("senderName", jsonClass.getMethod("getString", String.class).invoke(jsonObj, "senderName"));
                                        
                                        // Handle avatar data if present
                                        try {
                                            Object avatarData = jsonClass.getMethod("get", String.class).invoke(jsonObj, "senderAvatar");
                                            if (avatarData != null) {
                                                msgMap.put("senderAvatar", avatarData);
                                            }
                                        } catch (Exception e) {
                                            // Avatar might be null or not present
                                            msgMap.put("senderAvatar", null);
                                        }
                                        
                                        messagesData.add(msgMap);
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error processing JSONArray: " + e.getMessage());
                                    e.printStackTrace();
                                    callback.onError("Error processing messages: " + e.getMessage());
                                    return;
                                }
                            } else {
                                // Direct cast if it's already a List (fallback)
                                @SuppressWarnings("unchecked")
                                List<java.util.Map<String, Object>> directList = (List<java.util.Map<String, Object>>) os[1];
                                messagesData = directList;
                            }
                            
                            List<Model_Group_Message_Display> messages = new ArrayList<>();
                            
                            for (java.util.Map<String, Object> msgData : messagesData) {
                                try {
                                    Model_Group_Message_Display message = new Model_Group_Message_Display();
                                    message.setMessageID(((Number) msgData.get("messageID")).intValue());
                                    message.setGroupID(((Number) msgData.get("groupID")).intValue());
                                    message.setSenderID(((Number) msgData.get("senderID")).intValue());
                                    message.setMessageType((String) msgData.get("messageType"));
                                    message.setMessageContent((String) msgData.get("messageContent"));
                                    message.setSentAt(new java.util.Date(((Number) msgData.get("sentAt")).longValue()));
                                    message.setSenderName((String) msgData.get("senderName"));
                                    
                                    // Handle avatar data
                                    Object avatarData = msgData.get("senderAvatar");
                                    if (avatarData != null && avatarData instanceof byte[]) {
                                        message.setSenderAvatar((byte[]) avatarData);
                                    }
                                    
                                    messages.add(message);
                                    
                                    System.out.println("Parsed message: " + message.getSenderName() + " - " + message.getMessageContent());
                                } catch (Exception e) {
                                    System.err.println("Error parsing message: " + e.getMessage());
                                }
                            }
                            
                            System.out.println("=== PARSED " + messages.size() + " GROUP MESSAGES ===");
                            callback.onSuccess(messages);
                            
                        } catch (Exception e) {
                            System.err.println("Error parsing messages data: " + e.getMessage());
                            e.printStackTrace();
                            callback.onError("Error parsing messages: " + e.getMessage());
                        }
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "No messages found";
                        callback.onError(error);
                    }
                } else {
                    callback.onError("No response from server");
                }
            }
        });
    }
    
    public interface GroupMessagesCallback {
        void onSuccess(List<Model_Group_Message_Display> messages);
        void onError(String error);
    }

    // Method to get group member count
    public void getGroupMemberCount(int groupID, GroupMemberCountCallback callback) {
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        System.out.println("=== CLIENT REQUESTING GROUP MEMBER COUNT FOR GROUP " + groupID + " ===");
        
        client.emit("get_group_member_count", groupID, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("=== CLIENT RECEIVED GROUP MEMBER COUNT RESPONSE ===");
                System.out.println("Response objects: " + os.length);
                
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    System.out.println("Success: " + success);
                    
                    if (success && os.length > 1) {
                        try {
                            int memberCount = ((Number) os[1]).intValue();
                            System.out.println("Group " + groupID + " has " + memberCount + " members");
                            callback.onSuccess(memberCount);
                        } catch (Exception e) {
                            System.err.println("Error parsing member count: " + e.getMessage());
                            e.printStackTrace();
                            callback.onError("Error parsing member count: " + e.getMessage());
                        }
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "No member count found";
                        callback.onError(error);
                    }
                } else {
                    callback.onError("No response from server");
                }
            }
        });
    }
    
    public interface GroupMemberCountCallback {
        void onSuccess(int memberCount);
        void onError(String error);
    }
    
    // Method to add member to group
    public void addMemberToGroup(int groupID, int userID, String role, AddMemberToGroupCallback callback) {
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        System.out.println("=== CLIENT ADDING MEMBER TO GROUP ===");
        System.out.println("GroupID: " + groupID);
        System.out.println("UserID: " + userID);
        System.out.println("Role: " + role);
        
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put("groupID", groupID);
        requestData.put("userID", userID);
        requestData.put("role", role);
        
        client.emit("add_member_to_group", requestData, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("=== CLIENT RECEIVED ADD_MEMBER_TO_GROUP RESPONSE ===");
                System.out.println("Response data: " + java.util.Arrays.toString(os));
                
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    if (success) {
                        System.out.println("Successfully added member to group");
                        callback.onSuccess();
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "Failed to add member to group";
                        System.err.println("Failed to add member: " + error);
                        callback.onError(error);
                    }
                } else {
                    callback.onError("No response from server");
                }
            }
        });
    }
    
    public interface AddMemberToGroupCallback {
        void onSuccess();
        void onError(String error);
    }
    
    // Method to leave group
    public void leaveGroup(int groupID, LeaveGroupCallback callback) {
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        System.out.println("=== CLIENT LEAVING GROUP ===");
        System.out.println("GroupID: " + groupID);
        System.out.println("UserID: " + user.getUserID());
        
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put("groupID", groupID);
        requestData.put("userID", user.getUserID());
        
        System.out.println("=== CLIENT: ABOUT TO EMIT leave_group EVENT ===");
        System.out.println("Request data: " + requestData);
        
        client.emit("leave_group", requestData, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("=== CLIENT RECEIVED LEAVE_GROUP RESPONSE ===");
                System.out.println("Response data: " + java.util.Arrays.toString(os));
                
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    if (success) {
                        System.out.println("Successfully left group");
                        callback.onSuccess();
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "Failed to leave group";
                        System.err.println("Failed to leave group: " + error);
                        callback.onError(error);
                    }
                } else {
                    callback.onError("No response from server");
                }
            }
        });
        
        System.out.println("=== CLIENT: leave_group EVENT EMITTED ===");
    }
    
    // Method to update group avatar
    public void updateGroupAvatar(int groupID, byte[] avatarData, UpdateGroupAvatarCallback callback) {
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        System.out.println("=== CLIENT UPDATING GROUP AVATAR ===");
        System.out.println("GroupID: " + groupID);
        System.out.println("Avatar size: " + avatarData.length + " bytes");
        System.out.println("User ID: " + user.getUserID());
        
        // Create request data as Map to ensure proper serialization
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put("groupID", Integer.valueOf(groupID));
        requestData.put("updatedBy", Integer.valueOf(user.getUserID()));
        
        // Convert byte array to ArrayList<Integer> for better JSON serialization
        java.util.List<Integer> avatarList = new java.util.ArrayList<>();
        for (byte b : avatarData) {
            avatarList.add((int) (b & 0xFF)); // Convert to unsigned int
        }
        requestData.put("avatarData", avatarList);
        
        System.out.println("Sending request with data: " + requestData.keySet());
        System.out.println("Avatar data converted to list size: " + avatarList.size());
        
        client.emit("update_group_avatar", requestData, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("=== CLIENT RECEIVED UPDATE_GROUP_AVATAR RESPONSE ===");
                System.out.println("Response data: " + java.util.Arrays.toString(os));
                
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    if (success) {
                        String message = os.length > 1 ? (String) os[1] : "Group avatar updated successfully";
                        System.out.println("Successfully updated group avatar: " + message);
                        callback.onSuccess(message);
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "Failed to update group avatar";
                        System.err.println("Failed to update group avatar: " + error);
                        callback.onError(error);
                    }
                } else {
                    callback.onError("No response from server");
                }
            }
        });
    }
    
    public interface UpdateGroupAvatarCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    // Simple method to remove member from group
    public void removeMemberFromGroup(int groupID, int userID, RemoveMemberCallback callback) {
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        System.out.println("=== CLIENT REMOVING MEMBER FROM GROUP ===");
        System.out.println("GroupID: " + groupID);
        System.out.println("UserID: " + userID);
        
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put("groupID", groupID);
        requestData.put("userID", userID);
        
        client.emit("remove_member_simple", requestData, new Ack() {
            @Override
            public void call(Object... os) {
                System.out.println("=== CLIENT RECEIVED REMOVE_MEMBER_SIMPLE RESPONSE ===");
                System.out.println("Response data: " + java.util.Arrays.toString(os));
                
                if (os.length > 0) {
                    boolean success = (Boolean) os[0];
                    if (success) {
                        System.out.println("Successfully removed member from group");
                        callback.onSuccess();
                    } else {
                        String error = os.length > 1 ? (String) os[1] : "Failed to remove member from group";
                        System.err.println("Failed to remove member: " + error);
                        callback.onError(error);
                    }
                } else {
                    callback.onError("No response from server");
                }
            }
        });
    }
    
    public interface RemoveMemberCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface LeaveGroupCallback {
        void onSuccess();
        void onError(String error);
    }
    
    // Method to check connection status
    public boolean isConnected() {
        return client != null && client.connected();
    }
}
