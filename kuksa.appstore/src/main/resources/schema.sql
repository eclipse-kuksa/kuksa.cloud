
CREATE TABLE IF NOT EXISTS `app` (
    `id` bigint(20) NOT NULL auto_increment,
    `name` varchar(200) NOT NULL,
    `hawkbitname` varchar(4000) NOT NULL,
    `description` varchar(4000) NOT NULL,
    `version` varchar(100) NOT NULL,
    `owner` varchar(100) NOT NULL,
    `downloadcount` bigint(20) NOT NULL,
    `publishdate` TIMESTAMP, 
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint(20) NOT NULL auto_increment,
    `user_name` varchar(200) NOT NULL,
    `password` varchar(200) NOT NULL,
    `adminuser` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS  `usersapps` (
	`userid` BIGINT(20) NOT NULL,
	`appid` BIGINT(20) NOT NULL,
	`status` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8_turkish_ci',
	`date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`userid`, `appid`),
	INDEX `FK_appid_app` (`appid`),
	CONSTRAINT `FK_appid_app` FOREIGN KEY (`appid`) REFERENCES `app` (`id`) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT `FK_userid_user` FOREIGN KEY (`userid`) REFERENCES `user` (`id`) ON UPDATE CASCADE ON DELETE CASCADE
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;