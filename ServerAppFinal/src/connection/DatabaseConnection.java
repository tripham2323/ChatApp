/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package connection;
import java.sql.SQLException;
import java.sql.Connection;

/**
 *
 * @author Admin
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    public static DatabaseConnection getInstance(){
             if(instance == null){
                     instance = new DatabaseConnection();
             }
        return instance;
    }
    private DatabaseConnection(){
    
    }
    public void connectToDatabase() throws SQLException{
           String server = "localhost";
           String port = "3306";
           String database = "chatappfinal";
           String userName = "root";
           String password = "";
           connection = java.sql.DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/" + database, userName, password);
    }
       /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @param connection the connection to set
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
