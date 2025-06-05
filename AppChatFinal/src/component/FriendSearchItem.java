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
import service.Service;
import swing.ImageAvatar;

/**
 *
 * @author Admin
 */
public class FriendSearchItem extends JPanel {
    private Model_User_Account user;
    private ImageAvatar avatar;
    private JLabel nameLabel;
    private JLabel statusLabel;
    private JButton addFriendButton;
    
    public FriendSearchItem(Model_User_Account user) {
        this.user = user;
        initComponents();
        setUserData();
    }    private void initComponents() {
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
        
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        statusLabel.setForeground(new Color(128, 128, 128));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
          infoPanel.add(nameLabel);
        infoPanel.add(statusLabel);
        
        centerPanel.add(infoPanel, BorderLayout.WEST);
        
        // Right panel with button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 8));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setPreferredSize(new Dimension(70, 50));
        
        // Add friend button
        addFriendButton = new JButton("Kết bạn");
        addFriendButton.setFont(new Font("JetBrains Mono", Font.BOLD, 9));
        addFriendButton.setBackground(new Color(107, 13, 158));
        addFriendButton.setForeground(Color.WHITE);
        addFriendButton.setBorder(new EmptyBorder(3, 8, 3, 8));
        addFriendButton.setFocusPainted(false);
        addFriendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addFriendButton.setPreferredSize(new Dimension(55, 30));
        addFriendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFriendRequest();
            }
        });
        buttonPanel.add(addFriendButton);
        
        // Add all panels to main panel
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
    }
    
    private void setUserData() {
        if (user != null) {
            nameLabel.setText(user.getUserName());
            statusLabel.setText(user.getStatus() ? "Đang hoạt động" : "Không hoạt động");
            statusLabel.setForeground(user.getStatus() ? 
                new Color(34, 139, 34) : new Color(128, 128, 128));
            
            // Set avatar
            if (user.getImage() != null && user.getImage().length > 0) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(user.getImage());
                    BufferedImage bImage = ImageIO.read(bis);
                    ImageIcon avatarIcon = new ImageIcon(bImage);
                    avatar.setImage(avatarIcon);
                } catch (IOException ex) {
                    Logger.getLogger(FriendSearchItem.class.getName()).log(Level.SEVERE, null, ex);
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
    
    private void sendFriendRequest() {
        addFriendButton.setText("Đang gửi...");
        addFriendButton.setEnabled(false);
        
        Service.getInstance().sendFriendRequest(user.getUserID(), new FriendRequestCallback() {
            @Override
            public void onSuccess() {
                addFriendButton.setText("Đã gửi");
                addFriendButton.setBackground(new Color(128, 128, 128));
                addFriendButton.setEnabled(false);
            }
            
            @Override
            public void onError(String error) {
                addFriendButton.setText("Kết bạn");
                addFriendButton.setEnabled(true);
                // Show error message (you can implement a toast or dialog here)
                System.err.println("Error sending friend request: " + error);
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
