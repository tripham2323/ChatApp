/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import javax.swing.ImageIcon;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
public class Model_User_Account {

    private int UserID;
    private String userName;
    private String gender;
    private byte[] image;
    private Boolean status;
    private ImageIcon imAvatar;
    private boolean isGroup = false; // Flag to indicate if this represents a group
    
      public Model_User_Account(int UserID, String userName, String gender, byte[] image, Boolean status) {
        this.UserID = UserID;
        this.userName = userName;
        this.gender = gender;
        this.image = image;
        this.status = status;
    }
    
    // Default constructor
    public Model_User_Account() {
        // Initialize with default values
        this.UserID = 0;
        this.userName = "";
        this.gender = "";
        this.image = null;
        this.status = false;
    }
    
     public Model_User_Account(Object json){
            JSONObject obj = (JSONObject) json;
        try {
            UserID = obj.getInt("userID");
            userName = obj.getString("userName");
            gender = obj.getString("gender");
            if(!obj.isNull("image")){
                image = (byte[]) obj.get("image");
            }
            status = obj.getBoolean("status");
            
        } catch (JSONException e) {
            System.err.println(e);
        }
    }
    /**
     * @return the UserID
     */
    public int getUserID() {
        return UserID;
    }

    /**
     * @param UserID the UserID to set
     */
    public void setUserID(int UserID) {
        this.UserID = UserID;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return the image
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * @return the status
     */
    public Boolean getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Boolean status) {
        this.status = status;
    }
    /**
     * @return the imAvatar
     */
    public ImageIcon getImAvatar() {
        return imAvatar;
    }

    /**
     * @param imAvatar the imAvatar to set
     */
    public void setImAvatar(ImageIcon imAvatar) {
        this.imAvatar = imAvatar;
    }
      public boolean isGroup() {
        return isGroup;
    }
    
    public void setGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }
    
}
