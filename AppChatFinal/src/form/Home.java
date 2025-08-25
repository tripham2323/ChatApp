

package form;

import model.Model_User_Account;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Admin
 */
public class Home extends javax.swing.JLayeredPane {    private Chat chat;
    private Menu_Left menuLeft;
    
    public Home() {
        initComponents();
        init();
    }

     private void init(){
        setLayout(new MigLayout("fillx, filly", "5[200!]5[fill, 100%]5[200!]5", "0[fill]5")); // set bố cục layout 
        menuLeft = new Menu_Left();
        this.add(menuLeft);
        chat = new Chat();
        chat.setMenuLeft(menuLeft); // Pass Menu_Left reference to Chat
        this.add(chat);
        this.add(new Menu_Right());
        chat.setVisible(false);
     }
     
     public void setUser(Model_User_Account user){
        chat.setUser(user);
        chat.setVisible(true);
     }
     
     public void updateUser(Model_User_Account user){
        chat.updateUser(user);
    
    }
    
    public void updateGroupAvatar(int groupID, byte[] newAvatar) {
        // Update the group avatar in the menu left
        menuLeft.updateGroupAvatar(groupID, newAvatar);
    }
    
    public void refreshGroupsList() {
        // Reload groups from server
        menuLeft.refreshGroupsList();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
