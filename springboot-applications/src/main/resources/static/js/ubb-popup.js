(function () {
    'use strict';
    if (window.ubbPopup) return;   // evita registrarlo dos veces si la página ya lo cargó

    var ICONOS = {
        exito: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M14 27l8 8 16-17"/></svg>',
        error: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M18 18l16 16M34 18L18 34"/></svg>',
        confirm: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M26 15v13M26 35v1"/></svg>'
    };
    var TITULOS = { exito: '¡Operación exitosa!', error: 'No se pudo completar', confirm: '¿Estás seguro?' };

    function mostrar(tipo, mensaje, opciones, resolver) {
        opciones = opciones || {};
        var esConfirm = tipo === 'confirm';

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

        var acciones = document.createElement('div');
        acciones.className = 'ubb-popup-actions';

        var cancelar = null;
        if (esConfirm) {
            cancelar = document.createElement('button');
            cancelar.type = 'button';
            cancelar.className = 'secundario';
            cancelar.textContent = opciones.cancelar || 'Cancelar';
            acciones.appendChild(cancelar);
        }
        var boton = document.createElement('button');
        boton.type = 'button';
        boton.textContent = opciones.boton || (esConfirm ? 'Confirmar' : 'Aceptar');
        acciones.appendChild(boton);

        caja.appendChild(icono);
        caja.appendChild(titulo);
        caja.appendChild(texto);
        caja.appendChild(acciones);
        overlay.appendChild(caja);
        document.body.appendChild(overlay);

        var cerrado = false;
        function cerrar(resultado) {
            if (cerrado) return;
            cerrado = true;
            document.removeEventListener('keydown', teclas);
            overlay.classList.add('saliendo');
            setTimeout(function () {
                overlay.remove();
                if (typeof opciones.onClose === 'function') opciones.onClose();
                if (resolver) resolver(resultado);
            }, 200);
        }
        function teclas(e) {
            if (e.key === 'Escape') { e.preventDefault(); cerrar(false); }
            else if (e.key === 'Enter') { e.preventDefault(); cerrar(true); }
        }

        boton.addEventListener('click', function () { cerrar(true); });
        if (cancelar) cancelar.addEventListener('click', function () { cerrar(false); });
        overlay.addEventListener('click', function (e) { if (e.target === overlay) cerrar(false); });
        document.addEventListener('keydown', teclas);
        boton.focus();
    }

    window.ubbPopup = {
        exito: function (mensaje, opciones) { mostrar('exito', mensaje, opciones); },
        error: function (mensaje, opciones) { mostrar('error', mensaje, opciones); },
        // Devuelve una Promise<boolean>: true si el usuario confirma
        confirmar: function (mensaje, opciones) {
            return new Promise(function (resolve) { mostrar('confirm', mensaje, opciones, resolve); });
        }
    };

    document.addEventListener('DOMContentLoaded', function () {
        var flash = document.querySelector('[data-ubb-flash]');
        if (!flash || !flash.dataset.mensaje) return;
        var tipo = flash.dataset.tipo === 'error' ? 'error' : 'exito';
        window.ubbPopup[tipo](flash.dataset.mensaje, { titulo: flash.dataset.titulo || undefined });
    });
})();