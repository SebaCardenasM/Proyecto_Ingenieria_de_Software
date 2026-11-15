/* =========================================================
   Popup de confirmación - Prácticas UBB
   Ubicación: src/main/resources/static/js/ubb-popup.js

   Uso desde JavaScript:
     ubbPopup.exito('Mensaje', { titulo: '¡Listo!', onClose: () => location.reload() });
     ubbPopup.error('Mensaje');

   Uso desde Thymeleaf (mensajes flash del controlador):
     <div th:if="${mensajeExito}" data-ubb-flash hidden
          th:attr="data-mensaje=${mensajeExito},data-titulo=${tituloExito}"></div>
   ========================================================= */
(function () {
    'use strict';

    var ICONOS = {
        exito: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M14 27l8 8 16-17"/></svg>',
        error: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M18 18l16 16M34 18L18 34"/></svg>'
    };
    var TITULOS = { exito: '¡Operación exitosa!', error: 'No se pudo completar' };

    function mostrar(tipo, mensaje, opciones) {
        opciones = opciones || {};

        var previo = document.querySelector('.ubb-popup-overlay');
        if (previo) previo.remove();

        var overlay = document.createElement('div');
        overlay.className = 'ubb-popup-overlay';

        var caja = document.createElement('div');
        caja.className = 'ubb-popup ' + tipo;
        caja.setAttribute('role', 'alertdialog');
        caja.setAttribute('aria-modal', 'true');

        var icono = document.createElement('div');
        icono.className = 'ubb-popup-icon';
        icono.innerHTML = ICONOS[tipo];

        var titulo = document.createElement('h4');
        titulo.textContent = opciones.titulo || TITULOS[tipo];

        var texto = document.createElement('p');
        texto.textContent = mensaje || '';

        var boton = document.createElement('button');
        boton.type = 'button';
        boton.textContent = opciones.boton || 'Aceptar';

        caja.appendChild(icono);
        caja.appendChild(titulo);
        caja.appendChild(texto);
        caja.appendChild(boton);
        overlay.appendChild(caja);
        document.body.appendChild(overlay);

        var cerrado = false;
        function cerrar() {
            if (cerrado) return;
            cerrado = true;
            document.removeEventListener('keydown', teclas);
            overlay.classList.add('saliendo');
            setTimeout(function () {
                overlay.remove();
                if (typeof opciones.onClose === 'function') opciones.onClose();
            }, 200);
        }
        function teclas(e) {
            if (e.key === 'Escape' || e.key === 'Enter') { e.preventDefault(); cerrar(); }
        }

        boton.addEventListener('click', cerrar);
        overlay.addEventListener('click', function (e) { if (e.target === overlay) cerrar(); });
        document.addEventListener('keydown', teclas);
        boton.focus();
    }

    window.ubbPopup = {
        exito: function (mensaje, opciones) { mostrar('exito', mensaje, opciones); },
        error: function (mensaje, opciones) { mostrar('error', mensaje, opciones); }
    };

    // Mensajes enviados por el servidor (RedirectAttributes) -> se muestran al cargar la página
    document.addEventListener('DOMContentLoaded', function () {
        var flash = document.querySelector('[data-ubb-flash]');
        if (!flash || !flash.dataset.mensaje) return;
        var tipo = flash.dataset.tipo === 'error' ? 'error' : 'exito';
        window.ubbPopup[tipo](flash.dataset.mensaje, { titulo: flash.dataset.titulo || undefined });
    });
})();
