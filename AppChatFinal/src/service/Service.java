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
import model.Model_Package_Sender;
import model.Model_Receive_Message;
import model.Model_Send_Message;
import model.Model_User_Account;
import model.Model_Voice_Receive;
import model.Model_Friend_Request;
import org.json.JSONObject;
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

    public void startServer() {
            //xóa file cache trong folder client_data
             File f = new File("client_data");
        for (File fs : f.listFiles()) {
            fs.delete();
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
                    Model_Receive_Message message = new Model_Receive_Message(os[0]);
           
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
                    
            });
            client.on("receive_voice", new Emitter.Listener() {
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
            
            client.open();
        } catch (URISyntaxException e) {
            error(e);
        }
    }
    
    public Model_File_Sender addFile(File file, Model_Send_Message message) throws IOException {
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
    }
    
    // Method to search users by username
    public void searchUsers(String searchText, component.FriendSearchPanel.SearchCallback callback) {
        System.out.println("Đang tìm kiếm người dùng: " + searchText);
        client.emit("search_users", searchText, new Ack() {
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
                                System.out.println("Số lượng kết quả: " + length);

                                for (int i = 0; i < length; i++) {
                                    try {
                                        Object userObj = jsonArray.getClass().getMethod("get", int.class).invoke(jsonArray, i);
                                        Model_User_Account user = new Model_User_Account(userObj);
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
                                System.out.println("Số lượng kết quả (Object[]): " + results.length);
                                for (Object o : results) {
                                    try {
                                        Model_User_Account user = new Model_User_Account(o);
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
                                    requestMap.put("senderUserID", senderId);
                                    requestMap.put("senderUserName", senderName);
                                    requestMap.put("senderGender", senderGender);
                                    requestMap.put("senderStatus", senderStatus);
                                    requestMap.put("createdAt", createdAt);
                                    requestMap.put("status", "pending");
                                    requestMap.put("senderImage", new byte[0]); // Empty for now
                                    
                                    System.out.println("Manual JSON parsing successful");
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
    
    private void error(Exception e) {
        System.err.println(e);
    }
}
