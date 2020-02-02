CREATE TABLE `users`.`couple`
(
    `cid`          INT      NOT NULL AUTO_INCREMENT,
    `uid`          INT      NOT NULL,
    `pid`          INT      NOT NULL,
    `connected_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`cid`),
    UNIQUE INDEX `cid_UNIQUE` (`cid` ASC) VISIBLE
);