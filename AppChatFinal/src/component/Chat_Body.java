/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package component;

import app.MessageType;
import emoji.Emoji;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import model.Model_Receive_Message;
import model.Model_Send_Message;
import model.Model_User_Account;
import model.Model_Voice_Receive;
import net.miginfocom.swing.MigLayout;
import swing.ScrollBar;

/**
 *
 * @author Admin
 */
public class Chat_Body extends javax.swing.JPanel {

    /**
     * Creates new form Chat_Body
     */
    public Chat_Body() {
        initComponents();
        init();
//         addItemRight("hello This method is called from within the constructor to initialize the form This method is called from within the constructor to initialize the form");
//             addItemRight("hello This method is called from within the constructor to initialize the form This method is called from within the constructor to initialize the form");
//           addDate("20/10/2021");
//           String img[] = {"LTJuDjOau6bw~9SjjYW=ada}RPae"};
//         addItemLeft("helladsasdasd", "Phuc", new ImageIcon(getClass().getResource("/icon/testing/IMG_20200127_151308.jpg")), new ImageIcon(getClass().getResource("/icon/testing/IMG_20200127_151308.jpg")));
//     addItemLeft("hello\nerererew\newewe", "Phuc", img);
//     addItemLeft("","Phuc", new ImageIcon(getClass().getResource("/icon/testing/285814c1978842b2c0a87065d882571d.jpg")));
//     addItemFile("","phuc", "Wordssssssssssssssssssss.txt","5 MB");
//     addItemRight("","myFile.rar","20 MB");
     
    }
    private void init(){
    body.setLayout(new MigLayout("fillx", "", "5[]5"));
    sp.setVerticalScrollBar(new ScrollBar());//khởi tạo thanh trượt scroll từ thư viện import swing
    sp.getVerticalScrollBar().setBackground(Color.WHITE);
    
    }
    public void addItemLeft(Model_Receive_Message data, File file, Model_User_Account user){
     if(data.getMessageType() == MessageType.TEXT){
       Chat_Left_With_Profile item = new  Chat_Left_With_Profile();
       setAvatar(item, user); 
       item.setText(data.getText());
       item.setTime();
       body.add(item, "wrap, w 100::80%");// thiết lập căn lề khung chat
     
     }else if(data.getMessageType() == MessageType.EMOJI){
       Chat_Left_With_Profile item = new  Chat_Left_With_Profile();
       setAvatar(item, user);
       item.setEmoji(Emoji.getInstance().getEmoji(Integer.valueOf(data.getText())).getIcon());
       item.setTime();
       body.add(item, "wrap, w 100::80%");
     }else if(data.getMessageType() == MessageType.IMAGE){
       Chat_Left_With_Profile item = new  Chat_Left_With_Profile();
       setAvatar(item, user);
       item.setText("");
       item.setImage(data.getDataImage(), file);
       item.setTime();
       body.add(item, "wrap, w 100::80%");
     }else if(data.getMessageType() == MessageType.FILE){
       Chat_Left_With_Profile item = new Chat_Left_With_Profile();
       setAvatar(item, user);
       item.setText("");
       item.setFile(data);
       item.setTime();
       body.add(item, "wrap, w 100::80%");
     }  
    repaint();
    revalidate();
    }
      public void addItemLeft(Model_Voice_Receive data, Model_User_Account user){
       Chat_Left_With_Profile item = new Chat_Left_With_Profile();
       setAvatar(item, user);
       item.setText("");
       item.setSound(data);
       item.setTime();
       body.add(item, "wrap, w 100::80%");
    repaint();
    revalidate();
    }
      private void setAvatar(Chat_Left_With_Profile item, Model_User_Account user){
      if (user.getImAvatar() != null) {
             item.setImageProfile(user.getImAvatar());
         }else{
             item.setImageProfile(new ImageIcon(getClass().getResource("/icon/profile2.png")));
         }
      
      }
//    public void addItemLeft(String text, String user, String[] image){
//    Chat_Left_With_Profile item = new  Chat_Left_With_Profile();
//    item.setText(text);
//    item.setImage(image);
//    item.setTime();
//    item.setUserProfile(user);
//    body.add(item, "wrap, w 100::80%");// thiết lập căn lề khung chat
//    body.repaint();
//    body.revalidate();
//    }
    public void addItemFile(String text, String user, String fileName, String fileSize){
    Chat_Left_With_Profile item = new  Chat_Left_With_Profile();
    item.setText(text);
    item.setFile(fileName, fileSize);
    item.setTime();
    item.setUserProfile(user);
    body.add(item, "wrap, w 100::80%");// thiết lập căn lề khung chat
    body.repaint();
    body.revalidate();
    }
       public void addItemRight(Model_Send_Message data){
    if(data.getMessageType() ==  MessageType.TEXT){
        Chat_Right item = new  Chat_Right();
        item.setText(data.getText());
        item.setTime();
        body.add(item, "wrap, al right, w 100::80%");// thiết lập căn lề khung chat

    }else if(data.getMessageType() == MessageType.EMOJI){
        Chat_Right item = new  Chat_Right();
        item.setEmoji(Emoji.getInstance().getEmoji(Integer.valueOf(data.getText())).getIcon());
        item.setTime();
        body.add(item, "wrap, al right, w 100::80%");// thiết lập căn lề khung chat
 
    }else if(data.getMessageType() == MessageType.IMAGE){
        Chat_Right item = new  Chat_Right();
        item.setText("");
        item.setImage(data.getFile());
        item.setTime();
        body.add(item, "wrap, al right, w 100::80%");// thiết lập căn lề khung chat
    }else if(data.getMessageType() == MessageType.FILE){
         Chat_Right item = new  Chat_Right();
         item.setText("");
         item.setFile(data.getFile());
         body.add(item, "wrap, al right, w 100::80%");// thiết lập căn lề khung chat
    
    }
    
    repaint();
    revalidate();
    scrollToBottom();
    }
      public void addItemRight(Model_Voice_Receive data){
  
         Chat_Right item = new  Chat_Right();
         item.setText("");
         item.setSound(data);
         body.add(item, "wrap, al right, w 100::80%");// thiết lập căn lề khung chat
    repaint();
    revalidate();
    scrollToBottom();
    }
       public void addItemFileRight(String text, String fileName, String fileSize){
    Chat_Right item = new  Chat_Right();
    item.setText(text);
//    item.setFile(fileName, fileSize);
    body.add(item, "wrap, al right, w 100::80%");// thiết lập căn lề khung chat
    body.repaint();
    body.revalidate();
    }   
    public void addDate(String date){
        Chat_Date item = new Chat_Date();
        item.setDate(date);
        body.add(item, "wrap, al center");
        body.repaint();
        body.revalidate();
    }
    public void clearChat(){
        body.removeAll();
        repaint();
        revalidate();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sp = new javax.swing.JScrollPane();
        body = new javax.swing.JPanel();

        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        body.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout bodyLayout = new javax.swing.GroupLayout(body);
        body.setLayout(bodyLayout);
        bodyLayout.setHorizontalGroup(
            bodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 507, Short.MAX_VALUE)
        );
        bodyLayout.setVerticalGroup(
            bodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 532, Short.MAX_VALUE)
        );

        sp.setViewportView(body);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sp, javax.swing.GroupLayout.DEFAULT_SIZE, 714, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sp)
        );
    }// </editor-fold>//GEN-END:initComponents

private void scrollToBottom() {
    JScrollBar verticalBar = sp.getVerticalScrollBar();
    AdjustmentListener downScroller = new AdjustmentListener() {
        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            Adjustable adjustable = e.getAdjustable();
            adjustable.setValue(adjustable.getMaximum());
            verticalBar.removeAdjustmentListener(this);
        }
    };
    verticalBar.addAdjustmentListener(downScroller);
}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel body;
    private javax.swing.JScrollPane sp;
    // End of variables declaration//GEN-END:variables
}
