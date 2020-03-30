CREATE TABLE `users`.`couple`
(
    `cid`          INT              NOT NULL AUTO_INCREMENT,
    `uid`          INT              NOT NULL,
    `pid`          INT              NOT NULL,
    `connected_at` BIGINT UNSIGNED  NOT NULL,
    PRIMARY KEY (`cid`),
    UNIQUE INDEX `cid_UNIQUE` (`cid` ASC) VISIBLE
);