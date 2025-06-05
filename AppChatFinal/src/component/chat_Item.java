/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JLayeredPane;
import javax.swing.border.EmptyBorder;
import model.Model_File_Sender;
import model.Model_Receive_Image;
import model.Model_Receive_Message;
import model.Model_Voice_Receive;
import swing.PictureBox;

/**
 *
 * @author Admin
 */
public class chat_Item extends javax.swing.JLayeredPane {
    private JLabel label;

    /**
     * Creates new form chat_Item
     */
    public chat_Item() {
        initComponents();
        txt.setEditable(false);
        txt.setBackground(new Color(0, 0, 0, 0));
        txt.setOpaque(false);
    }
    public void setUserProfile(String user){
        JLayeredPane layer = new JLayeredPane();
        layer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        layer.setBorder(new EmptyBorder(10, 10, 0, 10));
        JButton cmd = new JButton(user);
        cmd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmd.setBorder(null);
        cmd.setContentAreaFilled(false);
        cmd.setFocusable(false);
        cmd.setForeground(new Color(108, 32, 139));
        cmd.setFont(new java.awt.Font("Tahoma", 1, 13)); 
        txt.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        layer.add(cmd);
        add(layer, 0);
    }
    public void setText(String text){
        txt.setText(text);
    }
    public void setTime(String time){
        JLayeredPane layer = new JLayeredPane();
        layer.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        layer.setBorder(new EmptyBorder(0, 5, 10, 5));
        label = new JLabel(time);
        label.setForeground(new Color(110, 110, 110));
        label.setHorizontalTextPosition(JLabel.LEFT);
        layer.add(label);
        add(layer);
    
    }
    public void setImage(boolean right, Model_File_Sender fileSender){
    
        JLayeredPane layer = new JLayeredPane();
        layer.setLayout(new FlowLayout(right?FlowLayout.RIGHT:FlowLayout.LEFT));
        layer.setBorder(new EmptyBorder(0, 5, 0, 5));
        Chat_Image chatImage = new Chat_Image(right);
        chatImage.addImage(fileSender);
        layer.add(chatImage);
        add(layer);
       
    }
     public void setImage(boolean right, Model_Receive_Image dataImage, File file){
        JLayeredPane layer = new JLayeredPane();
        layer.setLayout(new FlowLayout(right?FlowLayout.RIGHT:FlowLayout.LEFT));
        layer.setBorder(new EmptyBorder(0, 5, 0, 5));
        Chat_Image chatImage = new Chat_Image(right);
        chatImage.addImage(dataImage, file);
        layer.add(chatImage);
        add(layer);
        
        
    }
     
     public void setFile(boolean right, Model_Receive_Message data){
        JLayeredPane layer = new JLayeredPane();
        layer.setLayout(new FlowLayout(right?FlowLayout.RIGHT:FlowLayout.LEFT));
        layer.setBorder(new EmptyBorder(0, 5, 0, 5));
        Chat_File chatFile = new Chat_File();
        chatFile.setFile(data);
        layer.add(chatFile);
        add(layer);
        
        
    }
     public void setFile(boolean right, Model_File_Sender data){
        JLayeredPane layer = new JLayeredPane();
        layer.setLayout(new FlowLayout(right?FlowLayout.RIGHT:FlowLayout.LEFT));
        layer.setBorder(new EmptyBorder(0, 5, 0, 5));
        Chat_File chatFile = new Chat_File();
        chatFile.setFile(data);
        layer.add(chatFile);
        add(layer);
        
        
    }
     public void setSound(boolean right, Model_Voice_Receive data){
        JLayeredPane layer = new JLayeredPane();
        layer.setLayout(new FlowLayout(right?FlowLayout.RIGHT:FlowLayout.LEFT));
        layer.setBorder(new EmptyBorder(0, 5, 0, 5));
        Chat_Sound chatSound = new Chat_Sound();
        if(right == true){
          chatSound.setBackground(new Color(211, 181, 255));
        }
        chatSound.set(data.getData(), data.getTime());
        layer.add(chatSound);
        add(layer);
     }
     public void setEmoji(boolean right, Icon icon){
        PictureBox pic = new PictureBox();
        pic.setLayout(new FlowLayout(right?FlowLayout.RIGHT:FlowLayout.LEFT));
        pic.setBorder(new EmptyBorder(0, 5, 0, 5));
        pic.setPreferredSize(getAutoSize(icon, 220, 220)); //set kích thước của image
        pic.setImage(icon);
        add(pic);
        setBackground(null);
     
     }
    public void sendSuccess(){
        if(label!=null){
           label.setIcon(new ImageIcon(getClass().getResource("/icon/tick.png")));
        }
    
    }
    public void seen(){
        if(label!=null){
           label.setIcon(new ImageIcon(getClass().getResource("/icon/double_tick.png")));
        }
    
    }
    public void hideText(){
       txt.setVisible(false);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txt = new swing.JIMSendTextPane();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        txt.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 5, 10));
        txt.setSelectionColor(new java.awt.Color(156, 115, 255));
        add(txt);
    }// </editor-fold>//GEN-END:initComponents
    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        if(getBackground() != null){
           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
           g2.setColor(getBackground());
           g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        }
        super.paintComponent(grphcs);
    }
    
    //hàm căn chỉnh image
     private Dimension getAutoSize(Icon image, int w, int h) {
       if (w > image.getIconWidth()) {
            w = image.getIconWidth();
        }
        if (h > image.getIconHeight()) {
            h = image.getIconHeight();
        }
        int iw = image.getIconWidth();
        int ih = image.getIconHeight();
        double xScale = (double) w / iw;
        double yScale = (double) h / ih;
        double scale = Math.min(xScale, yScale);
        int width = (int) (scale * iw);
        int height = (int) (scale * ih);
        return new Dimension(width, height);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private swing.JIMSendTextPane txt;
    // End of variables declaration//GEN-END:variables
}
