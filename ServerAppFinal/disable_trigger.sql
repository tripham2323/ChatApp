-- Disable the trigger that automatically creates friendships
-- Run this if you're getting duplicate entry errors

USE chatappfinal;

-- Drop the trigger if it exists
DROP TRIGGER IF EXISTS after_friend_request_accepted;

-- Verify trigger is dropped
SHOW TRIGGERS LIKE 'friend_requests';
