/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package form;

import app.MessageType;
import component.Chat_Body;
import component.Chat_Title;
import component.chat_Bottom;
import event.EventChat;
import event.PublicEvent;
import java.io.File;
import model.Model_Receive_Message;
import model.Model_Send_Message;
import model.Model_User_Account;
import model.Model_Voice_Receive;
import model.Model_Group_Chat;
import net.miginfocom.swing.MigLayout;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class  Chat extends javax.swing.JPanel {    private Chat_Title chatTitle;
    private Chat_Body chatBody;
    private chat_Bottom chatBottom;
    private HashMap<Integer, List<Object>> chatHistory; // Lưu trữ lịch sử chat theo userID
    private HashMap<Integer, Model_User_Account> friendsCache; // Cache danh sách bạn bè với avatar
    private Menu_Left menuLeft; // Reference to Menu_Left for accessing friends list

    public Chat() {
        initComponents();
        init();
        chatHistory = new HashMap<>();
        friendsCache = new HashMap<>();
        loadFriendsCache(); // Load friends vào cache khi khởi tạo
    }
    private void init(){
        setLayout(new MigLayout("fillx", "0[fill]0","0[]0[100%, fill]0[shrink 0]0"));
        chatTitle = new Chat_Title();
        chatBody = new Chat_Body();
        chatBottom = new chat_Bottom();
        PublicEvent.getInstance().addEventChat(new EventChat() {
            @Override
            public void sendMessage(Model_Send_Message data) {
                chatBody.addItemRight(data);
                saveToChatHistory(user.getUserID(), data, true);
            }
            @Override
            public void sendMessage(Model_Voice_Receive data) {
                chatBody.addItemRight(data);
                saveToChatHistory(user.getUserID(), data, true);
            }            @Override
            public void receiveMessage(Model_Receive_Message data, File file) {
                System.out.println("=== RECEIVED PRIVATE MESSAGE ===");
                System.out.println("From UserID: " + data.getFromUserID());
                System.out.println("Message: " + data.getText());
                System.out.println("Current chat user: " + (chatTitle.getUser() != null ? chatTitle.getUser().getUserID() : "null"));
                
                // Lưu tin nhắn vào lịch sử LUÔN LUÔN (bất kể có hiển thị hay không)
                int fromUserID = data.getFromUserID();
                if (!chatHistory.containsKey(fromUserID)) {
                    chatHistory.put(fromUserID, new ArrayList<>());
                }
                Object[] messageInfo = new Object[]{data, false};
                chatHistory.get(fromUserID).add(messageInfo);
                
                // *** CHỈ HIỂN THỊ TIN NHẮN KHI ĐANG CHAT VỚI NGƯỜI GỬI ***
                // Kiểm tra: đang chat với đúng người gửi và không phải group chat
                boolean isCorrectPrivateChat = (chatTitle.getUser() != null && 
                                               chatTitle.getUser().getUserID() == fromUserID &&
                                               chatTitle.getUser().getUserName() != null && 
                                               !chatTitle.getUser().getUserName().startsWith("Group"));
                
                if (isCorrectPrivateChat) {
                    System.out.println("*** DISPLAYING PRIVATE MESSAGE - CORRECT CHAT ***");
                    
                    // Lấy thông tin thật của người gửi 
                    Model_User_Account sender = getRealUserInfo(fromUserID);
                    System.out.println("*** SENDER INFO: " + (sender != null ? sender.getUserName() + " (ID: " + sender.getUserID() + ")" : "null") + " ***");
                    
                    // HIỂN THỊ TIN NHẮN BÊN TRÁI (từ người khác)
                    System.out.println("*** ABOUT TO ADD MESSAGE TO CHATBODY ***");
                    System.out.println("ChatBody object: " + chatBody);
                    System.out.println("ChatBody class: " + (chatBody != null ? chatBody.getClass().getName() : "null"));
                    
                    chatBody.addItemLeft(data, file, sender);
                    System.out.println("*** PRIVATE MESSAGE DISPLAYED ON LEFT! ***");
                    
                    // FORCE REFRESH UI
                    chatBody.revalidate();
                    chatBody.repaint();
                    Chat.this.revalidate();
                    Chat.this.repaint();
                } else {
                    System.out.println("*** PRIVATE MESSAGE NOT DISPLAYED - DIFFERENT CHAT ***");
                    System.out.println("*** Message from: " + fromUserID + ", Current chat: " + 
                                     (chatTitle.getUser() != null ? chatTitle.getUser().getUserID() : "none") + " ***");
                }
                
                // Force scroll to bottom to see new message
                javax.swing.SwingUtilities.invokeLater(() -> {
                    try {
                        Thread.sleep(100); // Wait a bit for UI to update
                        chatBody.revalidate();
                        chatBody.repaint();
                        System.out.println("*** FORCED UI REFRESH COMPLETED ***");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                System.out.println("*** MESSAGE PROCESSING COMPLETED ***");
            }
            @Override
            public void receiveMessage(Model_Voice_Receive data) {
                // Lưu tin nhắn voice vào lịch sử bất kể người dùng hiện tại là ai
                int fromUserID = data.getFromUserID();
                if (!chatHistory.containsKey(fromUserID)) {
                    chatHistory.put(fromUserID, new ArrayList<>());
                }
                Object[] messageInfo = new Object[]{data, false};
                chatHistory.get(fromUserID).add(messageInfo);

                // Chỉ hiển thị tin nhắn nếu đang chat với người gửi
                if(chatTitle.getUser() != null && chatTitle.getUser().getUserID() == fromUserID){
                    // Truyền thông tin của người gửi thay vì người dùng hiện tại
                    chatBody.addItemLeft(data, chatTitle.getUser());
                }
            }            @Override
            public void receiveGroupMessage(Model_Receive_Message data, Model_User_Account senderInfo, int groupID) {
                System.out.println("=== CHAT RECEIVED GROUP MESSAGE ===");
                System.out.println("Group ID: " + groupID);
                System.out.println("Sender: " + (senderInfo != null ? senderInfo.getUserName() : "Unknown"));
                System.out.println("Message: " + data.getText());
                  // Lưu tin nhắn vào lịch sử nhóm (sử dụng groupID âm để phân biệt với user)
                int groupKey = -groupID; // Use negative to distinguish from userID
                if (!chatHistory.containsKey(groupKey)) {
                    chatHistory.put(groupKey, new ArrayList<>());
                }
                  // Kiểm tra xem tin nhắn có phải từ người dùng hiện tại không để lưu đúng flag
                Model_User_Account currentUser = service.Service.getInstance().getUser();
                boolean isFromCurrentUser = (senderInfo != null && currentUser != null && senderInfo.getUserID() == currentUser.getUserID());
                Object[] messageInfo = new Object[]{data, isFromCurrentUser, senderInfo}; // Add sender info and current user flag
                chatHistory.get(groupKey).add(messageInfo);
                  // Kiểm tra xem có đang chat với nhóm này không
                if (chatTitle.getUser() != null && chatTitle.getUser().getUserID() == groupID) {
                    System.out.println("Displaying group message in current chat");
                      // Kiểm tra xem tin nhắn có phải từ người dùng hiện tại không
                    if (senderInfo != null && currentUser != null && senderInfo.getUserID() == currentUser.getUserID()) {
                        // Tin nhắn từ người dùng hiện tại - hiển thị bên phải
                        System.out.println("Message from current user - displaying on right");
                          // Convert Model_Receive_Message to Model_Send_Message để sử dụng addItemRight
                        Model_Send_Message sendMessage = new Model_Send_Message(data.getMessageType(), data.getFromUserID(), groupID, data.getText());
                        chatBody.addItemRight(sendMessage);
                    } else {
                        // Tin nhắn từ người khác trong nhóm - hiển thị bên trái với avatar của người gửi
                        System.out.println("Message from other user - displaying on left with sender avatar");
                        chatBody.addItemLeft(data, null, senderInfo);
                    }
                } else {
                    System.out.println("Not currently chatting with this group (current: " + 
                                     (chatTitle.getUser() != null ? chatTitle.getUser().getUserID() : "none") + 
                                     ", message group: " + groupID + ")");
                }
            }
        });
        add(chatTitle, "wrap");
        add(chatBody, "wrap");
        add(chatBottom, "h ::50%");



    }    public void setUser(Model_User_Account user){
        this.user = user;
        
        // Kiểm tra xem có phải chat nhóm không
        boolean isGroupChat = false;
        if (user != null && user.getUserID() > 0) {
            try {
                // Thử gọi method isGroup nếu có
                isGroupChat = user.isGroup();
            } catch (Exception e) {
                // Nếu không có method isGroup, có thể kiểm tra theo cách khác
                // Ví dụ: nếu userName bắt đầu bằng "Group:" hoặc có pattern đặc biệt
                isGroupChat = user.getUserName() != null && 
                             (user.getUserName().startsWith("Group:") || 
                              user.getUserName().equals("test")); // Tạm thời check theo tên
            }
        }
        
        if (isGroupChat) {
            // Đây là group chat - sử dụng setGroupName và lấy member count
            System.out.println("=== setUser DETECTED GROUP CHAT ===");
            System.out.println("Group ID: " + user.getUserID());
            System.out.println("Group Name: " + user.getUserName());
            
            chatTitle.setGroupName(user);
            chatBottom.setUser(user);
            
            // Lấy số lượng thành viên cho group
            service.Service.getInstance().getGroupMemberCount(user.getUserID(), new service.Service.GroupMemberCountCallback() {
                @Override
                public void onSuccess(int memberCount) {
                    System.out.println("=== setUser GROUP MEMBER COUNT RECEIVED ===");
                    System.out.println("Group " + user.getUserID() + " has " + memberCount + " members");
                    chatTitle.setMemberCount(memberCount);
                }
                
                @Override
                public void onError(String error) {
                    System.err.println("=== setUser ERROR GETTING GROUP MEMBER COUNT ===");
                    System.err.println("Error: " + error);
                    // Fallback to default text
                    chatTitle.setMemberCount(0);
                }
            });
        } else {
            // Chat cá nhân bình thường
            chatTitle.setUserName(user);
            chatBottom.setUser(user);
        }        // Xóa nội dung chat hiện tại
        chatBody.clearChat();

        // Load chat history based on type (group or individual)
        if (user != null && user.getUserID() > 0) {
            if (isGroupChat) {
                System.out.println("=== LOADING GROUP CHAT HISTORY ===");
                System.out.println("Group ID: " + user.getUserID());
                System.out.println("Group Name: " + user.getUserName());
                
                // Load tin nhắn nhóm từ database
                loadGroupMessageHistory(user.getUserID());
            } else {
                // Chat cá nhân - tải lịch sử chat bình thường
                loadChatHistory(user.getUserID());
            }
        }
    }
    
    // Set reference to Menu_Left for accessing friends list
    public void setMenuLeft(Menu_Left menuLeft) {
        this.menuLeft = menuLeft;
        System.out.println("*** MENU_LEFT REFERENCE SET ***");
    }

    public void updateUser(Model_User_Account user){
        chatTitle.updateUser(user);

    }    // Method to set user for group chat (group info)
    public void setGroup(Model_Group_Chat group) {
        System.out.println("=== SETTING GROUP ===");
        System.out.println("Group ID: " + group.getGroupID());
        System.out.println("Group Name: " + group.getGroupName());
        
        this.user = new Model_User_Account(); // Create a fake user object for group
        this.user.setUserID(group.getGroupID());
        this.user.setUserName(group.getGroupName());
        this.user.setStatus(true); // Groups are always "online"
        
        System.out.println("=== CALLING setGroupChat ===");
        chatTitle.setGroupChat(group.getGroupName(), group.getGroupID());
        chatBottom.setUser(this.user);
        
        System.out.println("=== CALLING getGroupMemberCount ===");
        // Get and display group member count
        service.Service.getInstance().getGroupMemberCount(group.getGroupID(), new service.Service.GroupMemberCountCallback() {
            @Override
            public void onSuccess(int memberCount) {
                System.out.println("=== GROUP MEMBER COUNT RECEIVED ===");
                System.out.println("Group " + group.getGroupID() + " has " + memberCount + " members");
                System.out.println("=== CALLING setMemberCount ===");
                chatTitle.setMemberCount(memberCount);
            }
            
            @Override
            public void onError(String error) {
                System.err.println("Error getting group member count: " + error);
                // Fallback to default status text
                chatTitle.setMemberCount(0);
            }
        });
        
        // Clear current chat content
        chatBody.clearChat();
        
        // Load group message history from database
        loadGroupMessageHistory(group.getGroupID());
    }
      // Load group message history from database
    private void loadGroupMessageHistory(int groupID) {
        System.out.println("=== LOADING GROUP MESSAGE HISTORY FROM DATABASE ===");
        System.out.println("Group ID: " + groupID);
        
        try {
            service.Service.getInstance().loadGroupMessages(groupID, new service.Service.GroupMessagesCallback() {
                @Override
                public void onSuccess(List<model.Model_Group_Message_Display> messages) {
                    System.out.println("=== GROUP MESSAGES LOADED SUCCESSFULLY ===");
                    System.out.println("Number of messages: " + messages.size());
                    
                    // Hiển thị các tin nhắn trên UI (phải chạy trên EDT)
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        for (model.Model_Group_Message_Display msg : messages) {
                            try {
                                // Tạo Model_Receive_Message từ group message
                                app.MessageType msgType = app.MessageType.TEXT; // Default to TEXT
                                Model_Receive_Message receiveMsg = new Model_Receive_Message(
                                    msgType, 
                                    msg.getSenderID(), 
                                    msg.getMessageContent()
                                );
                                  // Tạo sender info với avatar
                                Model_User_Account senderInfo = new Model_User_Account();
                                senderInfo.setUserID(msg.getSenderID());
                                senderInfo.setUserName(msg.getSenderName());
                                senderInfo.setImage(msg.getSenderAvatar());
                                  // Kiểm tra xem tin nhắn có phải từ người dùng hiện tại không
                                Model_User_Account currentUser = service.Service.getInstance().getUser();
                                if (currentUser != null && msg.getSenderID() == currentUser.getUserID()) {
                                    // Tin nhắn từ người dùng hiện tại - hiển thị bên phải
                                    System.out.println("History message from current user - displaying on right");
                                    Model_Send_Message sendMessage = new Model_Send_Message(msgType, msg.getSenderID(), groupID, msg.getMessageContent());
                                    chatBody.addItemRight(sendMessage);
                                } else {
                                    // Tin nhắn từ người khác - hiển thị bên trái với avatar
                                    System.out.println("History message from other user - displaying on left");
                                    chatBody.addItemLeft(receiveMsg, null, senderInfo);
                                }
                                
                                System.out.println("Displayed message from " + msg.getSenderName() + ": " + msg.getMessageContent());
                                
                            } catch (Exception e) {
                                System.err.println("Error displaying group message: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        // Refresh UI
                        chatBody.revalidate();
                        chatBody.repaint();
                        
                        System.out.println("=== GROUP MESSAGE HISTORY DISPLAYED ===");
                    });
                }
                
                @Override
                public void onError(String error) {
                    System.err.println("=== ERROR LOADING GROUP MESSAGES ===");
                    System.err.println("Error: " + error);
                }
            });
        } catch (Exception e) {
            System.err.println("=== EXCEPTION LOADING GROUP MESSAGES ===");
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 586, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 448, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private Model_User_Account user;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    // Thêm phương thức lưu tin nhắn vào lịch sử
    private void saveToChatHistory(int userID, Object messageData, boolean isRight) {
        if (!chatHistory.containsKey(userID)) {
            chatHistory.put(userID, new ArrayList<>());
        }

        // Lưu tin nhắn và thông tin là tin nhắn gửi hay nhận
        Object[] messageInfo = new Object[]{messageData, isRight};
        chatHistory.get(userID).add(messageInfo);
    }

    // Thêm phương thức tải lịch sử chat
    private void loadChatHistory(int userID) {
        if (chatHistory.containsKey(userID)) {
            List<Object> history = chatHistory.get(userID);
            for (Object obj : history) {
                Object[] messageInfo = (Object[]) obj;
                Object messageData = messageInfo[0];
                boolean isRight = (boolean) messageInfo[1];

                if (messageData instanceof Model_Send_Message) {
                    if (isRight) {
                        chatBody.addItemRight((Model_Send_Message) messageData);
                    }
                } else if (messageData instanceof Model_Receive_Message) {
                    if (!isRight) {
                        chatBody.addItemLeft((Model_Receive_Message) messageData, null, user);
                    }
                } else if (messageData instanceof Model_Voice_Receive) {
                    if (isRight) {
                        chatBody.addItemRight((Model_Voice_Receive) messageData);
                    } else {
                        chatBody.addItemLeft((Model_Voice_Receive) messageData, user);
                    }
                }
            }
        }
    }    // Phương thức lấy thông tin user thật từ Menu_Left friends list
    private Model_User_Account getRealUserInfo(int userID) {
        System.out.println("*** GETTING REAL USER INFO FOR ID: " + userID + " ***");
        
        // Kiểm tra Menu_Left friends list trước
        if (menuLeft != null) {
            Model_User_Account friend = menuLeft.findFriend(userID);
            if (friend != null) {
                System.out.println("*** FOUND FRIEND IN MENU_LEFT: " + friend.getUserName() + " ***");
                if (friend.getImage() != null && friend.getImage().length > 0) {
                    System.out.println("*** FRIEND HAS AVATAR: " + friend.getImage().length + " bytes ***");
                } else {
                    System.out.println("*** FRIEND HAS NO AVATAR ***");
                }
                return friend;
            } else {
                System.out.println("*** FRIEND NOT FOUND IN MENU_LEFT ***");
            }
        } else {
            System.out.println("*** MENU_LEFT REFERENCE IS NULL ***");
        }
        
        // Fallback to local cache
        if (friendsCache.containsKey(userID)) {
            Model_User_Account cachedFriend = friendsCache.get(userID);
            System.out.println("*** FOUND FRIEND IN LOCAL CACHE: " + cachedFriend.getUserName() + " ***");
            if (cachedFriend.getImage() != null && cachedFriend.getImage().length > 0) {
                System.out.println("*** CACHED FRIEND HAS AVATAR: " + cachedFriend.getImage().length + " bytes ***");
            } else {
                System.out.println("*** CACHED FRIEND HAS NO AVATAR ***");
            }
            return cachedFriend;
        }
        
        System.out.println("*** FRIEND NOT FOUND ANYWHERE, CREATING BASIC USER ***");
        
        // Nếu không có trong cache, tạo user với thông tin cơ bản
        Model_User_Account realUser = new Model_User_Account();
        realUser.setUserID(userID);
        
        // Lấy tên từ database/friends list (tạm thời dùng tên mặc định)
        if (userID == 10) {
            realUser.setUserName("phuc");
        } else if (userID == 11) {
            realUser.setUserName("nhu");
        } else {
            realUser.setUserName("User_" + userID);
        }
        
        realUser.setStatus(true);
        
        System.out.println("*** RETURNING BASIC USER INFO: " + realUser.getUserName() + " ***");
        return realUser;
    }
    
    // Phương thức tìm thông tin user theo ID
    private Model_User_Account findUserInfo(int userID) {
        // Tìm trong danh sách user hiện có (có thể từ Menu_Left hoặc Service)
        try {
            // Tạm thời tạo user với thông tin cơ bản
            Model_User_Account user = new Model_User_Account();
            user.setUserID(userID);
            user.setUserName("User " + userID); // Tên tạm thời
            user.setStatus(true);
            return user;
        } catch (Exception e) {
            return null;
        }
    }    // Phương thức load friends vào cache (tạm thời comment để tránh lỗi)
    private void loadFriendsCache() {
        System.out.println("*** LOADING FRIENDS CACHE (DISABLED) ***");
        // TODO: Fix Service implementation later
        /*
        System.out.println("*** LOADING FRIENDS CACHE ***");
        try {
            service.Service.getInstance().getFriendsList(new service.Service.FriendsListCallback() {
                @Override
                public void onResult(java.util.List<Model_User_Account> friends) {
                    System.out.println("*** UPDATING FRIENDS CACHE ***");
                    System.out.println("Number of friends: " + friends.size());
                    
                    friendsCache.clear();
                    for (Model_User_Account friend : friends) {
                        friendsCache.put(friend.getUserID(), friend);
                        System.out.println("*** CACHED FRIEND: " + friend.getUserName() + " (ID: " + friend.getUserID() + ") ***");
                        if (friend.getImage() != null && friend.getImage().length > 0) {
                            System.out.println("*** FRIEND HAS AVATAR: " + friend.getImage().length + " bytes ***");
                        } else {
                            System.out.println("*** FRIEND HAS NO AVATAR ***");
                        }
                    }
                    System.out.println("*** FRIENDS CACHE UPDATED ***");
                }
                
                @Override
                public void onError(String error) {
                    System.err.println("*** ERROR LOADING FRIENDS CACHE: " + error + " ***");
                }
            });
        } catch (Exception e) {
            System.err.println("*** ERROR INITIALIZING FRIENDS CACHE: " + e.getMessage() + " ***");
        }
        */    }
    // Phương thức refresh friends cache (có thể gọi từ bên ngoài)
    public void refreshFriendsCache() {
        System.out.println("*** REFRESHING FRIENDS CACHE (DISABLED) ***");
        // loadFriendsCache();
    }
}
