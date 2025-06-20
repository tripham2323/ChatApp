-- Quick script to add friendship between users
-- Modify the user IDs as needed based on your existing users

USE chatappfinal;

-- First, let's see what users exist
SELECT UserID, UserName FROM user_account WHERE Status = '1' ORDER BY UserID;

-- Add friendships between the first few users
-- Replace with actual user IDs from your database
INSERT IGNORE INTO friendships (user1ID, user2ID) VALUES 
(10, 11);  -- Assuming user 10 (phuc) and user 11 exist

-- Verify the friendship was created
SELECT f.*, 
       u1.UserName as user1_name, 
       u2.UserName as user2_name
FROM friendships f
JOIN user_account u1 ON f.user1ID = u1.UserID
JOIN user_account u2 ON f.user2ID = u2.UserID;
