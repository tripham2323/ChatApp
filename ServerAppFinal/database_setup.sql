-- Database setup for Friend Request System
-- Run this SQL script to create the required tables for the friend request functionality

USE chatappfinal;

-- Create friend_requests table to store pending friend requests
CREATE TABLE IF NOT EXISTS friend_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fromUserID INT NOT NULL,
    toUserID INT NOT NULL,
    status ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (fromUserID) REFERENCES user(UserID) ON DELETE CASCADE,
    FOREIGN KEY (toUserID) REFERENCES user(UserID) ON DELETE CASCADE,
    
    -- Ensure no duplicate requests
    UNIQUE KEY unique_request (fromUserID, toUserID)
);

-- Create friendships table to store confirmed friendships
CREATE TABLE IF NOT EXISTS friendships (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user1ID INT NOT NULL,
    user2ID INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user1ID) REFERENCES user(UserID) ON DELETE CASCADE,
    FOREIGN KEY (user2ID) REFERENCES user(UserID) ON DELETE CASCADE,
    
    -- Ensure no duplicate friendships and order consistency (smaller ID first)
    UNIQUE KEY unique_friendship (user1ID, user2ID),
    CHECK (user1ID < user2ID)
);

-- Create indexes for better performance
CREATE INDEX idx_friend_requests_from ON friend_requests(fromUserID);
CREATE INDEX idx_friend_requests_to ON friend_requests(toUserID);
CREATE INDEX idx_friend_requests_status ON friend_requests(status);
CREATE INDEX idx_friendships_user1 ON friendships(user1ID);
CREATE INDEX idx_friendships_user2 ON friendships(user2ID);

-- Example trigger to automatically create friendship when request is accepted
DELIMITER //
CREATE TRIGGER after_friend_request_accepted
    AFTER UPDATE ON friend_requests
    FOR EACH ROW
BEGIN
    IF NEW.status = 'accepted' AND OLD.status = 'pending' THEN
        INSERT IGNORE INTO friendships (user1ID, user2ID) 
        VALUES (
            LEAST(NEW.fromUserID, NEW.toUserID), 
            GREATEST(NEW.fromUserID, NEW.toUserID)
        );
    END IF;
END;//
DELIMITER ;
