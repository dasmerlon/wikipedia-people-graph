/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PersonData` (
  `TITLE` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `LINK` text COLLATE utf8mb4_unicode_ci,
  `SHORT_DESCRIPTION` text COLLATE utf8mb4_unicode_ci,
  `IMAGE` text COLLATE utf8mb4_unicode_ci,
  `NAME` text COLLATE utf8mb4_unicode_ci,
  `BIRTH_NAME` text COLLATE utf8mb4_unicode_ci,
  `BIRTH_DATE` text COLLATE utf8mb4_unicode_ci,
  `BIRTH_PLACE` text COLLATE utf8mb4_unicode_ci,
  `DEATH_DATE` text COLLATE utf8mb4_unicode_ci,
  `DEATH_PLACE` text COLLATE utf8mb4_unicode_ci,
  `DEATH_CAUSE` text COLLATE utf8mb4_unicode_ci,
  `NATIONALITY` text COLLATE utf8mb4_unicode_ci,
  `EDUCATION` text COLLATE utf8mb4_unicode_ci,
  `KNOWN_FOR` text COLLATE utf8mb4_unicode_ci,
  `OCCUPATION` text COLLATE utf8mb4_unicode_ci,
  `ORDERS` text COLLATE utf8mb4_unicode_ci,
  `OFFICE` text COLLATE utf8mb4_unicode_ci,
  `TERM_START` text COLLATE utf8mb4_unicode_ci,
  `TERM_END` text COLLATE utf8mb4_unicode_ci,
  `PARTY` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`TITLE`),
  KEY `index_1` (`TITLE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Relationships` (
  `PERSON1` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `PERSON2` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `SENTENCE` mediumtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`PERSON1`,`PERSON2`),
  KEY `index_p1` (`PERSON1`),
  KEY `index_p2` (`PERSON2`),
  CONSTRAINT `fk_person1` FOREIGN KEY (`PERSON1`) REFERENCES `PersonData` (`TITLE`),
  CONSTRAINT `fk_person2` FOREIGN KEY (`PERSON2`) REFERENCES `PersonData` (`TITLE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
