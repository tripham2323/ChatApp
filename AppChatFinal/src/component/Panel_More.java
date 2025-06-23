/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package component;

import app.MessageType;
import emoji.Emoji;
import emoji.Model_Emoji;
import event.PublicEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import main.Main;
import model.Model_Send_Message;
import model.Model_User_Account;
import net.miginfocom.swing.MigLayout;
import service.Service;
import swing.FileChooser;
import swing.ScrollBar;
import swing.WrapLayout;

/**
 *
 * @author Admin
 */
public class Panel_More extends javax.swing.JPanel {
    
    public Model_User_Account getUser() {
        return user;
    }

    public void setUser(Model_User_Account user) {
        this.user = user;
    }
    
    private Model_User_Account user;
    
    public Panel_More() {
        initComponents();
        init();
    }
    private void init(){
        setLayout(new MigLayout("fillx"));
        panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.LINE_AXIS));
        panelHeader.add(getButtonFile());
        panelHeader.add(getButtonImage());
        panelHeader.add(getButtonVoice());
        panelHeader.add(getEmojiStyle1());
        panelHeader.add(getEmojiStyle2());
        panelHeader.add(getEmojiStyle3());
        panelHeader.add(getEmojiStyle4());
        
        panelHeader.setBackground(new Color(242,242,242));
        add(panelHeader, "w 100%, h 30!, wrap");
        panelDetail = new JPanel();
        panelDetail.setBackground(new Color(242,242,242));
        panelDetail.setLayout(new WrapLayout(WrapLayout.LEFT));// Sử dụng thư viện WrapLayout chia bố cục cho các component
        JScrollPane jscroll = new JScrollPane(panelDetail);
        jscroll.setBorder(null);
        jscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jscroll.setVerticalScrollBar(new ScrollBar());
 
        add(jscroll, "w 100%, h 100%");
       
    }
    
    private JButton getButtonImage(){
        OptionButton cmd = new OptionButton();
        cmd.setIcon(new ImageIcon(getClass().getResource("/icon/image.png")));
        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ea) {
                 JFileChooser ch = new JFileChooser();
                 FileChooser preview = new FileChooser();
                 ch.setAccessory(preview);
                 ch.addPropertyChangeListener(preview);
                 ch.setMultiSelectionEnabled(true);
                 ch.setFileFilter(new FileFilter(){
                     @Override
                     public boolean accept(File file) {
                            return file.isDirectory()||isImageFile(file);
                         }

                     @Override
                     public String getDescription() {
                            return "Image File";
                         }
                     
                 });
                 int option = ch.showOpenDialog(Main.getFrames()[0]);
                 if (option == JFileChooser.APPROVE_OPTION){
                        File files[] = ch.getSelectedFiles();
                        try {
                           for(File file:files){                               Model_Send_Message message = new Model_Send_Message(MessageType.IMAGE, service.Service.getInstance().getUser().getUserID(), user.getUserID(), "");
                               service.Service.getInstance().addFile(file, message);
                               PublicEvent.getInstance().getEventChat().sendMessage(message);
                          
                           }
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                   }
                     
                 }
        });
        return cmd;
        
    }
    private JButton getButtonFile(){
        OptionButton cmd = new OptionButton();
        cmd.setIcon(new ImageIcon(getClass().getResource("/icon/link.png")));
        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser ch = new JFileChooser();
                int option = ch.showOpenDialog(Main.getFrames()[0]);
                 if (option == JFileChooser.APPROVE_OPTION){
                     try {
                        File file = ch.getSelectedFile();
                        Model_Send_Message message = new Model_Send_Message(MessageType.FILE, Service.getInstance().getUser().getUserID(), user.getUserID(), "");
                        Service.getInstance().addFile(file, message);
                        PublicEvent.getInstance().getEventChat().sendMessage(message); 
                     } catch (IOException evt) {
                     }
                   }
                //update next
               }
        });
        return cmd;
    }
     private JButton getButtonVoice(){ //button của chức năng voice chat
         OptionButton cmd = new OptionButton();
         cmd.setIcon(new ImageIcon(getClass().getResource("/icon/mic.png")));
         cmd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
           
                   popMix popmix = new popMix();
                   popmix.setToUserID(user.getUserID());// id của user đích       
                   popmix.show(panelDetail, 170, -90);
                 System.out.println("tesst");
               }
         });
         
         
         return cmd;
    }
    
    private JButton getEmojiStyle1(){ //button của emoji style 1 
         OptionButton cmd = new OptionButton();
         cmd.setIcon((Emoji.getInstance().getEmoji(1).toSize(25, 25).getIcon()));
         cmd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 clearSelected();
                 cmd.setSelected(true);//set màu UI khi select 
                 panelDetail.removeAll();
                 for(Model_Emoji d:Emoji.getInstance().getStyle1()){
                     panelDetail.add(getButton(d));//duyệt list emoji vào panel detail
                 
                 }  //Khởi tạo list emoji style 1
                     
                 panelDetail.repaint();
                 panelDetail.revalidate();
                 
               }
         });
         
         
         return cmd;
    }
    private JButton getEmojiStyle2(){//button của emoji style 2 
         OptionButton cmd = new OptionButton();
         cmd.setIcon((Emoji.getInstance().getEmoji(90).toSize(30, 30).getIcon()));
         cmd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                  clearSelected();
                  cmd.setSelected(true);//set màu UI khi select 
                 panelDetail.removeAll();
                 for(Model_Emoji d:Emoji.getInstance().getStyle2()){
                     panelDetail.add(getButton(d));//duyệt list emoji vào panel detail
                 
                 }  //Khởi tạo list emoji style 2
                     
                 panelDetail.repaint();
                 panelDetail.revalidate();
                 
               }
         });
         
         
         return cmd;
    }
    private JButton getEmojiStyle3(){//button của emoji style 3 
         OptionButton cmd = new OptionButton();
         cmd.setIcon((Emoji.getInstance().getEmoji(114).toSize(30, 30).getIcon()));
         cmd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 clearSelected();
                  cmd.setSelected(true);//set màu UI khi select 
                 panelDetail.removeAll();
                 for(Model_Emoji d:Emoji.getInstance().getStyle3()){
                      panelDetail.add(getButton(d));//duyệt list emoji vào panel detail
                 
                 }  //Khởi tạo list emoji style 3
                     
                 panelDetail.repaint();
                 panelDetail.revalidate();
                 
               }
         });
         
         
         return cmd;
    }
    private JButton getEmojiStyle4(){//button của emoji style 4 
         OptionButton cmd = new OptionButton();
         cmd.setIcon((Emoji.getInstance().getEmoji(134).toSize(30, 30).getIcon()));
         cmd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                  clearSelected();
                  cmd.setSelected(true);//set màu UI khi select 
                          
                 panelDetail.removeAll();
                 for(Model_Emoji d:Emoji.getInstance().getStyle4()){
                     panelDetail.add(getButton(d));//duyệt list emoji vào panel detail
                 
                 }  //Khởi tạo list emoji style 4
                     
                 panelDetail.repaint();
                 panelDetail.revalidate();
                 
               }
         });
         
         
         return cmd;
    }
    
    private JButton getButton(Model_Emoji data){ //duyệt từng emoji để hiển thị vào từng component button 
                     JButton cmd = new JButton(data.getIcon());
                     cmd.setName(data.getId()+"");
                     cmd.setBorder(new EmptyBorder(3, 3, 3, 3));
                     cmd.setCursor(new Cursor(Cursor.HAND_CURSOR));
                     cmd.setContentAreaFilled(false);                     cmd.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent e) { //event khi nhấn vào emoji để gửi đí
                             Model_Send_Message message = new Model_Send_Message(MessageType.EMOJI, service.Service.getInstance().getUser().getUserID(), user.getUserID(), data.getId()+"");
                             sendMessage(message);
                             PublicEvent.getInstance().getEventChat().sendMessage(message);
                         }
                     });
                     return cmd;
    }    private void sendMessage(Model_Send_Message data){
        // Check if this is a group chat
        if (isGroupChat()) {
            System.out.println("=== Panel_More: SENDING GROUP EMOJI ===");
            System.out.println("Group ID: " + data.getToUserID());
            System.out.println("From User: " + data.getFromUserID());
            System.out.println("Emoji ID: " + data.getText());
            System.out.println("Message Type: " + data.getMessageType());
            
            // Send as Map instead of JSONObject to be consistent with chat_Bottom
            java.util.Map<String, Object> messageData = new java.util.HashMap<>();
            messageData.put("fromUserID", data.getFromUserID());
            messageData.put("toUserID", data.getToUserID()); // This is groupID
            messageData.put("messageType", data.getMessageType());
            messageData.put("text", data.getText());
            
            service.Service.getInstance().getClient().emit("send_to_group", messageData);
            System.out.println("Panel_More: Emitted send_to_group event successfully");
        } else {
            System.out.println("=== Panel_More: SENDING USER EMOJI ===");
            System.out.println("To User: " + data.getToUserID());
            System.out.println("From User: " + data.getFromUserID());
            System.out.println("Emoji ID: " + data.getText());
            System.out.println("Message Type: " + data.getMessageType());
            
            // Send as Map to be consistent with chat_Bottom
            java.util.Map<String, Object> messageData = new java.util.HashMap<>();
            messageData.put("fromUserID", data.getFromUserID());
            messageData.put("toUserID", data.getToUserID());
            messageData.put("messageType", data.getMessageType());
            messageData.put("text", data.getText());
            
            service.Service.getInstance().getClient().emit("send_to_user", messageData);
            System.out.println("Panel_More: Emitted send_to_user event successfully");
        }
    }
    
    private boolean isGroupChat() {
        // Check if the user object represents a group
        // A group chat is identified by having a special flag or by checking the user ID range
        // Groups typically have negative IDs or a specific range, or we can add a flag
        return user != null && user.isGroup();
    }
    private void clearSelected(){
        for(Component c:panelHeader.getComponents()){
             if(c instanceof OptionButton){
               ((OptionButton)c).setSelected(false);
             }
        
        }
    }
    private boolean isImageFile(File file){ //kiểm tra đuôi file là định dạng image hợp lệ
            String name = file.getName().toLowerCase();
            return name.endsWith(".jpg")||name.endsWith(".png")||name.endsWith(".jpeg")||name.endsWith(".gif");
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(242, 242, 242));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 528, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 145, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private JPanel panelHeader;
    private JPanel panelDetail;
  
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
