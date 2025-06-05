/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import com.corundumstudio.socketio.SocketIOClient;

/**
 *
 * @author Admin
 */
public class Model_Client {

    public Model_Client(SocketIOClient client, Model_User_Account user) {
        this.client = client;
        this.user = user;
    }
    public Model_Client(){
    
    }

    /**
     * @return the client
     */
    public SocketIOClient getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(SocketIOClient client) {
        this.client = client;
    }

    /**
     * @return the user
     */
    public Model_User_Account getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(Model_User_Account user) {
        this.user = user;
    }
    private SocketIOClient client;
    private Model_User_Account user;
    
    
}
