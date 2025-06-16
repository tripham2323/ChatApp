-- Check if user 'phuc' has image data in database
USE chatappfinal;

-- Check user_account table for user with ID 10 (phuc)
SELECT UserID, UserName, Gender, 
       CASE 
           WHEN Image IS NULL THEN 'NULL'
           WHEN LENGTH(Image) = 0 THEN 'EMPTY'
           ELSE CONCAT('HAS_DATA (', LENGTH(Image), ' bytes)')
       END as ImageStatus
FROM user_account 
WHERE UserID = 10 OR UserName = 'phuc';

-- Also check if there are any users with image data
SELECT UserID, UserName, 
       CASE 
           WHEN Image IS NULL THEN 'NULL'
           WHEN LENGTH(Image) = 0 THEN 'EMPTY'
           ELSE CONCAT('HAS_DATA (', LENGTH(Image), ' bytes)')
       END as ImageStatus
FROM user_account 
WHERE Image IS NOT NULL AND LENGTH(Image) > 0
LIMIT 5;
