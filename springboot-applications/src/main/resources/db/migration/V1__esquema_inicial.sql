-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: practicas_db
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `coordinadores`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coordinadores` (
  `rut` varchar(12) NOT NULL,
  PRIMARY KEY (`rut`),
  CONSTRAINT `FKnuv11h5oxnlyhdpap6jd9e4u1` FOREIGN KEY (`rut`) REFERENCES `usuarios` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `documentos`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `documentos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_subida` datetime(6) DEFAULT NULL,
  `nombre_archivo` varchar(255) NOT NULL,
  `numero_practica` int(11) DEFAULT NULL,
  `ruta_servidor` varchar(255) NOT NULL,
  `tipo_documento` enum('INFORME_FINAL','PAUTA','PLANIFICACION') NOT NULL,
  `estudiante_id` varchar(12) DEFAULT NULL,
  `practica_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf6vakxuvlt7n10qe8hx8khr64` (`estudiante_id`),
  KEY `FK519lmbf832u2vxuhbrw2vmh45` (`practica_id`),
  CONSTRAINT `FK519lmbf832u2vxuhbrw2vmh45` FOREIGN KEY (`practica_id`) REFERENCES `practicas` (`id`),
  CONSTRAINT `FKf6vakxuvlt7n10qe8hx8khr64` FOREIGN KEY (`estudiante_id`) REFERENCES `estudiantes` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `estudiantes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `estudiantes` (
  `ano_ingreso` int(11) DEFAULT NULL,
  `practica_actual` int(11) DEFAULT NULL,
  `rut` varchar(12) NOT NULL,
  `profesor_coordinador_rut` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`rut`),
  KEY `FKefgtmii7pjj7up40luhs9udpg` (`profesor_coordinador_rut`),
  CONSTRAINT `FK64ouxxmltht3ynynillk7e69y` FOREIGN KEY (`rut`) REFERENCES `usuarios` (`rut`),
  CONSTRAINT `FKefgtmii7pjj7up40luhs9udpg` FOREIGN KEY (`profesor_coordinador_rut`) REFERENCES `profesores` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `informes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `informes` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKkbwxjyq3o8j5g8mpvddktv1y6` FOREIGN KEY (`id`) REFERENCES `documentos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pautas`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pautas` (
  `observaciones` text DEFAULT NULL,
  `puntaje_obtenido` double DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKcqhm64f0quiak7p103y8nmacu` FOREIGN KEY (`id`) REFERENCES `documentos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `planificaciones`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `planificaciones` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKk251moydmnaqngaay0n9hs9ld` FOREIGN KEY (`id`) REFERENCES `documentos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `practicas`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `practicas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ano` int(11) DEFAULT NULL,
  `estado` enum('APROBADA','PENDIENTE','REPROBADA') NOT NULL,
  `fecha_fin` date DEFAULT NULL,
  `fecha_inicio` date DEFAULT NULL,
  `nota_aprobacion` double DEFAULT NULL,
  `numero_practica` int(11) DEFAULT NULL,
  `semestre` int(11) DEFAULT NULL,
  `rut_estudiante` varchar(12) DEFAULT NULL,
  `rut_profesor` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqn11a6lnrf91kbxh3rr4ldbj6` (`rut_estudiante`),
  KEY `FK13kj5kctogpilg9f22ak89mek` (`rut_profesor`),
  CONSTRAINT `FK13kj5kctogpilg9f22ak89mek` FOREIGN KEY (`rut_profesor`) REFERENCES `profesores` (`rut`),
  CONSTRAINT `FKqn11a6lnrf91kbxh3rr4ldbj6` FOREIGN KEY (`rut_estudiante`) REFERENCES `estudiantes` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `profesores`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profesores` (
  `rut` varchar(12) NOT NULL,
  PRIMARY KEY (`rut`),
  CONSTRAINT `FKsdm97wkqful5t8d38xyb82sy0` FOREIGN KEY (`rut`) REFERENCES `usuarios` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuarios`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usuarios` (
  `rut` varchar(12) NOT NULL,
  `apellido` varchar(255) DEFAULT NULL,
  `correo` varchar(255) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `rol` enum('COORDINADOR','ESTUDIANTE','PROFESOR') NOT NULL,
  PRIMARY KEY (`rut`),
  UNIQUE KEY `UKcdmw5hxlfj78uf4997i3qyyw5` (`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-16 23:53:10
