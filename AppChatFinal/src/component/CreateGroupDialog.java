/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Model_User_Account;
import service.Service;

/**
 *
 * @author Admin
 */
public class CreateGroupDialog extends JDialog {
    private JTextField groupNameField;
    private JTextArea groupDescriptionArea;
    private JPanel friendsPanel;
    private List<Model_User_Account> availableFriends;
    private List<JCheckBox> friendCheckboxes;
    private JButton createButton;
    private JButton cancelButton;
    
    public CreateGroupDialog(Frame parent, List<Model_User_Account> friends) {
        super(parent, "Tạo nhóm", true);
        this.availableFriends = friends;
        this.friendCheckboxes = new ArrayList<>();
        initComponents();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(400, 600);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(27, 149, 226));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Tạo nhóm mới");
        titleLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(242, 242, 242));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Group info panel
        JPanel groupInfoPanel = createGroupInfoPanel();
        mainPanel.add(groupInfoPanel, BorderLayout.NORTH);
        
        // Friends selection panel
        JPanel friendsSelectionPanel = createFriendsSelectionPanel();
        mainPanel.add(friendsSelectionPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createGroupInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(242, 242, 242));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Thông tin nhóm",
            0, 0, new Font("JetBrains Mono", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Group Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Tên nhóm:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        groupNameField = new JTextField();
        groupNameField.setPreferredSize(new Dimension(250, 30));
        groupNameField.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        panel.add(groupNameField, gbc);
        
        // Group Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Mô tả:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.3;
        groupDescriptionArea = new JTextArea(3, 20);
        groupDescriptionArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        groupDescriptionArea.setLineWrap(true);
        groupDescriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(groupDescriptionArea);
        panel.add(descScrollPane, gbc);
        
        return panel;
    }
    
    private JPanel createFriendsSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(242, 242, 242));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Chọn thành viên",
            0, 0, new Font("JetBrains Mono", Font.BOLD, 12)
        ));
        
        // Select All checkbox
        JPanel selectAllPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectAllPanel.setBackground(new Color(242, 242, 242));
        JCheckBox selectAllCheckbox = new JCheckBox("Chọn tất cả");
        selectAllCheckbox.setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        selectAllCheckbox.setBackground(new Color(242, 242, 242));
        selectAllCheckbox.addActionListener(e -> {
            boolean selected = selectAllCheckbox.isSelected();
            for (JCheckBox cb : friendCheckboxes) {
                cb.setSelected(selected);
            }
        });
        selectAllPanel.add(selectAllCheckbox);
        panel.add(selectAllPanel, BorderLayout.NORTH);
        
        // Friends list
        friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        friendsPanel.setBackground(new Color(242, 242, 242));
        
        if (availableFriends != null && !availableFriends.isEmpty()) {
            for (Model_User_Account friend : availableFriends) {
                JPanel friendItemPanel = createFriendItemPanel(friend);
                friendsPanel.add(friendItemPanel);
                friendsPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel noFriendsLabel = new JLabel("Bạn chưa có bạn bè nào để thêm vào nhóm");
            noFriendsLabel.setFont(new Font("JetBrains Mono", Font.ITALIC, 12));
            noFriendsLabel.setForeground(Color.GRAY);
            noFriendsLabel.setHorizontalAlignment(JLabel.CENTER);
            friendsPanel.add(noFriendsLabel);
        }
        
        JScrollPane scrollPane = new JScrollPane(friendsPanel);
        scrollPane.setPreferredSize(new Dimension(350, 300));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFriendItemPanel(Model_User_Account friend) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Friend info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
          // Avatar with user's actual image
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(35, 35));
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        avatarLabel.setVerticalAlignment(JLabel.CENTER);
        
        // Check if user has image data
        if (friend.getImage() != null && friend.getImage().length > 0) {
            try {
                // Create ImageIcon from byte array
                ImageIcon originalIcon = new ImageIcon(friend.getImage());
                
                // Scale image to fit avatar size (35x35)
                Image scaledImage = originalIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                
                avatarLabel.setIcon(scaledIcon);
                avatarLabel.setOpaque(false);
            } catch (Exception e) {
                // If image loading fails, fall back to text avatar
                setTextAvatar(avatarLabel, friend);
            }
        } else {
            // No image, use text avatar
            setTextAvatar(avatarLabel, friend);
        }
        infoPanel.add(avatarLabel, BorderLayout.WEST);
        
        // Name and status
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.setBackground(Color.WHITE);
        namePanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        JLabel nameLabel = new JLabel(friend.getUserName());
        nameLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        namePanel.add(nameLabel, BorderLayout.NORTH);
        
        JLabel statusLabel = new JLabel(friend.getStatus() ? "Đang hoạt động" : "Không hoạt động");
        statusLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        statusLabel.setForeground(friend.getStatus() ? new Color(0, 150, 0) : Color.GRAY);
        namePanel.add(statusLabel, BorderLayout.SOUTH);
        
        infoPanel.add(namePanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setBackground(Color.WHITE);
        checkBox.putClientProperty("friendID", friend.getUserID());
        friendCheckboxes.add(checkBox);
        panel.add(checkBox, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(242, 242, 242));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        cancelButton = new JButton("Hủy");
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.addActionListener(e -> dispose());
        
        createButton = new JButton("Tạo nhóm");
        createButton.setPreferredSize(new Dimension(100, 35));
        createButton.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        createButton.setBackground(new Color(27, 149, 226));
        createButton.setForeground(Color.WHITE);
        createButton.addActionListener(this::createGroup);
        
        panel.add(cancelButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(createButton);
        
        return panel;
    }
    
    private void createGroup(ActionEvent e) {
        String groupName = groupNameField.getText().trim();
        if (groupName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhóm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get selected friends
        List<Integer> selectedMemberIDs = new ArrayList<>();
        for (JCheckBox checkBox : friendCheckboxes) {
            if (checkBox.isSelected()) {
                Integer friendID = (Integer) checkBox.getClientProperty("friendID");
                if (friendID != null) {
                    selectedMemberIDs.add(friendID);
                }
            }
        }
        
        if (selectedMemberIDs.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Bạn có muốn tạo nhóm chỉ có mình bạn không?", 
                "Xác nhận", 
                JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Disable button and show loading
        createButton.setEnabled(false);
        createButton.setText("Đang tạo...");
        
        // Create group via service
        SwingUtilities.invokeLater(() -> {
            try {
                Service.getInstance().createGroup(
                    groupName,
                    groupDescriptionArea.getText().trim(),
                    selectedMemberIDs,
                    new CreateGroupCallback() {
                        @Override
                        public void onSuccess(String message) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(CreateGroupDialog.this, 
                                    "Tạo nhóm thành công!", 
                                    "Thành công", 
                                    JOptionPane.INFORMATION_MESSAGE);
                                dispose();
                            });
                        }
                        
                        @Override
                        public void onError(String error) {
                            SwingUtilities.invokeLater(() -> {
                                createButton.setEnabled(true);
                                createButton.setText("Tạo nhóm");
                                JOptionPane.showMessageDialog(CreateGroupDialog.this, 
                                    "Lỗi tạo nhóm: " + error, 
                                    "Lỗi", 
                                    JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }
                );
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    createButton.setEnabled(true);
                    createButton.setText("Tạo nhóm");
                    JOptionPane.showMessageDialog(CreateGroupDialog.this, 
                        "Lỗi tạo nhóm: " + ex.getMessage(), 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }        });
    }
    
    private void setTextAvatar(JLabel avatarLabel, Model_User_Account friend) {
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(27, 149, 226));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
        if (friend.getUserName() != null && !friend.getUserName().isEmpty()) {
            avatarLabel.setText(friend.getUserName().substring(0, 1).toUpperCase());
        } else {
            avatarLabel.setText("?");
        }
    }
    
    public interface CreateGroupCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
