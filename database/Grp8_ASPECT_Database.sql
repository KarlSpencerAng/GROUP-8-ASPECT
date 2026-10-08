-- ASPECT GitHub Database
-- Sanitized from the final working database export.
-- Keeps the complete schema plus curriculum/subject reference data.
-- Removes personal/test enrollments, grades, concerns, OLA scores, and audit history.
--
-- Demo accounts:
-- Registrar: registrar / registrar123
-- Teacher: teacher / 1234
-- Student: studentdemo / 1234
--
CREATE DATABASE IF NOT EXISTS ASPECT_DB
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE ASPECT_DB;

-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: gradingsystem_test
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `curriculum_courses`
--

DROP TABLE IF EXISTS `curriculum_courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `curriculum_courses` (
  `CurriculumCourseID` int NOT NULL AUTO_INCREMENT,
  `SubjectCode` varchar(20) NOT NULL,
  `SubjectName` varchar(150) NOT NULL,
  `Units` int NOT NULL,
  `CurriculumYear` int DEFAULT NULL,
  `YearLevel` int DEFAULT NULL,
  `Term` int DEFAULT NULL,
  `Category` varchar(50) DEFAULT NULL,
  `LectureHours` decimal(5,2) DEFAULT '0.00',
  `LaboratoryHours` decimal(5,2) DEFAULT '0.00',
  `Prerequisites` varchar(255) DEFAULT NULL,
  `Corequisites` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`CurriculumCourseID`),
  UNIQUE KEY `SubjectCode` (`SubjectCode`),
  CONSTRAINT `chk_curriculum_units` CHECK ((`Units` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `curriculum_courses`
--

LOCK TABLES `curriculum_courses` WRITE;
/*!40000 ALTER TABLE `curriculum_courses` DISABLE KEYS */;
INSERT INTO `curriculum_courses` VALUES (1,'CSS121P','Computer Programming 1',3,2025,1,1,'Major',2.00,3.00,NULL,NULL),(2,'FW01-2','Physical Activities Toward Health and Fitness 1 (PATHFIT 1): Movement Competency Training',2,2025,1,1,'PE',3.00,0.00,NULL,NULL),(3,'GED101','Understanding the Self',3,2025,1,1,'General Education',4.50,0.00,NULL,NULL),(4,'GED103','Readings in Philippine History',3,2025,1,1,'General Education',4.50,0.00,NULL,NULL),(5,'ITS100','Introduction to Information Technology',3,2025,1,1,'Major',4.50,0.00,NULL,NULL),(6,'MATH165','College Algebra with Analytic Geometry',3,2025,1,1,'Mathematics',4.50,0.00,NULL,NULL),(7,'NSTP001','National Service Training Program General Module',2,2025,1,1,'NSTP',4.50,0.00,NULL,NULL),(8,'CSS122P','Computer Programming 2',3,2025,1,2,'Major',2.00,3.00,'CSS121P',NULL),(9,'CWTS001','Civic Welfare Training Service 1',2,2025,1,2,'NSTP',4.50,0.00,'NSTP001',NULL),(10,'FW02-2','Physical Activities Toward Health and Fitness 2 (PATHFIT 2): Exercise-Based Fitness Activities',2,2025,1,2,'PE',3.00,0.00,'FW01-2',NULL),(11,'GED104','Science, Technology and Society',3,2025,1,2,'General Education',4.50,0.00,NULL,NULL),(12,'GED105','The Contemporary World',3,2025,1,2,'General Education',4.50,0.00,NULL,NULL),(13,'ITS110P','Computer Hardware Fundamentals',3,2025,1,2,'Major',2.00,3.00,NULL,NULL),(14,'ITS121-1L','Web Systems and Technologies 1 Laboratory',2,2025,1,2,'Major',4.50,0.00,'CSS121P',NULL),(15,'MATH170','Linear Algebra with Computer Applications',3,2025,1,2,'Mathematics',4.50,0.00,'MATH165',NULL),(16,'CSS130-1','Data Structures and Algorithms',3,2025,1,3,'Major',4.50,0.00,'CSS121P',NULL),(17,'CWTS002','Civic Welfare Training Service 2',2,2025,1,3,'NSTP',4.50,0.00,'CWTS001',NULL),(18,'FW03-2','Physical Activities Toward Health and Fitness 3 (PATHFIT 3): Dance / Martial Arts',2,2025,1,3,'PE',3.00,0.00,'FW02-2',NULL),(19,'GED106','Purposive Communication',3,2025,1,3,'General Education',4.50,0.00,NULL,NULL),(20,'GED107','Ethics',3,2025,1,3,'General Education',4.50,0.00,NULL,NULL),(21,'ITS131P','Information Management',3,2025,1,3,'Major',4.50,0.00,'CSS122P',NULL),(22,'ITS161-1L','Data Communication and Networking Fundamentals',2,2025,1,3,'Major',4.50,0.00,'CSS121P',NULL),(23,'MATH174','Differential and Integral Calculus',3,2025,1,3,'Mathematics',4.50,0.00,'MATH165',NULL),(24,'CSS123P','Computer Programming 3',3,2025,2,1,'Major',2.00,3.00,'ITS131P',NULL),(25,'GED108','Art Appreciation',3,2025,2,1,'General Education',4.50,0.00,NULL,NULL),(26,'ISS120','Information Systems and Business Processes',3,2025,2,1,'Major',4.50,0.00,'ITS100',NULL),(27,'ITS162-1L','Data Communication and Networking Essentials',2,2025,2,1,'Major',4.50,0.00,'ITS161-1L',NULL),(28,'MATH181','Quantitative Methods',3,2025,2,1,'Mathematics',4.50,0.00,'MATH170',NULL),(29,'PCC150','Professional Communications Course',3,2025,2,1,'General Education',4.50,0.00,NULL,NULL),(30,'ENV121','Environmental Science and Sustainability',3,2025,2,2,'General Education',4.50,0.00,'2ND YEAR STANDING',NULL),(31,'GED102','Mathematics in the Modern World',3,2025,2,2,'General Education',4.50,0.00,NULL,NULL),(32,'GED110','People and Earth\'s Ecosystem',3,2025,2,2,'General Education',4.50,0.00,NULL,NULL),(33,'GEE130','GE Elective',3,2025,2,2,'Elective',4.50,0.00,NULL,NULL),(34,'ITS163-1L','Data Communication and Networking Core',2,2025,2,2,'Major',4.50,0.00,'ITS162-1L',NULL),(35,'CSS131-1','Discrete Mathematics 1',3,2025,2,3,'Major',4.50,0.00,'CSS130-1',NULL),(36,'DSS110','Introduction to Data Science',3,2025,2,3,'Major',4.50,0.00,'ITS131P',NULL),(37,'FW04-2','Physical Activities Toward Health and Fitness 4 (PATHFIT 4): Group Exercise',2,2025,2,3,'PE',3.00,0.00,'FW03-2',NULL),(38,'GEE120','GE Elective',3,2025,2,3,'Elective',4.50,0.00,NULL,NULL),(39,'ITS112P','Computer Architecture and Organization',3,2025,2,3,'Major',3.00,4.50,'ITS110P',NULL),(40,'ITS122P','Web Systems and Technologies 2',3,2025,2,4,'Major',3.00,4.50,'ITS121-1L, ITS131P',NULL),(41,'ITS132P','Data Warehousing and Data Mining',3,2025,2,4,'Major',3.00,4.50,'ITS131P',NULL),(42,'ITS141-1','Human-Computer Interaction 1',3,2025,2,4,'Major',4.50,0.00,'CSS123P',NULL),(43,'ITS151P','Systems Integration and Architecture 1',3,2025,2,4,'Major',3.00,4.50,'CSS123P, ISS120',NULL),(44,'CSS140-1','Artificial Intelligence',3,2025,3,1,'Major',4.50,0.00,'CSS130-1',NULL),(45,'ITS120P','Application Development and Emerging Technologies',3,2025,3,1,'Major',3.00,4.50,'CSS123P',NULL),(46,'ITS142P','Human-Computer Interaction 2',3,2025,3,1,'Major',3.00,4.50,'ITS141-1',NULL),(47,'ITS152P','Systems Integration and Architecture 2',3,2025,3,1,'Major',3.00,4.50,'ITS151P',NULL),(48,'ITS165-1','Information Security and Assurance 1',3,2025,3,1,'Major',4.50,0.00,'ITS131P, ITS161-1L',NULL),(49,'ITS160','Project Management',3,2025,3,2,'Major',4.50,0.00,'CSS123P',NULL),(50,'ITS150P','Operating Systems',3,2025,3,2,'Major',3.00,4.50,'ITS112P',NULL),(51,'ITS166P','Information Security and Assurance 2',3,2025,3,2,'Major',3.00,4.50,'ITS165-1',NULL),(52,'RZL110','The Life and Works of Rizal',3,2025,3,2,'General Education',4.50,0.00,NULL,NULL),(53,'CSS153P','Software Quality',3,2025,3,3,'Major',3.00,4.50,'ITS142P',NULL),(54,'IE103-2','Technopreneurship 101',3,2025,3,3,'Major',4.50,0.00,NULL,NULL),(55,'ITS109-1','Research Methods in Information Technology',3,2025,3,3,'Major',4.50,0.00,'ITS152P',NULL),(56,'ITS153P','Systems Administration and Maintenance',3,2025,3,3,'Major',3.00,4.50,'ITS152P, ITS163-1L',NULL),(57,'ITS105-1','Social and Professional Issues',3,2025,3,4,'Major',4.50,0.00,'CSS123P, GED107',NULL),(58,'ITS199-1R','Practicum 1',3,2025,3,4,'Major',4.50,0.00,'3RD YEAR STANDING',NULL),(59,'ITS200-01','Thesis 1',1,2025,3,4,'Major',1.50,0.00,'ITS109-1',NULL),(60,'ITS198F','Career Development and Seminar in IT',1,2025,4,1,'Major',4.50,0.00,'FOR GRADUATING STUDENTS ONLY',NULL),(61,'ITS199-2R','Practicum 2',3,2025,4,1,'Major',4.50,0.00,'ITS199-1R',NULL),(62,'ITS200-02','Thesis 2',1,2025,4,1,'Major',4.50,0.00,'ITS200-01',NULL),(63,'SGE100X','Student Global Experience',0,2025,4,1,'Elective',0.00,0.00,'4TH YEAR STANDING',NULL),(64,'CSS171-1','GRAPHICS AND VISUAL COMPUTING',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL),(65,'CSS172-1','PATTERN RECOGNITION',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL),(66,'ECS176-1','INTRODUCTION TO GAME PROGRAMMING',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL),(67,'ISS171-1','SUPPLY CHAIN MANAGEMENT',3,2025,3,4,'IT Elective',4.50,0.00,'ITS122P',NULL),(68,'ISS172-1','CUSTOMER RELATION MANAGEMENT',3,2025,3,4,'IT Elective',4.50,0.00,'ITS131P',NULL),(69,'ISS173-1','ESSENTIAL OF SAS',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL),(70,'ISS174-1','IT AUDIT AND CONTROL',3,2025,3,4,'IT Elective',4.50,0.00,'ITS131P',NULL),(71,'ITS170-1','IT INFRASTRUCTURE LIBRARY FOUNDATION COURSE',3,2025,3,4,'IT Elective',4.50,0.00,'ITS131P',NULL),(72,'ITS171-1','FUNDAMENTALS OF SAP',3,2025,3,4,'IT Elective',4.50,0.00,'ITS131P',NULL),(73,'ITS172-1','MOBILE APPLICATION DEVELOPMENT',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL),(74,'ITS173-1','EMBEDDED SYSTEMS',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL),(75,'ITS174-1','INTERNET OF THINGS',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL),(76,'ITS175-1','CLOUD COMPUTING',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL),(77,'ITS176','BLOCKCHAIN TECHNOLOGY',3,2025,3,4,'IT Elective',4.50,0.00,'CSS123P',NULL);
/*!40000 ALTER TABLE `curriculum_courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `direct_grade_revision_history`
--

DROP TABLE IF EXISTS `direct_grade_revision_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `direct_grade_revision_history` (
  `RevisionID` int NOT NULL AUTO_INCREMENT,
  `GradeID` int NOT NULL,
  `TeacherID` int NOT NULL,
  `Reason` varchar(1000) NOT NULL,
  `OldCO1` decimal(5,2) NOT NULL,
  `OldCO2` decimal(5,2) NOT NULL,
  `OldCO3` decimal(5,2) NOT NULL,
  `OldFinalExam` decimal(5,2) NOT NULL,
  `OldOLA` decimal(5,2) NOT NULL,
  `OldCoursera` decimal(5,2) NOT NULL,
  `OldFinalPercentage` decimal(6,2) NOT NULL,
  `OldNumericalGrade` decimal(3,2) NOT NULL,
  `NewCO1` decimal(5,2) NOT NULL,
  `NewCO2` decimal(5,2) NOT NULL,
  `NewCO3` decimal(5,2) NOT NULL,
  `NewFinalExam` decimal(5,2) NOT NULL,
  `NewOLA` decimal(5,2) NOT NULL,
  `NewCoursera` decimal(5,2) NOT NULL,
  `NewFinalPercentage` decimal(6,2) NOT NULL,
  `NewNumericalGrade` decimal(3,2) NOT NULL,
  `RevisedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`RevisionID`),
  KEY `TeacherID` (`TeacherID`),
  KEY `idx_direct_revision_grade` (`GradeID`,`RevisedAt`),
  CONSTRAINT `direct_grade_revision_history_ibfk_1` FOREIGN KEY (`GradeID`) REFERENCES `grades` (`GradeID`),
  CONSTRAINT `direct_grade_revision_history_ibfk_2` FOREIGN KEY (`TeacherID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `direct_grade_revision_history`
--

LOCK TABLES `direct_grade_revision_history` WRITE;
/*!40000 ALTER TABLE `direct_grade_revision_history` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `direct_grade_revision_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollment_requests`
--

DROP TABLE IF EXISTS `enrollment_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollment_requests` (
  `RequestID` int NOT NULL AUTO_INCREMENT,
  `StudentID` int NOT NULL,
  `SubjectID` int NOT NULL,
  `Status` enum('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending',
  `RequestedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ReviewedAt` datetime DEFAULT NULL,
  `ReviewedBy` int DEFAULT NULL,
  `ReviewNote` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`RequestID`),
  UNIQUE KEY `uq_request_student_subject` (`StudentID`,`SubjectID`),
  KEY `fk_request_reviewer` (`ReviewedBy`),
  KEY `idx_requests_subject_status` (`SubjectID`,`Status`),
  CONSTRAINT `fk_request_reviewer` FOREIGN KEY (`ReviewedBy`) REFERENCES `users` (`UserID`),
  CONSTRAINT `fk_request_student` FOREIGN KEY (`StudentID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `fk_request_subject` FOREIGN KEY (`SubjectID`) REFERENCES `subjects` (`SubjectID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollment_requests`
--

LOCK TABLES `enrollment_requests` WRITE;
/*!40000 ALTER TABLE `enrollment_requests` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `enrollment_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollments` (
  `EnrollmentID` int NOT NULL AUTO_INCREMENT,
  `StudentID` int NOT NULL,
  `SubjectID` int NOT NULL,
  `EnrollmentDate` date NOT NULL,
  `Status` enum('Enrolled','Completed','Withdrawn') NOT NULL DEFAULT 'Enrolled',
  PRIMARY KEY (`EnrollmentID`),
  UNIQUE KEY `uq_student_subject` (`StudentID`,`SubjectID`),
  KEY `fk_enrollment_subject` (`SubjectID`),
  CONSTRAINT `fk_enrollment_student` FOREIGN KEY (`StudentID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `fk_enrollment_subject` FOREIGN KEY (`SubjectID`) REFERENCES `subjects` (`SubjectID`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollments`
--

LOCK TABLES `enrollments` WRITE;
/*!40000 ALTER TABLE `enrollments` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grade_concerns`
--

DROP TABLE IF EXISTS `grade_concerns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grade_concerns` (
  `ConcernID` int NOT NULL AUTO_INCREMENT,
  `StudentID` int NOT NULL,
  `SubjectID` int NOT NULL,
  `TeacherID` int NOT NULL,
  `ConcernText` text NOT NULL,
  `Status` enum('Pending','Under Review','Revised') NOT NULL DEFAULT 'Pending',
  `DateRaised` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ConcernID`),
  KEY `fk_concern_student` (`StudentID`),
  KEY `fk_concern_subject` (`SubjectID`),
  KEY `fk_concern_teacher` (`TeacherID`),
  CONSTRAINT `fk_concern_student` FOREIGN KEY (`StudentID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `fk_concern_subject` FOREIGN KEY (`SubjectID`) REFERENCES `subjects` (`SubjectID`),
  CONSTRAINT `fk_concern_teacher` FOREIGN KEY (`TeacherID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grade_concerns`
--

LOCK TABLES `grade_concerns` WRITE;
/*!40000 ALTER TABLE `grade_concerns` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `grade_concerns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grade_revision_history`
--

DROP TABLE IF EXISTS `grade_revision_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grade_revision_history` (
  `RevisionID` int NOT NULL AUTO_INCREMENT,
  `GradeID` int NOT NULL,
  `ConcernID` int NOT NULL,
  `OldCO1` decimal(5,2) NOT NULL,
  `OldCO2` decimal(5,2) NOT NULL,
  `OldCO3` decimal(5,2) NOT NULL,
  `OldFinalExam` decimal(5,2) NOT NULL,
  `OldOLA` decimal(5,2) NOT NULL,
  `OldCoursera` decimal(5,2) NOT NULL,
  `OldFinalPercentage` decimal(5,2) NOT NULL,
  `OldNumericalGrade` decimal(5,2) NOT NULL,
  `NewCO1` decimal(5,2) NOT NULL,
  `NewCO2` decimal(5,2) NOT NULL,
  `NewCO3` decimal(5,2) NOT NULL,
  `NewFinalExam` decimal(5,2) NOT NULL,
  `NewOLA` decimal(5,2) NOT NULL,
  `NewCoursera` decimal(5,2) NOT NULL,
  `NewFinalPercentage` decimal(5,2) NOT NULL,
  `NewNumericalGrade` decimal(5,2) NOT NULL,
  `RevisionDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`RevisionID`),
  UNIQUE KEY `uq_revision_concern` (`ConcernID`),
  KEY `fk_revision_grade` (`GradeID`),
  CONSTRAINT `fk_revision_concern` FOREIGN KEY (`ConcernID`) REFERENCES `grade_concerns` (`ConcernID`),
  CONSTRAINT `fk_revision_grade` FOREIGN KEY (`GradeID`) REFERENCES `grades` (`GradeID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grade_revision_history`
--

LOCK TABLES `grade_revision_history` WRITE;
/*!40000 ALTER TABLE `grade_revision_history` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `grade_revision_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grade_submissions`
--

DROP TABLE IF EXISTS `grade_submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grade_submissions` (
  `SubmissionID` int NOT NULL AUTO_INCREMENT,
  `TeacherID` int NOT NULL,
  `SubjectCode` varchar(50) NOT NULL,
  `Status` enum('Pending','Returned','Posted') NOT NULL DEFAULT 'Pending',
  `SubmittedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ReviewedBy` int DEFAULT NULL,
  `ReviewedAt` datetime DEFAULT NULL,
  `RegistrarComment` text,
  PRIMARY KEY (`SubmissionID`),
  KEY `fk_grade_submission_teacher` (`TeacherID`),
  KEY `fk_grade_submission_registrar` (`ReviewedBy`),
  CONSTRAINT `fk_grade_submission_registrar` FOREIGN KEY (`ReviewedBy`) REFERENCES `users` (`UserID`),
  CONSTRAINT `fk_grade_submission_teacher` FOREIGN KEY (`TeacherID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grade_submissions`
--

LOCK TABLES `grade_submissions` WRITE;
/*!40000 ALTER TABLE `grade_submissions` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `grade_submissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grades` (
  `GradeID` int NOT NULL AUTO_INCREMENT,
  `StudentID` int NOT NULL,
  `SubjectID` int NOT NULL,
  `CO1` decimal(5,2) NOT NULL,
  `CO2` decimal(5,2) NOT NULL,
  `CO3` decimal(5,2) NOT NULL,
  `FinalExam` decimal(5,2) NOT NULL,
  `OLA` decimal(5,2) NOT NULL,
  `Coursera` decimal(5,2) NOT NULL,
  `FinalPercentage` decimal(6,2) NOT NULL,
  `NumericalGrade` decimal(3,2) NOT NULL,
  `CalculationComplete` tinyint(1) NOT NULL DEFAULT '0',
  `SubmissionStatus` enum('Draft','Submitted','Approved','Revised','Pending','Returned','Posted') NOT NULL DEFAULT 'Draft',
  PRIMARY KEY (`GradeID`),
  UNIQUE KEY `uq_grade_student_subject` (`StudentID`,`SubjectID`),
  KEY `fk_grade_subject` (`SubjectID`),
  CONSTRAINT `fk_grade_student` FOREIGN KEY (`StudentID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `fk_grade_subject` FOREIGN KEY (`SubjectID`) REFERENCES `subjects` (`SubjectID`),
  CONSTRAINT `chk_co1` CHECK (((`CO1` >= 0) and (`CO1` <= 100))),
  CONSTRAINT `chk_co2` CHECK (((`CO2` >= 0) and (`CO2` <= 100))),
  CONSTRAINT `chk_co3` CHECK (((`CO3` >= 0) and (`CO3` <= 100))),
  CONSTRAINT `chk_coursera` CHECK (((`Coursera` >= 0) and (`Coursera` <= 100))),
  CONSTRAINT `chk_final_exam` CHECK (((`FinalExam` >= 0) and (`FinalExam` <= 100))),
  CONSTRAINT `chk_final_percentage` CHECK (((`FinalPercentage` >= 0) and (`FinalPercentage` <= 100))),
  CONSTRAINT `chk_ola` CHECK (((`OLA` >= 0) and (`OLA` <= 100)))
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grades`
--

LOCK TABLES `grades` WRITE;
/*!40000 ALTER TABLE `grades` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `grades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historical_records`
--

DROP TABLE IF EXISTS `historical_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historical_records` (
  `HistoricalRecordID` int NOT NULL AUTO_INCREMENT,
  `StudentID` int NOT NULL,
  `CurriculumCourseID` int NOT NULL,
  `Status` enum('Taken','Incomplete','Exempted/Credited') NOT NULL,
  `Note` varchar(255) DEFAULT NULL,
  `IsDemo` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`HistoricalRecordID`),
  UNIQUE KEY `uq_historical_student_course` (`StudentID`,`CurriculumCourseID`),
  KEY `fk_history_curriculum` (`CurriculumCourseID`),
  CONSTRAINT `fk_history_curriculum` FOREIGN KEY (`CurriculumCourseID`) REFERENCES `curriculum_courses` (`CurriculumCourseID`),
  CONSTRAINT `fk_history_student` FOREIGN KEY (`StudentID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historical_records`
--

LOCK TABLES `historical_records` WRITE;
/*!40000 ALTER TABLE `historical_records` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `historical_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ola_assessments`
--

DROP TABLE IF EXISTS `ola_assessments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ola_assessments` (
  `AssessmentID` int NOT NULL AUTO_INCREMENT,
  `SubjectID` int NOT NULL,
  `AssessmentName` varchar(100) NOT NULL,
  `MaximumScore` decimal(6,2) NOT NULL,
  `Weight` decimal(5,2) NOT NULL,
  PRIMARY KEY (`AssessmentID`),
  KEY `fk_ola_assessment_subject` (`SubjectID`),
  CONSTRAINT `fk_ola_assessment_subject` FOREIGN KEY (`SubjectID`) REFERENCES `subjects` (`SubjectID`),
  CONSTRAINT `chk_activity_weight` CHECK (((`Weight` >= 0) and (`Weight` <= 100))),
  CONSTRAINT `chk_maximum_score` CHECK ((`MaximumScore` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ola_assessments`
--

LOCK TABLES `ola_assessments` WRITE;
/*!40000 ALTER TABLE `ola_assessments` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `ola_assessments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ola_revision_history`
--

DROP TABLE IF EXISTS `ola_revision_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ola_revision_history` (
  `RevisionID` int NOT NULL AUTO_INCREMENT,
  `ConcernID` int NOT NULL,
  `AssessmentID` int NOT NULL,
  `StudentID` int NOT NULL,
  `TeacherID` int NOT NULL,
  `OldScore` decimal(6,2) NOT NULL,
  `NewScore` decimal(6,2) NOT NULL,
  `RevisionDate` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`RevisionID`),
  UNIQUE KEY `uq_ola_revision` (`ConcernID`,`AssessmentID`),
  KEY `AssessmentID` (`AssessmentID`),
  KEY `StudentID` (`StudentID`),
  KEY `TeacherID` (`TeacherID`),
  CONSTRAINT `ola_revision_history_ibfk_1` FOREIGN KEY (`ConcernID`) REFERENCES `grade_concerns` (`ConcernID`),
  CONSTRAINT `ola_revision_history_ibfk_2` FOREIGN KEY (`AssessmentID`) REFERENCES `ola_assessments` (`AssessmentID`),
  CONSTRAINT `ola_revision_history_ibfk_3` FOREIGN KEY (`StudentID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `ola_revision_history_ibfk_4` FOREIGN KEY (`TeacherID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ola_revision_history`
--

LOCK TABLES `ola_revision_history` WRITE;
/*!40000 ALTER TABLE `ola_revision_history` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `ola_revision_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ola_scores`
--

DROP TABLE IF EXISTS `ola_scores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ola_scores` (
  `OLAScoreID` int NOT NULL AUTO_INCREMENT,
  `AssessmentID` int NOT NULL,
  `StudentID` int NOT NULL,
  `Score` decimal(6,2) NOT NULL,
  PRIMARY KEY (`OLAScoreID`),
  UNIQUE KEY `uq_ola_student` (`AssessmentID`,`StudentID`),
  KEY `fk_ola_score_student` (`StudentID`),
  CONSTRAINT `fk_ola_score_assessment` FOREIGN KEY (`AssessmentID`) REFERENCES `ola_assessments` (`AssessmentID`),
  CONSTRAINT `fk_ola_score_student` FOREIGN KEY (`StudentID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `chk_ola_score_nonnegative` CHECK ((`Score` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ola_scores`
--

LOCK TABLES `ola_scores` WRITE;
/*!40000 ALTER TABLE `ola_scores` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `ola_scores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_submission_assessment_revisions`
--

DROP TABLE IF EXISTS `post_submission_assessment_revisions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_submission_assessment_revisions` (
  `RevisionID` int NOT NULL AUTO_INCREMENT,
  `AssessmentID` int NOT NULL,
  `GradeID` int NOT NULL,
  `TeacherID` int NOT NULL,
  `Reason` varchar(1000) NOT NULL,
  `OldOLA` decimal(6,2) NOT NULL,
  `NewOLA` decimal(6,2) NOT NULL,
  `OldFinalPercentage` decimal(6,2) NOT NULL,
  `NewFinalPercentage` decimal(6,2) NOT NULL,
  `OldNumericalGrade` decimal(3,2) NOT NULL,
  `NewNumericalGrade` decimal(3,2) NOT NULL,
  `RevisedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`RevisionID`),
  KEY `AssessmentID` (`AssessmentID`),
  KEY `TeacherID` (`TeacherID`),
  KEY `idx_post_submission_grade` (`GradeID`,`RevisedAt`),
  CONSTRAINT `post_submission_assessment_revisions_ibfk_1` FOREIGN KEY (`AssessmentID`) REFERENCES `ola_assessments` (`AssessmentID`),
  CONSTRAINT `post_submission_assessment_revisions_ibfk_2` FOREIGN KEY (`GradeID`) REFERENCES `grades` (`GradeID`),
  CONSTRAINT `post_submission_assessment_revisions_ibfk_3` FOREIGN KEY (`TeacherID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_submission_assessment_revisions`
--

LOCK TABLES `post_submission_assessment_revisions` WRITE;
/*!40000 ALTER TABLE `post_submission_assessment_revisions` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `post_submission_assessment_revisions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subjects`
--

DROP TABLE IF EXISTS `subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subjects` (
  `SubjectID` int NOT NULL AUTO_INCREMENT,
  `CurriculumCourseID` int NOT NULL,
  `SubjectCode` varchar(20) NOT NULL,
  `SubjectName` varchar(150) NOT NULL,
  `TeacherID` int DEFAULT NULL,
  `Units` int NOT NULL,
  `CurriculumYear` int DEFAULT NULL,
  `YearLevel` int DEFAULT NULL,
  `Term` int DEFAULT NULL,
  `Category` varchar(50) DEFAULT NULL,
  `Prerequisites` varchar(255) DEFAULT NULL,
  `Corequisites` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SubjectID`),
  UNIQUE KEY `CurriculumCourseID` (`CurriculumCourseID`),
  UNIQUE KEY `SubjectCode` (`SubjectCode`),
  KEY `fk_subject_teacher` (`TeacherID`),
  CONSTRAINT `fk_subject_curriculum` FOREIGN KEY (`CurriculumCourseID`) REFERENCES `curriculum_courses` (`CurriculumCourseID`),
  CONSTRAINT `fk_subject_teacher` FOREIGN KEY (`TeacherID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `chk_subject_units` CHECK ((`Units` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subjects`
--

LOCK TABLES `subjects` WRITE;
/*!40000 ALTER TABLE `subjects` DISABLE KEYS */;
INSERT INTO `subjects` VALUES (1,1,'CSS121P','Computer Programming 1',6,3,2025,1,1,'Major',NULL,NULL),(2,2,'FW01-2','Physical Activities Toward Health and Fitness 1 (PATHFIT 1): Movement Competency Training',9,2,2025,1,1,'PE',NULL,NULL),(3,3,'GED101','Understanding the Self',8,3,2025,1,1,'General Education',NULL,NULL),(4,4,'GED103','Readings in Philippine History',8,3,2025,1,1,'General Education',NULL,NULL),(5,5,'ITS100','Introduction to Information Technology',7,3,2025,1,1,'Major',NULL,NULL),(6,6,'MATH165','College Algebra with Analytic Geometry',6,3,2025,1,1,'Mathematics',NULL,NULL),(7,7,'NSTP001','National Service Training Program General Module',9,2,2025,1,1,'NSTP',NULL,NULL),(8,8,'CSS122P','Computer Programming 2',6,3,2025,1,2,'Major','CSS121P',NULL),(9,9,'CWTS001','Civic Welfare Training Service 1',9,2,2025,1,2,'NSTP','NSTP001',NULL),(10,10,'FW02-2','Physical Activities Toward Health and Fitness 2 (PATHFIT 2): Exercise-Based Fitness Activities',9,2,2025,1,2,'PE','FW01-2',NULL),(11,11,'GED104','Science, Technology and Society',8,3,2025,1,2,'General Education',NULL,NULL),(12,12,'GED105','The Contemporary World',8,3,2025,1,2,'General Education',NULL,NULL),(13,13,'ITS110P','Computer Hardware Fundamentals',7,3,2025,1,2,'Major',NULL,NULL),(14,14,'ITS121-1L','Web Systems and Technologies 1 Laboratory',7,2,2025,1,2,'Major','CSS121P',NULL),(15,15,'MATH170','Linear Algebra with Computer Applications',6,3,2025,1,2,'Mathematics','MATH165',NULL),(16,16,'CSS130-1','Data Structures and Algorithms',9,3,2025,1,3,'Major','CSS121P',NULL),(17,17,'CWTS002','Civic Welfare Training Service 2',9,2,2025,1,3,'NSTP','CWTS001',NULL),(18,18,'FW03-2','Physical Activities Toward Health and Fitness 3 (PATHFIT 3): Dance / Martial Arts',9,2,2025,1,3,'PE','FW02-2',NULL),(19,19,'GED106','Purposive Communication',8,3,2025,1,3,'General Education',NULL,NULL),(20,20,'GED107','Ethics',8,3,2025,1,3,'General Education',NULL,NULL),(21,21,'ITS131P','Information Management',7,3,2025,1,3,'Major','CSS122P',NULL),(22,22,'ITS161-1L','Data Communication and Networking Fundamentals',7,2,2025,1,3,'Major','CSS121P',NULL),(23,23,'MATH174','Differential and Integral Calculus',6,3,2025,1,3,'Mathematics','MATH165',NULL),(24,24,'CSS123P','Computer Programming 3',6,3,2025,2,1,'Major','ITS131P',NULL),(25,25,'GED108','Art Appreciation',8,3,2025,2,1,'General Education',NULL,NULL),(26,26,'ISS120','Information Systems and Business Processes',9,3,2025,2,1,'Major','ITS100',NULL),(27,27,'ITS162-1L','Data Communication and Networking Essentials',7,2,2025,2,1,'Major','ITS161-1L',NULL),(28,28,'MATH181','Quantitative Methods',6,3,2025,2,1,'Mathematics','MATH170',NULL),(29,29,'PCC150','Professional Communications Course',9,3,2025,2,1,'General Education',NULL,NULL),(30,30,'ENV121','Environmental Science and Sustainability',8,3,2025,2,2,'General Education','2ND YEAR STANDING',NULL),(31,31,'GED102','Mathematics in the Modern World',8,3,2025,2,2,'General Education',NULL,NULL),(32,32,'GED110','People and Earth\'s Ecosystem',8,3,2025,2,2,'General Education',NULL,NULL),(33,33,'GEE130','GE Elective',9,3,2025,2,2,'Elective',NULL,NULL),(34,34,'ITS163-1L','Data Communication and Networking Core',7,2,2025,2,2,'Major','ITS162-1L',NULL),(35,35,'CSS131-1','Discrete Mathematics 1',9,3,2025,2,3,'Major','CSS130-1',NULL),(36,36,'DSS110','Introduction to Data Science',9,3,2025,2,3,'Major','ITS131P',NULL),(37,37,'FW04-2','Physical Activities Toward Health and Fitness 4 (PATHFIT 4): Group Exercise',9,2,2025,2,3,'PE','FW03-2',NULL),(38,38,'GEE120','GE Elective',9,3,2025,2,3,'Elective',NULL,NULL),(39,39,'ITS112P','Computer Architecture and Organization',9,3,2025,2,3,'Major','ITS110P',NULL),(40,40,'ITS122P','Web Systems and Technologies 2',9,3,2025,2,4,'Major','ITS121-1L, ITS131P',NULL),(41,41,'ITS132P','Data Warehousing and Data Mining',9,3,2025,2,4,'Major','ITS131P',NULL),(42,42,'ITS141-1','Human-Computer Interaction 1',9,3,2025,2,4,'Major','CSS123P',NULL),(43,43,'ITS151P','Systems Integration and Architecture 1',9,3,2025,2,4,'Major','CSS123P, ISS120',NULL),(44,44,'CSS140-1','Artificial Intelligence',9,3,2025,3,1,'Major','CSS130-1',NULL),(45,45,'ITS120P','Application Development and Emerging Technologies',9,3,2025,3,1,'Major','CSS123P',NULL),(46,46,'ITS142P','Human-Computer Interaction 2',9,3,2025,3,1,'Major','ITS141-1',NULL),(47,47,'ITS152P','Systems Integration and Architecture 2',9,3,2025,3,1,'Major','ITS151P',NULL),(48,48,'ITS165-1','Information Security and Assurance 1',9,3,2025,3,1,'Major','ITS131P, ITS161-1L',NULL),(49,49,'ITS160','Project Management',9,3,2025,3,2,'Major','CSS123P',NULL),(50,50,'ITS150P','Operating Systems',9,3,2025,3,2,'Major','ITS112P',NULL),(51,51,'ITS166P','Information Security and Assurance 2',9,3,2025,3,2,'Major','ITS165-1',NULL),(52,52,'RZL110','The Life and Works of Rizal',8,3,2025,3,2,'General Education',NULL,NULL),(53,53,'CSS153P','Software Quality',9,3,2025,3,3,'Major','ITS142P',NULL),(54,54,'IE103-2','Technopreneurship 101',9,3,2025,3,3,'Major',NULL,NULL),(55,55,'ITS109-1','Research Methods in Information Technology',9,3,2025,3,3,'Major','ITS152P',NULL),(56,56,'ITS153P','Systems Administration and Maintenance',9,3,2025,3,3,'Major','ITS152P, ITS163-1L',NULL),(57,57,'ITS105-1','Social and Professional Issues',9,3,2025,3,4,'Major','CSS123P, GED107',NULL),(58,58,'ITS199-1R','Practicum 1',9,3,2025,3,4,'Major','3RD YEAR STANDING',NULL),(59,59,'ITS200-01','Thesis 1',9,1,2025,3,4,'Major','ITS109-1',NULL),(60,60,'ITS198F','Career Development and Seminar in IT',9,1,2025,4,1,'Major','FOR GRADUATING STUDENTS ONLY',NULL),(61,61,'ITS199-2R','Practicum 2',9,3,2025,4,1,'Major','ITS199-1R',NULL),(62,62,'ITS200-02','Thesis 2',9,1,2025,4,1,'Major','ITS200-01',NULL),(63,63,'SGE100X','Student Global Experience',9,0,2025,4,1,'Elective','4TH YEAR STANDING',NULL),(64,64,'CSS171-1','GRAPHICS AND VISUAL COMPUTING',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL),(65,65,'CSS172-1','PATTERN RECOGNITION',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL),(66,66,'ECS176-1','INTRODUCTION TO GAME PROGRAMMING',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL),(67,67,'ISS171-1','SUPPLY CHAIN MANAGEMENT',NULL,3,2025,3,4,'IT Elective','ITS122P',NULL),(68,68,'ISS172-1','CUSTOMER RELATION MANAGEMENT',NULL,3,2025,3,4,'IT Elective','ITS131P',NULL),(69,69,'ISS173-1','ESSENTIAL OF SAS',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL),(70,70,'ISS174-1','IT AUDIT AND CONTROL',NULL,3,2025,3,4,'IT Elective','ITS131P',NULL),(71,71,'ITS170-1','IT INFRASTRUCTURE LIBRARY FOUNDATION COURSE',NULL,3,2025,3,4,'IT Elective','ITS131P',NULL),(72,72,'ITS171-1','FUNDAMENTALS OF SAP',NULL,3,2025,3,4,'IT Elective','ITS131P',NULL),(73,73,'ITS172-1','MOBILE APPLICATION DEVELOPMENT',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL),(74,74,'ITS173-1','EMBEDDED SYSTEMS',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL),(75,75,'ITS174-1','INTERNET OF THINGS',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL),(76,76,'ITS175-1','CLOUD COMPUTING',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL),(77,77,'ITS176','BLOCKCHAIN TECHNOLOGY',NULL,3,2025,3,4,'IT Elective','CSS123P',NULL);
/*!40000 ALTER TABLE `subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_class_schedules`
--

DROP TABLE IF EXISTS `teacher_class_schedules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_class_schedules` (
  `ScheduleID` int NOT NULL AUTO_INCREMENT,
  `TeacherID` int NOT NULL,
  `SubjectID` int NOT NULL,
  `DayOfWeek` enum('Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday') NOT NULL,
  `StartTime` time NOT NULL,
  `EndTime` time NOT NULL,
  `Room` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`ScheduleID`),
  KEY `fk_tcs_subject` (`SubjectID`),
  KEY `idx_tcs_teacher_subject` (`TeacherID`,`SubjectID`),
  CONSTRAINT `fk_tcs_subject` FOREIGN KEY (`SubjectID`) REFERENCES `subjects` (`SubjectID`),
  CONSTRAINT `fk_tcs_teacher` FOREIGN KEY (`TeacherID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `chk_tcs_time` CHECK ((`EndTime` > `StartTime`))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_class_schedules`
--

LOCK TABLES `teacher_class_schedules` WRITE;
/*!40000 ALTER TABLE `teacher_class_schedules` DISABLE KEYS */;
-- Public release: no transactional/test data included.
/*!40000 ALTER TABLE `teacher_class_schedules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `FirstName` varchar(50) NOT NULL,
  `LastName` varchar(50) NOT NULL,
  `Username` varchar(50) NOT NULL,
  `Password` varchar(255) NOT NULL,
  `Role` enum('Student','Teacher','Registrar') NOT NULL,
  `StudentNumber` varchar(20) DEFAULT NULL,
  `TeacherNumber` varchar(20) DEFAULT NULL,
  `Section` varchar(50) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `MustChangePassword` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `Username` (`Username`),
  UNIQUE KEY `StudentNumber` (`StudentNumber`),
  UNIQUE KEY `TeacherNumber` (`TeacherNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES
(1,'Demo','Student','studentdemo','1234','Student','20260001',NULL,'BSIT-1','student@example.com',1),
(6,'Demo','Teacher','teacher','1234','Teacher',NULL,'TCH001',NULL,'teacher@example.com',0),
(7,'Demo','Teacher Two','teacher2','1234','Teacher',NULL,'TCH002',NULL,'teacher2@example.com',0),
(8,'Demo','Teacher Three','teacher3','1234','Teacher',NULL,'TCH003',NULL,'teacher3@example.com',0),
(9,'Demo','Teacher Four','teacher4','1234','Teacher',NULL,'TCH004',NULL,'teacher4@example.com',0),
(14,'System','Registrar','registrar','registrar123','Registrar',NULL,NULL,NULL,'registrar@example.com',0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-07 23:19:36
