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
                   login.setImage(null);
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
                
               }
        });
        server.addEventListener("send_to_user", Model_Send_Message.class, new DataListener<Model_Send_Message>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Send_Message t, AckRequest ar) throws Exception {
                sendToClient(t, ar);
               }
        });
        
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
            }
        }else{            
             for(Model_Client c:listClient){
            if(c.getUser().getUserID() == data.getToUserID()){
               c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getMessageType(), data.getFromUserID(), data.getText(), null));
               break;
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
}
