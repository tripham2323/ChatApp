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
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        addButton = new JButton("Thêm thành viên");
        addButton.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
        addButton.setBackground(new Color(33, 150, 243));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(140, 40));
        addButton.setBorder(BorderFactory.createEmptyBorder());
        addButton.setFocusPainted(false);
        addButton.setEnabled(false);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSelectedMembers();
            }
        });
        
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
        
        // Sử dụng dữ liệu thật từ database thông qua Service
        loadFriendsFromDatabase();
    }
    

    
    private void displayAvailableFriends() {
        System.out.println("=== DISPLAY AVAILABLE FRIENDS CALLED ===");
        System.out.println("Available friends count: " + availableFriends.size());
        System.out.println("Current thread: " + Thread.currentThread().getName());
        
        membersPanel.removeAll();
        
        if (availableFriends.isEmpty()) {
            System.out.println("No friends available - showing 'no friends' message");
            JLabel noFriendsLabel = new JLabel("Không có bạn bè nào có thể thêm vào nhóm");
            noFriendsLabel.setFont(new Font("JetBrains Mono", Font.ITALIC, 12));
            noFriendsLabel.setForeground(Color.GRAY);
            noFriendsLabel.setHorizontalAlignment(JLabel.CENTER);
            noFriendsLabel.setBorder(new EmptyBorder(50, 20, 50, 20));
            membersPanel.add(noFriendsLabel);
        } else {
            System.out.println("Adding " + availableFriends.size() + " friend items to UI");
            for (Model_User_Account friend : availableFriends) {
                System.out.println("Adding friend item: " + friend.getUserName() + " (ID: " + friend.getUserID() + ")");
                
                // Kiểm tra xem friend này đã được chọn chưa
                boolean isAlreadySelected = false;
                for (Model_User_Account selectedMember : selectedMembers) {
                    if (selectedMember.getUserID() == friend.getUserID()) {
                        isAlreadySelected = true;
                        break;
                    }
                }
                
                FriendItem friendItem = new FriendItem(friend, isAlreadySelected);
                membersPanel.add(friendItem);
            }
        }
        
        System.out.println("Calling revalidate and repaint...");
        membersPanel.revalidate();
        membersPanel.repaint();
        System.out.println("=== DISPLAY AVAILABLE FRIENDS COMPLETED ===");
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
                // Kiểm tra xem friend này đã được chọn chưa
                boolean isAlreadySelected = false;
                for (Model_User_Account selectedMember : selectedMembers) {
                    if (selectedMember.getUserID() == friend.getUserID()) {
                        isAlreadySelected = true;
                        break;
                    }
                }
                
                FriendItem friendItem = new FriendItem(friend, isAlreadySelected);
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
        
        // Lấy current user để biết ai là người thêm
        Model_User_Account currentUser = null;
        try {
            currentUser = service.Service.getInstance().getUser();
            System.out.println("Current user adding members: " + currentUser.getUserName() + " (ID: " + currentUser.getUserID() + ")");
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Không thể xác định người dùng hiện tại: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                addButton.setEnabled(true);
                addButton.setText("Thêm thành viên");
            });
            return;
        }
        
        // Add members to group via Service
        try {
            // Counter để track số member đã thêm thành công
            final int[] successCount = {0};
            final int[] errorCount = {0};
            final int totalMembers = selectedMembers.size();
            
            for (Model_User_Account member : selectedMembers) {
                System.out.println("Adding member: " + member.getUserName() + " (ID: " + member.getUserID() + ") to group " + groupID);
                
                // Call Service để thêm member vào group
                service.Service.getInstance().addMemberToGroup(
                    groupID, 
                    member.getUserID(),
                    "member", // role
                    new service.Service.AddMemberToGroupCallback() {
                        @Override
                        public void onSuccess() {
                            System.out.println("Successfully added " + member.getUserName() + " to group " + groupID);
                            successCount[0]++;
                            
                            // Kiểm tra xem đã thêm hết chưa
                            if (successCount[0] + errorCount[0] == totalMembers) {
                                SwingUtilities.invokeLater(() -> {
                                    showAddMembersResult(successCount[0], errorCount[0], totalMembers);
                                });
                            }
                        }
                        
                        @Override
                        public void onError(String error) {
                            System.err.println("Error adding " + member.getUserName() + " to group: " + error);
                            errorCount[0]++;
                            
                            // Kiểm tra xem đã thêm hết chưa
                            if (successCount[0] + errorCount[0] == totalMembers) {
                                SwingUtilities.invokeLater(() -> {
                                    showAddMembersResult(successCount[0], errorCount[0], totalMembers);
                                });
                            }
                        }
                    }
                );
            }
            
            // Show result after all members processed
            SwingUtilities.invokeLater(() -> {
                showAddMembersResult(successCount[0], errorCount[0], totalMembers);
            });
            
        } catch (Exception e) {
            System.err.println("Exception while adding members: " + e.getMessage());
            e.printStackTrace();
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Có lỗi xảy ra khi thêm thành viên:\n" + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                    
                addButton.setEnabled(true);
                addButton.setText("Thêm thành viên");
            });
        }
    }
    
    private void showAddMembersResult(int successCount, int errorCount, int totalMembers) {
        StringBuilder message = new StringBuilder();
        
        if (successCount > 0) {
            message.append("Đã thêm thành công ").append(successCount).append(" thành viên");
            if (errorCount > 0) {
                message.append("\n");
            }
        }
        
        if (errorCount > 0) {
            message.append("Có ").append(errorCount).append(" thành viên không thể thêm");
        }
        
        message.append("\nvào nhóm ").append(groupName).append("!");
        
        if (errorCount == 0) {
            // Tất cả thành công
            JOptionPane.showMessageDialog(this, 
                message.toString(), 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else if (successCount > 0) {
            // Một phần thành công
            JOptionPane.showMessageDialog(this, 
                message.toString(), 
                "Hoàn thành một phần", 
                JOptionPane.WARNING_MESSAGE);
            dispose();
        } else {
            // Tất cả thất bại
            JOptionPane.showMessageDialog(this, 
                "Không thể thêm bất kỳ thành viên nào vào nhóm!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
                
            addButton.setEnabled(true);
            addButton.setText("Thêm thành viên");
        }
    }
    
    // Inner class for friend item
    private class FriendItem extends JPanel {
        private Model_User_Account friend;
        private JCheckBox checkBox;
        
        public FriendItem(Model_User_Account friend) {
            this(friend, false); // Default to not selected
        }
        
        public FriendItem(Model_User_Account friend, boolean isSelected) {
            this.friend = friend;
            initItem(isSelected);
        }
        
        private void initItem(boolean isSelected) {
            setLayout(new BorderLayout());
            setBackground(isSelected ? new Color(232, 245, 255) : Color.WHITE);
            setBorder(new EmptyBorder(12, 25, 12, 25));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            
            // Avatar with user's actual image - giống CreateGroupDialog
            JLabel avatarLabel = new JLabel();
            avatarLabel.setPreferredSize(new Dimension(45, 45));
            avatarLabel.setHorizontalAlignment(JLabel.CENTER);
            avatarLabel.setVerticalAlignment(JLabel.CENTER);
            
            // Check if user has image data
            if (friend.getImage() != null && friend.getImage().length > 0) {
                try {
                    // Create ImageIcon from byte array
                    ImageIcon originalIcon = new ImageIcon(friend.getImage());
                    
                    // Scale image to fit avatar size (45x45)
                    Image scaledImage = originalIcon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
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
            
            add(avatarLabel, BorderLayout.WEST);
            
            // Friend info panel - giống CreateGroupDialog
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setOpaque(false);
            infoPanel.setBorder(new EmptyBorder(0, 15, 0, 15));
            
            JLabel nameLabel = new JLabel(friend.getUserName());
            nameLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 15));
            nameLabel.setForeground(new Color(50, 50, 50));
            
            JLabel statusLabel = new JLabel(friend.getStatus() ? "Đang hoạt động" : "Không hoạt động");
            statusLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
            statusLabel.setForeground(friend.getStatus() ? new Color(76, 175, 80) : Color.GRAY);
            
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
            checkBox.setSelected(isSelected); // Set initial state based on parameter
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (checkBox.isSelected()) {
                        // Only add if not already in selectedMembers
                        boolean alreadySelected = false;
                        for (Model_User_Account selectedMember : selectedMembers) {
                            if (selectedMember.getUserID() == friend.getUserID()) {
                                alreadySelected = true;
                                break;
                            }
                        }
                        if (!alreadySelected) {
                            selectedMembers.add(friend);
                        }
                        setBackground(new Color(232, 245, 255)); // Light blue when selected
                    } else {
                        // Remove from selectedMembers
                        selectedMembers.removeIf(member -> member.getUserID() == friend.getUserID());
                        setBackground(Color.WHITE);
                    }
                    updateSelectedCount();
                }
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
        System.out.println("Group ID to add members to: " + groupID);
        
        // Lấy current user từ Service thật
        Model_User_Account currentUser = null;
        try {
            currentUser = service.Service.getInstance().getUser();
            System.out.println("Got current user from service: " + currentUser.getUserName() + " (ID: " + currentUser.getUserID() + ")");
        } catch (Exception e) {
            System.err.println("Error getting current user from service: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Không thể lấy thông tin người dùng hiện tại: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                dispose();
            });
            return;
        }
        
        final int currentUserID = currentUser.getUserID();
        System.out.println("Current user ID from database: " + currentUserID);
        
        // Lấy danh sách bạn bè thật từ database thông qua Service
        try {
            service.Service.getInstance().getFriendsList(new service.Service.FriendsListCallback() {
                @Override
                public void onResult(List<Model_User_Account> allFriends) {
                    System.out.println("=== GOT FRIENDS FROM DATABASE: " + allFriends.size() + " ===");
                    
                    for (Model_User_Account friend : allFriends) {
                        System.out.println("Friend: " + friend.getUserName() + " (ID: " + friend.getUserID() + ")");
                    }
                    
                    // Lấy danh sách thành viên hiện tại của nhóm từ database
                    System.out.println("=== ABOUT TO GET GROUP MEMBERS FOR GROUP " + groupID + " ===");
                    
                    // Tạo timeout để xử lý trường hợp getGroupMembers không trả về
                    final boolean[] responseReceived = {false};
                    
                    // Tạo timer để timeout sau 3 giây
                    Timer timeoutTimer = new Timer(300, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (!responseReceived[0]) {
                                System.err.println("=== TIMEOUT GETTING GROUP MEMBERS - USING FALLBACK ===");
                                responseReceived[0] = true;
                                
                                SwingUtilities.invokeLater(() -> {
                                    // Không hiển thị warning dialog, chỉ fallback im lặng
                                    System.out.println("Using fallback: showing all friends due to timeout");
                                    
                                    // Fallback: sử dụng tất cả bạn bè
                                    availableFriends.clear();
                                    availableFriends.addAll(allFriends);
                                    displayAvailableFriends();
                                });
                            }
                        }
                    });
                    timeoutTimer.setRepeats(false);
                    timeoutTimer.start();
                    
                    try {
                        service.Service.getInstance().getGroupMembers(groupID, new service.Service.GroupMembersCallback() {
                            @Override
                            public void onSuccess(List<Model_User_Account> groupMembers) {
                                System.out.println("=== GOT GROUP MEMBERS FROM DATABASE: " + groupMembers.size() + " ===");
                                
                                if (responseReceived[0]) {
                                    System.out.println("Response already handled by timeout - ignoring");
                                    return;
                                }
                                responseReceived[0] = true;
                                timeoutTimer.stop();
                                
                                for (Model_User_Account member : groupMembers) {
                                    System.out.println("Group member: " + member.getUserName() + " (ID: " + member.getUserID() + ")");
                                }
                                
                                // Lọc ra những bạn bè chưa có trong nhóm
                                availableFriends.clear();
                                
                                for (Model_User_Account friend : allFriends) {
                                    boolean isAlreadyInGroup = false;
                                    
                                    // Kiểm tra xem bạn này đã có trong nhóm chưa
                                    for (Model_User_Account member : groupMembers) {
                                        if (friend.getUserID() == member.getUserID()) {
                                            isAlreadyInGroup = true;
                                            System.out.println("Friend " + friend.getUserName() + " (ID: " + friend.getUserID() + ") already in group");
                                            break;
                                        }
                                    }
                                    
                                    if (!isAlreadyInGroup) {
                                        availableFriends.add(friend);
                                        System.out.println("Available to add: " + friend.getUserName() + " (ID: " + friend.getUserID() + ")");
                                    }
                                }
                                
                                System.out.println("=== FINAL AVAILABLE FRIENDS: " + availableFriends.size() + " ===");
                                
                                // Cập nhật UI trên EDT
                                SwingUtilities.invokeLater(() -> {
                                    displayAvailableFriends();
                                });
                            }
                            
                            @Override
                            public void onError(String error) {
                                System.err.println("=== ERROR GETTING GROUP MEMBERS FROM DATABASE: " + error + " ===");
                                
                                if (responseReceived[0]) {
                                    System.out.println("Response already handled by timeout - ignoring");
                                    return;
                                }
                                responseReceived[0] = true;
                                timeoutTimer.stop();
                                
                                // Fallback im lặng: chỉ hiển thị tất cả bạn bè
                                SwingUtilities.invokeLater(() -> {
                                    System.out.println("Using fallback: showing all friends due to error");
                                    
                                    // Fallback: sử dụng tất cả bạn bè
                                    availableFriends.clear();
                                    availableFriends.addAll(allFriends);
                                    displayAvailableFriends();
                                });
                            }
                        });
                    } catch (Exception ex) {
                        System.err.println("=== EXCEPTION CALLING getGroupMembers: " + ex.getMessage() + " ===");
                        ex.printStackTrace();
                        
                        if (!responseReceived[0]) {
                            responseReceived[0] = true;
                            timeoutTimer.stop();
                            
                            SwingUtilities.invokeLater(() -> {
                                System.out.println("Using fallback: showing all friends due to exception");
                                
                                // Fallback: sử dụng tất cả bạn bè
                                availableFriends.clear();
                                availableFriends.addAll(allFriends);
                                displayAvailableFriends();
                            });
                        }
                    }
                }
                
                @Override
                public void onError(String error) {
                    System.err.println("=== ERROR GETTING FRIENDS FROM DATABASE: " + error + " ===");
                    
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(AddMemberDialog.this, 
                            "Không thể lấy danh sách bạn bè: " + error, 
                            "Lỗi", 
                            JOptionPane.ERROR_MESSAGE);
                        dispose();
                    });
                }
            });
            
        } catch (Exception e) {
            System.err.println("=== EXCEPTION LOADING FRIENDS FROM DATABASE: " + e.getMessage() + " ===");
            e.printStackTrace();
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi kết nối database: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                dispose();
            });
        }
    }
    
    private void setTextAvatar(JLabel avatarLabel, Model_User_Account friend) {
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(33, 150, 243));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 18));
        if (friend.getUserName() != null && !friend.getUserName().isEmpty()) {
            avatarLabel.setText(friend.getUserName().substring(0, 1).toUpperCase());
        } else {
            avatarLabel.setText("?");
        }
    }
}
