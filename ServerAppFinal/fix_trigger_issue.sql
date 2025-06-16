-- Check and remove trigger that's causing the duplicate entry issue
USE chatappfinal;

-- Show all triggers on friend_requests table
SHOW TRIGGERS WHERE `Table` = 'friend_requests';

-- Drop the trigger if it exists
DROP TRIGGER IF EXISTS after_friend_request_accepted;

-- Verify trigger is removed
SHOW TRIGGERS WHERE `Table` = 'friend_requests';

-- Show current status
SELECT 'Trigger removed successfully' as Status;
