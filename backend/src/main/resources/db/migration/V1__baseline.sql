-- Flyway baseline migration
-- Generated from production schema dump (mysqldump --no-data --single-transaction)
-- AUTO_INCREMENT current values and DROP TABLE statements intentionally removed.
-- Table order follows FK dependency order for readability;
-- FOREIGN_KEY_CHECKS is still toggled as a safety net.

SET FOREIGN_KEY_CHECKS = 0;

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(100) NOT NULL,
  `nickname` varchar(50) NOT NULL,
  `provider` enum('GITHUB','LOCAL') NOT NULL,
  `provider_id` varchar(100) NOT NULL,
  `role` enum('ADMIN','USER') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `skill_category`
--

CREATE TABLE `skill_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2i2j64lpx49b4p34e3l2hhooc` (`parent_id`),
  KEY `FKs2nunpu01qlbhdyw5jksvibdj` (`user_id`),
  CONSTRAINT `FK2i2j64lpx49b4p34e3l2hhooc` FOREIGN KEY (`parent_id`) REFERENCES `skill_category` (`id`),
  CONSTRAINT `FKs2nunpu01qlbhdyw5jksvibdj` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `skill`
--

CREATE TABLE `skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `display_order` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `proficiency` int NOT NULL,
  `status` enum('LEARNING','PROFICIENT') NOT NULL,
  `category_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9aoaclqrmkwbv9umbbwlo5x9s` (`category_id`),
  KEY `FKohg89pau976dm4s904dadgqhl` (`user_id`),
  CONSTRAINT `FK9aoaclqrmkwbv9umbbwlo5x9s` FOREIGN KEY (`category_id`) REFERENCES `skill_category` (`id`),
  CONSTRAINT `FKohg89pau976dm4s904dadgqhl` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `study_log`
--

CREATE TABLE `study_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text,
  `log_date` date NOT NULL,
  `title` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8nrflo5jrpjub941b4xy17lw9` (`user_id`,`log_date`),
  CONSTRAINT `FK8ms3urcnyopot5ohlavi52vvx` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `study_log_skill`
--

CREATE TABLE `study_log_skill` (
  `study_log_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  PRIMARY KEY (`study_log_id`,`skill_id`),
  KEY `FKmc11h56ykaf5syi6axt513188` (`skill_id`),
  CONSTRAINT `FKcnaefbkw8789kykn7dbu0alcw` FOREIGN KEY (`study_log_id`) REFERENCES `study_log` (`id`),
  CONSTRAINT `FKmc11h56ykaf5syi6axt513188` FOREIGN KEY (`skill_id`) REFERENCES `skill` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `github_account`
--

CREATE TABLE `github_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar_url` varchar(500) DEFAULT NULL,
  `follower_count` int DEFAULT NULL,
  `following_count` int DEFAULT NULL,
  `github_username` varchar(255) NOT NULL,
  `last_synced_at` datetime(6) DEFAULT NULL,
  `public_repo_count` int DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8uqa0ybx8rjdrsobechyukw4n` (`user_id`),
  CONSTRAINT `FKkd2ygqml52yvlw1cvicvsigor` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `github_contribution`
--

CREATE TABLE `github_contribution` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `count` int NOT NULL,
  `contribution_date` date NOT NULL,
  `github_account_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5d5i0aqbci9jk942bsn779ycr` (`github_account_id`,`contribution_date`),
  CONSTRAINT `FKm1s61f99oxc11m26yum8oa0nq` FOREIGN KEY (`github_account_id`) REFERENCES `github_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `github_repository`
--

CREATE TABLE `github_repository` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) DEFAULT NULL,
  `is_fork` bit(1) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `pushed_at` datetime(6) DEFAULT NULL,
  `stars` int DEFAULT NULL,
  `github_account_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKggcwcs00mqe0xpgi377mh84eq` (`github_account_id`),
  CONSTRAINT `FKggcwcs00mqe0xpgi377mh84eq` FOREIGN KEY (`github_account_id`) REFERENCES `github_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `roadmap`
--

CREATE TABLE `roadmap` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) DEFAULT NULL,
  `target_date` date DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd5i0uf9j7wr1bjchvjgr1vekt` (`user_id`),
  CONSTRAINT `FKd5i0uf9j7wr1bjchvjgr1vekt` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `roadmap_link`
--

CREATE TABLE `roadmap_link` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(255) DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  `roadmap_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf6irsxqf49qgx6vjmn5t5eqsi` (`roadmap_id`),
  CONSTRAINT `FKf6irsxqf49qgx6vjmn5t5eqsi` FOREIGN KEY (`roadmap_id`) REFERENCES `roadmap` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `roadmap_step`
--

CREATE TABLE `roadmap_step` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) DEFAULT NULL,
  `display_order` int NOT NULL,
  `link` varchar(500) DEFAULT NULL,
  `status` enum('DONE','IN_PROGRESS','TODO') NOT NULL,
  `target_date` date DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `roadmap_id` bigint NOT NULL,
  `skill_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb4bclarl705xs02gvcxydgg2s` (`roadmap_id`),
  KEY `FKsw5uviu979toutaxbnpa3g75x` (`skill_id`),
  CONSTRAINT `FKb4bclarl705xs02gvcxydgg2s` FOREIGN KEY (`roadmap_id`) REFERENCES `roadmap` (`id`),
  CONSTRAINT `FKsw5uviu979toutaxbnpa3g75x` FOREIGN KEY (`skill_id`) REFERENCES `skill` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
