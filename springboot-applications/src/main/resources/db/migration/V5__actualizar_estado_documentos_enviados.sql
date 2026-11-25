-- Actualizar documentos generales e informes finales a ENVIADO para que aparezcan en el gestor
UPDATE `documento`
SET `estado_envio` = 'ENVIADO'
WHERE `tipo_documento` IN ('DOCUMENTO_GENERAL', 'INFORME_FINAL')
  AND (`estado_envio` IS NULL OR `estado_envio` = 'BORRADOR');
