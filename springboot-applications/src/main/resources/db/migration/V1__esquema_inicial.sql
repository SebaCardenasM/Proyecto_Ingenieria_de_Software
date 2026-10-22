CREATE TABLE `usuario` (
  `rut` varchar(12) NOT NULL,
  `apellido` varchar(255) DEFAULT NULL,
  `correo` varchar(255) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `rol` enum('COORDINADOR','ESTUDIANTE','PROFESOR') NOT NULL,
  PRIMARY KEY (`rut`),
  UNIQUE KEY `UK2mlfr087gb1ce55f2j87o74t` (`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `profesor` (
  `rut` varchar(12) NOT NULL,
  PRIMARY KEY (`rut`),
  CONSTRAINT `FKg65ew6ss7ehvg89dmq2yibtw0` FOREIGN KEY (`rut`) REFERENCES `usuario` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `coordinador` (
  `rut` varchar(12) NOT NULL,
  PRIMARY KEY (`rut`),
  CONSTRAINT `FKgi0puqmi7gvmiftjjyc0eycmt` FOREIGN KEY (`rut`) REFERENCES `usuario` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `estudiante` (
  `ano_ingreso` int(11) DEFAULT NULL,
  `practica_actual` int(11) DEFAULT NULL,
  `rut` varchar(12) NOT NULL,
  `profesor_coordinador_rut` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`rut`),
  KEY `FKiygvpdt9vtpp0rpykworvqe25` (`profesor_coordinador_rut`),
  CONSTRAINT `FKfxlisfcitmmqgm5mjqb8qkddr` FOREIGN KEY (`rut`) REFERENCES `usuario` (`rut`),
  CONSTRAINT `FKiygvpdt9vtpp0rpykworvqe25` FOREIGN KEY (`profesor_coordinador_rut`) REFERENCES `profesor` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `practica` (
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
  KEY `FKarbjtfqjsod9p4ddv152ob943` (`rut_estudiante`),
  KEY `FKrswqr6uryijjm887ujq6ayaqw` (`rut_profesor`),
  CONSTRAINT `FKarbjtfqjsod9p4ddv152ob943` FOREIGN KEY (`rut_estudiante`) REFERENCES `estudiante` (`rut`),
  CONSTRAINT `FKrswqr6uryijjm887ujq6ayaqw` FOREIGN KEY (`rut_profesor`) REFERENCES `profesor` (`rut`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `documento` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_subida` datetime(6) DEFAULT NULL,
  `nombre_archivo` varchar(255) NOT NULL,
  `numero_practica` int(11) DEFAULT NULL,
  `ruta_servidor` varchar(255) NOT NULL,
  `tipo_documento` enum('INFORME_FINAL','PAUTA','PLANIFICACION') NOT NULL,
  `estudiante_id` varchar(12) DEFAULT NULL,
  `practica_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK67cewaver21c795ljaejamhsh` (`estudiante_id`),
  KEY `FKf3qgkdd9q2vvprcvbrmtnfaio` (`practica_id`),
  CONSTRAINT `FK67cewaver21c795ljaejamhsh` FOREIGN KEY (`estudiante_id`) REFERENCES `estudiante` (`rut`),
  CONSTRAINT `FKf3qgkdd9q2vvprcvbrmtnfaio` FOREIGN KEY (`practica_id`) REFERENCES `practica` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `informe` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKoms0u4ismxx0of74gink9bmvh` FOREIGN KEY (`id`) REFERENCES `documento` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `pauta` (
  `observaciones` text DEFAULT NULL,
  `puntaje_obtenido` double DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK44wv53pb2jq4s6t1cqscjga9e` FOREIGN KEY (`id`) REFERENCES `documento` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `planificacion` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK5hofxx6tplvqgucp31gcog1gl` FOREIGN KEY (`id`) REFERENCES `documento` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;