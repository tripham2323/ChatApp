

package form;

import model.Model_User_Account;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Admin
 */
public class Home extends javax.swing.JLayeredPane {

    private Chat chat;
    public Home() {
        initComponents();
        init();
    }

     private void init(){
        setLayout(new MigLayout("fillx, filly", "5[200!]5[fill, 100%]5[200!]5", "0[fill]5")); // set bố cục layout 
        this.add(new Menu_Left());
        chat = new Chat();
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
