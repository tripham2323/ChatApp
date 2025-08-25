/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONArray;
import model.Model_Group_Chat;
import model.Model_User_Account;
import event.PublicEvent;
import io.socket.client.Ack;
import service.Service;

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
    private JLabel groupAvatarLabel; // Store reference to avatar label for updates
    
    public Item_Group(Model_Group_Chat group) {
        this.group = group;
        System.out.println("=== CREATING ITEM_GROUP ===");
        System.out.println("Group ID: " + group.getGroupID());
        System.out.println("Group Name: " + group.getGroupName());
        System.out.println("Has custom avatar: " + (group.getGroupImage() != null && group.getGroupImage().length > 0));
        if (group.getGroupImage() != null) {
            System.out.println("Avatar size: " + group.getGroupImage().length + " bytes");
        }
        initComponents();
        
        // Force reload avatar data from database immediately
        SwingUtilities.invokeLater(() -> {
            System.out.println("=== FORCING AVATAR RELOAD FROM DATABASE ===");
            forceReloadAvatarFromDatabase();
        });
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
        this.groupAvatarLabel = avatarLabel; // Store reference for updates
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
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        System.out.println("Creating avatar for group: " + group.getGroupID() + " - " + group.getGroupName());
        
        // Add mouse listener with extensive debugging
        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("=== MOUSE CLICKED ON GROUP AVATAR ===");
                System.out.println("Group ID: " + group.getGroupID());
                System.out.println("Group Name: " + group.getGroupName());
                System.out.println("Click count: " + e.getClickCount());
                System.out.println("Button: " + e.getButton());
                System.out.println("Is left button: " + javax.swing.SwingUtilities.isLeftMouseButton(e));
                
                // Handle both double-click and right-click for testing
                if (e.getClickCount() == 2 && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    System.out.println("Double-click detected! Opening avatar dialog...");
                    openGroupAvatarDialog();
                } else if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    System.out.println("Right-click detected! Showing context menu...");
                    showAvatarContextMenu(e);
                } else if (e.getClickCount() == 1) {
                    System.out.println("Single click detected");
                } else {
                    System.out.println("Other click type - count: " + e.getClickCount());
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                System.out.println("Mouse entered avatar for group: " + group.getGroupID());
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Mouse pressed on avatar for group: " + group.getGroupID());
            }
        });
        
        // Load group avatar or members to create composite avatar
        loadGroupAvatar(avatarLabel);
        
        return avatarLabel;
    }
    
    private void loadGroupMembersForAvatar(JLabel avatarLabel) {
        // CRITICAL: Don't load composite if avatar already has custom icon
        if (avatarLabel.getIcon() != null) {
            System.out.println("⚠️ Avatar label already has custom icon, SKIPPING composite avatar creation completely");
            return;
        }
        
        System.out.println("Loading group members for composite avatar...");
        
        // SKIP composite avatar completely - just create default
        System.out.println("Creating default avatar instead of composite to avoid conflicts");
        createDefaultGroupAvatar(avatarLabel);
        return;
        
        /* COMMENTED OUT COMPOSITE LOGIC TO PREVENT CONFLICTS
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
                                // Triple check: only create composite if no custom avatar exists
                                if (avatarLabel.getIcon() == null) {
                                    System.out.println("Creating composite avatar with " + members.size() + " members");
                                    createCompositeAvatar(avatarLabel, members);
                                } else {
                                    System.out.println("⚠️ Custom avatar already loaded, ABSOLUTELY skipping composite creation");
                                }
                            });
                        } else if ("onError".equals(method.getName())) {
                            SwingUtilities.invokeLater(() -> {
                                // Only create default if no custom avatar exists
                                if (avatarLabel.getIcon() == null) {
                                    System.out.println("Creating default avatar due to member loading error");
                                    createDefaultGroupAvatar(avatarLabel);
                                } else {
                                    System.out.println("⚠️ Custom avatar already loaded, skipping default creation");
                                }
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
            // Only create default if no custom avatar exists
            if (avatarLabel.getIcon() == null) {
                System.out.println("Creating default avatar due to exception");
                createDefaultGroupAvatar(avatarLabel);
            } else {
                System.out.println("⚠️ Custom avatar already loaded, skipping default creation on exception");
            }
        }
        */
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
        System.out.println("=== CREATING DEFAULT GROUP AVATAR ===");
        System.out.println("Group ID: " + group.getGroupID());
        System.out.println("Group Name: " + group.getGroupName());
        
        // Clear any existing icon first
        avatarLabel.setIcon(null);
        
        // Set background and text properties
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(27, 149, 226));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        avatarLabel.setVerticalAlignment(JLabel.CENTER);
        
        if (group.getGroupName() != null && !group.getGroupName().isEmpty()) {
            String initial = group.getGroupName().substring(0, 1).toUpperCase();
            avatarLabel.setText(initial);
            System.out.println("Set default avatar text: " + initial);
        } else {
            avatarLabel.setText("G");
            System.out.println("Set default avatar text: G (fallback)");
        }
        
        // Force UI update
        avatarLabel.revalidate();
        avatarLabel.repaint();
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
    
    // Method to open group avatar selection dialog
    private void openGroupAvatarDialog() {
        System.out.println("=== OPENING GROUP AVATAR DIALOG ===");
        System.out.println("Group ID: " + group.getGroupID());
        System.out.println("Group Name: " + group.getGroupName());
        
        // Test connection first
        if (service.Service.getInstance() == null) {
            System.err.println("Service instance is null!");
            JOptionPane.showMessageDialog(this, "Service không khả dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (service.Service.getInstance().getUser() == null) {
            System.err.println("User not logged in!");
            JOptionPane.showMessageDialog(this, "Bạn chưa đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (service.Service.getInstance().getClient() == null || !service.Service.getInstance().getClient().connected()) {
            System.err.println("Client not connected!");
            JOptionPane.showMessageDialog(this, "Không kết nối được tới server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("All checks passed, opening file chooser...");
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn avatar cho nhóm: " + group.getGroupName());
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Ảnh (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
            uploadGroupAvatar(selectedFile);
        } else {
            System.out.println("File selection cancelled");
        }
    }
    
    // Method to upload group avatar - copy from Menu_Right approach
    private void uploadGroupAvatar(File imageFile) {
        try {
            System.out.println("=== UPLOADING GROUP AVATAR (MENU_RIGHT STYLE) ===");
            System.out.println("File: " + imageFile.getAbsolutePath());
            System.out.println("Group ID: " + group.getGroupID());
            
            // Check connections
            if (service.Service.getInstance().getUser() == null) {
                System.err.println("User not logged in!");
                return;
            }
            
            if (service.Service.getInstance().getClient() == null || !service.Service.getInstance().getClient().connected()) {
                System.err.println("Client not connected!");
                return;
            }
            
            // Initialize for chunked upload (like Menu_Right)
            this.selectedImageFile = imageFile;
            this.currentGroupID = group.getGroupID();
            
            // Create ByteBuffer like Menu_Right
            this.bb = java.nio.ByteBuffer.allocate((int) imageFile.length());
            this.fileSize = imageFile.length();
            
            // Read file into ByteBuffer
            FileInputStream fis = new FileInputStream(imageFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bb.put(buffer, 0, bytesRead);
            }
            fis.close();
            bb.flip(); // Prepare for reading
            
            System.out.println("File loaded into buffer: " + fileSize + " bytes");
            
            // Start upload process
            sendGroupImageAvatar();
            
        } catch (Exception e) {
            System.err.println("Error in uploadGroupAvatar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Variables for upload state (like Menu_Right)
    private File selectedImageFile;
    private int currentGroupID;
    private java.nio.ByteBuffer bb;
    private long fileSize;
    
    // Send group avatar chunks (copy from Menu_Right.sendImageAvatar)
    private void sendGroupImageAvatar() throws IOException {
        try {
            System.out.println("=== SENDING GROUP IMAGE AVATAR ===");
            
            // Create Model_Package_Sender
            Class<?> packageSenderClass = Class.forName("model.Model_Package_Sender");
            Object data = packageSenderClass.newInstance();
            
            // Set group ID as fromUserID (server will handle this as group ID)
            packageSenderClass.getMethod("setFromUserID", int.class).invoke(data, currentGroupID);
            
            byte[] bytes = readGroupData();
            if (bytes != null) {
                packageSenderClass.getMethod("setData", byte[].class).invoke(data, bytes);
                packageSenderClass.getMethod("setFinish", boolean.class).invoke(data, false);
                System.out.println("Sending chunk: " + bytes.length + " bytes");
            } else {
                packageSenderClass.getMethod("setFinish", boolean.class).invoke(data, true);
                System.out.println("Sending finish signal");
            }
            
            // Get JSON object
            Object jsonObject = packageSenderClass.getMethod("toJsonObject").invoke(data);
            
            // Send to server using group avatar event
            service.Service.getInstance().getClient().emit("send_group_avatar", jsonObject, new io.socket.client.Ack() {
                @Override
                public void call(Object... os) {
                    System.out.println("=== GROUP AVATAR CHUNK RESPONSE ===");
                    System.out.println("Response length: " + os.length);
                    
                    if (os.length > 0) {
                        boolean success = (boolean) os[0];
                        System.out.println("Chunk success: " + success);
                        
                        if (success) {
                            try {
                                boolean isFinished = (Boolean) packageSenderClass.getMethod("isFinish").invoke(data);
                                if (!isFinished) {
                                    sendGroupImageAvatar(); // Continue with next chunk
                                } else {
                                    // Upload completed
                                    SwingUtilities.invokeLater(() -> {
                                        System.out.println("✅ Group avatar upload completed!");
                                        
                                        // Update UI immediately
                                        try {
                                            byte[] imageBytes = new byte[(int) fileSize];
                                            bb.rewind();
                                            bb.get(imageBytes);
                                            
                                            ImageIcon icon = new ImageIcon(imageBytes);
                                            Image scaledImage = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                                            groupAvatarLabel.setIcon(new ImageIcon(scaledImage));
                                            groupAvatarLabel.setText("");
                                            groupAvatarLabel.setOpaque(false);
                                            group.setGroupImage(imageBytes);
                                            
                                            JOptionPane.showMessageDialog(Item_Group.this, 
                                                "Cập nhật avatar nhóm thành công!", 
                                                "Thành công", 
                                                JOptionPane.INFORMATION_MESSAGE);
                                                
                                            // Refresh groups list
                                            PublicEvent.getInstance().getEventMain().refreshGroupsList();
                                            
                                        } catch (Exception e) {
                                            System.err.println("Error updating UI: " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                System.err.println("Error in success callback: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                System.err.println("❌ Group avatar upload failed!");
                                JOptionPane.showMessageDialog(Item_Group.this, 
                                    "Lỗi khi upload avatar nhóm!", 
                                    "Lỗi", 
                                    JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }
                }
            });
            
            System.out.println("Group avatar data sent: " + (bytes != null ? bytes.length : 0) + " bytes");
            
        } catch (Exception e) {
            System.err.println("Error in sendGroupImageAvatar: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to send group avatar", e);
        }
    }
    
    // Read data chunks (copy from Menu_Right.readData)
    public synchronized byte[] readGroupData() {
        if (bb.position() >= fileSize) {
            return null; // Finished
        }
        
        int max = 1500; // Same chunk size as Menu_Right
        int remaining = (int) (fileSize - bb.position());
        int chunkSize = Math.min(max, remaining);
        
        byte[] chunk = new byte[chunkSize];
        bb.get(chunk);
        
        System.out.println("Read chunk: " + chunkSize + " bytes, position: " + bb.position() + "/" + fileSize);
        return chunk;
    }
    
    // Method to load group avatar (either custom or composite from members)
    private void loadGroupAvatar(JLabel avatarLabel) {
        System.out.println("=== LOADING GROUP AVATAR ===");
        System.out.println("Group ID: " + group.getGroupID());
        System.out.println("Group name: " + group.getGroupName());
        
        // ALWAYS clear the avatar first to prevent conflicts
        avatarLabel.setIcon(null);
        avatarLabel.setText("");
        avatarLabel.setOpaque(false);
        avatarLabel.setBackground(null);
        
        // Check group image data with detailed logging
        byte[] groupImageData = group.getGroupImage();
        System.out.println("Group image data from model: " + (groupImageData != null ? "not null" : "null"));
        
        boolean hasCustomAvatar = (groupImageData != null && groupImageData.length > 0);
        System.out.println("Has custom avatar: " + hasCustomAvatar);
        if (hasCustomAvatar) {
            System.out.println("Custom avatar size: " + groupImageData.length + " bytes");
            
            try {
                System.out.println("✅ Loading custom group avatar (" + groupImageData.length + " bytes)");
                
                // Try BufferedImage approach first - most reliable
                java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(groupImageData);
                java.awt.image.BufferedImage bImage = javax.imageio.ImageIO.read(bis);
                bis.close();
                
                if (bImage != null) {
                    // Create scaled image for avatar
                    Image scaledImage = bImage.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    ImageIcon avatar = new ImageIcon(scaledImage);
                    avatarLabel.setIcon(avatar);
                    
                    // Force UI update
                    avatarLabel.revalidate();
                    avatarLabel.repaint();
                    this.revalidate();
                    this.repaint();
                    
                    System.out.println("✅ Successfully loaded custom group avatar using BufferedImage");
                    return; // IMPORTANT: Return here to prevent default avatar creation
                } else {
                    System.err.println("❌ BufferedImage is null after reading - trying direct ImageIcon");
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading custom avatar with BufferedImage: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Fallback: Try direct ImageIcon approach
            try {
                ImageIcon icon = new ImageIcon(groupImageData);
                if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    Image scaledImage = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    avatarLabel.setIcon(new ImageIcon(scaledImage));
                    
                    // Force UI update
                    avatarLabel.revalidate();
                    avatarLabel.repaint();
                    this.revalidate();
                    this.repaint();
                    
                    System.out.println("✅ Successfully loaded custom group avatar using direct ImageIcon");
                    return; // IMPORTANT: Return here to prevent default avatar creation
                } else {
                    System.err.println("❌ Direct ImageIcon has zero dimensions");
                }
            } catch (Exception e) {
                System.err.println("❌ Error with direct ImageIcon approach: " + e.getMessage());
                e.printStackTrace();
            }
            
            // If both methods failed but we have data, log the failure
            System.err.println("❌ Failed to load custom avatar despite having " + groupImageData.length + " bytes of data");
            
        } else if (groupImageData != null) {
            System.out.println("Group image data exists but length is: " + groupImageData.length);
        } else {
            System.out.println("Group image data is completely null - will use default avatar");
        }
        
        System.out.println("Creating default avatar (no custom avatar found or failed to load)");
        // Only create default if no custom avatar loaded successfully
        createDefaultGroupAvatar(avatarLabel);
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
    
    // Public method to refresh the group avatar display
    public void refreshAvatar() {
        System.out.println("=== REFRESH AVATAR CALLED ===");
        System.out.println("Group ID: " + group.getGroupID());
        
        if (groupAvatarLabel != null) {
            // Force reload fresh data from database instead of just using cached data
            forceReloadAvatarFromDatabase();
        } else {
            System.err.println("❌ groupAvatarLabel is null, cannot refresh avatar");
        }
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
    
    // Testing method: Show context menu for avatar options
    private void showAvatarContextMenu(MouseEvent e) {
        System.out.println("=== SHOWING AVATAR CONTEXT MENU ===");
        JPopupMenu popup = new JPopupMenu();
        
        JMenuItem uploadItem = new JMenuItem("Upload Avatar");
        uploadItem.addActionListener(ev -> {
            System.out.println("Context menu - Upload Avatar clicked");
            openGroupAvatarDialog();
        });
        
        JMenuItem testItem = new JMenuItem("Test Direct Upload");
        testItem.addActionListener(ev -> {
            System.out.println("Context menu - Test Direct Upload clicked");
            testDirectUpload();
        });
        
        JMenuItem debugItem = new JMenuItem("Debug Avatar Data");
        debugItem.addActionListener(ev -> {
            System.out.println("Context menu - Debug Avatar Data clicked");
            debugAvatarData();
        });
        
        JMenuItem forceItem = new JMenuItem("Force Load from DB");
        forceItem.addActionListener(ev -> {
            System.out.println("Context menu - Force Load from DB clicked");
            forceLoadAvatarFromDB();
        });
        
        JMenuItem refreshItem = new JMenuItem("Force Refresh Avatar");
        refreshItem.addActionListener(ev -> {
            System.out.println("Context menu - Force Refresh Avatar clicked");
            PublicEvent.getInstance().getEventMain().refreshGroupsList();
        });
        
        JMenuItem forceLoadItem = new JMenuItem("Force Load Avatar");
        forceLoadItem.addActionListener(ev -> {
            System.out.println("Context menu - Force Load Avatar clicked");
            forceLoadCustomAvatar();
        });
        
        popup.add(uploadItem);
        popup.add(testItem);
        popup.add(debugItem);
        popup.add(forceItem);
        popup.add(refreshItem);
        popup.add(debugItem);
        popup.add(forceLoadItem);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
    
    // Method to debug avatar data
    private void debugAvatarData() {
        System.out.println("=== DEBUG AVATAR DATA ===");
        System.out.println("Group ID: " + group.getGroupID());
        System.out.println("Group Name: " + group.getGroupName());
        
        byte[] avatarData = group.getGroupImage();
        System.out.println("Avatar data in model: " + (avatarData != null ? "EXISTS" : "NULL"));
        if (avatarData != null) {
            System.out.println("Avatar data size: " + avatarData.length + " bytes");
            if (avatarData.length > 0) {
                System.out.println("First 10 bytes: ");
                for (int i = 0; i < Math.min(10, avatarData.length); i++) {
                    System.out.print((avatarData[i] & 0xFF) + " ");
                }
                System.out.println();
                
                // Try to load this data directly to avatar
                try {
                    System.out.println("Attempting to display avatar data...");
                    groupAvatarLabel.setIcon(null);
                    groupAvatarLabel.setText("");
                    groupAvatarLabel.setOpaque(false);
                    
                    ImageIcon icon = new ImageIcon(avatarData);
                    Image scaledImage = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    groupAvatarLabel.setIcon(new ImageIcon(scaledImage));
                    groupAvatarLabel.revalidate();
                    groupAvatarLabel.repaint();
                    
                    System.out.println("✅ Successfully displayed avatar from model data");
                    
                } catch (Exception e) {
                    System.err.println("❌ Failed to display avatar: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Current avatar label state:");
        System.out.println("- Has icon: " + (groupAvatarLabel.getIcon() != null));
        System.out.println("- Text: '" + groupAvatarLabel.getText() + "'");
        System.out.println("- Is opaque: " + groupAvatarLabel.isOpaque());
        System.out.println("- Background: " + groupAvatarLabel.getBackground());
    }
    
    // Testing method: Direct upload with test data
    private void testDirectUpload() {
        System.out.println("=== TEST DIRECT UPLOAD START ===");
        try {
            // Create a small test image (1x1 pixel red PNG)
            byte[] testImageData = {
                (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
                0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x02, 0x00, 0x00, 0x00, (byte)0x90, (byte)0x77, 0x53, (byte)0xDE, 0x00, 0x00, 0x00, 0x0C,
                0x49, 0x44, 0x41, 0x54, 0x08, (byte)0xD7, 0x63, (byte)0xF8, 0x0F, 0x00, 0x00, 0x01,
                0x00, 0x01, (byte)0x80, 0x18, (byte)0xDD, (byte)0x8D, (byte)0xB4, 0x1C, 0x00, 0x00, 0x00, 0x00,
                0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82
            };
            
            System.out.println("Test image data created: " + testImageData.length + " bytes");
            System.out.println("Group ID: " + group.getGroupID());
            
            // Convert to List<Integer> for JSON
            List<Integer> imageDataList = new ArrayList<>();
            for (byte b : testImageData) {
                imageDataList.add((int) (b & 0xFF));
            }
            
            // Create JSON object
            Map<String, Object> data = new HashMap<>();
            data.put("groupID", group.getGroupID());
            data.put("imageData", imageDataList);
            
            System.out.println("Sending test data via socket...");
            System.out.println("Data keys: " + data.keySet());
            System.out.println("GroupID: " + data.get("groupID"));
            System.out.println("ImageData size: " + imageDataList.size());
            
            // Emit directly
            if (Service.getInstance() != null && Service.getInstance().getClient() != null) {
                Service.getInstance().getClient().emit("update_group_avatar", data);
                System.out.println("✓ Test data emitted successfully");
                
                // Also try alternative event name
                Service.getInstance().getClient().emit("updateGroupAvatar", data);
                System.out.println("✓ Alternative event name emitted");
            } else {
                System.err.println("✗ Service or client is null!");
            }
            
        } catch (Exception e) {
            System.err.println("Test upload error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== TEST DIRECT UPLOAD END ===");
    }
    
    // Method to force reload avatar from database and update UI
    private void forceReloadAvatarFromDatabase() {
        System.out.println("=== FORCE RELOAD AVATAR FROM DATABASE ===");
        System.out.println("Group ID: " + group.getGroupID());
        
        try {
            // Request fresh avatar data from server
            service.Service.getInstance().getClient().emit("get_group_avatar", group.getGroupID(), new io.socket.client.Ack() {
                @Override
                public void call(Object... os) {
                    System.out.println("=== GET GROUP AVATAR RESPONSE (RELOAD) ===");
                    System.out.println("Response length: " + os.length);
                    
                    if (os.length > 0) {
                        boolean success = (Boolean) os[0];
                        System.out.println("Success: " + success);
                        
                        if (success && os.length > 1) {
                            try {
                                // Handle both JSONArray and List cases
                                byte[] avatarBytes = null;
                                int dataSize = 0;
                                
                                if (os[1] instanceof org.json.JSONArray) {
                                    System.out.println("Processing JSONArray avatar data");
                                    org.json.JSONArray avatarArray = (org.json.JSONArray) os[1];
                                    dataSize = avatarArray.length();
                                    System.out.println("Received fresh avatar data size: " + dataSize);
                                    
                                    if (dataSize > 0) {
                                        // Convert JSONArray to byte array
                                        avatarBytes = new byte[dataSize];
                                        for (int i = 0; i < dataSize; i++) {
                                            avatarBytes[i] = (byte) avatarArray.getInt(i);
                                        }
                                    }
                                } else if (os[1] instanceof java.util.List) {
                                    System.out.println("Processing List avatar data");
                                    @SuppressWarnings("unchecked")
                                    java.util.List<Integer> avatarList = (java.util.List<Integer>) os[1];
                                    dataSize = avatarList.size();
                                    System.out.println("Received fresh avatar data size: " + dataSize);
                                    
                                    if (dataSize > 0) {
                                        // Convert List to byte array
                                        avatarBytes = new byte[dataSize];
                                        for (int i = 0; i < dataSize; i++) {
                                            avatarBytes[i] = avatarList.get(i).byteValue();
                                        }
                                    }
                                } else {
                                    System.err.println("Unknown avatar data type: " + os[1].getClass().getName());
                                }
                                
                                if (avatarBytes != null && avatarBytes.length > 0) {
                                    System.out.println("Converted fresh data to byte array: " + avatarBytes.length + " bytes");
                                    
                                    // Create final reference for lambda
                                    final byte[] finalAvatarBytes = avatarBytes;
                                    
                                    // Update group model and UI immediately
                                    SwingUtilities.invokeLater(() -> {
                                        // Update the group model with fresh data
                                        group.setGroupImage(finalAvatarBytes);
                                        
                                        // Force clear current avatar display
                                        if (groupAvatarLabel != null) {
                                            groupAvatarLabel.setIcon(null);
                                            groupAvatarLabel.setText("");
                                            groupAvatarLabel.setOpaque(false);
                                            groupAvatarLabel.setBackground(null);
                                            
                                            // Reload avatar with fresh data
                                            loadGroupAvatar(groupAvatarLabel);
                                            
                                            // Force UI refresh
                                            groupAvatarLabel.revalidate();
                                            groupAvatarLabel.repaint();
                                            revalidate();
                                            repaint();
                                        }
                                        
                                        System.out.println("✅ Successfully reloaded avatar from database!");
                                    });
                                } else {
                                    System.out.println("❌ Avatar data is empty, using default avatar");
                                    SwingUtilities.invokeLater(() -> {
                                        if (groupAvatarLabel != null) {
                                            createDefaultGroupAvatar(groupAvatarLabel);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                System.err.println("❌ Error processing fresh avatar data: " + e.getMessage());
                                e.printStackTrace();
                                SwingUtilities.invokeLater(() -> {
                                    if (groupAvatarLabel != null) {
                                        createDefaultGroupAvatar(groupAvatarLabel);
                                    }
                                });
                            }
                        } else {
                            System.out.println("❌ No fresh avatar data received, using default");
                            SwingUtilities.invokeLater(() -> {
                                if (groupAvatarLabel != null) {
                                    createDefaultGroupAvatar(groupAvatarLabel);
                                }
                            });
                        }
                    } else {
                        System.out.println("❌ Empty response, using default avatar");
                        SwingUtilities.invokeLater(() -> {
                            if (groupAvatarLabel != null) {
                                createDefaultGroupAvatar(groupAvatarLabel);
                            }
                        });
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error requesting fresh avatar from DB: " + e.getMessage());
            e.printStackTrace();
            // Fallback to existing data
            SwingUtilities.invokeLater(() -> {
                if (groupAvatarLabel != null) {
                    loadGroupAvatar(groupAvatarLabel);
                }
            });
        }
    }

    // Method to force load avatar directly from database
    private void forceLoadAvatarFromDB() {
        System.out.println("=== FORCE LOAD AVATAR FROM DB START ===");
        System.out.println("Group ID: " + group.getGroupID());
        
        try {
            // Request fresh data from server
            service.Service.getInstance().getClient().emit("get_group_avatar", group.getGroupID(), new io.socket.client.Ack() {
                @Override
                public void call(Object... os) {
                    System.out.println("=== GET GROUP AVATAR RESPONSE ===");
                    System.out.println("Response length: " + os.length);
                    
                    if (os.length > 0) {
                        boolean success = (Boolean) os[0];
                        System.out.println("Success: " + success);
                        
                        if (success && os.length > 1) {
                            try {
                                // Handle both JSONArray and List cases
                                byte[] avatarBytes = null;
                                int dataSize = 0;
                                
                                if (os[1] instanceof org.json.JSONArray) {
                                    System.out.println("Processing JSONArray avatar data");
                                    org.json.JSONArray avatarArray = (org.json.JSONArray) os[1];
                                    dataSize = avatarArray.length();
                                    System.out.println("Received avatar data size: " + dataSize);
                                    
                                    if (dataSize > 0) {
                                        // Convert JSONArray to byte array
                                        avatarBytes = new byte[dataSize];
                                        for (int i = 0; i < dataSize; i++) {
                                            avatarBytes[i] = (byte) avatarArray.getInt(i);
                                        }
                                    }
                                } else if (os[1] instanceof java.util.List) {
                                    System.out.println("Processing List avatar data");
                                    @SuppressWarnings("unchecked")
                                    java.util.List<Integer> avatarList = (java.util.List<Integer>) os[1];
                                    dataSize = avatarList.size();
                                    System.out.println("Received avatar data size: " + dataSize);
                                    
                                    if (dataSize > 0) {
                                        // Convert List to byte array
                                        avatarBytes = new byte[dataSize];
                                        for (int i = 0; i < dataSize; i++) {
                                            avatarBytes[i] = avatarList.get(i).byteValue();
                                        }
                                    }
                                } else {
                                    System.err.println("Unknown avatar data type: " + os[1].getClass().getName());
                                }
                                
                                if (avatarBytes != null && avatarBytes.length > 0) {
                                    System.out.println("Converted to byte array: " + avatarBytes.length + " bytes");
                                    
                                    // Create final reference for lambda
                                    final byte[] finalAvatarBytes = avatarBytes;
                                    
                                    // Update group and UI
                                    SwingUtilities.invokeLater(() -> {
                                        group.setGroupImage(finalAvatarBytes);
                                        
                                        // Force clear and reload avatar
                                        groupAvatarLabel.setIcon(null);
                                        groupAvatarLabel.setText("");
                                        groupAvatarLabel.setOpaque(false);
                                        groupAvatarLabel.setBackground(null);
                                        
                                        loadGroupAvatar(groupAvatarLabel);
                                        
                                        // Force UI refresh
                                        groupAvatarLabel.revalidate();
                                        groupAvatarLabel.repaint();
                                        revalidate();
                                        repaint();
                                        
                                        System.out.println("✅ Force loaded avatar from DB successfully!");
                                    });
                                } else {
                                    System.out.println("❌ Avatar data is empty");
                                }
                            } catch (Exception e) {
                                System.err.println("❌ Error processing avatar data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("❌ No avatar data received or request failed");
                        }
                    } else {
                        System.out.println("❌ Empty response");
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error requesting avatar from DB: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== FORCE LOAD AVATAR FROM DB END ===");
    }
    
    // Method to force load custom avatar, ignoring composite
    private void forceLoadCustomAvatar() {
        System.out.println("=== FORCE LOAD CUSTOM AVATAR ===");
        System.out.println("Group ID: " + group.getGroupID());
        
        byte[] avatarData = group.getGroupImage();
        if (avatarData != null && avatarData.length > 0) {
            System.out.println("Found avatar data: " + avatarData.length + " bytes");
            
            try {
                // Clear current display
                groupAvatarLabel.setIcon(null);
                groupAvatarLabel.setText("");
                groupAvatarLabel.setOpaque(false);
                groupAvatarLabel.setBackground(null);
                
                // Force load the custom avatar
                java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(avatarData);
                java.awt.image.BufferedImage bImage = javax.imageio.ImageIO.read(bis);
                
                if (bImage != null) {
                    ImageIcon avatar = new ImageIcon(bImage);
                    Image scaledImage = avatar.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                    groupAvatarLabel.setIcon(new ImageIcon(scaledImage));
                    
                    // Force UI refresh
                    groupAvatarLabel.revalidate();
                    groupAvatarLabel.repaint();
                    this.revalidate();
                    this.repaint();
                    
                    System.out.println("✅ Successfully force loaded custom avatar!");
                    JOptionPane.showMessageDialog(this, 
                        "Avatar đã được load thành công!", 
                        "Thành công", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    System.err.println("❌ Failed to create BufferedImage from avatar data");
                    JOptionPane.showMessageDialog(this, 
                        "Không thể tạo BufferedImage từ dữ liệu avatar!", 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error force loading avatar: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi load avatar: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("❌ No avatar data found in group model");
            JOptionPane.showMessageDialog(this, 
                "Không có dữ liệu avatar trong model!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
}
