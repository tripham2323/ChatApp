/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package event;

import model.Model_Login;
import model.Model_Message;
import model.Model_Register;

/**
 *
 * @author Admin
 */
public interface EventLogin {
    public void Login(Model_Login data);
    public void Register(Model_Register data, EventMessage message);
    public void goRegister();
    public void goLogin();
            
}
