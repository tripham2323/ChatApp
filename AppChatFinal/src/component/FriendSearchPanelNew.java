/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import model.Model_User_Account;
import model.Model_Friend_Request;
import service.Service;
import swing.ScrollBar;
import event.EventFriendRequest;
import event.PublicEvent;

/**
 *
 * @author Admin
 */
public class FriendSearchPanelNew extends JPanel implements EventFriendRequest {
    private JTextField searchField;
    private JButton searchButton;
    private JPanel searchResultPanel;
    private JPanel friendRequestPanel;
    private JScrollPane searchScrollPane;
    private JScrollPane requestScrollPane;
    private JTabbedPane tabbedPane;      public FriendSearchPanelNew() {
        initComponents();
        // Register this component as the friend request event handler
        PublicEvent.getInstance().addEventFriendRequest(this);
        
        // Check for any pending friend request notifications
        SwingUtilities.invokeLater(() -> {
            Service.getInstance().checkAndProcessPendingNotifications();
        });
    }
      private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(242, 242, 242));
        setPreferredSize(new Dimension(205, 550));
        setMaximumSize(new Dimension(205, 550));
          // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new java.awt.Font("JetBrains Mono", 0, 11));
        tabbedPane.setPreferredSize(new Dimension(200, 540));
        
        // Create search tab
        JPanel searchTab = createSearchTab();
        tabbedPane.addTab("Tìm kiếm", searchTab);
        
        // Create friend request tab
        JPanel requestTab = createRequestTab();
        tabbedPane.addTab("Lời mời", requestTab);        add(tabbedPane, BorderLayout.CENTER);
        
        // Delay loading friend requests to ensure user is logged in
        SwingUtilities.invokeLater(() -> {
            System.out.println("FriendSearchPanelNew: Initial loading timer triggered");
            // Wait a bit more for login to complete, then load friend requests
            javax.swing.Timer timer = new javax.swing.Timer(1500, e -> {
                System.out.println("FriendSearchPanelNew: Timer fired, attempting initial load");
                if (Service.getInstance().getUser() != null) {
                    System.out.println("FriendSearchPanelNew: User found, loading friend requests");
                    loadFriendRequests();
                } else {
                    System.out.println("FriendSearchPanelNew: User still null, will try again later");
                    // Try again after another delay
                    javax.swing.Timer retryTimer = new javax.swing.Timer(2000, evt -> {
                        if (Service.getInstance().getUser() != null) {
                            loadFriendRequests();
                        }
                    });
                    retryTimer.setRepeats(false);
                    retryTimer.start();
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    
    private JPanel createSearchTab() {
        JPanel searchTab = new JPanel(new BorderLayout());
        searchTab.setBackground(new Color(242, 242, 242));
        searchTab.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Title
        JLabel titleLabel = new JLabel("Tìm kiếm bạn bè");
        titleLabel.setFont(new java.awt.Font("JetBrains Mono", 1, 14));
        titleLabel.setForeground(new Color(85, 85, 85));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        searchTab.add(titleLabel, BorderLayout.NORTH);
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(new Color(242, 242, 242));
        searchPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.setBorder(new EmptyBorder(6, 8, 6, 8));
        searchField.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
        
        searchButton = new JButton("Tìm");
        searchButton.setPreferredSize(new Dimension(55, 35));
        searchButton.setFont(new java.awt.Font("JetBrains Mono", 1, 11));
        searchButton.setBackground(new Color(27, 149, 226));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchTab.add(searchPanel, BorderLayout.CENTER);
        
        // Results panel
        searchResultPanel = new JPanel();
        searchResultPanel.setLayout(new BoxLayout(searchResultPanel, BoxLayout.Y_AXIS));
        searchResultPanel.setBackground(new Color(242, 242, 242));
          searchScrollPane = new JScrollPane(searchResultPanel);
        searchScrollPane.setPreferredSize(new Dimension(195, 450));
        searchScrollPane.setVerticalScrollBar(new ScrollBar());
        searchScrollPane.getVerticalScrollBar().setBackground(Color.WHITE);
        searchScrollPane.getViewport().setBackground(Color.WHITE);
        searchScrollPane.setBackground(new Color(242, 242, 242));
        searchScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        searchTab.add(searchScrollPane, BorderLayout.SOUTH);
        
        // Add search functionality
        ActionListener searchAction = e -> performSearch();
        searchButton.addActionListener(searchAction);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
        
        return searchTab;
    }      private JPanel createRequestTab() {
        JPanel requestTab = new JPanel(new BorderLayout());
        requestTab.setBackground(new Color(242, 242, 242));
        requestTab.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Header panel with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(242, 242, 242));
        
        // Title
        JLabel titleLabel = new JLabel("Lời mời kết bạn");
        titleLabel.setFont(new java.awt.Font("JetBrains Mono", 1, 14));
        titleLabel.setForeground(new Color(85, 85, 85));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
          // Refresh button
        JButton refreshButton = new JButton("⟳");
        refreshButton.setFont(new java.awt.Font("JetBrains Mono", 1, 16));
        refreshButton.setBackground(new Color(27, 149, 226));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(new EmptyBorder(2, 8, 2, 8));
        refreshButton.setToolTipText("Tải lại danh sách lời mời kết bạn");
        refreshButton.addActionListener(e -> {
            System.out.println("=== MANUAL REFRESH BUTTON CLICKED ===");
            refreshButton.setEnabled(false);
            refreshButton.setText("...");
            
            // Force a fresh load after a small delay to ensure UI updates
            SwingUtilities.invokeLater(() -> {
                try {
                    loadFriendRequests();
                } finally {
                    // Re-enable button after 2 seconds regardless
                    new javax.swing.Timer(2000, evt -> {
                        refreshButton.setEnabled(true);
                        refreshButton.setText("⟳");
                        ((javax.swing.Timer) evt.getSource()).stop();
                    }).start();
                }
            });
        });
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        requestTab.add(headerPanel, BorderLayout.NORTH);
        
        // Friend request panel
        friendRequestPanel = new JPanel();
        friendRequestPanel.setLayout(new BoxLayout(friendRequestPanel, BoxLayout.Y_AXIS));
        friendRequestPanel.setBackground(new Color(242, 242, 242));
          requestScrollPane = new JScrollPane(friendRequestPanel);
        requestScrollPane.setPreferredSize(new Dimension(195, 500));
        requestScrollPane.setVerticalScrollBar(new ScrollBar());
        requestScrollPane.getVerticalScrollBar().setBackground(Color.WHITE);
        requestScrollPane.getViewport().setBackground(Color.WHITE);
        requestScrollPane.setBackground(new Color(242, 242, 242));
        requestScrollPane.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        requestTab.add(requestScrollPane, BorderLayout.CENTER);
        
        return requestTab;
    }    
    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            return;
        }
        
        // Clear previous results
        searchResultPanel.removeAll();
        
        // Add loading message
        JLabel loadingLabel = new JLabel("Đang tìm kiếm...");
        loadingLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
        loadingLabel.setForeground(new Color(128, 128, 128));
        loadingLabel.setHorizontalAlignment(JLabel.CENTER);
        loadingLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        searchResultPanel.add(loadingLabel);
        searchResultPanel.revalidate();
        searchResultPanel.repaint();
        
        // Perform search in background
        SwingUtilities.invokeLater(() -> {
            Service.getInstance().searchUsers(searchText, new component.FriendSearchPanel.SearchCallback() {
                @Override
                public void onResult(List<Model_User_Account> users) {
                    SwingUtilities.invokeLater(() -> {
                        searchResultPanel.removeAll();
                        
                        if (users.isEmpty()) {
                            JLabel noResultsLabel = new JLabel("Không tìm thấy người dùng nào");
                            noResultsLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
                            noResultsLabel.setForeground(new Color(128, 128, 128));
                            noResultsLabel.setHorizontalAlignment(JLabel.CENTER);
                            noResultsLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
                            searchResultPanel.add(noResultsLabel);
                        } else {
                            for (Model_User_Account user : users) {
                                FriendSearchItem item = new FriendSearchItem(user);
                                item.setMaximumSize(new Dimension(260, 60));
                                searchResultPanel.add(item);
                            }
                        }
                        
                        searchResultPanel.revalidate();
                        searchResultPanel.repaint();
                    });
                }
                
                @Override
                public void onError(String error) {
                    SwingUtilities.invokeLater(() -> {
                        searchResultPanel.removeAll();
                        
                        JLabel errorLabel = new JLabel("Lỗi: " + error);
                        errorLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
                        errorLabel.setForeground(Color.RED);
                        errorLabel.setHorizontalAlignment(JLabel.CENTER);
                        errorLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
                        searchResultPanel.add(errorLabel);
                        
                        searchResultPanel.revalidate();
                        searchResultPanel.repaint();
                    });
                }
            });
        });
    }    private void loadFriendRequests() {
        System.out.println("FriendSearchPanelNew: Starting to load friend requests...");
          // Check if user is logged in
        if (Service.getInstance().getUser() == null) {
            System.out.println("FriendSearchPanelNew: User not logged in, cannot load friend requests");
            SwingUtilities.invokeLater(() -> {
                friendRequestPanel.removeAll();
                JLabel errorLabel = new JLabel("Vui lòng đăng nhập để xem lời mời");
                errorLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
                errorLabel.setForeground(Color.RED);
                errorLabel.setHorizontalAlignment(JLabel.CENTER);
                errorLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
                friendRequestPanel.add(errorLabel);
                friendRequestPanel.revalidate();
                friendRequestPanel.repaint();
            });
            return;
        }
        
        System.out.println("FriendSearchPanelNew: User logged in as: " + Service.getInstance().getUser().getUserName());
        
        // Show loading indicator
        SwingUtilities.invokeLater(() -> {
            friendRequestPanel.removeAll();
            JLabel loadingLabel = new JLabel("Đang tải lời mời kết bạn...");
            loadingLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
            loadingLabel.setForeground(new Color(85, 85, 85));
            loadingLabel.setHorizontalAlignment(JLabel.CENTER);
            loadingLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
            friendRequestPanel.add(loadingLabel);
            friendRequestPanel.revalidate();
            friendRequestPanel.repaint();
        });
          // Call service to get friend requests (not in SwingUtilities.invokeLater)
        Service.getInstance().getFriendRequests(new Service.FriendRequestListCallback() {
            @Override
            public void onResult(List<Model_Friend_Request> requests) {
                System.out.println("=== FRIENDSEARCHPANELNEW ONRESULT CALLED ===");
                System.out.println("Thread: " + Thread.currentThread().getName());
                System.out.println("Received " + requests.size() + " friend requests");
                System.out.println("Current UI component count before update: " + friendRequestPanel.getComponentCount());
                
                // Debug each request
                for (int i = 0; i < requests.size(); i++) {
                    Model_Friend_Request req = requests.get(i);
                    System.out.println("  Request " + i + ": ID=" + req.getRequestID() + 
                                     ", From=" + req.getFromUserID() + 
                                     ", SenderName=" + (req.getSenderInfo() != null ? req.getSenderInfo().getUserName() : "null"));
                }
                
                // Ensure UI update happens on EDT
                if (SwingUtilities.isEventDispatchThread()) {
                    System.out.println("Already on EDT, updating UI directly");
                    updateFriendRequestsUI(requests);
                } else {
                    System.out.println("Not on EDT, queuing UI update");
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("UI update queued task executing on: " + Thread.currentThread().getName());
                        updateFriendRequestsUI(requests);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                System.out.println("=== FRIENDSEARCHPANELNEW ONERROR CALLED ===");
                System.out.println("Error: " + error);
                SwingUtilities.invokeLater(() -> {
                    friendRequestPanel.removeAll();
                    
                    JLabel errorLabel = new JLabel("Lỗi: " + error);
                    errorLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
                    errorLabel.setForeground(Color.RED);
                    errorLabel.setHorizontalAlignment(JLabel.CENTER);
                    errorLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
                    friendRequestPanel.add(errorLabel);
                    
                    friendRequestPanel.revalidate();
                    friendRequestPanel.repaint();
                });
            }
        });
    }
    
    private void updateFriendRequestsUI(List<Model_Friend_Request> requests) {
        System.out.println("=== UPDATING FRIEND REQUESTS UI ===");
        System.out.println("Update thread: " + Thread.currentThread().getName());
        System.out.println("Updating UI with " + requests.size() + " requests");
        System.out.println("Panel before clear has " + friendRequestPanel.getComponentCount() + " components");
        
        friendRequestPanel.removeAll();
        System.out.println("Panel cleared, now has " + friendRequestPanel.getComponentCount() + " components");
        
        if (requests.isEmpty()) {
            JLabel noRequestsLabel = new JLabel("Không có lời mời nào");
            noRequestsLabel.setFont(new java.awt.Font("JetBrains Mono", 0, 12));
            noRequestsLabel.setForeground(new Color(128, 128, 128));
            noRequestsLabel.setHorizontalAlignment(JLabel.CENTER);
            noRequestsLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
            friendRequestPanel.add(noRequestsLabel);
            System.out.println("Added 'no requests' label");
        } else {
            System.out.println("Adding " + requests.size() + " request items to UI");
            for (int i = 0; i < requests.size(); i++) {
                Model_Friend_Request request = requests.get(i);
                try {
                    System.out.println("Creating FriendRequestItem for request " + i + ": " + 
                                     (request.getSenderInfo() != null ? request.getSenderInfo().getUserName() : "unknown"));
                    FriendRequestItem item = new FriendRequestItem(request);
                    item.setMaximumSize(new Dimension(195, 60));
                    friendRequestPanel.add(item);
                    System.out.println("Successfully added request item " + i + " to panel");
                } catch (Exception e) {
                    System.err.println("Error creating FriendRequestItem for request " + i + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Panel after adding components has " + friendRequestPanel.getComponentCount() + " components");
        friendRequestPanel.revalidate();
        friendRequestPanel.repaint();
        System.out.println("=== UI UPDATE COMPLETED ===");
    }
      public void refreshFriendRequests() {
        loadFriendRequests();
    }
    
    // Public method to force refresh friend requests (can be called from outside)
    public void forceRefreshFriendRequests() {
        System.out.println("=== FORCE REFRESH TRIGGERED FROM EXTERNAL SOURCE ===");
        loadFriendRequests();
    }
    
    // Public method to get current request count (for debugging)
    public int getCurrentRequestCount() {
        return friendRequestPanel.getComponentCount();
    }    // EventFriendRequest interface implementation
    @Override
    public void onFriendRequestReceived() {
        System.out.println("=== FriendSearchPanelNew.onFriendRequestReceived() CALLED ===");
        System.out.println("Current thread: " + Thread.currentThread().getName());
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("=== UI UPDATE FOR NEW FRIEND REQUEST ===");
            loadFriendRequests();
            // Switch to friend request tab to show new request
            tabbedPane.setSelectedIndex(1);
            System.out.println("=== SWITCHED TO FRIEND REQUEST TAB ===");
        });
    }
    
    @Override
    public void onFriendRequestAccepted() {
        SwingUtilities.invokeLater(() -> {
            loadFriendRequests();
        });
    }
    
    @Override
    public void onFriendRequestRejected() {
        SwingUtilities.invokeLater(() -> {
            loadFriendRequests();
        });
    }
    
    @Override
    public void onRefreshFriendRequests() {
        SwingUtilities.invokeLater(() -> {
            loadFriendRequests();
        });
    }
      // Test method to manually trigger friend request loading (for debugging)
    public void testLoadFriendRequests() {
        System.out.println("=== MANUAL TEST LOAD TRIGGERED ===");
        System.out.println("Current panel component count: " + friendRequestPanel.getComponentCount());
        loadFriendRequests();
    }
}
