/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package form;

import component.Item_people;
import event.EventMenuLeft;
import event.PublicEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import model.Model_User_Account;
import swing.ScrollBar;

/**
 *
 * @author Admin
 */
public class Menu_Left extends javax.swing.JPanel implements component.Item_Group.SelectionCallback {
    private List<Model_User_Account> userAccount;
    private List<Model_User_Account> friendsList; // Danh sách bạn bè
    private component.FriendSearchPanelNew friendSearchPanel;
    private List<component.Item_Group> groupItems = new ArrayList<>();
    /**
     * Creates new form Menu_Left
     */
    public Menu_Left() {
        initComponents();
        init();
    }    private void init(){
           sp.setVerticalScrollBar(new ScrollBar()); //set cuộn page
        userAccount = new ArrayList<>();// list của all info client khác với clien đăng nhập hiện tại
        friendsList = new ArrayList<>();// list của bạn bè
        PublicEvent.getInstance().addEventMenuLeft(new EventMenuLeft() {               @Override
               public void newUser(List<Model_User_Account> users) {// all list info client khác với clien đăng nhập hiện tại
                   System.out.println("=== NEW USER EVENT - Received " + users.size() + " users ===");
                   for (Model_User_Account d:users){//duyệt từng element của mảng users cho biến d
                       userAccount.add(d);
                       
                       // Chỉ add vào UI nếu không phải đang ở tab Message (vì Message chỉ show bạn bè)
                       if(!menuMessage.isSelected()) {
                           Item_people item = new Item_people(d);
                           // Set fixed height for each item to prevent excessive height
                           item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));
                           item.setPreferredSize(new java.awt.Dimension(200, 60));
                           menuList.add(item);
                       }
                    }
                   // Chỉ refresh nếu không phải tab Message
                   if(!menuMessage.isSelected()) {
                       refreshMenuList();
                   }
                   
                   // Nếu đang ở tab Message, load friends list sau khi user data đã sẵn sàng
                   if(menuMessage.isSelected()) {
                       System.out.println("Message tab is selected, reloading friends list...");
                       javax.swing.Timer delayTimer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
                           @Override
                           public void actionPerformed(java.awt.event.ActionEvent e) {
                               loadFriendsListWithRetry(0);
                           }
                       });
                       delayTimer.setRepeats(false);
                       delayTimer.start();
                   }
                   
                   System.out.println("=== NEW USER EVENT COMPLETED ===");
                  }@Override
               public void userConnect(Model_User_Account user) {
                   // Cập nhật danh sách bạn bè nếu có
                   if(friendsList != null){
                       for(Model_User_Account u:friendsList){
                           if(u.getUserID() == user.getUserID()){
                              u.setStatus(true);
                              break;
                           }
                       }
                   }
                   // Cập nhật danh sách tất cả user
                   for(Model_User_Account u:userAccount){
                       if(u.getUserID() == user.getUserID()){
                          u.setStatus(true);
                          PublicEvent.getInstance().getEventMain().updateUser(u);
                          break;
                       }
                   }
                   if(menuMessage.isSelected()){
                       for(Component com:menuList.getComponents()){
                            // Chỉ cast nếu component là Item_people
                            if(com instanceof Item_people){
                                Item_people item = (Item_people)com;
                                if(item.getUser().getUserID() == user.getUserID()){
                                    item.updateStatus();
                                    break;
                                }
                            }
                       } 
                    }
                  }               @Override
               public void userDisconnect(int userID) {
                   // Cập nhật danh sách bạn bè nếu có
                   if(friendsList != null){
                       for(Model_User_Account u:friendsList){
                           if(u.getUserID() == userID){
                              u.setStatus(false);
                              break;
                           }
                       }
                   }
                   // Cập nhật danh sách tất cả user
                   for(Model_User_Account u:userAccount){
                       if(u.getUserID() == userID){
                          u.setStatus(false);
                          PublicEvent.getInstance().getEventMain().updateUser(u);
                          break;
                       }
                   }
                   if(menuMessage.isSelected()){
                       for(Component com:menuList.getComponents()){
                            // Chỉ cast nếu component là Item_people
                            if(com instanceof Item_people){
                                Item_people item = (Item_people)com;
                                if(item.getUser().getUserID() == userID){
                                    item.updateStatus();
                                    break;
                                }
                            }
                       } 
                    }
                  }
               
           });
       showMessage();
    }    private void showMessage() {
        System.out.println("=== SHOW MESSAGE CALLED - SHOWING FRIENDS ONLY ===");
        // Use BoxLayout for proper vertical stacking of message items, like the original
        menuList.setLayout(new javax.swing.BoxLayout(menuList, javax.swing.BoxLayout.Y_AXIS));
        
        menuList.removeAll();
        
        // Show loading message first
        javax.swing.JLabel loadingLabel = new javax.swing.JLabel("Đang tải danh sách bạn bè...");
        loadingLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
        loadingLabel.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        menuList.add(loadingLabel);
        refreshMenuList();
        
        // Schedule friends list loading after a delay to ensure user is ready
        javax.swing.Timer loadTimer = new javax.swing.Timer(2000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                loadFriendsListWithRetry(0);
            }
        });
        loadTimer.setRepeats(false);
        loadTimer.start();
    }
    
    private void loadFriendsListWithRetry(int attemptCount) {
        if (attemptCount == 0) {
            // Show loading message on first attempt
            javax.swing.JLabel loadingLabel = new javax.swing.JLabel("Đang tải danh sách bạn bè...");
            loadingLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
            loadingLabel.setHorizontalAlignment(javax.swing.JLabel.CENTER);
            menuList.add(loadingLabel);
            refreshMenuList();
        }
          try {
            // Get Service instance
            System.out.println("=== DEBUG: Getting Service class ===");
            Object serviceObj = Class.forName("service.Service").getMethod("getInstance").invoke(null);
            System.out.println("Service instance: " + serviceObj);
            
            // Check if user is available using reflection
            System.out.println("=== DEBUG: Getting user from Service ===");
            Object user = serviceObj.getClass().getMethod("getUser").invoke(serviceObj);
            System.out.println("User object: " + user);
            
            if (user == null && attemptCount < 10) {
                // User not ready yet, retry after delay                System.out.println("User not ready, retrying in 500ms... (attempt " + (attemptCount + 1) + ")");
                javax.swing.Timer timer = new javax.swing.Timer(500, new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        loadFriendsListWithRetry(attemptCount + 1);
                    }
                });
                timer.setRepeats(false);
                timer.start();
                return;
            }
            
            if (user == null) {
                // After 10 attempts, show error
                System.err.println("User still not available after 10 attempts");
                showNoFriendsMessage("Vui lòng đăng nhập lại");
                return;
            }
            
            System.out.println("User found, loading friends list...");
            
            // Get user ID using reflection
            int userID = (Integer) user.getClass().getMethod("getUserID").invoke(user);
            System.out.println("Loading friends for user ID: " + userID);
            
            // Call getFriendsList using reflection
            System.out.println("=== DEBUG: Creating callback ===");
            Class<?> callbackClass = Class.forName("service.Service$FriendsListCallback");
            Object callback = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class[]{callbackClass},
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if ("onResult".equals(method.getName())) {
                            @SuppressWarnings("unchecked")
                            List<Model_User_Account> friends = (List<Model_User_Account>) args[0];
                            handleFriendsListResult(friends);
                        } else if ("onError".equals(method.getName())) {
                            String error = (String) args[0];
                            handleFriendsListError(error);
                        }
                        return null;
                    }
                });
            
            serviceObj.getClass().getMethod("getFriendsList", callbackClass).invoke(serviceObj, callback);
            
        } catch (Exception e) {
            System.err.println("Error loading friends list: " + e.getMessage());
            e.printStackTrace();
            showNoFriendsMessage("Lỗi kết nối");
        }
    }
    
    private void handleFriendsListResult(List<Model_User_Account> friends) {
        System.out.println("=== FRIENDS LIST RECEIVED ===");
        System.out.println("Number of friends: " + friends.size());
        
        // Lưu danh sách bạn bè
        friendsList = friends;
        
        // Ensure UI update happens on EDT
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Updating UI with friends list...");
                menuList.removeAll();
                if (friends.isEmpty()) {
                    showNoFriendsMessage("Chưa có bạn bè nào");
                } else {
                    for (Model_User_Account friend : friends) {
                        System.out.println("Adding friend: " + friend.getUserName());
                        Item_people item = new Item_people(friend);
                        // Set fixed height for each item to prevent excessive height
                        item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));
                        item.setPreferredSize(new java.awt.Dimension(200, 60));
                        menuList.add(item);
                    }
                    System.out.println("All friends added to UI");
                }
                refreshMenuList();
                System.out.println("UI refresh completed");
            }
        });
    }
    
    private void handleFriendsListError(String error) {
        System.err.println("=== ERROR LOADING FRIENDS LIST ===");
        System.err.println("Error: " + error);
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showNoFriendsMessage("Lỗi: " + error);
            }
        });
    }
    
    private void showNoFriendsMessage(String message) {
        menuList.removeAll();
        javax.swing.JLabel messageLabel = new javax.swing.JLabel(message);
        messageLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
        messageLabel.setHorizontalAlignment(javax.swing.JLabel.CENTER);        messageLabel.setForeground(java.awt.Color.GRAY);
        menuList.add(messageLabel);
        refreshMenuList();
    }    private void showGroup() {
        // Use BorderLayout for group functionality
        menuList.setLayout(new java.awt.BorderLayout());
        
        menuList.removeAll();
        
        // Create main group panel
        javax.swing.JPanel groupMainPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        groupMainPanel.setBackground(new java.awt.Color(242, 242, 242));
        
        // Header with create group button
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        headerPanel.setBackground(new java.awt.Color(242, 242, 242));
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
        
        javax.swing.JLabel titleLabel = new javax.swing.JLabel("Nhóm chat");
        titleLabel.setFont(new java.awt.Font("JetBrains Mono", java.awt.Font.BOLD, 14));
        titleLabel.setForeground(new java.awt.Color(85, 85, 85));
        headerPanel.add(titleLabel, java.awt.BorderLayout.WEST);
        
        javax.swing.JButton createGroupButton = new javax.swing.JButton("+ Tạo nhóm");
        createGroupButton.setFont(new java.awt.Font("JetBrains Mono", java.awt.Font.BOLD, 11));
        createGroupButton.setBackground(new java.awt.Color(27, 149, 226));
        createGroupButton.setForeground(java.awt.Color.WHITE);
        createGroupButton.setBorder(new javax.swing.border.EmptyBorder(5, 10, 5, 10));
        createGroupButton.setFocusPainted(false);        createGroupButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Menu_Left.this.showCreateGroupDialog(evt);
            }
        });
        headerPanel.add(createGroupButton, java.awt.BorderLayout.EAST);
        
        groupMainPanel.add(headerPanel, java.awt.BorderLayout.NORTH);
        
        // Group list panel
        javax.swing.JPanel groupListPanel = new javax.swing.JPanel();
        groupListPanel.setLayout(new javax.swing.BoxLayout(groupListPanel, javax.swing.BoxLayout.Y_AXIS));
        groupListPanel.setBackground(new java.awt.Color(242, 242, 242));
        
        // Scroll pane for group list
        javax.swing.JScrollPane groupScrollPane = new javax.swing.JScrollPane(groupListPanel);
        groupScrollPane.setVerticalScrollBar(new ScrollBar());
        groupScrollPane.getVerticalScrollBar().setBackground(java.awt.Color.WHITE);
        groupScrollPane.getViewport().setBackground(new java.awt.Color(242, 242, 242));
        groupScrollPane.setBackground(new java.awt.Color(242, 242, 242));
        groupScrollPane.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        
        groupMainPanel.add(groupScrollPane, java.awt.BorderLayout.CENTER);
        
        menuList.add(groupMainPanel, java.awt.BorderLayout.CENTER);
        
        // Load user groups
        loadUserGroups(groupListPanel);
        
        refreshMenuList();
    }private void showBox() {
        // Use BoxLayout only for friend search functionality
        menuList.setLayout(new javax.swing.BoxLayout(menuList, javax.swing.BoxLayout.Y_AXIS));
        
        menuList.removeAll();
        
        // Reuse existing friend search panel or create new one if not exists
        if (friendSearchPanel == null) {
            friendSearchPanel = new component.FriendSearchPanelNew();
            // Event registration is already handled in the constructor of FriendSearchPanelNew
        }
        
        menuList.add(friendSearchPanel);
        
        refreshMenuList();
    }
      private void refreshMenuList() {
        menuList.repaint();
        menuList.revalidate();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menu = new javax.swing.JLayeredPane();
        menuMessage = new component.MenuButton();
        menuGroup = new component.MenuButton();
        menuBox = new component.MenuButton();
        sp = new javax.swing.JScrollPane();
        menuList = new javax.swing.JLayeredPane();

        setBackground(new java.awt.Color(242, 242, 242));

        menu.setBackground(new java.awt.Color(229, 229, 229));
        menu.setOpaque(true);
        menu.setLayout(new java.awt.GridLayout(1, 3));

        menuMessage.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/icon/message_selected.png"))); // NOI18N
        menuMessage.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/icon/message.png"))); // NOI18N
        menuMessage.setSelected(true);
        menuMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMessageActionPerformed(evt);
            }
        });
        menu.add(menuMessage);

        menuGroup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/group.png"))); // NOI18N
        menuGroup.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/icon/group_selected.png"))); // NOI18N
        menuGroup.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/icon/group.png"))); // NOI18N
        menuGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuGroupActionPerformed(evt);
            }
        });
        menu.add(menuGroup);

        menuBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/box.png"))); // NOI18N
        menuBox.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/icon/box_selected.png"))); // NOI18N
        menuBox.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/icon/box.png"))); // NOI18N
        menuBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuBoxActionPerformed(evt);
            }
        });
        menu.add(menuBox);

        sp.setBackground(new java.awt.Color(242, 242, 242));
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        menuList.setBackground(new java.awt.Color(242, 242, 242));
        menuList.setOpaque(true);

        javax.swing.GroupLayout menuListLayout = new javax.swing.GroupLayout(menuList);
        menuList.setLayout(menuListLayout);
        menuListLayout.setHorizontalGroup(
            menuListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        menuListLayout.setVerticalGroup(
            menuListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 541, Short.MAX_VALUE)
        );

        sp.setViewportView(menuList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(menu, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
            .addComponent(sp, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(menu, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(sp, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
//set hiển thị icon 2nd của nút khi được nhấn
    private void menuMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMessageActionPerformed
          if(!menuMessage.isSelected()){
          menuMessage.setSelected(true);
          menuGroup.setSelected(false);
          menuBox.setSelected(false);
          showMessage();
          }
          
    }//GEN-LAST:event_menuMessageActionPerformed

    private void menuGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGroupActionPerformed
         if(!menuGroup.isSelected()){
         menuMessage.setSelected(false);
         menuGroup.setSelected(true);
         menuBox.setSelected(false);
         showGroup();
         }
    }//GEN-LAST:event_menuGroupActionPerformed

    private void menuBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBoxActionPerformed
         if(!menuBox.isSelected()){
         menuMessage.setSelected(false);
         menuGroup.setSelected(false);
         menuBox.setSelected(true);
         showBox();
         }
    }//GEN-LAST:event_menuBoxActionPerformed

    private void showCreateGroupDialog(java.awt.event.ActionEvent evt) {
        // First, get user's friends list using reflection
        try {
            Object serviceObj = Class.forName("service.Service").getMethod("getInstance").invoke(null);
              // Create anonymous callback using reflection
            Class<?> callbackClass = Class.forName("service.Service$FriendsListCallback");
            Object callback = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class[]{callbackClass},
                new java.lang.reflect.InvocationHandler() {                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if ("onResult".equals(method.getName())) {
                            @SuppressWarnings("unchecked")
                            List<Model_User_Account> friends = (List<Model_User_Account>) args[0];
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(Menu_Left.this);
                                component.CreateGroupDialog dialog = new component.CreateGroupDialog(parentFrame, friends);
                                dialog.setVisible(true);
                                
                                // Refresh groups list after dialog closes
                                if (menuGroup.isSelected()) {
                                    showGroup();
                                }
                            });
                        } else if ("onError".equals(method.getName())) {
                            String error = (String) args[0];
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                javax.swing.JOptionPane.showMessageDialog(Menu_Left.this, 
                                    "Không thể tải danh sách bạn bè: " + error, 
                                    "Lỗi", 
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
                            });
                        }
                        return null;
                    }
                }
            );
            
            serviceObj.getClass().getMethod("getFriendsList", callbackClass).invoke(serviceObj, callback);
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải danh sách bạn bè", 
                "Lỗi", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadUserGroups(javax.swing.JPanel groupListPanel) {
        try {
            Object serviceObj = Class.forName("service.Service").getMethod("getInstance").invoke(null);
            
            // Create anonymous callback using reflection
            Class<?> callbackClass = Class.forName("service.Service$GroupListCallback");
            Object callback = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class[]{callbackClass},
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if ("onSuccess".equals(method.getName())) {
                            @SuppressWarnings("unchecked")
                            List<model.Model_Group_Chat> groups = (List<model.Model_Group_Chat>) args[0];
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                updateGroupsList(groupListPanel, groups);
                            });
                        } else if ("onError".equals(method.getName())) {
                            String error = (String) args[0];
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                System.err.println("Error loading groups: " + error);
                                showEmptyGroupState(groupListPanel);
                            });
                        }
                        return null;
                    }
                }
            );
            
            serviceObj.getClass().getMethod("getUserGroups", callbackClass).invoke(serviceObj, callback);
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.SwingUtilities.invokeLater(() -> {
                showEmptyGroupState(groupListPanel);
            });
        }
    }
      private void updateGroupsList(javax.swing.JPanel groupListPanel, List<model.Model_Group_Chat> groups) {
        System.out.println("=== UPDATING GROUPS LIST ===");
        System.out.println("Number of groups received: " + groups.size());
        
        groupListPanel.removeAll();
        groupItems.clear();
        
        if (groups.isEmpty()) {
            showEmptyGroupState(groupListPanel);
            return;
        }
        
        for (model.Model_Group_Chat group : groups) {
            System.out.println("Processing group:");
            System.out.println("- Group ID: " + group.getGroupID());
            System.out.println("- Group Name: " + group.getGroupName());
            System.out.println("- Created By: " + group.getCreatedBy());
            
            component.Item_Group groupItem = new component.Item_Group(group, this);
            groupItems.add(groupItem);
            groupListPanel.add(groupItem);
            groupListPanel.add(javax.swing.Box.createVerticalStrut(2));
        }
        
        groupListPanel.revalidate();
        groupListPanel.repaint();
    }
    
    private void showEmptyGroupState(javax.swing.JPanel groupListPanel) {
        groupListPanel.removeAll();
        
        javax.swing.JPanel emptyPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        emptyPanel.setBackground(new java.awt.Color(242, 242, 242));
        emptyPanel.setBorder(new javax.swing.border.EmptyBorder(50, 20, 50, 20));
        
        javax.swing.JLabel emptyLabel = new javax.swing.JLabel("<html><div style='text-align: center;'>" +
                                     "Bạn chưa tham gia nhóm nào<br>" +
                                     "Hãy tạo nhóm mới để bắt đầu trò chuyện!" +
                                     "</div></html>");
        emptyLabel.setFont(new java.awt.Font("JetBrains Mono", java.awt.Font.PLAIN, 12));
        emptyLabel.setForeground(new java.awt.Color(128, 128, 128));
        emptyLabel.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        emptyPanel.add(emptyLabel, java.awt.BorderLayout.CENTER);
        
        groupListPanel.add(emptyPanel);
        groupListPanel.revalidate();
        groupListPanel.repaint();
    }    // Public getter for friends list to be used by Chat.java
    public List<Model_User_Account> getFriendsList() {
        return friendsList;
    }
    
    // Public method to find a friend by user ID
    public Model_User_Account findFriend(int userID) {
        if (friendsList == null) {
            return null;
        }
        for (Model_User_Account friend : friendsList) {
            if (friend.getUserID() == userID) {
                return friend;
            }
        }
        return null;
    }
    
    @Override
    public void onItemSelected(component.Item_Group selectedItem) {
        // Deselect all groups first
        for (component.Item_Group item : groupItems) {
            item.setSelected(false);
        }
        
        // Select the clicked item
        selectedItem.setSelected(true);
        
        // Repaint to update visual state
        // Find the parent panel and repaint it
        if (menuList != null) {
            menuList.repaint();
        }
    }
    
    public void updateGroupAvatar(int groupID, byte[] newAvatar) {
        // Find the group item with the specified groupID and update its avatar
        for (component.Item_Group groupItem : groupItems) {
            if (groupItem.getGroup().getGroupID() == groupID) {
                // Update the group's avatar data
                groupItem.getGroup().setGroupImage(newAvatar);
                // Refresh the avatar display
                groupItem.refreshAvatar();
                break;
            }
        }
        
        // Repaint the menu to show the updated avatar
        if (menuList != null) {
            menuList.repaint();
        }
    }
    
    public void refreshGroupsList() {
        // Reload groups from server to get updated avatar data
        if (menuGroup.isSelected()) {
            showGroup();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane menu;
    private component.MenuButton menuBox;
    private component.MenuButton menuGroup;
    private javax.swing.JLayeredPane menuList;
    private component.MenuButton menuMessage;
    private javax.swing.JScrollPane sp;
    // End of variables declaration//GEN-END:variables
}
