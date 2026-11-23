(function () {
    'use strict';
    if (window.ubbPopup) return;   // Evita registrarlo dos veces si la página ya lo cargó

    var ICONOS = {
        exito: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M14 27l8 8 16-17"/></svg>',
        error: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M18 18l16 16M34 18L18 34"/></svg>',
        confirm: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M20.5 19a5.5 5.5 0 0 1 10 2.5c0 3.2-4.5 4.5-4.5 8.5M26 36v.5"/></svg>',
        aviso: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M26 15v13M26 35v1"/></svg>',
        info: '<svg viewBox="0 0 52 52" aria-hidden="true"><circle class="c" cx="26" cy="26" r="24"/><path class="k" d="M26 16v1M26 23v14"/></svg>'
    };

    var TITULOS = {
        exito: '¡Operación exitosa!',
        error: 'No se pudo completar',
        confirm: '¿Estás seguro?',
        aviso: 'Atención',
        alerta: 'Atención',
        warning: 'Atención',
        info: 'Información'
    };

    function normalizarTipo(tipo) {
        if (tipo === 'warning' || tipo === 'alerta') return 'aviso';
        if (!ICONOS[tipo]) return 'aviso';
        return tipo;
    }

    function mostrar(tipoOriginal, mensaje, opciones, resolver) {
        opciones = opciones || {};
        var tipo = normalizarTipo(tipoOriginal);
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
        icono.innerHTML = ICONOS[tipo] || ICONOS.aviso;

        var titulo = document.createElement('h4');
        titulo.textContent = opciones.titulo || TITULOS[tipoOriginal] || TITULOS[tipo] || 'Mensaje';

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
            if (e.key === 'Escape') {
                e.preventDefault();
                cerrar(false);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                cerrar(true);
            }
        }

        boton.addEventListener('click', function () { cerrar(true); });
        if (cancelar) cancelar.addEventListener('click', function () { cerrar(false); });
        overlay.addEventListener('click', function (e) { if (e.target === overlay) cerrar(false); });
        document.addEventListener('keydown', teclas);
        boton.focus();
    }

    window.ubbPopup = {
        exito: function (mensaje, opciones) {
            return new Promise(function (resolve) { mostrar('exito', mensaje, opciones, resolve); });
        },
        error: function (mensaje, opciones) {
            return new Promise(function (resolve) { mostrar('error', mensaje, opciones, resolve); });
        },
        aviso: function (mensaje, opciones) {
            return new Promise(function (resolve) { mostrar('aviso', mensaje, opciones, resolve); });
        },
        alerta: function (mensaje, opciones) {
            return new Promise(function (resolve) { mostrar('aviso', mensaje, opciones, resolve); });
        },
        info: function (mensaje, opciones) {
            return new Promise(function (resolve) { mostrar('info', mensaje, opciones, resolve); });
        },
        confirmar: function (mensaje, opciones) {
            return new Promise(function (resolve) { mostrar('confirm', mensaje, opciones, resolve); });
        }
    };

    // Reemplaza el alert nativo del navegador con el popup UBB estilizado
    window.alert = function (mensaje) {
        return window.ubbPopup.aviso(mensaje, { titulo: 'Atención' });
    };

    // Intercepción declarativa de formularios y botones con [data-ubb-confirm]
    var ultimoSubmitter = null;
    document.addEventListener('click', function (e) {
        var submitBtn = e.target.closest('button[type="submit"], input[type="submit"]');
        if (submitBtn) {
            ultimoSubmitter = submitBtn;
        }

        // Si el clic es dentro de un formulario con confirmación o en un botón de envío, lo procesa el evento 'submit'
        if (e.target.closest('form[data-ubb-confirm]') || submitBtn) {
            return;
        }

        var el = e.target.closest('[data-ubb-confirm]');
        if (!el || el.tagName === 'FORM') return;
        if ((el.tagName === 'BUTTON' || el.tagName === 'INPUT') && el.type === 'submit') return;
        if (el._ubbConfirmed) return;

        var confirmMsg = el.getAttribute('data-ubb-confirm');
        if (!confirmMsg) return;

        e.preventDefault();
        e.stopPropagation();
        var titulo = el.getAttribute('data-ubb-titulo') || '¿Estás seguro?';
        var boton = el.getAttribute('data-ubb-boton') || 'Confirmar';
        var cancelar = el.getAttribute('data-ubb-cancelar') || 'Cancelar';

        window.ubbPopup.confirmar(confirmMsg, { titulo: titulo, boton: boton, cancelar: cancelar }).then(function (ok) {
            if (ok) {
                el._ubbConfirmed = true;
                if (el.tagName === 'A' && el.href) {
                    window.location.href = el.href;
                } else if (typeof el.onclick === 'function') {
                    el.onclick(e);
                } else {
                    el.click();
                }
                el._ubbConfirmed = false;
            }
        });
    }, true);

    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (form._ubbConfirmed) {
            form._ubbConfirmed = false;
            return;
        }

        var submitter = e.submitter || ultimoSubmitter;
        var confirmMsg = (submitter && submitter.getAttribute('data-ubb-confirm')) || form.getAttribute('data-ubb-confirm');
        if (!confirmMsg) return;

        e.preventDefault();
        var titulo = (submitter && submitter.getAttribute('data-ubb-titulo')) || form.getAttribute('data-ubb-titulo') || '¿Estás seguro?';
        var boton = (submitter && submitter.getAttribute('data-ubb-boton')) || form.getAttribute('data-ubb-boton') || 'Confirmar';
        var cancelar = (submitter && submitter.getAttribute('data-ubb-cancelar')) || form.getAttribute('data-ubb-cancelar') || 'Cancelar';

        window.ubbPopup.confirmar(confirmMsg, { titulo: titulo, boton: boton, cancelar: cancelar }).then(function (ok) {
            if (ok) {
                form._ubbConfirmed = true;
                if (submitter && submitter.name) {
                    var hidden = document.createElement('input');
                    hidden.type = 'hidden';
                    hidden.name = submitter.name;
                    hidden.value = submitter.value;
                    form.appendChild(hidden);
                }
                HTMLFormElement.prototype.submit.call(form);
            }
        });
    }, true);

    // Detección automática de flash messages de Spring Boot
    document.addEventListener('DOMContentLoaded', function () {
        var flashes = document.querySelectorAll('[data-ubb-flash]');
        flashes.forEach(function (flash) {
            if (!flash || !flash.dataset.mensaje) return;
            var tipo = flash.dataset.tipo || 'exito';
            if (tipo === 'warning' || tipo === 'alerta') tipo = 'aviso';
            if (typeof window.ubbPopup[tipo] === 'function') {
                window.ubbPopup[tipo](flash.dataset.mensaje, { titulo: flash.dataset.titulo || undefined });
            } else {
                window.ubbPopup.exito(flash.dataset.mensaje, { titulo: flash.dataset.titulo || undefined });
            }
        });
    });
})();
