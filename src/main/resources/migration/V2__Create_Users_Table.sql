CREATE TABLE `users`.`user`
(
    `uid`            INT                NOT NULL AUTO_INCREMENT,
    `pid`            INT                NULL,
    `cid`            INT                NULL,
    `email`          VARCHAR(150)       NOT NULL,
    `first_name`     VARCHAR(70)        NOT NULL,
    `last_name`      VARCHAR(70)        NOT NULL,
    `accepted_terms` TINYINT(1)         NOT NULL,
    `of_age`         TINYINT(1)         NOT NULL,
    `created_at`     BIGINT UNSIGNED    NOT NULL,
    PRIMARY KEY (`uid`),
    UNIQUE INDEX `uid_UNIQUE` (`uid` ASC) VISIBLE,
    UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE,
    INDEX `fk_users_couples_cid_idx` (`cid` ASC) VISIBLE,
    CONSTRAINT `fk_users_couples_cid`
        FOREIGN KEY (`cid`)
            REFERENCES `users`.`couple` (`cid`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);