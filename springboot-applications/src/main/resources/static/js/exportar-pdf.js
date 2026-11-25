/**
 * exportar-pdf.js - Utilidad oficial de exportación a PDF para listas y tablas en la Plataforma UBB
 * Permite descargar como PDF cualquier tabla o vista de datos respetando los filtros activos
 * (tanto filtros del servidor en la URL como filtros dinámicos de búsqueda en el cliente).
 */
(function() {
    'use strict';

    window.exportarTablaAPdf = function(tablaIdOSelector, titulo, opciones = {}) {
        let tabla = null;
        if (typeof tablaIdOSelector === 'string') {
            tabla = document.getElementById(tablaIdOSelector) || document.querySelector(tablaIdOSelector);
        } else if (tablaIdOSelector instanceof HTMLElement) {
            tabla = tablaIdOSelector;
        }

        if (!tabla) {
            console.error('No se encontró la tabla para exportar:', tablaIdOSelector);
            alert('No se encontró la tabla de datos a exportar.');
            return;
        }

        const tituloDoc = titulo || 'Reporte de Datos';
        const subtitulo = opciones.subtitulo || 'Escuela de Pedagogía en Educación Matemática - Universidad del Bío-Bío';

        // Detectar si hay algún filtro de búsqueda activo en la página
        let filtroAplicado = 'Sin filtros (Todos los registros)';
        if (opciones.filtro) {
            filtroAplicado = opciones.filtro;
        } else {
            const inputs = document.querySelectorAll('input[type="text"], input[type="search"], select');
            for (const input of inputs) {
                if (input.id && (input.id.toLowerCase().includes('buscar') || input.id.toLowerCase().includes('filtro') || input.id.toLowerCase().includes('estado'))) {
                    const val = input.value ? input.value.trim() : '';
                    if (val && val !== 'TODAS' && val !== 'TODOS') {
                        filtroAplicado = 'Filtro aplicado: "' + val + '"';
                        break;
                    }
                }
            }
        }

        // Clonar la tabla original para procesarla
        const tablaClon = tabla.cloneNode(true);

        // Identificar índice de columnas a excluir (ej. "Acciones", "Acción", "Aporte", o vacías)
        const thead = tablaClon.querySelector('thead');
        const theadOrig = tabla.querySelector('thead');
        const indicesExcluidos = new Set();
        const headersTexto = [];

        if (thead) {
            const ths = thead.querySelectorAll('th');
            ths.forEach((th, idx) => {
                const txt = th.textContent.trim().toLowerCase();
                headersTexto.push(txt);
                // Excluir columnas de acciones y la columna APORTE requerida por el usuario
                if (txt === 'acciones' || txt === 'acción' || txt === 'accion' || txt === 'opciones' || txt === 'aporte' || txt === '') {
                    indicesExcluidos.add(idx);
                }
            });

            // Remover del encabezado clonado las columnas excluidas (de derecha a izquierda para no alterar índices previos)
            for (let i = ths.length - 1; i >= 0; i--) {
                if (indicesExcluidos.has(i)) {
                    ths[i].remove();
                }
            }
        }

        // Procesar las filas del cuerpo: incluir SOLO filas visibles
        const filasOriginales = Array.from(tabla.querySelectorAll('tbody tr'));
        const filasClon = Array.from(tablaClon.querySelectorAll('tbody tr'));
        let totalFilasVisibles = 0;

        for (let i = 0; i < filasClon.length; i++) {
            const filaClon = filasClon[i];
            const filaOrig = filasOriginales[i];

            // Si la fila original está oculta (por búsqueda, d-none, hidden, style display none)
            if (filaOrig) {
                const style = window.getComputedStyle(filaOrig);
                const estaOculta = filaOrig.classList.contains('d-none')
                    || filaOrig.hidden
                    || filaOrig.style.display === 'none'
                    || style.display === 'none';

                if (estaOculta) {
                    filaClon.remove();
                    continue;
                }
            }

            // Ignorar fila si es el mensaje de "No hay registros..."
            const celdasOrig = filaOrig ? Array.from(filaOrig.querySelectorAll('td')) : [];
            const celdasClon = Array.from(filaClon.querySelectorAll('td'));

            if (celdasClon.length === 1 && celdasClon[0].getAttribute('colspan')) {
                continue;
            }

            // Procesar cada celda y formatear columnas especiales antes de eliminar excluidas
            for (let j = 0; j < celdasClon.length; j++) {
                const tdClon = celdasClon[j];
                const tdOrig = celdasOrig[j];
                const colHeader = headersTexto[j] || '';

                if (tdOrig) {
                    // Columna 1: Autorizar clase / Planificación autorizada
                    if (colHeader.includes('autorizar')) {
                        const esAutorizada = filaOrig.getAttribute('data-estado') === 'AUTORIZADO'
                            || tdOrig.querySelector('.tick-ok') !== null
                            || tdOrig.textContent.toLowerCase().includes('autorizada');
                        const conObs = filaOrig.getAttribute('data-estado') === 'CON_OBSERVACIONES'
                            || tdOrig.textContent.toLowerCase().includes('observacion')
                            || tdOrig.textContent.toLowerCase().includes('esperando');

                        if (esAutorizada) {
                            tdClon.innerHTML = '<span style="font-weight: 700; color: #198754;">Autorizada</span>';
                        } else if (conObs) {
                            tdClon.innerHTML = '<span style="font-weight: 600; color: #dc3545;">Con observaciones</span>';
                        } else {
                            tdClon.innerHTML = '<span style="color: #6c757d;">Por autorizar</span>';
                        }
                    }
                    // Columna 2: Evaluar con pautas / Pautas enviadas
                    else if (colHeader.includes('pautas') || colHeader.includes('evaluar')) {
                        const conPautas = filaOrig.getAttribute('data-pautas') === 'true'
                            || tdOrig.querySelector('.tick-ok') !== null
                            || tdOrig.textContent.toLowerCase().includes('pauta enviada');
                        const esAutorizada = filaOrig.getAttribute('data-estado') === 'AUTORIZADO'
                            || tdOrig.querySelector('.tick-btn') !== null;

                        if (conPautas) {
                            tdClon.innerHTML = '<span style="font-weight: 700; color: #198754;">Pauta enviada</span>';
                        } else if (esAutorizada) {
                            tdClon.innerHTML = '<span style="color: #6c757d;">Sin pautas</span>';
                        } else {
                            tdClon.innerHTML = '<span style="color: #adb5bd;">—</span>';
                        }
                    } else {
                        // Limpiar botones, formularios o enlaces de acción
                        tdClon.querySelectorAll('button, a.btn, .btn, .dropdown, input[type="checkbox"], form').forEach(el => el.remove());
                    }
                }
            }

            // Remover celdas de columnas excluidas (acciones, aporte) de derecha a izquierda
            for (let j = celdasClon.length - 1; j >= 0; j--) {
                if (indicesExcluidos.has(j)) {
                    celdasClon[j].remove();
                }
            }

            totalFilasVisibles++;
        }

        // Remover en toda la tabla clonada cualquier recuadro, borde redondeado o fondo de badges/chips
        tablaClon.querySelectorAll('.badge, .chip, [class*="badge-"], [class*="chip-"], .coment-box').forEach(el => {
            el.style.border = 'none';
            el.style.borderRadius = '0';
            el.style.background = 'transparent';
            el.style.padding = '0';
            el.style.boxShadow = 'none';
            el.style.fontWeight = '600';
            el.style.color = '#212529';
        });

        // Si la tabla tiene muchas columnas (> 6), usar orientación horizontal
        const totalColumnas = thead ? thead.querySelectorAll('th').length : 5;
        const orientacion = totalColumnas > 6 ? 'landscape' : 'portrait';

        // Fecha actual formateada
        const ahora = new Date();
        const fechaHora = ahora.toLocaleDateString('es-CL', {
            day: '2-digit', month: '2-digit', year: 'numeric'
        }) + ' ' + ahora.toLocaleTimeString('es-CL', {
            hour: '2-digit', minute: '2-digit'
        });

        // Crear documento HTML para impresión
        const html = `
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>${tituloDoc}</title>
    <style>
        @page {
            size: ${orientacion};
            margin: 12mm 15mm;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            color: #212529;
            background: #fff;
            margin: 0;
            padding: 10px;
            font-size: 10.5pt;
        }
        .header {
            border-bottom: 2px solid #004b85;
            padding-bottom: 8px;
            margin-bottom: 15px;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
        }
        .header-title h1 {
            font-size: 15pt;
            margin: 0 0 4px 0;
            color: #004b85;
            font-weight: 700;
        }
        .header-title p {
            margin: 0;
            font-size: 8.5pt;
            color: #6c757d;
            font-weight: 600;
            text-transform: uppercase;
        }
        .header-meta {
            text-align: right;
            font-size: 8.5pt;
            color: #6c757d;
        }
        .info-strip {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 4px;
            padding: 6px 10px;
            margin-bottom: 15px;
            font-size: 8.5pt;
            display: flex;
            justify-content: space-between;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
            font-size: 9pt;
        }
        th, td {
            border: 1px solid #ced4da;
            padding: 6px 8px;
            vertical-align: middle;
        }
        th {
            background-color: #004b85 !important;
            color: #ffffff !important;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
            font-weight: 600;
            text-align: center;
        }
        tr:nth-child(even) td {
            background-color: #f8f9fa !important;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
        }
        tr {
            page-break-inside: avoid;
        }
        /* Eliminar cualquier recuadro redondo alrededor de texto en todas las exportaciones */
        .badge, .chip, [class*="badge-"], [class*="chip-"], .coment-box {
            display: inline !important;
            padding: 0 !important;
            font-size: inherit !important;
            font-weight: 600 !important;
            border: none !important;
            border-radius: 0 !important;
            background: transparent !important;
            box-shadow: none !important;
            color: inherit !important;
        }
        .footer {
            margin-top: 25px;
            border-top: 1px solid #dee2e6;
            padding-top: 8px;
            display: flex;
            justify-content: space-between;
            font-size: 8pt;
            color: #868e96;
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="header-title">
            <h1>${tituloDoc}</h1>
            <p>${subtitulo}</p>
        </div>
        <div class="header-meta">
            <div>Fecha de emisión: <strong>${fechaHora}</strong></div>
            <div>Universidad del Bío-Bío</div>
        </div>
    </div>

    <div class="info-strip">
        <div><strong>Estado / Criterio:</strong> ${filtroAplicado}</div>
        <div><strong>Total de registros en reporte:</strong> ${totalFilasVisibles}</div>
    </div>

    ${tablaClon.outerHTML}

    <div class="footer">
        <div>Plataforma Oficial de Prácticas Pedagógicas — Pedagogía en Educación Matemática UBB</div>
        <div>Documento para respaldo físico y registro administrativo</div>
    </div>
</body>
</html>
        `;

        // Crear iframe oculto para lanzar la impresión limpia
        let iframe = document.getElementById('iframe-impresion-pdf');
        if (!iframe) {
            iframe = document.createElement('iframe');
            iframe.id = 'iframe-impresion-pdf';
            iframe.style.position = 'fixed';
            iframe.style.right = '0';
            iframe.style.bottom = '0';
            iframe.style.width = '0';
            iframe.style.height = '0';
            iframe.style.border = 'none';
            document.body.appendChild(iframe);
        }

        const doc = iframe.contentWindow.document;
        doc.open();
        doc.write(html);
        doc.close();

        // Esperar a que el contenido se cargue en el iframe y disparar la ventana de PDF/Impresión
        setTimeout(() => {
            iframe.contentWindow.focus();
            iframe.contentWindow.print();
        }, 300);
    };
})();
