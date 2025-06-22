/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.sql.PreparedStatement;
import connection.DatabaseConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Model_Client;
import model.Model_Login;
import model.Model_Message;
import model.Model_Register;
import model.Model_User_Account;
import model.Model_Friend_Request;
/**
 *
 * @author Admin
 */
public class ServiceUser {
    private final String PATH_FILE = "server_data/profile.png";
  //SQL truy vấn
    private final String LOGIN = "select UserID, user_account.UserName, Gender, Image from `user` join user_account using (UserID) where `user`.UserName=BINARY(?) and `user`.`Password`=BINARY(?) and user_account.`Status`='1'";
    private final String INSERT_USER = "insert into user (UserName, Password) values (?,?)";
    private final String INSERT_USER_ACCOUNT = "insert into user_account (UserID, UserName, Image) values (?,?,?)";
    private final String CHECK_USER = "select UserID from user where UserName =? limit 1";    // lấy info các user còn lại trừ userID này ra
    private final String SELECT_USER_ACCOUNT = "select UserID, UserName, Gender, Image from user_account where user_account.`Status`='1' and UserID<>?";
    // Search users by username
    private final String SEARCH_USERS = "select UserID, UserName, Gender, Image from user_account where user_account.`Status`='1' and UserName LIKE ? LIMIT 10";
    // Friend request queries
    private final String INSERT_FRIEND_REQUEST = "insert into friend_requests (fromUserID, toUserID, status, created_at) values (?,?,'pending',NOW())";    private final String CHECK_EXISTING_REQUEST = "select id from friend_requests where fromUserID=? and toUserID=? and status='pending'";
    private final String CHECK_FRIENDSHIP_EXISTS = "select id from friendships where (user1ID=? and user2ID=?) or (user1ID=? and user2ID=?)";
    private final String UPDATE_FRIEND_REQUEST_STATUS = "update friend_requests set status=? where id=?";    private final String GET_PENDING_FRIEND_REQUESTS = "select fr.id, fr.fromUserID, ua.UserName, ua.Gender, ua.Image, fr.created_at from friend_requests fr join user_account ua on fr.fromUserID = ua.UserID where fr.toUserID=? and fr.status='pending' order by fr.created_at desc";
    // Get friends list query
    private final String GET_FRIENDS_LIST = "select ua.UserID, ua.UserName, ua.Gender, ua.Image from friendships f join user_account ua on (f.user1ID = ua.UserID or f.user2ID = ua.UserID) where (f.user1ID = ? or f.user2ID = ?) and ua.UserID != ? and ua.Status = '1' order by ua.UserName";
    //khởi tạo connect sql
    private final Connection con;
    public ServiceUser() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }
    public Model_Message register(Model_Register data) throws IOException{
    //kiểm tra user tồn tại
        Model_Message message = new Model_Message();
        try {
            
        File f = new File(PATH_FILE); 
         byte[] dataAvatar = Files.readAllBytes(f.toPath());
        PreparedStatement p = con.prepareStatement(CHECK_USER);
        p.setString(1, data.getUserName());
        ResultSet r = p.executeQuery();
        if(r.first()){  
             message.setAction(false);
             message.setMessage("Người dùng đã tồn tạiiiii");   
        }else{
             message.setAction(true);
        }
        r.close();
        p.close();
        if(message.isAction()){
         //insert user đang ký
         con.setAutoCommit(false);
         p = con.prepareStatement(INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS);
         p.setString(1, data.getUserName());
         p.setString(2, data.getPassword());
         p.execute();
         r = p.getGeneratedKeys();
         r.first();
         int userID = r.getInt(1);
         r.close();
         p.close();
         //create avatar image for new user register
       
         FileInputStream fis = new FileInputStream(f.getAbsolutePath());
         //tao user account
         p = con.prepareStatement(INSERT_USER_ACCOUNT);
         p.setInt(1, userID);
         p.setString(2, data.getUserName());
         p.setBinaryStream(3, fis);
         p.execute();
         p.close();
         con.commit(); //xác nhận commit thay đổi 
         con.setAutoCommit(true);          
             message.setAction(true);
             message.setMessage("Đăng ký tài khoản thành công !"    );
             message.setData(new Model_User_Account(userID, data.getUserName(), "", dataAvatar, true));//set list thông tin user để gửi về client hiển thị
       
       }
        } catch (SQLException e) {
            message.setAction(false);
            message.setMessage("Server error");
            try {
                if(con.getAutoCommit() == false){
                    con.rollback();
                    con.setAutoCommit(true);
                
                }
            } catch (Exception e1) {
            }
            
        }

       
       return message;
    
    }
    
    public Model_User_Account login(Model_Login login)throws SQLException, IOException{
    Model_User_Account data = null;
    PreparedStatement p = con.prepareStatement(LOGIN);
    p.setString(1, login.getUserName());
    p.setString(2, login.getPassword());
    ResultSet r = p.executeQuery();
    if(r.first()){
        int userID = r.getInt(1);
        String userName = r.getString(2);
        String gender = r.getString(3);
        InputStream input = r.getBinaryStream(4);
        byte[] imageAvatar = new byte[input.available()];
        imageAvatar = input.readAllBytes();
        data = new Model_User_Account(userID, userName, gender, imageAvatar, true);
        input.close();
    }
    r.close();
    p.close();
    return data;
    
    }
    
    public List<Model_User_Account> getUser(int exitUser) throws SQLException, IOException {
              List<Model_User_Account> list = new ArrayList<>();
              PreparedStatement p = con.prepareStatement(SELECT_USER_ACCOUNT);
              p.setInt(1, exitUser);
              ResultSet r = p.executeQuery();
              while(r.next()){
                  int userID = r.getInt(1);
                  String userName = r.getString(2);
                  String gender = r.getString(3);
                  InputStream input = r.getBinaryStream(4);
                  byte[] imageAvatar = new byte[input.available()];
                  imageAvatar = input.readAllBytes();
                  list.add(new Model_User_Account(userID, userName, gender, imageAvatar, checkUserStatus(userID)));
                  input.close();
              }
              r.close();
              p.close();
              
              return list;
    }   private boolean checkUserStatus(int userID){
       List<Model_Client> clients = Service.getInstance(null).getListClient();
       for(Model_Client c:clients ){
             if (c.getUser().getUserID() == userID) {
               return true;
           }
       }
       return false;
   }
   
   // Search users by username
   public List<Model_User_Account> searchUsersByName(String searchText) throws SQLException, IOException {
    System.out.println("Server đang tìm kiếm người dùng với từ khóa: " + searchText);
    List<Model_User_Account> list = new ArrayList<>();
    PreparedStatement p = null;
    ResultSet r = null;
    try {
        p = con.prepareStatement(SEARCH_USERS);
        p.setString(1, "%" + searchText + "%");
        r = p.executeQuery();
        System.out.println("Đã thực thi truy vấn tìm kiếm");

        while(r.next()) {
            int userID = r.getInt(1);
            String userName = r.getString(2);
            String gender = r.getString(3);
            InputStream input = r.getBinaryStream(4);
            
            if (input != null) {
                byte[] imageAvatar = new byte[input.available()];
                imageAvatar = input.readAllBytes();
                list.add(new Model_User_Account(userID, userName, gender, imageAvatar, checkUserStatus(userID)));
                input.close();
            } else {
                // Nếu không có avatar, tạo với mảng byte rỗng
                list.add(new Model_User_Account(userID, userName, gender, new byte[0], checkUserStatus(userID)));
            }
        }
        System.out.println("Tìm thấy " + list.size() + " người dùng");
    } catch (SQLException e) {
        System.err.println("Lỗi SQL khi tìm kiếm người dùng: " + e.getMessage());
        throw e;
    } catch (IOException e) {
        System.err.println("Lỗi IO khi đọc avatar: " + e.getMessage());
        throw e;
    } finally {
        if (r != null) {
            try {
                r.close();
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng ResultSet: " + e.getMessage());
            }
        }
        if (p != null) {
            try {
                p.close();
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng PreparedStatement: " + e.getMessage());
            }
        }
    }
    return list;
}

// Send friend request method
public boolean sendFriendRequest(int fromUserID, int toUserID) throws SQLException {
    System.out.println("Đang gửi lời mời kết bạn từ UserID " + fromUserID + " đến UserID " + toUserID);
    
    // Kiểm tra xem đã có yêu cầu kết bạn pending chưa
    PreparedStatement checkRequest = null;
    ResultSet rs = null;
    try {
        checkRequest = con.prepareStatement(CHECK_EXISTING_REQUEST);
        checkRequest.setInt(1, fromUserID);
        checkRequest.setInt(2, toUserID);
        rs = checkRequest.executeQuery();
        
        if (rs.next()) {
            System.out.println("Đã có yêu cầu kết bạn pending giữa hai người dùng này");
            return false; // Đã có yêu cầu pending
        }
    } finally {
        if (rs != null) rs.close();
        if (checkRequest != null) checkRequest.close();
    }
      // Kiểm tra xem đã là bạn bè chưa
    PreparedStatement checkFriendship = null;
    try {
        checkFriendship = con.prepareStatement(CHECK_FRIENDSHIP_EXISTS);
        checkFriendship.setInt(1, Math.min(fromUserID, toUserID));
        checkFriendship.setInt(2, Math.max(fromUserID, toUserID));
        checkFriendship.setInt(3, Math.min(fromUserID, toUserID));
        checkFriendship.setInt(4, Math.max(fromUserID, toUserID));
        rs = checkFriendship.executeQuery();
        
        if (rs.next()) {
            System.out.println("Hai người dùng này đã là bạn bè");
            return false; // Đã là bạn bè
        }
    } finally {
        if (rs != null) rs.close();
        if (checkFriendship != null) checkFriendship.close();
    }
    
    // Tạo yêu cầu kết bạn mới
    PreparedStatement insertRequest = null;
    try {
        insertRequest = con.prepareStatement(INSERT_FRIEND_REQUEST);
        insertRequest.setInt(1, fromUserID);
        insertRequest.setInt(2, toUserID);
        
        int rowsAffected = insertRequest.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Đã tạo yêu cầu kết bạn thành công");
            return true;
        } else {
            System.out.println("Không thể tạo yêu cầu kết bạn");
            return false;
        }
    } finally {
        if (insertRequest != null) insertRequest.close();
    }
}

// Accept friend request method - let database trigger handle friendship creation
public boolean acceptFriendRequest(int requestID) throws SQLException {
    PreparedStatement updateRequest = null;
    
    try {
        System.out.println("=== ACCEPTING FRIEND REQUEST ===");
        System.out.println("Request ID: " + requestID);
        
        // Simply update the request status - database trigger will handle friendship creation
        updateRequest = con.prepareStatement(UPDATE_FRIEND_REQUEST_STATUS);
        updateRequest.setString(1, "accepted");
        updateRequest.setInt(2, requestID);
        
        System.out.println("Executing SQL: " + UPDATE_FRIEND_REQUEST_STATUS);
        System.out.println("Parameters: status='accepted', requestID=" + requestID);
        
        int rowsUpdated = updateRequest.executeUpdate();
        System.out.println("Rows updated: " + rowsUpdated);
        
        if (rowsUpdated > 0) {
            System.out.println("Friend request accepted successfully. Request ID: " + requestID);
            System.out.println("Database trigger will automatically create the friendship entry.");
            return true;
        } else {
            System.out.println("ERROR: No friend request found to update with ID: " + requestID);
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("SQLException in acceptFriendRequest:");
        System.err.println("Error code: " + e.getErrorCode());
        System.err.println("SQL state: " + e.getSQLState());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
        return false;
    } catch (Exception e) {
        System.err.println("General Exception in acceptFriendRequest:");
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
        return false;
    } finally {
        try {
            if (updateRequest != null) updateRequest.close();
        } catch (SQLException e) {
            System.err.println("Error closing PreparedStatement: " + e.getMessage());
        }
    }
}

// Reject friend request method
public boolean rejectFriendRequest(int requestID) throws SQLException {
    PreparedStatement updateRequest = null;
    
    try {
        updateRequest = con.prepareStatement(UPDATE_FRIEND_REQUEST_STATUS);
        updateRequest.setString(1, "rejected");
        updateRequest.setInt(2, requestID);
        
        int rowsAffected = updateRequest.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Friend request rejected successfully. Request ID: " + requestID);
            return true;
        } else {
            System.out.println("Friend request not found with ID: " + requestID);
            return false;
        }
    } finally {
        if (updateRequest != null) updateRequest.close();
    }
}    // Get pending friend requests for a user
    public List<Model_Friend_Request> getPendingFriendRequests(int userID) throws SQLException, IOException {
        System.out.println("Getting pending friend requests for user ID: " + userID);
        List<Model_Friend_Request> requests = new ArrayList<>();
        PreparedStatement p = null;
        ResultSet r = null;
        
        try {
            p = con.prepareStatement(GET_PENDING_FRIEND_REQUESTS);
            p.setInt(1, userID);
            r = p.executeQuery();
            
            while (r.next()) {
                int requestId = r.getInt("id");
                int fromUserID = r.getInt("fromUserID");
                String userName = r.getString("UserName");
                String gender = r.getString("Gender");
                java.sql.Timestamp createdAt = r.getTimestamp("created_at");
                
                Model_User_Account senderInfo = new Model_User_Account();
                senderInfo.setUserID(fromUserID);
                senderInfo.setUserName(userName);
                senderInfo.setGender(gender);
                
                // Handle image
                InputStream input = r.getBinaryStream("Image");
                if (input != null) {
                    byte[] imageAvatar = new byte[input.available()];
                    imageAvatar = input.readAllBytes();
                    senderInfo.setImage(imageAvatar);
                    input.close();
                } else {
                    senderInfo.setImage(new byte[0]);
                }
                
                senderInfo.setStatus(checkUserStatus(fromUserID));
                
                Model_Friend_Request friendRequest = new Model_Friend_Request(requestId, fromUserID, userID, "pending", createdAt);
                friendRequest.setSenderInfo(senderInfo);
                requests.add(friendRequest);
            }
            
            System.out.println("Found " + requests.size() + " pending friend requests");
        } finally {
            if (r != null) r.close();
            if (p != null) p.close();
        }
        
        return requests;
    }

    // Get user by ID method for notifications
    private final String SELECT_USER_BY_ID = "select UserID, UserName, Gender, Image from user_account where UserID=? and Status='1'";
    
    public Model_User_Account getUserByID(int userID) throws SQLException, IOException {
        PreparedStatement p = null;
        ResultSet r = null;
        try {
            p = con.prepareStatement(SELECT_USER_BY_ID);
            p.setInt(1, userID);
            r = p.executeQuery();
            
            if (r.next()) {
                Model_User_Account user = new Model_User_Account();
                user.setUserID(r.getInt("UserID"));
                user.setUserName(r.getString("UserName"));
                user.setGender(r.getString("Gender"));
                
                // Handle image as byte array
                InputStream input = r.getBinaryStream("Image");
                if (input != null) {
                    byte[] imageAvatar = new byte[input.available()];
                    imageAvatar = input.readAllBytes();
                    user.setImage(imageAvatar);
                    input.close();
                }
                
                user.setStatus(true); // Set default status
                return user;
            }
        } finally {
            if (r != null) r.close();
            if (p != null) p.close();
        }
        return null;
    }    // Get friends list for a user
    public List<Model_User_Account> getFriendsList(int userID) throws SQLException, IOException {
        System.out.println("=== SERVER: Getting friends list for user ID: " + userID + " ===");
        List<Model_User_Account> friends = new ArrayList<>();
        PreparedStatement p = null;
        ResultSet r = null;
        
        try {
            System.out.println("SQL Query: " + GET_FRIENDS_LIST);
            p = con.prepareStatement(GET_FRIENDS_LIST);
            p.setInt(1, userID);  // f.user1ID = ?
            p.setInt(2, userID);  // f.user2ID = ?
            p.setInt(3, userID);  // ua.UserID != ?
            System.out.println("Parameters: userID=" + userID + ", userID=" + userID + ", userID=" + userID);
            
            r = p.executeQuery();
            
            int rowCount = 0;
            while (r.next()) {
                rowCount++;
                int friendUserID = r.getInt("UserID");
                String userName = r.getString("UserName");
                String gender = r.getString("Gender");
                
                System.out.println("Row " + rowCount + ": Found friend UserID=" + friendUserID + ", UserName=" + userName + ", Gender=" + gender);
                
                Model_User_Account friend = new Model_User_Account();
                friend.setUserID(friendUserID);
                friend.setUserName(userName);
                friend.setGender(gender);
                
                // Handle image
                InputStream input = r.getBinaryStream("Image");
                if (input != null) {
                    byte[] imageAvatar = new byte[input.available()];
                    imageAvatar = input.readAllBytes();
                    friend.setImage(imageAvatar);
                    input.close();
                    System.out.println("Image loaded: " + imageAvatar.length + " bytes");
                } else {
                    friend.setImage(new byte[0]);
                    System.out.println("No image found for user");
                }
                
                friend.setStatus(checkUserStatus(friendUserID));
                System.out.println("Friend status: " + friend.getStatus());
                friends.add(friend);
            }
            
            System.out.println("=== SERVER: Total friends found: " + friends.size() + " ===");
            if (rowCount == 0) {
                System.out.println("=== SERVER: No rows returned from query ===");
                // Let's debug the query step by step
                System.out.println("=== DEBUG: Checking friendships table ===");
                PreparedStatement debugP = con.prepareStatement("SELECT * FROM friendships WHERE user1ID = ? OR user2ID = ?");
                debugP.setInt(1, userID);
                debugP.setInt(2, userID);
                ResultSet debugR = debugP.executeQuery();
                System.out.println("Friendships for user " + userID + ":");
                while (debugR.next()) {
                    System.out.println("  Friendship: user1ID=" + debugR.getInt("user1ID") + ", user2ID=" + debugR.getInt("user2ID"));
                }
                debugR.close();
                debugP.close();
                
                System.out.println("=== DEBUG: Checking user_account table ===");
                PreparedStatement debugP2 = con.prepareStatement("SELECT UserID, UserName, Status FROM user_account WHERE UserID IN (10, 11)");
                ResultSet debugR2 = debugP2.executeQuery();
                System.out.println("User accounts:");
                while (debugR2.next()) {
                    System.out.println("  User: UserID=" + debugR2.getInt("UserID") + ", UserName=" + debugR2.getString("UserName") + ", Status=" + debugR2.getString("Status"));
                }
                debugR2.close();
                debugP2.close();
            }
            
        } finally {
            if (r != null) r.close();
            if (p != null) p.close();
        }
        
        return friends;
    }
    
    // Search users by username (excluding friends and pending requests)
    public List<Model_User_Account> searchUsersNotFriends(String searchText, int currentUserID) throws SQLException, IOException {
        System.out.println("Server đang tìm kiếm người chưa kết bạn với từ khóa: " + searchText + " cho UserID: " + currentUserID);
        List<Model_User_Account> list = new ArrayList<>();
        PreparedStatement p = null;
        ResultSet r = null;
        
        // SQL query để tìm người dùng không phải bạn bè và không có yêu cầu kết bạn đang chờ
        String SEARCH_NON_FRIENDS = "SELECT ua.UserID, ua.UserName, ua.Gender, ua.Image " +
                                   "FROM user_account ua " +
                                   "WHERE ua.Status = '1' " +
                                   "AND ua.UserID != ? " +
                                   "AND ua.UserName LIKE ? " +
                                   "AND ua.UserID NOT IN (" +
                                   "  SELECT CASE WHEN f.user1ID = ? THEN f.user2ID ELSE f.user1ID END " +
                                   "  FROM friendships f " +
                                   "  WHERE f.user1ID = ? OR f.user2ID = ?" +
                                   ") " +
                                   "AND ua.UserID NOT IN (" +
                                   "  SELECT fr.toUserID FROM friend_requests fr " +
                                   "  WHERE fr.fromUserID = ? AND fr.status = 'pending'" +
                                   ") " +
                                   "AND ua.UserID NOT IN (" +
                                   "  SELECT fr.fromUserID FROM friend_requests fr " +
                                   "  WHERE fr.toUserID = ? AND fr.status = 'pending'" +
                                   ") " +
                                   "LIMIT 10";
        
        try {
            p = con.prepareStatement(SEARCH_NON_FRIENDS);
            p.setInt(1, currentUserID);  // ua.UserID != ?
            p.setString(2, "%" + searchText + "%");  // ua.UserName LIKE ?
            p.setInt(3, currentUserID);  // CASE WHEN f.user1ID = ?
            p.setInt(4, currentUserID);  // WHERE f.user1ID = ?
            p.setInt(5, currentUserID);  // OR f.user2ID = ?
            p.setInt(6, currentUserID);  // WHERE fr.fromUserID = ?
            p.setInt(7, currentUserID);  // WHERE fr.toUserID = ?
            
            r = p.executeQuery();
            System.out.println("Đã thực thi truy vấn tìm kiếm người chưa kết bạn");

            while(r.next()) {
                int userID = r.getInt(1);
                String userName = r.getString(2);
                String gender = r.getString(3);
                InputStream input = r.getBinaryStream(4);
                
                if (input != null) {
                    byte[] imageAvatar = new byte[input.available()];
                    imageAvatar = input.readAllBytes();
                    list.add(new Model_User_Account(userID, userName, gender, imageAvatar, checkUserStatus(userID)));
                    input.close();
                } else {
                    // Nếu không có avatar, tạo với mảng byte rỗng
                    list.add(new Model_User_Account(userID, userName, gender, new byte[0], checkUserStatus(userID)));
                }
            }
            System.out.println("Tìm thấy " + list.size() + " người chưa kết bạn");
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tìm kiếm người chưa kết bạn: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            System.err.println("Lỗi IO khi đọc avatar: " + e.getMessage());
            throw e;
        } finally {
            if (r != null) r.close();
            if (p != null) p.close();
        }
        
        return list;
    }
}
