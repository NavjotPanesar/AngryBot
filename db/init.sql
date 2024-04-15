-- Adminer 4.8.1 MySQL 5.5.5-10.11.6-MariaDB-log dump
CREATE DATABASE bagana;
USE bagana;



SET NAMES utf8;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

DROP TABLE IF EXISTS `COMMAND_KEYWORD`;
CREATE TABLE `COMMAND_KEYWORD` (
  `COMMAND` tinytext NOT NULL,
  `KEYWORD` tinytext NOT NULL
);

INSERT INTO `COMMAND_KEYWORD` (`COMMAND`, `KEYWORD`) VALUES
('sherpa',	''),
('sherpa',	'list'),
('sherpa',	'add'),
('sherpa',	'remove'),
('scramble',	''),
('sherpalist',	'beginner;intermediate;advanced;black diamond'),
('embee',	''),
('',	''),
('crazydazed',	''),
('',	''),
('gunk',	''),
('ungunk',	''),
('score',	''),
('name',	'');

DROP TABLE IF EXISTS `GAMBLE`;
CREATE TABLE `GAMBLE` (
  `GUILD` bigint(20) unsigned NOT NULL,
  `BANANA_JACKPOT` int(10) unsigned zerofill NOT NULL DEFAULT 0000000025,
  PRIMARY KEY (`GUILD`)
);


DROP TABLE IF EXISTS `GUILD_USER`;
CREATE TABLE `GUILD_USER` (
  `GUILD` varchar(255) NOT NULL,
  `UID` varchar(255) NOT NULL,
  `BANANA_TOTAL` int(10) unsigned zerofill NOT NULL DEFAULT 0000000005,
  `BANANA_CURRENT` int(10) unsigned zerofill NOT NULL DEFAULT 0000000005,
  `GUNKED` int(10) unsigned zerofill NOT NULL,
  `GUNKS` int(10) unsigned zerofill NOT NULL,
  `TIMEOUT` int(10) NOT NULL DEFAULT 0,
  PRIMARY KEY (`GUILD`,`UID`)
);


-- 2024-04-15 02:51:12
