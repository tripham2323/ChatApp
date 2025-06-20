-- Test friendship data
-- This script will add some test friendships to test the friends list functionality

USE chatappfinal;

-- Check existing users
SELECT UserID, UserName FROM user_account WHERE Status = '1' LIMIT 10;

-- Insert some test friendships (assuming UserIDs 1-5 exist)
-- Note: friendships table uses LEAST/GREATEST to ensure user1ID < user2ID
INSERT IGNORE INTO friendships (user1ID, user2ID) VALUES 
(1, 2),  -- User 1 and User 2 are friends
(1, 3),  -- User 1 and User 3 are friends  
(2, 3),  -- User 2 and User 3 are friends
(1, 4),  -- User 1 and User 4 are friends
(2, 4),  -- User 2 and User 4 are friends
(3, 5);  -- User 3 and User 5 are friends

-- Check the friendships created
SELECT * FROM friendships;

-- Test query to get friends for user 1
SELECT ua.UserID, ua.UserName, ua.Gender 
FROM friendships f 
JOIN user_account ua ON (f.user1ID = ua.UserID OR f.user2ID = ua.UserID) 
WHERE (f.user1ID = 1 OR f.user2ID = 1) 
AND ua.UserID != 1 
AND ua.Status = '1' 
ORDER BY ua.UserName;
