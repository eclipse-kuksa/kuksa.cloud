CREATE TABLE IF NOT EXISTS `App` (
    `id` bigint(20) NOT NULL auto_increment,
    `name` varchar(2000) NOT NULL,
    `hawkbitname` varchar(2000) NOT NULL,
    `description` varchar(2000) NOT NULL,
    `version` varchar(2000) NOT NULL,
    `owner` varchar(2000) NOT NULL,
    `downloadcount` bigint(20) NOT NULL,
    `publishdate` TIMESTAMP, 
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `User` (
    `id` bigint(20) NOT NULL auto_increment,
    `user_name` varchar(200) NOT NULL,
    `password` varchar(200) NOT NULL,
    `adminuser` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `Usersapps` (
	`id` bigint(20) NOT NULL auto_increment,
	`userid` bigint(20) NOT NULL,
	`appid` bigint(20) NOT NULL,
	`status` varchar(50) NOT NULL,
    	`date` TIMESTAMP, 
	PRIMARY KEY (`id`)
);