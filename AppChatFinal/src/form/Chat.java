/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package form;

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
import net.miginfocom.swing.MigLayout;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class  Chat extends javax.swing.JPanel {

    private Chat_Title chatTitle;
    private Chat_Body chatBody;
    private chat_Bottom chatBottom;
    private HashMap<Integer, List<Object>> chatHistory; // Lưu trữ lịch sử chat theo userID
    private HashMap<Integer, Model_User_Account> friendsCache; // Cache danh sách bạn bè với avatar

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
                System.out.println("=== RECEIVED MESSAGE ===");
                System.out.println("From UserID: " + data.getFromUserID());
                System.out.println("Message: " + data.getText());
                System.out.println("Current chat user: " + (chatTitle.getUser() != null ? chatTitle.getUser().getUserID() : "null"));
                
                // Lưu tin nhắn vào lịch sử
                int fromUserID = data.getFromUserID();
                if (!chatHistory.containsKey(fromUserID)) {
                    chatHistory.put(fromUserID, new ArrayList<>());
                }
                Object[] messageInfo = new Object[]{data, false};
                chatHistory.get(fromUserID).add(messageInfo);                // *** LUÔN HIỂN THỊ TIN NHẮN - BẮT BUỘC ***
                System.out.println("*** FORCING MESSAGE DISPLAY - ABSOLUTE ***");
                
                // Lấy thông tin thật của người gửi 
                Model_User_Account sender = getRealUserInfo(fromUserID);
                System.out.println("*** SENDER INFO: " + (sender != null ? sender.getUserName() + " (ID: " + sender.getUserID() + ")" : "null") + " ***");
                  // HIỂN THỊ TIN NHẮN BÊN TRÁI (từ người khác) - BẮT BUỘC
                System.out.println("*** ABOUT TO ADD MESSAGE TO CHATBODY ***");
                System.out.println("ChatBody object: " + chatBody);
                System.out.println("ChatBody class: " + (chatBody != null ? chatBody.getClass().getName() : "null"));
                
                chatBody.addItemLeft(data, file, sender);
                System.out.println("*** MESSAGE ABSOLUTELY DISPLAYED ON LEFT! ***");
                  // FORCE REFRESH UI - BẮT BUỘC HIỂN THỊ
                chatBody.revalidate();
                chatBody.repaint();
                Chat.this.revalidate();
                Chat.this.repaint();
                
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
                Object[] messageInfo = new Object[]{data, false, senderInfo}; // Add sender info
                chatHistory.get(groupKey).add(messageInfo);
                
                // Kiểm tra xem có đang chat với nhóm này không
                if (chatTitle.getUser() != null && chatTitle.getUser().getUserID() == groupID) {
                    System.out.println("Displaying group message in current chat");
                    // Hiển thị tin nhắn với avatar của người gửi
                    chatBody.addItemLeft(data, null, senderInfo);
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



    }
    public void setUser(Model_User_Account user){
        this.user = user;
        chatTitle.setUserName(user);
        chatBottom.setUser(user);

        // Xóa nội dung chat hiện tại
        chatBody.clearChat();

        // Tải lại lịch sử chat nếu có
        loadChatHistory(user.getUserID());
    }

    public void updateUser(Model_User_Account user){
        chatTitle.updateUser(user);

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
    }    // Phương thức lấy thông tin user thật từ friends cache
    private Model_User_Account getRealUserInfo(int userID) {
        System.out.println("*** GETTING REAL USER INFO FOR ID: " + userID + " ***");
        
        // Kiểm tra cache trước
        if (friendsCache.containsKey(userID)) {
            Model_User_Account cachedFriend = friendsCache.get(userID);
            System.out.println("*** FOUND FRIEND IN CACHE: " + cachedFriend.getUserName() + " ***");
            if (cachedFriend.getImage() != null && cachedFriend.getImage().length > 0) {
                System.out.println("*** CACHED FRIEND HAS AVATAR: " + cachedFriend.getImage().length + " bytes ***");
            } else {
                System.out.println("*** CACHED FRIEND HAS NO AVATAR ***");
            }
            return cachedFriend;
        }
        
        System.out.println("*** FRIEND NOT FOUND IN CACHE, CREATING BASIC USER ***");
        
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
    }

    // Phương thức load friends vào cache
    private void loadFriendsCache() {
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
    }
    // Phương thức refresh friends cache (có thể gọi từ bên ngoài)
    public void refreshFriendsCache() {
        System.out.println("*** REFRESHING FRIENDS CACHE ***");
        loadFriendsCache();
    }
}
