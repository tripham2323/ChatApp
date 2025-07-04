/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import app.MessageType;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTextArea;
import model.Model_Client;
import model.Model_File;
import model.Model_Login;
import model.Model_Message;
import model.Model_Package_Sender;
import model.Model_Receive_Image;
import model.Model_Receive_Message;
import model.Model_Register;
import model.Model_Send_Message;
import model.Model_User_Account;
import model.Model_Voice_Sender;
import model.Model_Friend_Request;
import model.Model_Group_Chat;

/**
 *
 * @author Admin
 */
public class Service {

    
    private static Service instance;
    private SocketIOServer server;
    private JTextArea textArea;
    private final int PORT_NUMBER = 9999;
    private ServiceUser serviceUser;
    private ServiceFile serviceFile;
    private List<Model_Client> listClient;
    
    public static Service getInstance(JTextArea textArea){
     if (instance == null) {
            instance = new Service(textArea);
        }
        return instance;
    }
    private Service(JTextArea textArea){
        this.textArea = textArea;
        this.serviceUser = new ServiceUser();
        serviceFile = new ServiceFile();
        listClient = new ArrayList<>();
        ByteOutputStream = new ByteArrayOutputStream();
    }    public void startServer(){
         File f = new File("server_data");
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File fs : files) {
                    fs.delete();
                }
            }
        }
        
        
        Configuration config = new Configuration();
        config.setPort(PORT_NUMBER);//cấu hình cổng 9999 cho server lắng nghe
        server = new SocketIOServer(config);
//        startServerSocket();
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient sioc) {
                textArea.append("Một client đã kết nối\n");
               }
        });
        server.addEventListener("register", Model_Register.class, new DataListener<Model_Register>() { //lắng nghe event register từ client
            @Override
            public void onData(SocketIOClient socketClient, Model_Register t, AckRequest ar) throws Exception {
                Model_Message message = serviceUser.register(t);
                ar.sendAckData(message.isAction(), message.getMessage(), message.getData());// phản hồi lại client với gói tin, lớp ackRequest từ socketIO
                if(message.isAction()){
                   textArea.append("User đã đăng ký với tài khoản :"+t.getUserName()+" Mật khẩu :"+t.getPassword()+"\n");
                   server.getBroadcastOperations().sendEvent("list_user", (Model_User_Account)message.getData());
                   addClient(socketClient, (Model_User_Account)message.getData());
                }
             }
        });
        
        server.addEventListener("login", Model_Login.class, new DataListener<Model_Login>() {//lắng nghe event login từ client
            @Override
            public void onData(SocketIOClient socketClient, Model_Login t, AckRequest ar) throws Exception {
                Model_User_Account login = serviceUser.login(t);
                if(login != null){
                   ar.sendAckData(true, login);
                   // Don't set image to null - keep avatar data in server memory
                   // login.setImage(null);
                    addClient(socketClient, login);
                    userConnect(login);
                } else{
                   ar.sendAckData(false);
                
                }
               }
        });
        
        server.addEventListener("list_user", Integer.class, new DataListener<Integer>() {//lắng nghe event list_user từ client
            @Override
            public void onData(SocketIOClient socketClient, Integer userID, AckRequest ar) throws Exception {
                try {
                    List<Model_User_Account> list = serviceUser.getUser(userID);// lấy list all user có trong db trừ userID này ra
                    socketClient.sendEvent("list_user", list.toArray());//phản hồi về lại client qua socketIOClient
                } catch (SQLException e) {
                    System.err.println(e);
                }
               }
        });
        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient socketClient) {
                int userID = removeClient(socketClient);
                if(userID != 0){
                   //removed
                    userDisconnect(userID);
                }
                
               }        });
          // TEMPORARY DEBUG: Use Object.class to catch all send_to_user events        System.out.println("=== REGISTERING send_to_user EVENT LISTENER (MAP) ===");
        server.addEventListener("send_to_user", java.util.Map.class, new DataListener<java.util.Map>() {
            @Override
            public void onData(SocketIOClient socketClient, java.util.Map messageData, AckRequest ar) throws Exception {
                System.out.println("=== SERVER RECEIVED send_to_user EVENT (MAP) ===");
                System.out.println("Message Data: " + messageData);
                System.out.println("From UserID: " + messageData.get("fromUserID"));
                System.out.println("To UserID: " + messageData.get("toUserID"));
                System.out.println("Message Type: " + messageData.get("messageType"));
                System.out.println("Message Text: " + messageData.get("text"));                System.out.println("Socket Client ID: " + socketClient.getSessionId());
                
                // Convert Map to Model_Send_Message
                try {
                    // Handle messageType conversion from String to int
                    Object messageTypeObj = messageData.get("messageType");
                    int messageType;
                
                if (messageTypeObj instanceof Integer) {
                    // Direct integer value
                    messageType = (Integer) messageTypeObj;
                } else if (messageTypeObj instanceof String) {
                    // String representation, convert to int
                    String messageTypeStr = (String) messageTypeObj;
                    if ("TEXT".equals(messageTypeStr)) {
                        messageType = 1; // MessageType.TEXT
                    } else if ("EMOJI".equals(messageTypeStr)) {
                        messageType = 2; // MessageType.EMOJI
                    } else if ("FILE".equals(messageTypeStr)) {  
                        messageType = 3; // MessageType.FILE
                    } else if ("IMAGE".equals(messageTypeStr)) {
                        messageType = 4; // MessageType.IMAGE
                    } else {
                        messageType = 1; // Default to TEXT
                    }
                } else {
                    messageType = 1; // Default to TEXT
                }
                    
                    int fromUserID = (Integer) messageData.get("fromUserID");
                    int toUserID = (Integer) messageData.get("toUserID");
                    String text = (String) messageData.get("text");
                    
                    Model_Send_Message data = new Model_Send_Message(messageType, fromUserID, toUserID, text);
                    System.out.println("*** CONVERTED TO MODEL_SEND_MESSAGE SUCCESSFULLY ***");
                    System.out.println("Message Type Int: " + messageType);
                    
                    sendToClient(data, ar);
                    System.out.println("*** SENT TO CLIENT ***");
                } catch (Exception e) {
                    System.err.println("ERROR converting Map to Model_Send_Message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        System.out.println("=== send_to_user EVENT LISTENER (MAP) REGISTERED ===");
        
        server.addEventListener("send_file", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Package_Sender t, AckRequest ar) throws IOException {
                try {
                    
                    serviceFile.receiveFile(t);   
                    if(t.isFinish()){
                        ar.sendAckData(true);
                        Model_Receive_Image dataImage = new Model_Receive_Image();
                        dataImage.setFileID(t.getFileID());
                        dataImage.setFileName(t.getFileName());
                        Model_Send_Message message = serviceFile.closeFile(dataImage);
                        //gửi đến client 'message'
                        sendImgFileToClient(message, dataImage);
                        
                        
                    }else{
                        ar.sendAckData(true);
                    
                    }
                } catch (IOException | SQLException e) {
                    ar.sendAckData(false);
                    e.printStackTrace();
                }                }
        });
        
        System.out.println("=== ABOUT TO REGISTER send_to_group EVENT LISTENER ===");
        
        // Event listener for group messages - use Object.class to receive raw data
        server.addEventListener("send_to_group", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketClient, Object data, AckRequest ar) throws Exception {
                System.out.println("=== SERVER RECEIVED GROUP MESSAGE (OBJECT) ===");
                System.out.println("Data: " + data);
                System.out.println("Data type: " + (data != null ? data.getClass().getName() : "null"));
                  try {
                    if (data != null) {
                        // Handle both Model_Send_Message and Map data
                        Model_Send_Message message = null;
                        
                        if (data instanceof Model_Send_Message) {
                            message = (Model_Send_Message) data;
                            System.out.println("Received Model_Send_Message directly");
                        } else if (data instanceof java.util.Map) {
                            // Parse Map data to Model_Send_Message
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> messageData = (java.util.Map<String, Object>) data;
                            System.out.println("Parsing LinkedHashMap to Model_Send_Message");
                              Object messageTypeObj = messageData.get("messageType");
                            int messageType = 0;
                            if (messageTypeObj instanceof String) {
                                // Convert string to MessageType value
                                String messageTypeStr = (String) messageTypeObj;
                                if ("TEXT".equals(messageTypeStr)) {
                                    messageType = 1; // MessageType.TEXT
                                } else if ("EMOJI".equals(messageTypeStr)) {
                                    messageType = 2; // MessageType.EMOJI
                                } else if ("FILE".equals(messageTypeStr)) {
                                    messageType = 3; // MessageType.FILE
                                } else if ("IMAGE".equals(messageTypeStr)) {
                                    messageType = 4; // MessageType.IMAGE
                                }
                            } else if (messageTypeObj instanceof Number) {
                                messageType = ((Number) messageTypeObj).intValue();
                            }
                            
                            int fromUserID = ((Number) messageData.get("fromUserID")).intValue();
                            int toUserID = ((Number) messageData.get("toUserID")).intValue(); // This is groupID
                            String text = (String) messageData.get("text");
                            
                            message = new Model_Send_Message(messageType, fromUserID, toUserID, text);
                            System.out.println("Successfully created Model_Send_Message from Map");
                        }
                        
                        if (message != null) {
                            System.out.println("Processing message:");
                            System.out.println("FromUserID: " + message.getFromUserID());
                            System.out.println("ToUserID (GroupID): " + message.getToUserID());
                            System.out.println("MessageType: " + message.getMessageType());
                            System.out.println("Text: " + message.getText());
                            
                            // Save message to group_messages table
                            boolean saved = serviceUser.saveGroupMessage(message);
                            if (saved) {
                                System.out.println("Message saved to database successfully");
                                
                                // Get all group members
                                List<Model_User_Account> members = serviceUser.getGroupMembers(message.getToUserID());
                                System.out.println("Found " + members.size() + " members in group " + message.getToUserID());
                                  // Get sender info for avatar
                                Model_User_Account senderInfo = serviceUser.getUserByID(message.getFromUserID());
                                
                                // Send message to all group members except sender
                                for (Model_User_Account member : members) {
                                    System.out.println("Checking member: " + member.getUserName() + " (ID: " + member.getUserID() + ")");
                                    if (member.getUserID() != message.getFromUserID()) {
                                        // Find client by userID
                                        boolean found = false;
                                        for (Model_Client client : listClient) {
                                            if (client.getUser().getUserID() == member.getUserID()) {
                                                System.out.println("Sending group message to: " + member.getUserName());
                                                
                                                // Create a special group message format that includes sender info
                                                java.util.Map<String, Object> groupMessage = new java.util.HashMap<>();
                                                groupMessage.put("messageType", message.getMessageType());
                                                groupMessage.put("fromUserID", message.getFromUserID());
                                                groupMessage.put("text", message.getText());
                                                groupMessage.put("isGroupMessage", true);
                                                groupMessage.put("groupID", message.getToUserID());
                                                
                                                // Add sender info for avatar display
                                                if (senderInfo != null) {
                                                    groupMessage.put("senderName", senderInfo.getUserName());
                                                    groupMessage.put("senderAvatar", senderInfo.getImage());
                                                    System.out.println("Added sender info: " + senderInfo.getUserName() + 
                                                                     " (avatar: " + (senderInfo.getImage() != null ? senderInfo.getImage().length + " bytes" : "null") + ")");
                                                }
                                                
                                                // Send as special group message event
                                                client.getClient().sendEvent("receive_group_message", groupMessage);
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            System.out.println("Member " + member.getUserName() + " is not online");
                                        }
                                    } else {
                                        System.out.println("Skipping sender: " + member.getUserName());
                                    }
                                }
                                ar.sendAckData(true);
                            } else {
                                System.out.println("Failed to save message to database");
                                ar.sendAckData(false);
                            }
                        } else {
                            System.out.println("Could not parse message data, type: " + data.getClass().getName());
                            System.out.println("Data: " + data.toString());
                            ar.sendAckData(false, "Server could not parse message data");
                        }
                    } else {
                        System.out.println("Data is null!");
                        ar.sendAckData(false);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing group message: " + e.getMessage());
                    e.printStackTrace();
                    ar.sendAckData(false);
                }
            }
        });
        System.out.println("=== EVENT LISTENER REGISTERED: send_to_group ===");
        
        // Event listener for loading group messages from database
        server.addEventListener("load_group_messages", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketClient, Object data, AckRequest ar) throws Exception {
                System.out.println("=== SERVER RECEIVED LOAD GROUP MESSAGES REQUEST ===");
                System.out.println("Data: " + data);
                
                try {
                    int groupID = 0;
                    if (data instanceof Number) {
                        groupID = ((Number) data).intValue();
                    } else if (data instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> requestData = (java.util.Map<String, Object>) data;
                        groupID = ((Number) requestData.get("groupID")).intValue();
                    }
                    
                    System.out.println("Loading messages for group ID: " + groupID);
                    
                    // Get messages from database
                    List<model.Model_Group_Message> messages = serviceUser.getGroupMessages(groupID);
                    
                    // Convert to format suitable for client
                    List<java.util.Map<String, Object>> messageList = new ArrayList<>();
                    for (model.Model_Group_Message msg : messages) {
                        java.util.Map<String, Object> messageData = new java.util.HashMap<>();
                        messageData.put("messageID", msg.getMessageID());
                        messageData.put("groupID", msg.getGroupID());
                        messageData.put("senderID", msg.getSenderID());
                        messageData.put("messageType", msg.getMessageType());
                        messageData.put("messageContent", msg.getMessageContent());
                        messageData.put("sentAt", msg.getSentAt().getTime());
                        messageData.put("senderName", msg.getSenderName());
                        messageData.put("senderAvatar", msg.getSenderImage());
                        
                        messageList.add(messageData);
                    }
                    
                    System.out.println("Sending " + messageList.size() + " messages to client");
                    ar.sendAckData(true, messageList);
                    
                } catch (Exception e) {
                    System.err.println("Error loading group messages: " + e.getMessage());
                    e.printStackTrace();
                    ar.sendAckData(false, "Error loading messages: " + e.getMessage());
                }
            }
        });
        
        server.addEventListener("download", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Package_Sender t, AckRequest ar) throws Exception {
                    serviceFile.getFile(t);
                    socketClient.sendEvent("GetFile", new Model_Package_Sender(t.getFileID(), t.getFileName(), t.getData(), t.isFinish()));
                }
        } );
         server.addEventListener("send_voice", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Package_Sender t, AckRequest ar) throws Exception {
                 try {  
               
                     serviceFile.receiveVoice(t, ByteOutputStream); 
                    if(t.isFinish()){
                       ar.sendAckData(true);
                       byte[] dataVoice = ByteOutputStream.toByteArray();
                       System.out.println("độ dài data mic"+dataVoice.length);
                       sendVoiceToClient(t, dataVoice);
                       ByteOutputStream.reset();

                    }else{
                        ar.sendAckData(true);
                    
                    }
                } catch (IOException e) {
                    ar.sendAckData(false);
                    e.printStackTrace();
                }
                }
        }); 
         server.addEventListener("send_avatar", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Package_Sender t, AckRequest ar) throws Exception {
                try {
                     if(!t.isFinish()){
                       ByteOutputStream.write(t.getData());
                       ar.sendAckData(true);
                    }else{
                       ByteOutputStream.close();
                       ar.sendAckData(true);
                       byte[] data = ByteOutputStream.toByteArray();
                       System.out.println("độ dài data Avatar"+data.length);
                       serviceFile.updateAvatarData(data, t.getFromUserID());
                       ByteOutputStream.reset();
                       // next update send broadcast method to all client online at the moment
                   }
                  } catch (Exception e) {
                      ar.sendAckData(false);
                      e.printStackTrace();
                }
             
               }
        });
          // Friend search event handler - chỉ hiển thị người chưa kết bạn
        server.addEventListener("search_users", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketClient, Object requestData, AckRequest ar) throws Exception {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = (java.util.Map<String, Object>) requestData;
                    String searchText = (String) data.get("searchText");
                    int currentUserID = ((Number) data.get("currentUserID")).intValue();
                    
                    System.out.println("Server nhận yêu cầu tìm kiếm với từ khóa: " + searchText + " từ UserID: " + currentUserID);
                    
                    List<Model_User_Account> results = serviceUser.searchUsersNotFriends(searchText, currentUserID);
                    System.out.println("Tìm thấy " + results.size() + " người chưa kết bạn, trả về cho client");

                    // Gửi lại kết quả cho client
                    ar.sendAckData(true, results.toArray());
                } catch (Exception e) {
                    System.err.println("Lỗi khi tìm kiếm người dùng chưa kết bạn: " + e.getMessage());
                    e.printStackTrace();
                    ar.sendAckData(false, "Search failed: " + e.getMessage());
                }
            }
        });
        // Friend request event handler
        server.addEventListener("send_friend_request", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketClient, Object requestData, AckRequest ar) throws Exception {
                try {
                    // Parse the request data (it comes as a map-like object from Socket.IO)
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = (java.util.Map<String, Object>) requestData;
                    int fromUserID = ((Number) data.get("fromUserID")).intValue();
                    int toUserID = ((Number) data.get("toUserID")).intValue();
                    
                    boolean success = serviceUser.sendFriendRequest(fromUserID, toUserID);
                    if (success) {
                        ar.sendAckData(true, "Friend request sent successfully");
                        textArea.append("Friend request sent from UserID " + fromUserID + " to UserID " + toUserID + "\n");
                        
                        // Send real-time notification to the recipient
                        notifyFriendRequestReceived(toUserID, fromUserID);
                    } else {
                        ar.sendAckData(false, "Failed to send friend request");
                    }
                } catch (Exception e) {
                    ar.sendAckData(false, "Error processing friend request: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });        // Accept friend request event handler
        server.addEventListener("accept_friend_request", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketClient, Object requestData, AckRequest ar) throws Exception {
                try {
                    System.out.println("=== SERVER RECEIVED ACCEPT_FRIEND_REQUEST ===");
                    System.out.println("Request data: " + requestData);
                    System.out.println("Request data class: " + requestData.getClass().getName());
                    
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = (java.util.Map<String, Object>) requestData;
                    System.out.println("Parsed data map: " + data);
                    
                    Object requestIDObj = data.get("requestID");
                    System.out.println("RequestID object: " + requestIDObj + " (class: " + requestIDObj.getClass().getName() + ")");
                    
                    int requestID = ((Number) requestIDObj).intValue();
                    System.out.println("Parsed requestID: " + requestID);
                    
                    boolean success = serviceUser.acceptFriendRequest(requestID);
                    System.out.println("AcceptFriendRequest result: " + success);
                    
                    if (success) {
                        ar.sendAckData(true, "Friend request accepted");
                        textArea.append("Friend request accepted: Request ID " + requestID + "\n");
                        System.out.println("Sent success response to client");
                    } else {
                        ar.sendAckData(false, "Failed to accept friend request");
                        System.out.println("Sent failure response to client");
                    }
                } catch (Exception e) {
                    System.err.println("Exception in accept_friend_request handler:");
                    e.printStackTrace();
                    ar.sendAckData(false, "Error accepting friend request: " + e.getMessage());
                }
            }
        });
          // Reject friend request event handler
        server.addEventListener("reject_friend_request", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketClient, Object requestData, AckRequest ar) throws Exception {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = (java.util.Map<String, Object>) requestData;
                    int requestID = ((Number) data.get("requestID")).intValue();
                    
                    boolean success = serviceUser.rejectFriendRequest(requestID);
                    if (success) {
                        ar.sendAckData(true, "Friend request rejected");
                        textArea.append("Friend request rejected: Request ID " + requestID + "\n");
                    } else {
                        ar.sendAckData(false, "Failed to reject friend request");
                    }
                } catch (Exception e) {
                    ar.sendAckData(false, "Error rejecting friend request: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });        // Get pending friend requests event handler
        server.addEventListener("get_friend_requests", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient socketClient, Integer userID, AckRequest ar) throws Exception {
                try {
                    List<Model_Friend_Request> pendingRequests = serviceUser.getPendingFriendRequests(userID);
                    
                    // Convert to a format that can be sent via socket.io
                    List<Object> requestsData = new ArrayList<>();
                    for (Model_Friend_Request request : pendingRequests) {
                        java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
                        requestMap.put("requestID", request.getRequestID());
                        requestMap.put("fromUserID", request.getFromUserID());
                        requestMap.put("toUserID", request.getToUserID());
                        requestMap.put("status", request.getStatus());
                        requestMap.put("createdAt", request.getCreatedAt().toString());                        // Include sender info
                        Model_User_Account senderInfo = request.getSenderInfo();
                        requestMap.put("senderUserID", senderInfo.getUserID());
                        requestMap.put("senderUserName", senderInfo.getUserName());
                        requestMap.put("senderGender", senderInfo.getGender());
                        
                        // Debug image data
                        byte[] imageData = senderInfo.getImage();
                        System.out.println("Sending image data for user " + senderInfo.getUserName() + 
                                         ": " + (imageData != null ? imageData.length : "null") + " bytes");
                        requestMap.put("senderImage", senderInfo.getImage());
                        requestMap.put("senderStatus", senderInfo.getStatus());
                          requestsData.add(requestMap);
                    }
                    
                    // Send individual request objects, not as array
                    Object[] responseData = new Object[requestsData.size() + 1];
                    responseData[0] = true;
                    for (int i = 0; i < requestsData.size(); i++) {
                        responseData[i + 1] = requestsData.get(i);
                    }
                    ar.sendAckData(responseData);
                    textArea.append("Friend requests retrieved for UserID: " + userID + " (" + pendingRequests.size() + " requests)\n");
                } catch (Exception e) {
                    ar.sendAckData(false, "Error retrieving friend requests: " + e.getMessage());
                    e.printStackTrace();                }
            }
        });
          // Get friends list event handler
        server.addEventListener("get_friends_list", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient socketClient, Integer userID, AckRequest ar) throws Exception {
                try {
                    System.out.println("=== SERVER RECEIVED GET_FRIENDS_LIST REQUEST ===");
                    System.out.println("UserID: " + userID);
                    List<Model_User_Account> friendsList = serviceUser.getFriendsList(userID);
                    System.out.println("Found " + friendsList.size() + " friends");
                    
                    // Convert to a format that can be sent via socket.io
                    List<Object> friendsData = new ArrayList<>();
                    for (Model_User_Account friend : friendsList) {
                        System.out.println("Adding friend: " + friend.getUserName() + " (ID: " + friend.getUserID() + ")");
                        java.util.Map<String, Object> friendMap = new java.util.HashMap<>();
                        friendMap.put("userID", friend.getUserID());
                        friendMap.put("userName", friend.getUserName());
                        friendMap.put("gender", friend.getGender());
                        friendMap.put("image", friend.getImage());
                        friendMap.put("status", friend.getStatus());
                        friendsData.add(friendMap);
                    }
                    
                    // Send response with friends data
                    Object[] responseData = new Object[friendsData.size() + 1];
                    responseData[0] = true; // Success flag
                    for (int i = 0; i < friendsData.size(); i++) {
                        responseData[i + 1] = friendsData.get(i);
                    }
                    System.out.println("Sending response with " + friendsData.size() + " friends");
                    ar.sendAckData(responseData);
                    textArea.append("Friends list retrieved for UserID: " + userID + " (" + friendsList.size() + " friends)\n");
                } catch (Exception e) {
                    ar.sendAckData(false, "Error retrieving friends list: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        // Group chat event handlers
        server.addEventListener("create_group", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketClient, Object requestData, AckRequest ar) throws Exception {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> data = (java.util.Map<String, Object>) requestData;
                    String groupName = (String) data.get("groupName");
                    String groupDescription = (String) data.get("groupDescription");
                    int createdBy = ((Number) data.get("createdBy")).intValue();
                    
                    // Parse member IDs
                    @SuppressWarnings("unchecked")
                    java.util.List<Integer> memberList = (java.util.List<Integer>) data.get("memberIDs");
                    int[] memberIDs = new int[memberList.size()];
                    for (int i = 0; i < memberList.size(); i++) {
                        memberIDs[i] = memberList.get(i);
                    }
                    
                    Model_Group_Chat group = serviceUser.createGroup(groupName, groupDescription, memberIDs, createdBy);
                    
                    if (group != null) {
                        ar.sendAckData(true, "Group created successfully", group);
                        textArea.append("Group created: " + groupName + " by UserID " + createdBy + "\n");
                        
                        // Notify all group members about the new group
                        notifyGroupCreated(group, memberIDs);
                    } else {
                        ar.sendAckData(false, "Failed to create group");
                    }
                } catch (Exception e) {
                    ar.sendAckData(false, "Error creating group: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
          server.addEventListener("get_user_groups", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient socketClient, Integer userID, AckRequest ar) throws Exception {
                try {
                    System.out.println("=== SERVER GET_USER_GROUPS START ===");
                    System.out.println("UserID: " + userID);
                    
                    List<Model_Group_Chat> groups = serviceUser.getUserGroups(userID);
                    System.out.println("Groups retrieved from DB: " + groups.size() + " groups");
                    
                    // Convert to a format that can be sent via socket.io
                    List<Object> groupsData = new ArrayList<>();
                    for (Model_Group_Chat group : groups) {
                        System.out.println("--- Processing group ---");
                        System.out.println("GroupID: " + group.getGroupID());
                        System.out.println("GroupName: " + group.getGroupName());
                        System.out.println("CreatedBy: " + group.getCreatedBy());
                        
                        java.util.Map<String, Object> groupMap = new java.util.HashMap<>();
                        groupMap.put("groupID", group.getGroupID());
                        groupMap.put("groupName", group.getGroupName());
                        groupMap.put("groupDescription", group.getGroupDescription());
                        groupMap.put("groupImage", group.getGroupImage());
                        groupMap.put("createdBy", group.getCreatedBy());
                        groupMap.put("createdAt", group.getCreatedAt() != null ? group.getCreatedAt().toString() : null);
                        groupMap.put("lastMessage", group.getLastMessage());
                        groupMap.put("lastMessageTime", group.getLastMessageTime() != null ? group.getLastMessageTime().toString() : null);
                        groupMap.put("status", group.getStatus());
                        
                        System.out.println("GroupMap created: " + groupMap);
                        groupsData.add(groupMap);
                    }
                    
                    Object[] responseData = new Object[groupsData.size() + 1];
                    responseData[0] = true;
                    for (int i = 0; i < groupsData.size(); i++) {
                        responseData[i + 1] = groupsData.get(i);
                    }
                    
                    System.out.println("=== SERVER RESPONSE DATA ===");
                    System.out.println("Response length: " + responseData.length);
                    for (int i = 0; i < responseData.length; i++) {
                        System.out.println("responseData[" + i + "]: " + responseData[i]);
                    }
                    System.out.println("=== SERVER GET_USER_GROUPS END ===");
                    
                    ar.sendAckData(responseData);
                    textArea.append("Groups retrieved for UserID: " + userID + " (" + groups.size() + " groups)\n");
                } catch (Exception e) {
                    ar.sendAckData(false, "Error retrieving groups: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
        // Get group member count event handler
        server.addEventListener("get_group_member_count", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient socketClient, Integer groupID, AckRequest ar) throws Exception {
                try {
                    System.out.println("=== SERVER RECEIVED GET_GROUP_MEMBER_COUNT REQUEST ===");
                    System.out.println("Group ID: " + groupID);
                    
                    int memberCount = serviceUser.getGroupMemberCount(groupID);
                    System.out.println("Group " + groupID + " has " + memberCount + " members");
                    
                    ar.sendAckData(true, memberCount);
                    textArea.append("Group member count retrieved for GroupID: " + groupID + " (" + memberCount + " members)\n");
                } catch (Exception e) {
                    ar.sendAckData(false, "Error retrieving group member count: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        server.start();
        textArea.append("Server bắt đầu khởi tạo trên cổng : "+PORT_NUMBER+"\n");
    }
    
    private void sendToClient(Model_Send_Message data, AckRequest ar){
        if(data.getMessageType() == MessageType.IMAGE.getValue()||data.getMessageType() == MessageType.FILE.getValue()) {
            try {
                Model_File file = serviceFile.addFileReceiver(data.getText()); //put lên csdl sql và trả về 1 instance model_file với id file và phần đuôi mở rộng
                serviceFile.initFile(file, data);// tạo file mới null
                ar.sendAckData(file.getFileID());//trả về id file cho client
                
                 if(data.getMessageType() == MessageType.IMAGE.getValue()){
                          for(Model_Client c:listClient){
                          if(c.getUser().getUserID() == data.getToUserID()){
                          c.getClient().sendEvent("receive_data", new Model_File(file.getFileID(), file.getFileExtensions()));
                          break;
                      }
                   }
                }else if(data.getMessageType() == MessageType.FILE.getValue()){
                          
               }
           
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        
        }else if(data.getMessageType() == MessageType.AVATAR.getValue()){
            try {
                
                //next update
              
                
            } catch (Exception e) {
                 e.printStackTrace();
            }        }else{            
             System.out.println("=== SENDING MESSAGE TO CLIENT ===");
             System.out.println("Message Type: " + data.getMessageType());
             System.out.println("From UserID: " + data.getFromUserID());
             System.out.println("To UserID: " + data.getToUserID());
             System.out.println("Message Text: " + data.getText());
             System.out.println("Total clients: " + listClient.size());
             
             boolean clientFound = false;
             for(Model_Client c:listClient){
                System.out.println("Checking client with UserID: " + c.getUser().getUserID());
                if(c.getUser().getUserID() == data.getToUserID()){
                   System.out.println("*** FOUND TARGET CLIENT! SENDING MESSAGE ***");
                   c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getMessageType(), data.getFromUserID(), data.getText(), null));
                   clientFound = true;
                   break;
                }
             } 
             
             if (!clientFound) {
                 System.out.println("*** ERROR: TARGET CLIENT NOT FOUND! ***");
                 System.out.println("Looking for UserID: " + data.getToUserID());
                 System.out.println("Available clients:");
                 for(Model_Client c:listClient){
                     System.out.println("- UserID: " + c.getUser().getUserID() + ", UserName: " + c.getUser().getUserName());
                 }
             }
        }
        
       
    
    }
    private void sendImgFileToClient(Model_Send_Message data, Model_Receive_Image dataImage){
             try {
            for(Model_Client c:listClient){
            if(c.getUser().getUserID() == data.getToUserID()){
               c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getMessageType(), data.getFromUserID(), data.getText(), dataImage));           
               break;
            }
        
        } 
        } catch (Exception e) {
                 System.err.println(e);
        }
    }
    
    private void sendVoiceToClient(Model_Package_Sender data, byte[] dataVoice){
             try {
            for(Model_Client c:listClient){
            if(c.getUser().getUserID() == data.getFileID()){
               c.getClient().sendEvent("receive_voice", new Model_Voice_Sender(data.getFileID(), data.getFromUserID(), dataVoice, data.getFileName()));           
               break;
            }
        
        } 
        } catch (Exception e) {
                 System.err.println(e);
        }
    }
    private void userConnect(Model_User_Account data){
            server.getBroadcastOperations().sendEvent("user_status", data, true);
    }
    private void userDisconnect(int userID){
            server.getBroadcastOperations().sendEvent("user_status", userID, false);
    }
    private void addClient(SocketIOClient socketClient, Model_User_Account user){
            listClient.add(new Model_Client(socketClient, user));
    }
    public int removeClient(SocketIOClient client){
            for(Model_Client d:listClient){
                if(d.getClient() == client){
                     listClient.remove(d);
                     return d.getUser().getUserID();
                }
            }
            return 0;
    }
        /**
     * @return the listClient
     */
    public List<Model_Client> getListClient() {
        return listClient;
    }
    
      /**
     * @return the outputStream
     */
    public ByteArrayOutputStream getOutputStream() {
        return ByteOutputStream;
    }

    
    private ServerSocket serverSocket;
    private Thread run;
    private ByteArrayOutputStream ByteOutputStream;
    
    // Method to notify recipient about new friend request
    private void notifyFriendRequestReceived(int toUserID, int fromUserID) {
        try {
            // Find the recipient client and send notification
            for (Model_Client c : listClient) {
                if (c.getUser().getUserID() == toUserID) {
                    // Get sender's information
                    try {
                        Model_User_Account senderInfo = serviceUser.getUserByID(fromUserID);
                        if (senderInfo != null) {
                            // Send notification to recipient about new friend request
                            c.getClient().sendEvent("friend_request_received", senderInfo);
                            textArea.append("Notified UserID " + toUserID + " about friend request from UserID " + fromUserID + "\n");
                        }
                    } catch (Exception e) {
                        textArea.append("Error getting sender info: " + e.getMessage() + "\n");
                    }
                    break;
                }
            }
        } catch (Exception e) {
            textArea.append("Error notifying friend request: " + e.getMessage() + "\n");
        }
    }

    // Method to notify group members about new group creation
    private void notifyGroupCreated(Model_Group_Chat group, int[] memberIDs) {
        try {
            for (int memberID : memberIDs) {
                for (Model_Client c : listClient) {
                    if (c.getUser().getUserID() == memberID) {
                        // Send notification to group member about new group
                        c.getClient().sendEvent("group_created", group);
                        textArea.append("Notified UserID " + memberID + " about new group: " + group.getGroupName() + "\n");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            textArea.append("Error notifying group creation: " + e.getMessage() + "\n");
        }
    }
}
