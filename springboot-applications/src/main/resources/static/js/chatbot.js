/* Chatbot UBB: consultas + cadena de revisión (borrador -> revisión IA -> gestor) */
(function () {
    'use strict';

    const root = document.getElementById('ubb-chatbot');
    if (!root) return;

    const conArchivos = root.dataset.archivos === 'true';
    const obligatoria = root.dataset.obligatoria === 'true';
    const $ = (id) => document.getElementById(id);

    // ---------------------------------------------------------------- utilidades
    const esc = (v) => String(v ?? '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');

    const inline = (s) => s.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    const confirmar = (msg, o) => window.ubbPopup ? window.ubbPopup.confirmar(msg, o) : Promise.resolve(confirm(msg));

    // Texto de la IA -> HTML seguro (escapa primero; soporta **negrita** y viñetas)
    function formatear(texto) {
        let html = '', enLista = false;
        for (const linea of esc(texto).split(/\r?\n/)) {
            const m = linea.match(/^\s*[-*•]\s+(.*)$/);
            if (m) {
                if (!enLista) { html += '<ul>'; enLista = true; }
                html += '<li>' + inline(m[1]) + '</li>';
            } else {
                if (enLista) { html += '</ul>'; enLista = false; }
                if (linea.trim()) html += '<p>' + inline(linea) + '</p>';
            }
        }
        return enLista ? html + '</ul>' : html;
    }

    async function api(url, opciones = {}) {
        const headers = Object.assign({ 'Accept': 'application/json' }, opciones.headers || {});
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const cabecera = document.querySelector('meta[name="_csrf_header"]')?.content;
        if (token && cabecera) headers[cabecera] = token;
        return fetch(url, Object.assign({}, opciones, { headers }));
    }

    // ---------------------------------------------------------------- panel
    const panel = $('ubbChatPanel');

        let historialCargado = false;

    async function cargarHistorial() {
        if (historialCargado) return;
        historialCargado = true;
        try {
            const res = await api('/api/chat/historial');
            const lista = res.ok ? await res.json() : [];
            if (!lista.length) return;

            $('ubbChatChips').hidden = true;
            const sep = document.createElement('div');
            sep.className = 'ubb-sep';
            sep.textContent = 'Conversaciones anteriores';
            msgs.appendChild(sep);

            lista.forEach(m => {
                agregarMensaje('user', esc(m.pregunta));
                agregarMensaje('bot', formatear(m.respuesta));
            });
        } catch (e) { /* sin historial: el chat funciona igual */ }
    }

    async function abrir() {
        panel.hidden = false;
        $('ubbChatFab').hidden = true;
        if (!$('ubbChatMsgs').childElementCount) {
            await saludar();
            await cargarHistorial();
        }
    }
    function cerrar() { panel.hidden = true; $('ubbChatFab').hidden = false; }
    $('ubbChatFab').addEventListener('click', abrir);
    $('ubbChatClose').addEventListener('click', cerrar);
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !panel.hidden && !document.querySelector('.ubb-popup-overlay')) cerrar();
    });

    function activarTab(nombre) {
        root.querySelectorAll('[data-tab]').forEach(b => b.classList.toggle('active', b.dataset.tab === nombre));
        root.querySelectorAll('[data-panel]').forEach(p => { p.hidden = p.dataset.panel !== nombre; });
    }
    root.querySelectorAll('[data-tab]').forEach(b => b.addEventListener('click', () => {
        activarTab(b.dataset.tab);
        if (b.dataset.tab === 'revision') cargarBorradores();
    }));

    // ---------------------------------------------------------------- consultas
    const msgs = $('ubbChatMsgs');

    function agregarMensaje(rol, html, extra = '') {
        const div = document.createElement('div');
        div.className = 'ubb-msg ' + rol + (extra ? ' ' + extra : '');
        div.innerHTML = html;
        msgs.appendChild(div);
        msgs.scrollTop = msgs.scrollHeight;
        return div;
    }

    async function saludar() {
        let saludo = '<p>¡Hola! Soy <strong>RetroPlani Mate</strong>, tu asistente pedagógico en Educación Matemática (UBB).</p>';
        let preguntas = '';
        try {
            const resp = await api('/api/chat/configuracion-inicial');
            if (resp.ok) {
                const data = await resp.json();
                if (data.saludo) saludo = formatear(data.saludo);
                if (data.preguntas) preguntas = data.preguntas;
            }
        } catch (e) { /* fallback si falla la red */ }

        let html = saludo;
        if (preguntas) {
            html += '<div class="ubb-chat-preguntas-box mt-2 p-2 rounded bg-light border text-dark" style="font-size:0.85rem;">'
                + '<strong><i class="bi bi-patch-question-fill text-warning me-1"></i>Preguntas Diagnósticas Previas:</strong>'
                + '<p class="text-muted small mb-1">Al revisar tu planificación o diseñar tu clase, reflexiona sobre estas preguntas clave:</p>'
                + formatear(preguntas)
                + '</div>';
        }
        if (conArchivos) {
            html += '<p class="mt-2 mb-0">Antes de enviar una planificación o informe al gestor, puedes pedirme una <strong>revisión previa</strong> en la pestaña «Revisar documento».</p>';
        }
        agregarMensaje('bot', html);
    }

    async function preguntar(texto) {
        texto = texto.trim();
        if (!texto) return;
        $('ubbChatChips').hidden = true;
        agregarMensaje('user', esc(texto));
        const espera = agregarMensaje('bot', '<span class="ubb-typing"><i></i><i></i><i></i></span>');

        try {
            const res = await api('/api/chat/preguntar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ mensaje: texto })
            });
            const data = await res.json();
            espera.innerHTML = formatear(data.respuesta || 'No obtuve respuesta.');
            if (!res.ok) espera.classList.add('error');
        } catch (e) {
            espera.innerHTML = 'No pude conectar con el asistente. Revisa tu conexión o vuelve a iniciar sesión.';
            espera.classList.add('error');
        }
        msgs.scrollTop = msgs.scrollHeight;
    }

    $('ubbChatForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const input = $('ubbChatInput');
        const texto = input.value;
        input.value = '';
        preguntar(texto);
    });
    $('ubbChatChips').addEventListener('click', (e) => {
        const b = e.target.closest('[data-q]');
        if (b) preguntar(b.dataset.q);
    });

    // ---------------------------------------------------------------- revisión de documentos
    const state = { borradores: [], actual: null, revisiones: {}, enviado: false };
    const API = { abrirRevision: () => {}, obligatoria };
    window.ubbChatbot = API;

    if (conArchivos) {
        iniciarRevision();
    }

    function iniciarRevision() {
        $('rvIntro').textContent = obligatoria
            ? 'Paso obligatorio: tu borrador debe pasar por la revisión del asistente antes de enviarse al gestor.'
            : 'La revisión del asistente es opcional: úsala para mejorar tu documento antes de enviarlo al gestor.';

        $('rvBorradores').addEventListener('change', (e) => seleccionar(Number(e.target.value)));
        $('rvRevisar').addEventListener('click', revisar);
        $('rvEnviar').addEventListener('click', enviar);
        $('rvDescartar').addEventListener('click', descartar);
        $('rvSubir').addEventListener('click', subir);

        const archivo = $('rvArchivo');
        archivo.addEventListener('change', () => {
            $('rvArchivoNombre').textContent = archivo.files[0] ? archivo.files[0].name : 'Arrastra un archivo o haz clic para elegirlo';
        });
        const drop = root.querySelector('.rv-drop');
        ['dragenter', 'dragover'].forEach(ev => drop.addEventListener(ev, () => drop.classList.add('sobre')));
        ['dragleave', 'drop'].forEach(ev => drop.addEventListener(ev, () => drop.classList.remove('sobre')));

        cargarBorradores();   // para el contador del botón flotante
    }

    const TIPOS = { PLANIFICACION: 'Planificación', INFORME_FINAL: 'Informe final' };

    function mensaje(texto, tipo) {
        const el = $('rvMsg');
        if (!texto) { el.hidden = true; return; }
        el.className = 'rv-msg ' + (tipo === 'ok' ? 'ok' : 'error');
        el.textContent = texto;
        el.hidden = false;
    }

    function pasos() {
        let activo = 1;
        if (state.enviado) activo = 4;
        else if (state.actual) activo = state.actual.revisadoIa ? 3 : 2;
        root.querySelectorAll('#rvSteps li').forEach(li => {
            const n = Number(li.dataset.step);
            li.classList.toggle('done', n < activo);
            li.classList.toggle('active', n === activo);
        });
    }

    function actualizarBadge() {
        const badge = $('ubbChatBadge');
        badge.textContent = state.borradores.length;
        badge.hidden = state.borradores.length === 0;
    }

    async function cargarBorradores(idASeleccionar) {
        try {
            const res = await api('/api/documentos/mis-documentos');
            const lista = (res.ok && res.status !== 204) ? await res.json() : [];
            state.borradores = lista.filter(d => d.estado === 'BORRADOR');
        } catch (e) {
            state.borradores = [];
        }
        actualizarBadge();
        if (!$('rvBorradores')) return;

        const sel = $('rvBorradores');
        sel.innerHTML = state.borradores
            .map(d => `<option value="${d.id}">${esc(d.nombreArchivo)} · P${esc(d.numeroPractica ?? '?')}</option>`)
            .join('');

        const vacio = state.borradores.length === 0;
        $('rvVacio').hidden = !vacio;
        sel.hidden = vacio;
        root.querySelector('label[for="rvBorradores"]').hidden = vacio;

        if (vacio) { state.actual = null; $('rvCard').hidden = true; pasos(); return; }

        const id = state.borradores.some(d => d.id === idASeleccionar)
            ? idASeleccionar
            : (state.actual && state.borradores.some(d => d.id === state.actual.id) ? state.actual.id : state.borradores[0].id);
        sel.value = String(id);
        seleccionar(id);
    }

    function seleccionar(id) {
        state.actual = state.borradores.find(d => d.id === id) || null;
        state.enviado = false;
        pintarTarjeta();
    }

    function pintarTarjeta() {
        const d = state.actual;
        $('rvCard').hidden = !d;
        pasos();
        if (!d) return;

        $('rvNombre').textContent = d.nombreArchivo;
        $('rvMeta').textContent = (TIPOS[d.tipoDocumento] || d.tipoDocumento) + ' · Práctica ' + (d.numeroPractica ?? '?')
            + (d.revisadoIa ? ' · Revisado por el asistente' : '');

        const revision = state.revisiones[d.id];
        $('rvResultado').hidden = !revision;
        if (revision) $('rvResultado').innerHTML = formatear(revision);

        $('rvRevisar').innerHTML = d.revisadoIa
            ? '<i class="bi bi-arrow-repeat me-1"></i>Volver a revisar'
            : '<i class="bi bi-robot me-1"></i>Revisar con el asistente';

        const bloqueado = obligatoria && !d.revisadoIa && /\.(pdf|docx)$/i.test(d.rutaServidor || '');
        $('rvEnviar').disabled = bloqueado;
        $('rvNota').textContent = bloqueado
            ? 'Debes revisar el documento con el asistente antes de enviarlo.'
            : (d.revisadoIa
                ? 'Listo: puedes enviarlo al gestor o corregirlo y subir una nueva versión.'
                : 'Puedes enviarlo directamente o revisarlo primero con el asistente.');
    }

    function ocupado(si, texto) {
        $('rvCargando').hidden = !si;
        if (texto) $('rvCargandoTxt').textContent = texto;
        ['rvRevisar', 'rvEnviar', 'rvDescartar', 'rvSubir'].forEach(id => { $(id).disabled = si; });
        if (!si) pintarTarjeta();
    }

    async function revisar() {
        const d = state.actual;
        if (!d) return;
        mensaje('');
        ocupado(true, 'El asistente está leyendo tu documento…');
        try {
            const res = await api('/api/chat/revisar/' + d.id, { method: 'POST' });
            const data = await res.json().catch(() => ({}));
            if (!res.ok) {
                ocupado(false);
                mensaje(data.respuesta || 'No se pudo revisar el documento.', 'error');
                return;
            }
            state.revisiones[d.id] = data.respuesta;
            d.revisadoIa = true;
            ocupado(false);
            notificarCambio();
        } catch (e) {
            ocupado(false);
            mensaje('Error de conexión con el servidor.', 'error');
        }
    }

    async function enviar() {
        const d = state.actual;
        if (!d) return;
        const esPlan = d.tipoDocumento === 'PLANIFICACION';
        if (!(await confirmar(
            esPlan ? '¿Enviar «' + d.nombreArchivo + '» a tu profesor para revisión?\nNo podrás modificarla mientras la revisa.'
                   : '¿Enviar «' + d.nombreArchivo + '» al gestor?\nTus profesores podrán verlo y ya no podrás editarlo.',
            { titulo: esPlan ? 'Enviar al profesor' : 'Enviar al gestor', boton: 'Sí, enviar' }))) return;
        mensaje('');
        ocupado(true, esPlan ? 'Enviando a tu profesor…' : 'Enviando al gestor…');
        try {
            const res = await api('/api/documentos/enviar/' + d.id, { method: 'POST' });
            if (!res.ok) {
                ocupado(false);
                mensaje(await res.text() || 'No se pudo enviar el documento.', 'error');
                return;
            }
            delete state.revisiones[d.id];
            state.actual = null;
            ocupado(false);
            await cargarBorradores();
            state.enviado = true;
            pasos();
            mensaje(esPlan ? '¡Planificación enviada a tu profesor!' : '¡Documento enviado al gestor!', 'ok');
            notificarCambio();
        } catch (e) {
            ocupado(false);
            mensaje('Error de conexión con el servidor.', 'error');
        }
    }

    async function descartar() {
        const d = state.actual;
        if (!d) return;
        if (!(await confirmar('¿Descartar el borrador «' + d.nombreArchivo + '»? Se eliminará el archivo.',
        { titulo: 'Descartar borrador', boton: 'Sí, descartar' }))) return;
        mensaje('');
        try {
            const res = await api('/api/documentos/eliminar/' + d.id, { method: 'POST' });
            if (!res.ok) { mensaje(await res.text() || 'No se pudo descartar.', 'error'); return; }
            delete state.revisiones[d.id];
            state.actual = null;
            await cargarBorradores();
            mensaje('Borrador descartado. Puedes subir una nueva versión.', 'ok');
            notificarCambio();
        } catch (e) {
            mensaje('Error de conexión con el servidor.', 'error');
        }
    }

    async function subir() {
        const archivo = $('rvArchivo').files[0];
        const tipo = $('rvTipo').value;
        const numero = $('rvPractica').value;
        if (!numero || !tipo || !archivo) {
            mensaje('Elige la práctica, el tipo de documento y un archivo.', 'error');
            return;
        }
        mensaje('');
        const fd = new FormData();
        fd.append('archivo', archivo);
        fd.append('tipoDocumento', tipo);
        fd.append('numeroPractica', numero);

        ocupado(true, 'Subiendo archivo…');
        try {
            const res = await api('/api/documentos/subir', { method: 'POST', body: fd });
            if (!res.ok) {
                ocupado(false);
                mensaje(await res.text() || 'No se pudo subir el archivo.', 'error');
                return;
            }
            const doc = await res.json();
            $('rvArchivo').value = '';
            $('rvArchivoNombre').textContent = 'Arrastra un archivo o haz clic para elegirlo';
            $('rvSubirBox').open = false;
            ocupado(false);
            await cargarBorradores(doc.id);
            mensaje('Archivo guardado como borrador. Puedes revisarlo con el asistente antes de enviarlo.', 'ok');
            notificarCambio();
        } catch (e) {
            ocupado(false);
            mensaje('Error de conexión con el servidor.', 'error');
        }
    }

    function notificarCambio() {
        document.dispatchEvent(new CustomEvent('ubb:documentos-cambiaron'));
    }

    // API pública: la página del gestor la usa para abrir el asistente sobre un borrador
    API.abrirRevision = function (idDocumento) {
        if (!conArchivos) return;
        abrir();
        activarTab('revision');
        mensaje('');
        cargarBorradores(Number(idDocumento));
    };
})();