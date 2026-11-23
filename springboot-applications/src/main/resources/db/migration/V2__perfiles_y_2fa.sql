-- Campos adicionales para perfiles de usuario y 2FA
ALTER TABLE `usuario` ADD COLUMN IF NOT EXISTS `correo_secundario` varchar(255) DEFAULT NULL;
ALTER TABLE `usuario` ADD COLUMN IF NOT EXISTS `ciudad` varchar(100) DEFAULT NULL;
ALTER TABLE `usuario` ADD COLUMN IF NOT EXISTS `biografia` text DEFAULT NULL;
ALTER TABLE `usuario` ADD COLUMN IF NOT EXISTS `two_factor_enabled` bit(1) NOT NULL DEFAULT b'0';
ALTER TABLE `usuario` ADD COLUMN IF NOT EXISTS `two_factor_secret` varchar(100) DEFAULT NULL;

-- Campos adicionales para perfil de estudiante
ALTER TABLE `estudiante` ADD COLUMN IF NOT EXISTS `carrera` varchar(255) DEFAULT 'Pedagogía en Educación Matemática';
ALTER TABLE `estudiante` ADD COLUMN IF NOT EXISTS `estado_academico` varchar(100) DEFAULT 'Regular';

-- Campos adicionales para perfil de profesor
ALTER TABLE `profesor` ADD COLUMN IF NOT EXISTS `departamento` varchar(255) DEFAULT 'Departamento de Ciencias de la Educación';
ALTER TABLE `profesor` ADD COLUMN IF NOT EXISTS `grado_academico` varchar(255) DEFAULT NULL;
ALTER TABLE `profesor` ADD COLUMN IF NOT EXISTS `oficina` varchar(100) DEFAULT NULL;
ALTER TABLE `profesor` ADD COLUMN IF NOT EXISTS `horario_atencion` varchar(255) DEFAULT NULL;
