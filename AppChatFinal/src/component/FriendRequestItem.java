/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Model_User_Account;
import model.Model_Friend_Request;
import service.Service;
import event.PublicEvent;
import swing.ImageAvatar;

/**
 *
 * @author Admin
 */
public class FriendRequestItem extends JPanel {
    private Model_User_Account user;
    private Model_Friend_Request friendRequest;
    private ImageAvatar avatar;
    private JLabel nameLabel;
    private JLabel statusLabel;
    private JButton acceptButton;
    private JButton rejectButton;
      public FriendRequestItem(Model_Friend_Request friendRequest) {
        System.out.println("=== CREATING FRIENDREQUESTITEM ===");
        this.friendRequest = friendRequest;
        this.user = friendRequest.getSenderInfo();
        
        System.out.println("FriendRequest ID: " + friendRequest.getRequestID());
        System.out.println("From UserID: " + friendRequest.getFromUserID());
        System.out.println("To UserID: " + friendRequest.getToUserID());
        System.out.println("Status: " + friendRequest.getStatus());
        System.out.println("Sender Info: " + (user != null ? user.getUserName() : "null"));
        
        initComponents();
        setUserData();
        System.out.println("=== FRIENDREQUESTITEM CREATED SUCCESSFULLY ===");
    }private void initComponents() {
        setLayout(new BorderLayout(5, 0));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(5, 8, 5, 8));
        setMaximumSize(new Dimension(200, 50));
        setPreferredSize(new Dimension(200, 50));
        
        // Left panel with avatar
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(45, 50));
        
        avatar = new ImageAvatar();
        avatar.setBorderSize(1);
        avatar.setBorderColor(new Color(200, 200, 200));
        avatar.setPreferredSize(new Dimension(35, 35));
        avatar.setMaximumSize(new Dimension(35, 35));
        avatar.setMinimumSize(new Dimension(35, 35));
        leftPanel.add(avatar);
        
        // Center panel with user info
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        nameLabel = new JLabel();
        nameLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        nameLabel.setForeground(new Color(51, 51, 51));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        statusLabel = new JLabel("Muốn kết bạn");
        statusLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        statusLabel.setForeground(new Color(107, 13, 158));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(statusLabel);
        centerPanel.add(infoPanel, BorderLayout.WEST);
          // Right panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 8));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setPreferredSize(new Dimension(90, 50));
        
        // Accept button
        acceptButton = new JButton("✓");
        acceptButton.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        acceptButton.setBackground(new Color(34, 139, 34));
        acceptButton.setForeground(Color.WHITE);
        acceptButton.setBorder(new EmptyBorder(3, 8, 3, 8));
        acceptButton.setFocusPainted(false);
        acceptButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        acceptButton.setPreferredSize(new Dimension(35, 30));
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acceptFriendRequest();
            }
        });
        
        // Reject button
        rejectButton = new JButton("✗");
        rejectButton.setFont(new Font("JetBrains Mono", Font.BOLD, 12));
        rejectButton.setBackground(new Color(220, 53, 69));
        rejectButton.setForeground(Color.WHITE);
        rejectButton.setBorder(new EmptyBorder(3, 8, 3, 8));
        rejectButton.setFocusPainted(false);
        rejectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));        rejectButton.setPreferredSize(new Dimension(35, 30));
        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rejectFriendRequest();
            }
        });
        
        buttonPanel.add(acceptButton);
        buttonPanel.add(rejectButton);
        
        // Add all panels to main panel
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
    }
    
    private void setUserData() {
        if (user != null) {
            nameLabel.setText(user.getUserName());
            
            // Set avatar
            if (user.getImage() != null && user.getImage().length > 0) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(user.getImage());
                    BufferedImage bImage = ImageIO.read(bis);
                    ImageIcon avatarIcon = new ImageIcon(bImage);
                    avatar.setImage(avatarIcon);
                } catch (IOException ex) {
                    Logger.getLogger(FriendRequestItem.class.getName()).log(Level.SEVERE, null, ex);
                    setDefaultAvatar();
                }
            } else {
                setDefaultAvatar();
            }
        }
    }
    
    private void setDefaultAvatar() {
        try {
            ImageIcon defaultIcon = new ImageIcon(getClass().getResource("/icon/profile.png"));
            avatar.setImage(defaultIcon);
        } catch (Exception e) {
            // If default icon not found, create a simple colored circle
            avatar.setBackground(new Color(200, 200, 200));
        }
    }
      private void acceptFriendRequest() {
        acceptButton.setText("...");
        acceptButton.setEnabled(false);
        rejectButton.setEnabled(false);
        
        // Call service method to accept friend request using request ID
        Service.getInstance().acceptFriendRequest(friendRequest.getRequestID(), new FriendRequestCallback() {            @Override
            public void onSuccess() {
                // Remove this item from parent panel or mark as accepted
                statusLabel.setText("Đã chấp nhận");
                statusLabel.setForeground(new Color(34, 139, 34));
                acceptButton.setVisible(false);
                rejectButton.setVisible(false);
                
                // Trigger event for UI refresh
                if (PublicEvent.getInstance().getEventFriendRequest() != null) {
                    PublicEvent.getInstance().getEventFriendRequest().onFriendRequestAccepted();
                }
            }
            
            @Override
            public void onError(String error) {
                acceptButton.setText("✓");
                acceptButton.setEnabled(true);
                rejectButton.setEnabled(true);
                System.err.println("Error accepting friend request: " + error);
            }
        });
    }
      private void rejectFriendRequest() {
        rejectButton.setText("...");
        rejectButton.setEnabled(false);
        acceptButton.setEnabled(false);
        
        // Call service method to reject friend request using request ID
        Service.getInstance().rejectFriendRequest(friendRequest.getRequestID(), new FriendRequestCallback() {            @Override
            public void onSuccess() {
                // Remove this item from parent panel or mark as rejected
                statusLabel.setText("Đã từ chối");
                statusLabel.setForeground(new Color(220, 53, 69));
                acceptButton.setVisible(false);
                rejectButton.setVisible(false);
                
                // Trigger event for UI refresh
                if (PublicEvent.getInstance().getEventFriendRequest() != null) {
                    PublicEvent.getInstance().getEventFriendRequest().onFriendRequestRejected();
                }
            }
            
            @Override
            public void onError(String error) {
                rejectButton.setText("✗");
                rejectButton.setEnabled(true);
                acceptButton.setEnabled(true);
                System.err.println("Error rejecting friend request: " + error);
            }
        });
    }
    
    public Model_User_Account getUser() {
        return user;
    }
    
    // Interface for friend request callback
    public interface FriendRequestCallback {
        void onSuccess();
        void onError(String error);
    }
}
