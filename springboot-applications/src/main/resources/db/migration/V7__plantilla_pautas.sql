-- V7: Tabla para la personalización y edición de formularios web de pautas (Especialidad y Orientación)
CREATE TABLE IF NOT EXISTS plantilla_pauta (
    tipo_pauta VARCHAR(50) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500) NULL,
    esquema_json MEDIUMTEXT NOT NULL,
    esquema_borrador_json MEDIUMTEXT NULL,
    tiene_borrador BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_actualizacion DATETIME NULL,
    actualizado_por VARCHAR(100) NULL
);

-- Insertar configuración inicial por defecto para ESPECIALIDAD
INSERT INTO plantilla_pauta (tipo_pauta, titulo, descripcion, esquema_json, esquema_borrador_json, tiene_borrador, fecha_actualizacion, actualizado_por)
VALUES (
    'ESPECIALIDAD',
    'Pauta de Observación - Especialidad / Matemática',
    'Formulario web de observación de clases para prácticas en el área de especialidad matemática.',
    '[{"titulo":"ASPECTOS DE LA ESTRUCTURA DE LA CLASE","items":["El objetivo de la clase se presenta a los estudiantes. Es claro y pertinente al nivel.","El profesor/a desarrolla un inicio de la clase generando retroalimentación y/o activación de conocimientos previos.","El profesor/a realiza un desarrollo de la clase de manera pertinente al objetivo planteado.","El profesor/a realiza un desarrollo de la clase con tareas matemáticas pertinentes y coherentes con el objetivo planteado.","El profesor/a realiza un cierre de la clase con actividad(es) clave(s) para la verificación del cumplimiento del objetivo planteado.","El profesor/a realiza un cierre de la clase integrando y/o sintetizando los aprendizajes de la clase."]},{"titulo":"TAREAS MATEMÁTICAS PROPUESTAS","items":["El profesor/a hace uso de los errores y dificultades de los estudiantes como una instancia de devolución.","El profesor/a propone tareas matemáticas que se relacionen con alguna de las habilidades de: resolución de problemas, representación, modelación y/o argumentación y comunicación.","Las tareas matemáticas están contextualizadas al entorno de los estudiantes.","El profesor/a propone variadas estrategias de resolución de las tareas matemáticas presentadas.","El profesor/a explica, comprueba y/o demuestra las definiciones, teoremas, proposiciones o procedimientos de manera clara y adecuadas al nivel educativo.","El profesor/a utiliza estrategias de trabajo colaborativo para las/os estudiantes en las actividades de la clase."]},{"titulo":"AMBIENTE DE AULA Y RECURSOS DE APRENDIZAJE","items":["El profesor/a hace uso de un lenguaje acorde al nivel de enseñanza.","El profesor/a genera oportunidades de participación de los estudiantes durante toda la clase.","El profesor/a propicia un ambiente para el desarrollo de toda la clase.","El profesor/a brinda atención a todos los estudiantes del aula.","El profesor/a hace uso de materiales y/o recursos para el desarrollo de los aprendizajes en la clase.","El profesor/a promueve la autoestima, evitando el rechazo, fobia o miedo a la matemática.","El profesor/a promueve el diálogo y comunicación entre estudiantes favoreciendo la inclusión en la clase.","El profesor/a realiza observación sistemática del proceso cognitivo de los estudiantes durante toda la clase."]}]',
    NULL,
    FALSE,
    NOW(),
    'SISTEMA'
) ON DUPLICATE KEY UPDATE tipo_pauta=tipo_pauta;

-- Insertar configuración inicial por defecto para ORIENTACION
INSERT INTO plantilla_pauta (tipo_pauta, titulo, descripcion, esquema_json, esquema_borrador_json, tiene_borrador, fecha_actualizacion, actualizado_por)
VALUES (
    'ORIENTACION',
    'Pauta de Observación - Orientación',
    'Formulario web de observación de clases para prácticas en el área de orientación pedagógica.',
    '[{"titulo":"DIMENSIÓN A: DISEÑO DE LA CLASE Y PLANIFICACIÓN DE LA ENSEÑANZA","items":["Comunica a las/os estudiantes los propósitos de la clase y los aprendizajes a lograr.","Utiliza el tiempo disponible para la enseñanza en función de los objetivos de la clase.","Especifica en las unidades pedagógicas de orientación, los tres momentos de la clase: inicio, desarrollo y cierre."]},{"titulo":"DIMENSIÓN B: GENERACIÓN DE AMBIENTE PROPICIO PARA EL APRENDIZAJE","items":["Formula preguntas y concede el tiempo necesario para resolverlos.","Posee la capacidad de empatía, de atención personalizada propiciada por la clase y escucha activamente a los y las estudiantes.","Establece un clima de relaciones interpersonales respetuosas y empáticas con sus estudiantes.","Establece normas de comportamiento que son conocidas y comprensibles para sus estudiantes."]},{"titulo":"DIMENSIÓN C: USO DE METODOLOGÍAS Y DIDÁCTICA PARA LA ENSEÑANZA","items":["Utiliza diferentes metodologías pedagógicas, que motiven el interés de los y las estudiantes durante las clases.","Ofrece oportunidades y desafíos de participación activas a los y las estudiantes del grupo curso, considerando su diversidad.","Demuestra conocer variadas estrategias de enseñanza y actividades congruentes con el proceso de enseñanza y aprendizaje."]},{"titulo":"DIMENSIÓN D: EVALUACIÓN DE LOS APRENDIZAJES","items":["Emplea instrumentos de evaluación, acorde a las temáticas desarrolladas en el grupo curso.","Utiliza criterios de evaluación coherentes con los objetivos de aprendizaje propuestos.","Evalúa el grado en que las/os estudiantes alcanzaron los aprendizajes esperados."]},{"titulo":"DIMENSIÓN E: EVALUACIÓN DE SU DESEMPEÑO","items":["Reflexiona sobre la propia práctica pedagógica, considerando la capacidad de preparar y gestionar curricularmente sus clases en el área de orientación.","Evalúa el nivel de logro de los objetivos de aprendizaje planteados, valorando los aciertos y analizando los errores.","Evidencia el cumplimiento de los compromisos contraídos, presentación personal, puntualidad y responsabilidad profesional.","Analiza críticamente su desempeño durante las clases, a partir de los resultados de aprendizaje de las y los estudiantes.","Presenta anticipadamente la planificación de la clase, de acuerdo a la unidad didáctica, para su debida retroalimentación."]}]',
    NULL,
    FALSE,
    NOW(),
    'SISTEMA'
) ON DUPLICATE KEY UPDATE tipo_pauta=tipo_pauta;
