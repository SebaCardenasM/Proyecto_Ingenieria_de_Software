-- Ampliar columna estado_envio para soportar todos los estados de EstadoDocumento (BORRADOR, EN_REVISION, CON_OBSERVACIONES, ENVIADO, AUTORIZADO)
ALTER TABLE `documento` MODIFY COLUMN `estado_envio` VARCHAR(50) DEFAULT 'ENVIADO';
