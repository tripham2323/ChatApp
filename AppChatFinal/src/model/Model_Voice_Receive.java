/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import app.MessageType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
public class Model_Voice_Receive {



    public Model_Voice_Receive(int toUserID, byte[] data, String time) {
        this.toUserID = toUserID;
        this.data = data;
        this.time = time;
    }
    public Model_Voice_Receive(Object json){
           JSONObject obj = (JSONObject) json;
        try {
            toUserID = obj.getInt("toUserID");
            fromUserID = obj.getInt("fromUserID");
            data = (byte[]) obj.get("data");
            time = obj.getString("time");
        } catch (JSONException e) {
            System.err.println(e);
        }
    
    }
    /**
     * @return the toUserID
     */
    public int getToUserID() {
        return toUserID;
    }

    /**
     * @param toUserID the toUserID to set
     */
    public void setToUserID(int toUserID) {
        this.toUserID = toUserID;
    }
   /**
     * @return the fromUserID
     */
    public int getFromUserID() {
        return fromUserID;
    }

    /**
     * @param fromUserID the fromUserID to set
     */
    public void setFromUserID(int fromUserID) {
        this.fromUserID = fromUserID;
    }
    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }
    private int toUserID;
    private int fromUserID;
    private byte[] data;
    private String time;
}
