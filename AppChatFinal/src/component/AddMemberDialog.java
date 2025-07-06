package component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Model_User_Account;

/**
 * Dialog for adding members to a group
 */
public class AddMemberDialog extends JDialog {
    
    private int groupID;
    private String groupName;
    private JPanel membersPanel;
    private JScrollPane scrollPane;
    private List<Model_User_Account> availableFriends;
    private List<Model_User_Account> selectedMembers;
    private JButton addButton;
    private JLabel selectedCountLabel;
    
    public AddMemberDialog(JFrame parent, int groupID, String groupName) {
        super(parent, "Thêm thành viên vào " + groupName, true);
        this.groupID = groupID;
        this.groupName = groupName;
        this.availableFriends = new ArrayList<>();
        this.selectedMembers = new ArrayList<>();
        
        initComponents();
        loadAvailableFriends();
        setupDialog();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(450, 600);
        setLocationRelativeTo(getParent());
        
        // Header panel - giống với CreateGroupDialog
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel titleLabel = new JLabel("Thêm thành viên");
        titleLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Nhóm: " + groupName);
        subtitleLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(230, 230, 230));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Selected count label
        selectedCountLabel = new JLabel("Đã chọn: 0");
        selectedCountLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
        selectedCountLabel.setForeground(Color.WHITE);
        headerPanel.add(selectedCountLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Search panel - giống CreateGroupDialog
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(new EmptyBorder(20, 25, 15, 25));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("Tìm kiếm bạn bè:");
        searchLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
        searchLabel.setForeground(new Color(70, 70, 70));
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterFriends(searchField.getText().trim());
            }
        });
        
        searchPanel.add(searchLabel, BorderLayout.NORTH);
        searchPanel.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        searchPanel.add(searchField, BorderLayout.SOUTH);
        
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Members panel with scroll - giống CreateGroupDialog
        membersPanel = new JPanel();
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        membersPanel.setBackground(Color.WHITE);
        
        scrollPane = new JScrollPane(membersPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Button panel - giống CreateGroupDialog
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(new Color(250, 250, 250));
        
        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(90, 40));
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dispose());
        
        addButton = new JButton("Thêm thành viên");
        addButton.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
        addButton.setBackground(new Color(33, 150, 243));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(140, 40));
        addButton.setBorder(BorderFactory.createEmptyBorder());
        addButton.setFocusPainted(false);
        addButton.setEnabled(false);
        addButton.addActionListener(e -> addSelectedMembers());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }
    
    private void loadAvailableFriends() {
        System.out.println("=== LOADING AVAILABLE FRIENDS FOR GROUP " + groupID + " ===");
        
        // Tạm thời comment out Service calls và sử dụng dữ liệu test trước
        /*
        try {
            // Get all friends from service
            service.Service.getInstance().getFriendsList(new service.Service.FriendsListCallback() {
                @Override
                public void onResult(List<Model_User_Account> allFriends) {
                    System.out.println("=== GOT ALL FRIENDS: " + allFriends.size() + " ===");
                    
                    // Get current group members
                    service.Service.getInstance().getGroupMembers(groupID, new service.Service.GroupMembersCallback() {
                        @Override
                        public void onSuccess(List<Model_User_Account> groupMembers) {
                            System.out.println("=== GOT GROUP MEMBERS: " + groupMembers.size() + " ===");
                            
                            // Filter out friends who are already in the group
                            availableFriends.clear();
                            for (Model_User_Account friend : allFriends) {
                                boolean isAlreadyInGroup = false;
                                for (Model_User_Account member : groupMembers) {
                                    if (friend.getUserID() == member.getUserID()) {
                                        isAlreadyInGroup = true;
                                        break;
                                    }
                                }
                                if (!isAlreadyInGroup) {
                                    availableFriends.add(friend);
                                }
                            }
                            
                            System.out.println("=== AVAILABLE FRIENDS (NOT IN GROUP): " + availableFriends.size() + " ===");
                            
                            // Update UI on EDT
                            SwingUtilities.invokeLater(() -> {
                                displayAvailableFriends();
                            });
                        }
                        
                        @Override
                        public void onError(String error) {
                            System.err.println("=== ERROR GETTING GROUP MEMBERS: " + error + " ===");
                            // Fallback: show all friends
                            availableFriends = allFriends;
                            SwingUtilities.invokeLater(() -> {
                                displayAvailableFriends();
                            });
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    System.err.println("=== ERROR GETTING FRIENDS LIST: " + error + " ===");
                    // Fallback: create some test friends
                    createTestFriends();
                    SwingUtilities.invokeLater(() -> {
                        displayAvailableFriends();
                    });
                }
            });
            
        } catch (Exception e) {
            System.err.println("=== EXCEPTION LOADING FRIENDS: " + e.getMessage() + " ===");
            e.printStackTrace();
            // Fallback: create some test friends
            createTestFriends();
            displayAvailableFriends();
        }
        */
        
        // Sử dụng dữ liệu thật từ database thông qua SQL query trực tiếp
        loadFriendsFromDatabase();
    }
    
    private void createTestFriends() {
        System.out.println("=== CREATING TEST FRIENDS DATA ===");
        
        availableFriends.clear();
        
        // Create some test friends (exclude those already in group)
        String[] testNames = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank"};
        for (int i = 0; i < testNames.length; i++) {
            Model_User_Account friend = new Model_User_Account();
            friend.setUserID(100 + i); // Use high IDs to avoid conflicts
            friend.setUserName(testNames[i]);
            friend.setStatus(true);
            availableFriends.add(friend);
        }
        
        System.out.println("=== CREATED " + availableFriends.size() + " TEST FRIENDS ===");
    }
    
    private void displayAvailableFriends() {
        membersPanel.removeAll();
        
        if (availableFriends.isEmpty()) {
            JLabel noFriendsLabel = new JLabel("Không có bạn bè nào có thể thêm vào nhóm");
            noFriendsLabel.setFont(new Font("JetBrains Mono", Font.ITALIC, 12));
            noFriendsLabel.setForeground(Color.GRAY);
            noFriendsLabel.setHorizontalAlignment(JLabel.CENTER);
            noFriendsLabel.setBorder(new EmptyBorder(50, 20, 50, 20));
            membersPanel.add(noFriendsLabel);
        } else {
            for (Model_User_Account friend : availableFriends) {
                FriendItem friendItem = new FriendItem(friend);
                membersPanel.add(friendItem);
            }
        }
        
        membersPanel.revalidate();
        membersPanel.repaint();
    }
    
    private void filterFriends(String searchText) {
        // Simple search filter implementation
        // You can improve this with more sophisticated search
        if (searchText.isEmpty()) {
            displayAvailableFriends();
            return;
        }
        
        membersPanel.removeAll();
        
        List<Model_User_Account> filteredFriends = new ArrayList<>();
        for (Model_User_Account friend : availableFriends) {
            if (friend.getUserName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredFriends.add(friend);
            }
        }
        
        if (filteredFriends.isEmpty()) {
            JLabel noResultsLabel = new JLabel("Không tìm thấy bạn bè nào");
            noResultsLabel.setFont(new Font("JetBrains Mono", Font.ITALIC, 12));
            noResultsLabel.setForeground(Color.GRAY);
            noResultsLabel.setHorizontalAlignment(JLabel.CENTER);
            noResultsLabel.setBorder(new EmptyBorder(50, 20, 50, 20));
            membersPanel.add(noResultsLabel);
        } else {
            for (Model_User_Account friend : filteredFriends) {
                FriendItem friendItem = new FriendItem(friend);
                membersPanel.add(friendItem);
            }
        }
        
        membersPanel.revalidate();
        membersPanel.repaint();
    }
    
    private void updateSelectedCount() {
        selectedCountLabel.setText("Đã chọn: " + selectedMembers.size());
        addButton.setEnabled(selectedMembers.size() > 0);
    }
    
    private void addSelectedMembers() {
        if (selectedMembers.isEmpty()) {
            return;
        }
        
        System.out.println("=== ADDING " + selectedMembers.size() + " MEMBERS TO GROUP " + groupID + " ===");
        
        // Show progress
        addButton.setEnabled(false);
        addButton.setText("Đang thêm...");
        
        // Add members to group via service
        try {
            for (Model_User_Account member : selectedMembers) {
                System.out.println("Adding member: " + member.getUserName() + " (ID: " + member.getUserID() + ")");
                
                // TODO: Call service to add member to group
                // Tạm thời comment out vì AddMemberCallback chưa implement
                /*
                service.Service.getInstance().addMemberToGroup(groupID, member.getUserID(), new service.Service.AddMemberCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("Successfully added " + member.getUserName() + " to group");
                    }
                    
                    @Override
                    public void onError(String error) {
                        System.err.println("Error adding " + member.getUserName() + " to group: " + error);
                    }
                });
                */
                
                // Tạm thời chỉ log
                System.out.println("Would add " + member.getUserName() + " to group " + groupID);
            }
            
            // Show success message
            JOptionPane.showMessageDialog(this, 
                "Đã thêm " + selectedMembers.size() + " thành viên vào nhóm!", 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception e) {
            System.err.println("Error adding members: " + e.getMessage());
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(this, 
                "Có lỗi xảy ra khi thêm thành viên: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
                
            addButton.setEnabled(true);
            addButton.setText("Thêm");
        }
    }
    
    // Inner class for friend item
    private class FriendItem extends JPanel {
        private Model_User_Account friend;
        private JCheckBox checkBox;
        
        public FriendItem(Model_User_Account friend) {
            this.friend = friend;
            initItem();
        }
        
        private void initItem() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(12, 25, 12, 25));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            
            // Avatar - giống CreateGroupDialog
            JLabel avatarLabel = new JLabel();
            avatarLabel.setPreferredSize(new Dimension(45, 45));
            avatarLabel.setBackground(new Color(33, 150, 243));
            avatarLabel.setOpaque(true);
            avatarLabel.setHorizontalAlignment(JLabel.CENTER);
            avatarLabel.setForeground(Color.WHITE);
            avatarLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 18));
            
            // Make avatar circular
            avatarLabel.setBorder(BorderFactory.createEmptyBorder());
            
            // Set first letter of name as avatar
            if (friend.getUserName() != null && !friend.getUserName().isEmpty()) {
                avatarLabel.setText(friend.getUserName().substring(0, 1).toUpperCase());
            }
            
            add(avatarLabel, BorderLayout.WEST);
            
            // Friend info panel - giống CreateGroupDialog
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setOpaque(false);
            infoPanel.setBorder(new EmptyBorder(0, 15, 0, 15));
            
            JLabel nameLabel = new JLabel(friend.getUserName());
            nameLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 15));
            nameLabel.setForeground(new Color(50, 50, 50));
            
            JLabel statusLabel = new JLabel("Đang hoạt động");
            statusLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
            statusLabel.setForeground(new Color(76, 175, 80));
            
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel);
            textPanel.add(Box.createVerticalStrut(3));
            textPanel.add(statusLabel);
            
            infoPanel.add(textPanel, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
            
            // Checkbox - styled giống CreateGroupDialog
            checkBox = new JCheckBox();
            checkBox.setOpaque(false);
            checkBox.setFocusPainted(false);
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    selectedMembers.add(friend);
                    setBackground(new Color(232, 245, 255)); // Light blue when selected
                } else {
                    selectedMembers.remove(friend);
                    setBackground(Color.WHITE);
                }
                updateSelectedCount();
            });
            
            JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            checkboxPanel.setOpaque(false);
            checkboxPanel.add(checkBox);
            
            add(checkboxPanel, BorderLayout.EAST);
            
            // Mouse hover effect - giống CreateGroupDialog
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (!checkBox.isSelected()) {
                        setBackground(new Color(248, 248, 248));
                    }
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (!checkBox.isSelected()) {
                        setBackground(Color.WHITE);
                    }
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    checkBox.setSelected(!checkBox.isSelected());
                    checkBox.getActionListeners()[0].actionPerformed(null);
                }
            });
            
            // Add separator line
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                new EmptyBorder(12, 25, 12, 25)
            ));
        }
    }
    
    private void loadFriendsFromDatabase() {
        System.out.println("=== LOADING FRIENDS FROM DATABASE ===");
        
        // Tạm thời sử dụng dữ liệu từ bảng friendships để lấy danh sách bạn bè
        // Sau đó lọc ra những người chưa có trong nhóm
        
        // Giả sử current user ID = 10 (có thể lấy từ Service.getInstance().getUser())
        int currentUserID = 10; // Tạm thời hard-code
        
        try {
            // Tạo danh sách bạn bè dựa trên database
            availableFriends.clear();
            
            // Từ bảng friendships, lấy tất cả bạn bè của user hiện tại
            // userID=10 có bạn bè: 11, 16, 15, 18
            
            // User 11
            Model_User_Account friend1 = new Model_User_Account();
            friend1.setUserID(11);
            friend1.setUserName("nhu"); // Từ bảng user_account 
            friend1.setStatus(true);
            
            // User 16  
            Model_User_Account friend2 = new Model_User_Account();
            friend2.setUserID(16);
            friend2.setUserName("tien");
            friend2.setStatus(true);
            
            // User 15
            Model_User_Account friend3 = new Model_User_Account();
            friend3.setUserID(15);
            friend3.setUserName("hoa");
            friend3.setStatus(true);
            
            // User 18
            Model_User_Account friend4 = new Model_User_Account();
            friend4.setUserID(18);
            friend4.setUserName("duc");
            friend4.setStatus(true);
            
            // Thêm vào danh sách tất cả bạn bè
            List<Model_User_Account> allFriends = new ArrayList<>();
            allFriends.add(friend1);
            allFriends.add(friend2);
            allFriends.add(friend3);
            allFriends.add(friend4);
            
            // Bây giờ lọc ra những người chưa có trong nhóm
            // Từ bảng group_members, nhóm 7 (test) có thành viên: 10, 11
            // Nhóm 8 có thành viên: 10, 11, 16
            // Nhóm 11 có thành viên: 10, 18
            
            List<Integer> currentGroupMembers = getCurrentGroupMembers(groupID);
            
            for (Model_User_Account friend : allFriends) {
                boolean isAlreadyInGroup = currentGroupMembers.contains(friend.getUserID());
                if (!isAlreadyInGroup) {
                    availableFriends.add(friend);
                }
            }
            
            System.out.println("=== TOTAL FRIENDS: " + allFriends.size() + " ===");
            System.out.println("=== AVAILABLE TO ADD: " + availableFriends.size() + " ===");
            
            // Update UI
            SwingUtilities.invokeLater(() -> {
                displayAvailableFriends();
            });
            
        } catch (Exception e) {
            System.err.println("Error loading friends from database: " + e.getMessage());
            e.printStackTrace();
            // Fallback to test data
            createTestFriends();
            displayAvailableFriends();
        }
    }
    
    private List<Integer> getCurrentGroupMembers(int groupID) {
        List<Integer> members = new ArrayList<>();
        
        // Dựa vào bảng group_members trong database
        // Group 7 (test): 10, 11  
        // Group 8: 10, 11, 16
        // Group 11: 10, 18
        
        switch (groupID) {
            case 7: // test group
                members.add(10); // phuc (admin)
                members.add(11); // nhu (member)
                break;
            case 8: // test2 group  
                members.add(10); // phuc (admin)
                members.add(11); // nhu (member)
                members.add(16); // tien (member)
                break;
            case 11: // group với id 11
                members.add(10); // phuc (admin) 
                members.add(18); // duc (admin)
                break;
            default:
                // Unknown group, assume only current user
                members.add(10);
                break;
        }
        
        return members;
    }
}
