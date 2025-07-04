/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Model_Group_Chat;
import model.Model_User_Account;
import swing.ScrollBar;

/**
 *
 * @author Admin
 */
public class MenuGroup extends JPanel implements Item_Group.SelectionCallback {
    private JButton createGroupButton;
    private JPanel groupListPanel;
    private JScrollPane scrollPane;
    private List<Item_Group> groupItems;
    
    public MenuGroup() {
        this.groupItems = new ArrayList<>();
        initComponents();
        loadUserGroups();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(242, 242, 242));
        
        // Header with create group button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(242, 242, 242));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Nhóm chat");
        titleLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 14));
        titleLabel.setForeground(new Color(85, 85, 85));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        createGroupButton = new JButton("+ Tạo nhóm");
        createGroupButton.setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        createGroupButton.setBackground(new Color(27, 149, 226));
        createGroupButton.setForeground(Color.WHITE);
        createGroupButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        createGroupButton.setFocusPainted(false);
        createGroupButton.addActionListener(this::showCreateGroupDialog);
        headerPanel.add(createGroupButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Group list panel
        groupListPanel = new JPanel();
        groupListPanel.setLayout(new BoxLayout(groupListPanel, BoxLayout.Y_AXIS));
        groupListPanel.setBackground(new Color(242, 242, 242));
        
        scrollPane = new JScrollPane(groupListPanel);
        scrollPane.setVerticalScrollBar(new ScrollBar());
        scrollPane.getVerticalScrollBar().setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(new Color(242, 242, 242));
        scrollPane.setBackground(new Color(242, 242, 242));
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Initially show empty state
        showEmptyState();
    }
    
    private void showEmptyState() {
        groupListPanel.removeAll();
        
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(new Color(242, 242, 242));
        emptyPanel.setBorder(new EmptyBorder(50, 20, 50, 20));
        
        JLabel emptyLabel = new JLabel("<html><div style='text-align: center;'>" +
                                     "Bạn chưa tham gia nhóm nào<br>" +
                                     "Hãy tạo nhóm mới để bắt đầu trò chuyện!" +
                                     "</div></html>");
        emptyLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        emptyLabel.setForeground(new Color(128, 128, 128));
        emptyLabel.setHorizontalAlignment(JLabel.CENTER);
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        
        groupListPanel.add(emptyPanel);
        groupListPanel.revalidate();
        groupListPanel.repaint();
    }      private void showCreateGroupDialog(ActionEvent e) {
        // First, get user's friends list
        service.Service.getInstance().getFriendsList(new service.Service.FriendsListCallback() {
            @Override
            public void onResult(List<Model_User_Account> friends) {
                SwingUtilities.invokeLater(() -> {
                    Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(MenuGroup.this);
                    CreateGroupDialog dialog = new CreateGroupDialog(parentFrame, friends);
                    dialog.setVisible(true);
                    
                    // Refresh groups list after dialog closes
                    loadUserGroups();
                });
            }
            
            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(MenuGroup.this, 
                        "Không thể tải danh sách bạn bè: " + error, 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }
    
    public void loadUserGroups() {
        service.Service.getInstance().getUserGroups(new service.Service.GroupListCallback() {
            @Override
            public void onSuccess(List<Model_Group_Chat> groups) {
                SwingUtilities.invokeLater(() -> {
                    updateGroupsList(groups);
                });
            }
            
            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    System.err.println("Error loading groups: " + error);
                    showEmptyState();
                });
            }
        });
    }
    
    private void updateGroupsList(List<Model_Group_Chat> groups) {
        groupListPanel.removeAll();
        groupItems.clear();
        
        if (groups.isEmpty()) {
            showEmptyState();
            return;
        }
        
        for (Model_Group_Chat group : groups) {
            Item_Group groupItem = new Item_Group(group, this);
            groupItems.add(groupItem);
            groupListPanel.add(groupItem);
            groupListPanel.add(Box.createVerticalStrut(2));
        }
        
        groupListPanel.revalidate();
        groupListPanel.repaint();
    }
    
    public void addGroup(Model_Group_Chat group) {
        // Add a new group to the list
        Item_Group groupItem = new Item_Group(group, this);
        groupItems.add(groupItem);
        
        // If this is the first group, clear empty state
        if (groupItems.size() == 1) {
            groupListPanel.removeAll();
        }
        
        groupListPanel.add(groupItem);
        groupListPanel.add(Box.createVerticalStrut(2));
        groupListPanel.revalidate();
        groupListPanel.repaint();
    }
    
    public void selectGroup(int groupID) {
        // Deselect all groups first
        for (Item_Group item : groupItems) {
            item.setSelected(false);
        }
        
        // Select the specified group
        for (Item_Group item : groupItems) {
            if (item.getGroup().getGroupID() == groupID) {
                item.setSelected(true);
                break;
            }
        }
        
        repaint();
    }
    
    public Item_Group getSelectedGroup() {
        for (Item_Group item : groupItems) {
            if (item.isSelected()) {
                return item;
            }
        }
        return null;
    }
    
    public List<Item_Group> getGroupItems() {
        return groupItems;
    }
    
    @Override
    public void onItemSelected(Item_Group selectedItem) {
        // Deselect all groups first
        for (Item_Group item : groupItems) {
            item.setSelected(false);
        }
        
        // Select the clicked item
        selectedItem.setSelected(true);
        
        // Repaint to update visual state
        repaint();
    }
}
