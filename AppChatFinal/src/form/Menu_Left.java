/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package form;

import component.Item_people;
import event.EventMenuLeft;
import event.PublicEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import model.Model_User_Account;
import swing.ScrollBar;

/**
 *
 * @author Admin
 */
public class Menu_Left extends javax.swing.JPanel {
    private List<Model_User_Account> userAccount;
    private component.FriendSearchPanelNew friendSearchPanel;
    /**
     * Creates new form Menu_Left
     */
    public Menu_Left() {
        initComponents();
        init();
    }
    private void init(){
           sp.setVerticalScrollBar(new ScrollBar()); //set cuộn page
        userAccount = new ArrayList<>();// list của all info client khác với clien đăng nhập hiện tại
        PublicEvent.getInstance().addEventMenuLeft(new EventMenuLeft() {
               @Override
               public void newUser(List<Model_User_Account> users) {// all list info client khác với clien đăng nhập hiện tại
                   for (Model_User_Account d:users){//duyệt từng element của mảng users cho biến d
                       userAccount.add(d);
                       Item_people item = new Item_people(d);
                       // Set fixed height for each item to prevent excessive height
                       item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));
                       item.setPreferredSize(new java.awt.Dimension(200, 60));
                       menuList.add(item);
                       refreshMenuList();
                    }
                  }

               @Override
               public void userConnect(Model_User_Account user) {
                   for(Model_User_Account u:userAccount){
                       if(u.getUserID() == user.getUserID()){
                          u.setStatus(true);
                          PublicEvent.getInstance().getEventMain().updateUser(u);
                          break;
                       }
                   }
                   if(menuMessage.isSelected()){
                       for(Component com:menuList.getComponents()){
                            Item_people item = (Item_people)com;
                            if(item.getUser().getUserID() == user.getUserID()){
                                item.updateStatus();
                                
                                break;
                            
                            }
                       } 
                    }
                  }

               @Override
               public void userDisconnect(int userID) {
                   for(Model_User_Account u:userAccount){
                       if(u.getUserID() == userID){
                          u.setStatus(false);
                          PublicEvent.getInstance().getEventMain().updateUser(u);
                          break;
                       }
                   }
                   if(menuMessage.isSelected()){
                       for(Component com:menuList.getComponents()){
                            Item_people item = (Item_people)com;
                            if(item.getUser().getUserID() == userID){
                                item.updateStatus();
                                break;
                            
                            }
                       } 
                    }
                  }
               
           });
       showMessage();
    }
    private void showMessage() {
        // Use BoxLayout for proper vertical stacking of message items, like the original
        menuList.setLayout(new javax.swing.BoxLayout(menuList, javax.swing.BoxLayout.Y_AXIS));
        
        menuList.removeAll();
        for (Model_User_Account d:userAccount) {
            Item_people item = new Item_people(d);
            // Set fixed height for each item to prevent excessive height
            item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));
            item.setPreferredSize(new java.awt.Dimension(200, 60));
            menuList.add(item); //thêm list user onl trong menulistleft
        }
        refreshMenuList();
    }
    private void showGroup() {
        // Use BoxLayout for proper vertical stacking of group items, like the original
        menuList.setLayout(new javax.swing.BoxLayout(menuList, javax.swing.BoxLayout.Y_AXIS));
        
        menuList.removeAll();
        for (int i = 0; i < 5; i++) {
            Item_people item = new Item_people(null);
            // Set fixed height for each item to prevent excessive height
            item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));
            item.setPreferredSize(new java.awt.Dimension(200, 60));
            menuList.add(item);
        }
        refreshMenuList();
    }    private void showBox() {
        // Use BoxLayout only for friend search functionality
        menuList.setLayout(new javax.swing.BoxLayout(menuList, javax.swing.BoxLayout.Y_AXIS));
        
        menuList.removeAll();
        
        // Reuse existing friend search panel or create new one if not exists
        if (friendSearchPanel == null) {
            friendSearchPanel = new component.FriendSearchPanelNew();
            // Event registration is already handled in the constructor of FriendSearchPanelNew
        }
        
        menuList.add(friendSearchPanel);
        
        refreshMenuList();
    }
      private void refreshMenuList() {
        menuList.repaint();
        menuList.revalidate();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menu = new javax.swing.JLayeredPane();
        menuMessage = new component.MenuButton();
        menuGroup = new component.MenuButton();
        menuBox = new component.MenuButton();
        sp = new javax.swing.JScrollPane();
        menuList = new javax.swing.JLayeredPane();

        setBackground(new java.awt.Color(242, 242, 242));

        menu.setBackground(new java.awt.Color(229, 229, 229));
        menu.setOpaque(true);
        menu.setLayout(new java.awt.GridLayout(1, 3));

        menuMessage.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/icon/message_selected.png"))); // NOI18N
        menuMessage.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/icon/message.png"))); // NOI18N
        menuMessage.setSelected(true);
        menuMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMessageActionPerformed(evt);
            }
        });
        menu.add(menuMessage);

        menuGroup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/group.png"))); // NOI18N
        menuGroup.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/icon/group_selected.png"))); // NOI18N
        menuGroup.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/icon/group.png"))); // NOI18N
        menuGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuGroupActionPerformed(evt);
            }
        });
        menu.add(menuGroup);

        menuBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/box.png"))); // NOI18N
        menuBox.setIconSelected(new javax.swing.ImageIcon(getClass().getResource("/icon/box_selected.png"))); // NOI18N
        menuBox.setIconSimple(new javax.swing.ImageIcon(getClass().getResource("/icon/box.png"))); // NOI18N
        menuBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuBoxActionPerformed(evt);
            }
        });
        menu.add(menuBox);

        sp.setBackground(new java.awt.Color(242, 242, 242));
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        menuList.setBackground(new java.awt.Color(242, 242, 242));
        menuList.setOpaque(true);

        javax.swing.GroupLayout menuListLayout = new javax.swing.GroupLayout(menuList);
        menuList.setLayout(menuListLayout);
        menuListLayout.setHorizontalGroup(
            menuListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        menuListLayout.setVerticalGroup(
            menuListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 541, Short.MAX_VALUE)
        );

        sp.setViewportView(menuList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(menu, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
            .addComponent(sp, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(menu, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(sp, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
//set hiển thị icon 2nd của nút khi được nhấn
    private void menuMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMessageActionPerformed
          if(!menuMessage.isSelected()){
          menuMessage.setSelected(true);
          menuGroup.setSelected(false);
          menuBox.setSelected(false);
          showMessage();
          }
          
    }//GEN-LAST:event_menuMessageActionPerformed

    private void menuGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGroupActionPerformed
         if(!menuGroup.isSelected()){
         menuMessage.setSelected(false);
         menuGroup.setSelected(true);
         menuBox.setSelected(false);
         showGroup();
         }
    }//GEN-LAST:event_menuGroupActionPerformed

    private void menuBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuBoxActionPerformed
         if(!menuBox.isSelected()){
         menuMessage.setSelected(false);
         menuGroup.setSelected(false);
         menuBox.setSelected(true);
         showBox();
         }
    }//GEN-LAST:event_menuBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane menu;
    private component.MenuButton menuBox;
    private component.MenuButton menuGroup;
    private javax.swing.JLayeredPane menuList;
    private component.MenuButton menuMessage;
    private javax.swing.JScrollPane sp;
    // End of variables declaration//GEN-END:variables
}
