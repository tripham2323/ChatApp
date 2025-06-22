/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package event;

import java.io.File;
import model.Model_Receive_Message;
import model.Model_Send_Message;
import model.Model_Voice_Receive;
import model.Model_User_Account;

/**
 *
 * @author Admin
 */
public interface EventChat {
    public void sendMessage(Model_Send_Message data);
    public void sendMessage(Model_Voice_Receive data);
    public void receiveMessage(Model_Receive_Message data, File file);
    public void receiveMessage(Model_Voice_Receive data);
    public void receiveGroupMessage(Model_Receive_Message data, Model_User_Account senderInfo, int groupID);
    

    
}
