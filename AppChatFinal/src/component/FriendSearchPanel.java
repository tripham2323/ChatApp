/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

import event.PublicEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Model_User_Account;
import net.miginfocom.swing.MigLayout;
import service.Service;
import swing.ScrollBar;

/**
 *
 * @author Admin
 */
public class FriendSearchPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JPanel resultPanel;
    private JScrollPane scrollPane;
    private JLabel titleLabel;
    
    public FriendSearchPanel() {
        initComponents();
    }    private void initComponents() {
        setLayout(new MigLayout("fillx, wrap 1, insets 5", "[fill]", "[]5[40!]5[fill]"));
        setBackground(new Color(242, 242, 242));
        setMaximumSize(new Dimension(280, 600)); // Giới hạn chiều rộng
        
        // Title
        titleLabel = new JLabel("Tìm kiếm bạn bè");
        titleLabel.setFont(new java.awt.Font("JetBrains Mono", 1, 14));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(titleLabel, "align center");
          // Search panel
        JPanel searchPanel = new JPanel(new MigLayout("fillx", "[fill,grow]3[60!]", "[]"));
        searchPanel.setBackground(new Color(242, 242, 242));
        searchPanel.setMaximumSize(new Dimension(270, 40));
        
        searchField = new JTextField();
        searchField.setFont(new java.awt.Font("JetBrains Mono", 0, 11));
        searchField.setBorder(new EmptyBorder(6, 8, 6, 8));
        searchField.setBackground(Color.WHITE);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
        
        searchButton = new JButton("Tìm");
        searchButton.setFont(new java.awt.Font("JetBrains Mono", 1, 10));
        searchButton.setBackground(new Color(107, 13, 158));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(new EmptyBorder(6, 8, 6, 8));
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        searchPanel.add(searchField, "grow");
        searchPanel.add(searchButton, "w 60!");
        add(searchPanel, "growx, h 40!");
          // Results panel
        resultPanel = new JPanel(new MigLayout("fillx, wrap 1, insets 0", "[fill]", "[]2[]"));
        resultPanel.setBackground(new Color(242, 242, 242));
        
        scrollPane = new JScrollPane(resultPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(242, 242, 242));
        scrollPane.setVerticalScrollBar(new ScrollBar());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, "grow, pushy");
        
        // Initial message
        showInitialMessage();
    }
    
    private void showInitialMessage() {
        resultPanel.removeAll();
        JLabel messageLabel = new JLabel("Nhập tên người dùng để tìm kiếm");
        messageLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
        messageLabel.setForeground(new Color(128, 128, 128));
        messageLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        resultPanel.add(messageLabel, "align center");
        refreshResults();
    }
    
    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            showInitialMessage();
            return;
        }
        
        // Show loading message
        resultPanel.removeAll();
        JLabel loadingLabel = new JLabel("Đang tìm kiếm...");
        loadingLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
        loadingLabel.setForeground(new Color(128, 128, 128));
        loadingLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        resultPanel.add(loadingLabel, "align center");
        refreshResults();
        
        // Perform search
        try {
            Service.getInstance().searchUsers(searchText, new SearchCallback() {
                @Override
                public void onResult(List<Model_User_Account> users) {
                    // Ensure UI updates happen on the Event Dispatch Thread
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            displaySearchResults(users);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    // Ensure UI updates happen on the Event Dispatch Thread
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            showError(error);
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tìm kiếm: " + e.getMessage());
        }
    }
    
    private void displaySearchResults(List<Model_User_Account> users) {
        resultPanel.removeAll();
        
        if (users == null || users.isEmpty()) {
            JLabel noResultLabel = new JLabel("Không tìm thấy người dùng nào");
            noResultLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
            noResultLabel.setForeground(new Color(128, 128, 128));
            noResultLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
            resultPanel.add(noResultLabel, "align center");        } else {
            for (Model_User_Account user : users) {
                FriendSearchItem item = new FriendSearchItem(user);
                resultPanel.add(item, "growx, h 60!");
            }
        }
        
        refreshResults();
    }
    
    private void showError(String error) {
        resultPanel.removeAll();
        JLabel errorLabel = new JLabel("Lỗi: " + error);
        errorLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
        errorLabel.setForeground(Color.RED);
        errorLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        resultPanel.add(errorLabel, "align center");
        refreshResults();
    }
    
    private void refreshResults() {
        resultPanel.revalidate();
        resultPanel.repaint();
    }
    
    // Interface for search callback
    public interface SearchCallback {
        void onResult(List<Model_User_Account> users);
        void onError(String error);
    }
}
