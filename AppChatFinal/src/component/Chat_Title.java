/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package component;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import model.Model_User_Account;

/**
 *
 * @author Admin
 */
public class Chat_Title extends javax.swing.JPanel {

    
    public Model_User_Account getUser() {
        return user;
    }
    
    private Model_User_Account user;
    private JButton leaveGroupButton;
    private boolean isGroupChat = false;
    private int currentGroupID = -1; // Lưu group ID hiện tại

    public Chat_Title() {
        initLeaveGroupButton(); // Khởi tạo nút trước
        initComponents();
    }
    public void setUserName(Model_User_Account user){
        System.out.println("=== setUserName CALLED ===");
        System.out.println("user: " + (user != null ? user.getUserName() : "null"));
        System.out.println("Before - isGroupChat: " + this.isGroupChat);
        System.out.println("Before - currentGroupID: " + this.currentGroupID);
        
        this.user = user;
        this.isGroupChat = false; // Đánh dấu đây là private chat
        this.currentGroupID = -1; // Reset group ID cho private chat
        lbName.setText(user.getUserName());
        leaveGroupButton.setVisible(false); // Ẩn nút Out nhóm
        
        System.out.println("After - isGroupChat: " + this.isGroupChat);
        System.out.println("After - currentGroupID: " + this.currentGroupID);
        System.out.println("=== setUserName COMPLETED ===");
        
        if (user.getStatus()) {
            statusActive();
        }else{
            setStatusText("Đang Offline");
        }
    }
    
    // Method to set group chat info
    public void setGroupChat(String groupName, int groupID) {
        System.out.println("=== setGroupChat CALLED ===");
        System.out.println("groupName: " + groupName);
        System.out.println("groupID: " + groupID);
        System.out.println("Before - isGroupChat: " + this.isGroupChat);
        System.out.println("Before - currentGroupID: " + this.currentGroupID);
        
        this.isGroupChat = true;
        this.currentGroupID = groupID;
        lbName.setText(groupName);
        leaveGroupButton.setVisible(true); // Hiển thị nút Out nhóm cho group chat
        
        System.out.println("After - isGroupChat: " + this.isGroupChat);
        System.out.println("After - currentGroupID: " + this.currentGroupID);
        System.out.println("Button visible: " + leaveGroupButton.isVisible());
        System.out.println("=== setGroupChat COMPLETED ===");
    }
    
    public void updateUser(Model_User_Account user){
        if (this.user == user) {
            lbName.setText(user.getUserName());
            if (user.getStatus()) {
                statusActive();
            }else{
                setStatusText("Đang Offline");
           }
        }
    }
    private void statusActive(){
        lbStatus.setText("Đang hoạt động");
        lbStatus.setForeground(new java.awt.Color(2, 142, 83));
    
    }
    private void setStatusText(String text){
        lbStatus.setText(text);
         lbStatus.setForeground(new Color(160, 160, 160));
    }
    // Method to set member count for group chat
    public void setMemberCount(int memberCount) {
        if (memberCount == 1) {
            lbStatus.setText("1 thành viên");
        } else {
            lbStatus.setText(memberCount + " thành viên");
        }
        lbStatus.setForeground(new java.awt.Color(2, 142, 83));
    }
    
    // Method to set group name for group chat (without setting status)
    public void setGroupName(Model_User_Account groupUser) {
        System.out.println("=== setGroupName CALLED ===");
        System.out.println("groupUser: " + (groupUser != null ? groupUser.getUserName() + " (ID: " + groupUser.getUserID() + ")" : "null"));
        System.out.println("Before - isGroupChat: " + this.isGroupChat);
        System.out.println("Before - currentGroupID: " + this.currentGroupID);
        
        this.user = groupUser;
        this.isGroupChat = true; // Đánh dấu đây là group chat
        this.currentGroupID = groupUser.getUserID(); // Set group ID từ user ID
        lbName.setText(groupUser.getUserName());
        leaveGroupButton.setVisible(true); // Hiển thị nút Out nhóm
        
        System.out.println("After - isGroupChat: " + this.isGroupChat);
        System.out.println("After - currentGroupID: " + this.currentGroupID);
        System.out.println("Button visible: " + leaveGroupButton.isVisible());
        System.out.println("=== setGroupName COMPLETED ===");
        
        // Don't set status here - it will be set by setMemberCount
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        layer = new javax.swing.JLayeredPane();
        lbName = new javax.swing.JLabel();
        lbStatus = new javax.swing.JLabel();

        setBackground(new java.awt.Color(242, 242, 242));

        layer.setLayout(new java.awt.GridLayout(0, 1));

        lbName.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lbName.setForeground(new java.awt.Color(67, 67, 67));
        lbName.setText("Name");
        layer.add(lbName);

        lbStatus.setForeground(new java.awt.Color(2, 142, 83));
        lbStatus.setText("Đang hoạt động");
        layer.add(lbStatus);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(layer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 170, Short.MAX_VALUE)
                .addComponent(leaveGroupButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(layer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(leaveGroupButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane layer;
    private javax.swing.JLabel lbName;
    private javax.swing.JLabel lbStatus;
    // End of variables declaration//GEN-END:variables

    private void initLeaveGroupButton() {
        leaveGroupButton = new JButton("Out nhóm");
        leaveGroupButton.setFont(new java.awt.Font("Tahoma", 1, 11));
        leaveGroupButton.setBackground(new Color(220, 53, 69));
        leaveGroupButton.setForeground(Color.WHITE);
        leaveGroupButton.setBorderPainted(false);
        leaveGroupButton.setFocusPainted(false);
        leaveGroupButton.setVisible(false); // Ẩn mặc định
        
        leaveGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("=== LEAVE GROUP BUTTON CLICKED ===");
                System.out.println("isGroupChat: " + isGroupChat);
                System.out.println("currentGroupID: " + currentGroupID);
                leaveGroup();
            }
        });
    }
    
    private void leaveGroup() {
        System.out.println("=== LEAVE GROUP METHOD CALLED ===");
        System.out.println("isGroupChat: " + isGroupChat);
        System.out.println("currentGroupID: " + currentGroupID);
        
        if (!isGroupChat || currentGroupID == -1) {
            System.out.println("=== LEAVE GROUP CANCELLED - NOT GROUP CHAT OR INVALID ID ===");
            return;
        }
        
        String groupName = lbName.getText();
        int result = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn rời khỏi nhóm \"" + groupName + "\" không?", 
            "Xác nhận rời nhóm", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            try {
                System.out.println("=== LEAVING GROUP ===");
                System.out.println("Leaving group " + currentGroupID + " (" + groupName + ")");
                
                // Lấy current user ID
                service.Service serviceInstance = service.Service.getInstance();
                model.Model_User_Account currentUser = serviceInstance.getUser();
                
                if (currentUser == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Không thể xác định người dùng hiện tại!", 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int currentUserID = currentUser.getUserID();
                System.out.println("Current user ID: " + currentUserID);
                
                // Gọi API để xóa user khỏi group_members table
                serviceInstance.removeMemberFromGroup(currentGroupID, currentUserID, new service.Service.RemoveMemberCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("Successfully removed from group " + currentGroupID);
                        
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(Chat_Title.this, 
                                "Đã rời khỏi nhóm thành công!", 
                                "Thành công", 
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            // Clear chat và refresh UI
                            clearChat();
                            refreshGroupList();
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        System.err.println("Error removing from group: " + error);
                        
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(Chat_Title.this, 
                                "Có lỗi xảy ra khi rời nhóm: " + error, 
                                "Lỗi", 
                                JOptionPane.ERROR_MESSAGE);
                        });
                    }
                });
                
            } catch (Exception ex) {
                System.err.println("Exception leaving group: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Có lỗi xảy ra: " + ex.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearChat() {
        // Reset về trạng thái ban đầu
        user = null;
        isGroupChat = false;
        currentGroupID = -1;
        lbName.setText("Name");
        lbStatus.setText("Đang hoạt động");
        leaveGroupButton.setVisible(false);
        
        // Thông báo để form cha xử lý việc clear chat body
        // Có thể thêm event listener ở đây nếu cần
    }
    
    private void refreshGroupList() {
        // Trigger refresh của danh sách nhóm trong menu bên trái
        try {
            System.out.println("=== ATTEMPTING TO REFRESH GROUP LIST ===");
            // Có thể gọi event để refresh, nhưng hiện tại chỉ log
            System.out.println("Group list refresh triggered - user should restart app for full effect");
        } catch (Exception e) {
            System.err.println("Error refreshing group list: " + e.getMessage());
        }
    }
}
