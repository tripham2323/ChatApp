/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFi              @Override
            public void mousePressed(MouseEvent e) {
                // Handle group selection and open chat
                if (selectionCallback != null) {
                    selectionCallback.onItemSelected(Item_Group.this);
                } else {
                    setSelected(true);
                }
                openGroupChat();
            }em/Templates/Classes/Class.java to edit this template
 */
package component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Model_Group_Chat;
import model.Model_User_Account;
import event.PublicEvent;

/**
 *
 * @author Admin
 */
public class Item_Group extends JPanel {
    
    // Callback interface for selection events
    public interface SelectionCallback {
        void onItemSelected(Item_Group item);
    }
    
    private Model_Group_Chat group;
    private boolean mouseOver;
    private boolean isSelected;
    private SelectionCallback selectionCallback;
    
    public Item_Group(Model_Group_Chat group) {
        this.group = group;
        initComponents();
    }
    
    public Item_Group(Model_Group_Chat group, SelectionCallback callback) {
        this.group = group;
        this.selectionCallback = callback;
        initComponents();
    }
    
    public void setSelectionCallback(SelectionCallback callback) {
        this.selectionCallback = callback;
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(0, 70));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
          // Group avatar panel with 4-member composite image
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(50, 50));
        
        JLabel avatarLabel = createGroupAvatar();
        avatarPanel.add(avatarLabel, BorderLayout.CENTER);
        add(avatarPanel, BorderLayout.WEST);
        
        // Group info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        // Group name and last message
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(group.getGroupName());
        nameLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 13));
        nameLabel.setForeground(new Color(85, 85, 85));
        textPanel.add(nameLabel, BorderLayout.NORTH);
        
        String lastMessage = group.getLastMessage();
        if (lastMessage == null || lastMessage.trim().isEmpty()) {
            lastMessage = "Nhóm vừa được tạo";
        }
        
        JLabel messageLabel = new JLabel(lastMessage);
        messageLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        messageLabel.setForeground(new Color(128, 128, 128));
        textPanel.add(messageLabel, BorderLayout.SOUTH);
        
        infoPanel.add(textPanel, BorderLayout.CENTER);
        
        // Time and unread count panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(60, 50));
        
        // Time label
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(128, 128, 128));
        timeLabel.setHorizontalAlignment(JLabel.RIGHT);
        
        if (group.getLastMessageTime() != null) {
            // Format time (you can customize this)
            String timeStr = formatTime(group.getLastMessageTime().toString());
            timeLabel.setText(timeStr);
        }
        
        rightPanel.add(timeLabel, BorderLayout.NORTH);
        
        // Unread count (if any)
        if (group.getUnreadCount() > 0) {
            JLabel unreadLabel = new JLabel(String.valueOf(group.getUnreadCount()));
            unreadLabel.setOpaque(true);
            unreadLabel.setBackground(new Color(242, 38, 19));
            unreadLabel.setForeground(Color.WHITE);
            unreadLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
            unreadLabel.setHorizontalAlignment(JLabel.CENTER);
            unreadLabel.setPreferredSize(new Dimension(20, 20));
            unreadLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            rightPanel.add(unreadLabel, BorderLayout.SOUTH);
        } else {
            // Add "Add Member" button when no unread messages
            JButton addButton = new JButton("+");
            addButton.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
            addButton.setForeground(new Color(33, 150, 243));
            addButton.setBackground(Color.WHITE);
            addButton.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 1));
            addButton.setPreferredSize(new Dimension(25, 20));
            addButton.setFocusPainted(false);
            addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Add click event for the add button
            addButton.addActionListener(e -> {
                e.getSource(); // Prevent event bubbling
                openAddMemberDialog();
            });
            
            // Also add mouse listener to prevent parent click
            addButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    e.consume(); // Consume the event to prevent parent handling
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    e.consume(); // Consume the event to prevent parent handling
                }
            });
            
            rightPanel.add(addButton, BorderLayout.SOUTH);
        }
        
        infoPanel.add(rightPanel, BorderLayout.EAST);
        add(infoPanel, BorderLayout.CENTER);
        
        // Mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                mouseOver = false;
                repaint();
            }
              @Override
            public void mousePressed(MouseEvent e) {
                // Handle group selection and open chat
                setSelected(true);
                openGroupChat();
                
                // Notify selection callback
                if (selectionCallback != null) {
                    selectionCallback.onItemSelected(Item_Group.this);
                }
            }
        });
    }
    
    private JLabel createGroupAvatar() {
        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(45, 45));
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        
        // Load group members to create composite avatar
        loadGroupMembersForAvatar(avatarLabel);
        
        return avatarLabel;
    }
    
    private void loadGroupMembersForAvatar(JLabel avatarLabel) {
        try {
            // Get group members using Service
            Object serviceObj = Class.forName("service.Service").getMethod("getInstance").invoke(null);
            
            // Create callback for getting group members
            Class<?> callbackClass = Class.forName("service.Service$GroupMembersCallback");
            Object callback = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.getClassLoader(),
                new Class[]{callbackClass},
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if ("onSuccess".equals(method.getName())) {
                            @SuppressWarnings("unchecked")
                            List<Model_User_Account> members = (List<Model_User_Account>) args[0];
                            SwingUtilities.invokeLater(() -> {
                                createCompositeAvatar(avatarLabel, members);
                            });
                        } else if ("onError".equals(method.getName())) {
                            SwingUtilities.invokeLater(() -> {
                                createDefaultGroupAvatar(avatarLabel);
                            });
                        }
                        return null;
                    }
                }
            );
            
            serviceObj.getClass().getMethod("getGroupMembers", int.class, callbackClass)
                     .invoke(serviceObj, group.getGroupID(), callback);
                     
        } catch (Exception e) {
            e.printStackTrace();
            createDefaultGroupAvatar(avatarLabel);
        }
    }
    
    private void createCompositeAvatar(JLabel avatarLabel, List<Model_User_Account> members) {
        if (members == null || members.isEmpty()) {
            createDefaultGroupAvatar(avatarLabel);
            return;
        }
        
        // Create composite image from up to 4 members
        BufferedImage compositeImage = new BufferedImage(45, 45, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = compositeImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(242, 242, 242));
        g2d.fillRect(0, 0, 45, 45);
        
        int memberCount = Math.min(members.size(), 4);
        
        for (int i = 0; i < memberCount; i++) {
            Model_User_Account member = members.get(i);
            int x = (i % 2) * 22;
            int y = (i / 2) * 22;
            int size = 22;
            
            if (member.getImage() != null && member.getImage().length > 0) {
                try {
                    ImageIcon icon = new ImageIcon(member.getImage());
                    Image scaledImage = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    g2d.drawImage(scaledImage, x, y, null);
                } catch (Exception e) {
                    drawTextAvatar(g2d, member, x, y, size);
                }
            } else {
                drawTextAvatar(g2d, member, x, y, size);
            }
        }
        
        // Fill remaining squares if less than 4 members
        if (memberCount < 4) {
            g2d.setColor(new Color(200, 200, 200));
            for (int i = memberCount; i < 4; i++) {
                int x = (i % 2) * 22;
                int y = (i / 2) * 22;
                g2d.fillRect(x, y, 22, 22);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (22 - fm.stringWidth("+")) / 2;
                int textY = y + ((22 - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString("+", textX, textY);
                g2d.setColor(new Color(200, 200, 200));
            }
        }
        
        g2d.dispose();
        
        avatarLabel.setIcon(new ImageIcon(compositeImage));
    }
    
    private void drawTextAvatar(Graphics2D g2d, Model_User_Account member, int x, int y, int size) {
        // Background color for text avatar
        Color[] colors = {
            new Color(27, 149, 226),
            new Color(76, 175, 80),
            new Color(255, 152, 0),
            new Color(233, 30, 99),
            new Color(156, 39, 176)
        };
        
        Color bgColor = colors[member.getUserID() % colors.length];
        g2d.setColor(bgColor);
        g2d.fillRect(x, y, size, size);
        
        // Draw initial
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
        
        String initial = "?";
        if (member.getUserName() != null && !member.getUserName().isEmpty()) {
            initial = member.getUserName().substring(0, 1).toUpperCase();
        }
        
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (size - fm.stringWidth(initial)) / 2;
        int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(initial, textX, textY);
    }
    
    private void createDefaultGroupAvatar(JLabel avatarLabel) {
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(27, 149, 226));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
        
        if (group.getGroupName() != null && !group.getGroupName().isEmpty()) {
            avatarLabel.setText(group.getGroupName().substring(0, 1).toUpperCase());
        } else {
            avatarLabel.setText("G");
        }
    }    private void openGroupChat() {
        try {
            System.out.println("=== OPENING GROUP CHAT ===");
            System.out.println("Group ID: " + group.getGroupID());
            System.out.println("Group Name: " + group.getGroupName());
            System.out.println("Group object: " + group);
            
            // Verify group ID is not 0
            if (group.getGroupID() == 0) {
                System.err.println("ERROR: Group ID is 0! This is incorrect.");
                System.err.println("Group object details:");
                System.err.println("- Name: " + group.getGroupName());
                System.err.println("- Description: " + group.getGroupDescription());
                System.err.println("- CreatedBy: " + group.getCreatedBy());
                return;
            }            // Create a Model_User_Account object to represent the group
            Model_User_Account groupAsUser = new Model_User_Account();
            groupAsUser.setUserID(group.getGroupID());
            groupAsUser.setUserName(group.getGroupName());
            groupAsUser.setStatus(true); // Groups are always "online"
            groupAsUser.setGroup(true); // Mark this as a group
            
            System.out.println("Created groupAsUser with ID: " + groupAsUser.getUserID());
            System.out.println("Is group flag: " + groupAsUser.isGroup());
            
            // Trigger the chat event
            PublicEvent.getInstance().getEventMain().selectUser(groupAsUser);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening group chat: " + e.getMessage());
        }
    }
    
    // Method to open Add Member dialog
    private void openAddMemberDialog() {
        System.out.println("=== OPENING ADD MEMBER DIALOG ===");
        System.out.println("Group ID: " + group.getGroupID());
        System.out.println("Group Name: " + group.getGroupName());
        
        try {
            // Create and show the add member dialog
            AddMemberDialog dialog = new AddMemberDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                group.getGroupID(), 
                group.getGroupName()
            );
            dialog.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error opening add member dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String formatTime(String timestamp) {
        // Simple time formatting - you can improve this
        try {
            // Extract time part from timestamp
            if (timestamp.contains(" ")) {
                String timePart = timestamp.split(" ")[1];
                if (timePart.contains(":")) {
                    String[] parts = timePart.split(":");
                    return parts[0] + ":" + parts[1];
                }
            }
        } catch (Exception e) {
            // Ignore formatting errors
        }
        return "";
    }
    
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public Model_Group_Chat getGroup() {
        return group;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isSelected) {
            g2.setColor(new Color(229, 229, 229));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        } else if (mouseOver) {
            g2.setColor(new Color(245, 245, 245));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        }
        
        g2.dispose();
    }
}
