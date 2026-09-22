CREATE TABLE `career_analysis` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `idempotency_key` varchar(255) NOT NULL,
  `status` enum('PENDING','RUNNING','COMPLETED','FAILED') NOT NULL,
  `result` longtext,
  `error_message` varchar(1000) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_career_analysis_user_idem_key` (`user_id`,`idempotency_key`),
  CONSTRAINT `FK_career_analysis_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;