/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package event;

import model.Model_User_Account;

/**
 *
 * @author Admin
 */
public interface EventMain {
    public void showLoading(boolean show);
    public void initChat();
    public void selectUser(Model_User_Account user);
    public void updateUser(Model_User_Account user);
    public void updateGroupAvatar(int groupID, byte[] newAvatar);
    public void refreshGroupsList();
}
