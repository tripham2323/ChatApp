-- Tạo bảng nhóm chat
CREATE TABLE `chat_groups` (
  `groupID` int(11) NOT NULL AUTO_INCREMENT,
  `groupName` varchar(100) NOT NULL,
  `groupDescription` text,
  `groupImage` longblob,
  `createdBy` int(11) NOT NULL,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` enum('active','inactive') NOT NULL DEFAULT 'active',
  PRIMARY KEY (`groupID`),
  KEY `fk_groups_creator` (`createdBy`),
  CONSTRAINT `fk_groups_creator` FOREIGN KEY (`createdBy`) REFERENCES `user_account` (`UserID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo bảng thành viên nhóm
CREATE TABLE `group_members` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupID` int(11) NOT NULL,
  `userID` int(11) NOT NULL,
  `role` enum('admin','member') NOT NULL DEFAULT 'member',
  `joinedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('active','left','removed') NOT NULL DEFAULT 'active',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_group_member` (`groupID`,`userID`),
  KEY `fk_group_members_user` (`userID`),
  CONSTRAINT `fk_group_members_group` FOREIGN KEY (`groupID`) REFERENCES `chat_groups` (`groupID`) ON DELETE CASCADE,
  CONSTRAINT `fk_group_members_user` FOREIGN KEY (`userID`) REFERENCES `user_account` (`UserID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo bảng tin nhắn nhóm
CREATE TABLE `group_messages` (
  `messageID` int(11) NOT NULL AUTO_INCREMENT,
  `groupID` int(11) NOT NULL,
  `senderID` int(11) NOT NULL,
  `messageType` int(11) NOT NULL DEFAULT 0, -- 0: text, 1: image, 2: file, 3: voice
  `messageContent` text,
  `fileName` varchar(255) DEFAULT NULL,
  `fileSize` bigint(20) DEFAULT NULL,
  `sentAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('sent','delivered','read') NOT NULL DEFAULT 'sent',
  PRIMARY KEY (`messageID`),
  KEY `fk_group_messages_group` (`groupID`),
  KEY `fk_group_messages_sender` (`senderID`),
  KEY `idx_group_messages_time` (`sentAt`),
  CONSTRAINT `fk_group_messages_group` FOREIGN KEY (`groupID`) REFERENCES `chat_groups` (`groupID`) ON DELETE CASCADE,
  CONSTRAINT `fk_group_messages_sender` FOREIGN KEY (`senderID`) REFERENCES `user_account` (`UserID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo trigger để tự động thêm người tạo nhóm làm admin
DELIMITER $$
CREATE TRIGGER `add_group_creator_as_admin` 
AFTER INSERT ON `chat_groups`
FOR EACH ROW
BEGIN
    INSERT INTO `group_members` (`groupID`, `userID`, `role`, `status`) 
    VALUES (NEW.groupID, NEW.createdBy, 'admin', 'active');
END$$
DELIMITER ;

-- Tạo index để tối ưu hiệu suất
CREATE INDEX idx_group_members_active ON group_members(groupID, status);
CREATE INDEX idx_group_messages_group_time ON group_messages(groupID, sentAt DESC);
