/**
 * Lógica interactiva de evaluación ponderada por componentes (%).
 * Soporta:
 *  - Modo configuración de estructura (agregar/quitar componentes, tope 100%, ponderaciones iguales)
 *  - Modo evaluación de estudiante (calculadora ponderada en vivo + nota necesaria para aprobar con 4,0)
 */
(function () {
    "use strict";

    function parseNum(val) {
        if (val === null || val === undefined) return NaN;
        const clean = String(val).trim().replace(",", ".");
        if (clean === "") return NaN;
        return parseFloat(clean);
    }

    function fmtDecimal(num, dec) {
        return num.toFixed(dec).replace(".", ",");
    }

    function generarUid() {
        return "comp_" + Math.random().toString(36).substring(2, 10);
    }

    // =========================================================================
    // 1. MODO CONFIGURACIÓN DE ESTRUCTURA (configuracion-evaluacion.html)
    // =========================================================================
    const formConfig = document.getElementById("formConfigEvaluacion");
    if (formConfig) {
        const tbody = document.getElementById("tbodyComponentes");
        const totalEl = document.getElementById("totalPorcentaje");
        const alertaEl = document.getElementById("alertaPorcentaje");
        const btnGuardar = document.getElementById("btnGuardarEstructura");
        const btnAgregar = document.getElementById("btnAgregarComponente");
        const btnIguales = document.getElementById("btnPonderacionesIguales");

        function recalcularTotalConfig() {
            const inputs = tbody.querySelectorAll("input[name='peso']");
            let suma = 0;
            let hayInvalido = false;

            inputs.forEach(inp => {
                const v = parseNum(inp.value);
                if (isNaN(v) || v <= 0) {
                    hayInvalido = true;
                } else {
                    suma += v;
                }
            });

            suma = Math.round(suma * 100) / 100;
            totalEl.textContent = fmtDecimal(suma, 2) + " %";

            const okTotal = Math.abs(suma - 100.0) <= 0.01 && inputs.length > 0 && !hayInvalido;

            if (inputs.length === 0) {
                mostrarAlerta("danger", "Debes agregar al menos un componente de evaluación.");
            } else if (suma > 100.01) {
                const exceso = Math.round((suma - 100.0) * 100) / 100;
                mostrarAlerta("danger", "El total supera el máximo permitido de 100% (exceso de " + fmtDecimal(exceso, 2) + "%). Reduce los porcentajes antes de guardar.");
            } else if (suma < 99.99) {
                const falta = Math.round((100.0 - suma) * 100) / 100;
                mostrarAlerta("warning", "Los pesos deben sumar exactamente 100%. Falta distribuir " + fmtDecimal(falta, 2) + "%.");
            } else if (hayInvalido) {
                mostrarAlerta("warning", "Todos los componentes deben tener un porcentaje mayor a 0%.");
            } else {
                ocultarAlerta();
            }

            totalEl.classList.toggle("bg-success-subtle", okTotal);
            totalEl.classList.toggle("text-success", okTotal);
            totalEl.classList.toggle("border-success-subtle", okTotal);
            totalEl.classList.toggle("bg-danger-subtle", suma > 100.01);
            totalEl.classList.toggle("text-danger", suma > 100.01);
            totalEl.classList.toggle("border-danger-subtle", suma > 100.01);
            totalEl.classList.toggle("bg-warning-subtle", suma < 99.99);
            totalEl.classList.toggle("text-warning-emphasis", suma < 99.99);
            totalEl.classList.toggle("border-warning-subtle", suma < 99.99);
            btnGuardar.disabled = !okTotal;
            return okTotal;
        }

        function mostrarAlerta(tipo, texto) {
            alertaEl.className = "alert alert-" + tipo + " py-2 px-3 small mb-3";
            alertaEl.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-2"></i>' + texto;
            alertaEl.classList.remove("d-none");
        }

        function ocultarAlerta() {
            alertaEl.classList.add("d-none");
        }

        function crearFilaConfig(nombre, peso) {
            const tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="ps-3 py-2">' +
                    '<input type="hidden" name="uid" value="' + generarUid() + '">' +
                    '<input type="text" name="nombre" class="form-control" placeholder="Ej: Pauta de observación o Informe final" required maxlength="160" value="' + (nombre || "") + '">' +
                '</td>' +
                '<td class="py-2">' +
                    '<div class="input-group">' +
                        '<input type="number" name="peso" class="form-control text-end input-peso fw-semibold" step="0.01" min="0.1" max="100" required value="' + (peso || "") + '">' +
                        '<span class="input-group-text bg-light fw-bold">%</span>' +
                    '</div>' +
                '</td>' +
                '<td class="text-center py-2">' +
                    '<button type="button" class="btn btn-sm btn-outline-danger btn-eliminar-fila" title="Eliminar componente">' +
                        '<i class="bi bi-trash"></i>' +
                    '</button>' +
                '</td>';
            tbody.appendChild(tr);
            tr.querySelector("input[name='nombre']").focus();
            recalcularTotalConfig();
        }

        tbody.addEventListener("input", function (e) {
            if (e.target.classList.contains("input-peso")) {
                recalcularTotalConfig();
            }
        });

        tbody.addEventListener("click", function (e) {
            const btn = e.target.closest(".btn-eliminar-fila");
            if (!btn || btn.disabled) return;
            const filas = tbody.querySelectorAll("tr");
            if (filas.length <= 1) {
                mostrarAlerta("warning", "Debe quedar al menos un componente en la estructura.");
                return;
            }
            btn.closest("tr").remove();
            recalcularTotalConfig();
        });

        if (btnAgregar) {
            btnAgregar.addEventListener("click", function () {
                const inputs = tbody.querySelectorAll("input[name='peso']");
                let suma = 0;
                inputs.forEach(inp => {
                    const v = parseNum(inp.value);
                    if (!isNaN(v)) suma += v;
                });
                const sugerido = suma < 100 ? Math.round((100 - suma) * 100) / 100 : "";
                crearFilaConfig("", sugerido);
            });
        }

        if (btnIguales) {
            btnIguales.addEventListener("click", function () {
                const inputs = tbody.querySelectorAll("input[name='peso']");
                const n = inputs.length;
                if (n === 0) return;
                const base = Math.floor((10000 / n)) / 100;
                let acumulado = 0;
                inputs.forEach((inp, idx) => {
                    if (idx === n - 1) {
                        const ultimo = Math.round((100 - acumulado) * 100) / 100;
                        inp.value = ultimo;
                    } else {
                        inp.value = base;
                        acumulado += base;
                    }
                });
                recalcularTotalConfig();
            });
        }

        formConfig.addEventListener("submit", function (e) {
            if (!recalcularTotalConfig()) {
                e.preventDefault();
            }
        });

        recalcularTotalConfig();
    }

    // =========================================================================
    // 2. MODO EVALUACIÓN DE ESTUDIANTE (evaluar-practica.html)
    // =========================================================================
    const formEvaluar = document.getElementById("formEvaluarPractica");
    if (formEvaluar) {
        const tbody = document.getElementById("tbodyNotas");
        const chkPersonalizar = document.getElementById("chkPersonalizar");
        const inputPersonalizada = document.getElementById("inputPersonalizada");
        const barraPersonalizacion = document.getElementById("barraPersonalizacion");
        const btnAgregar = document.getElementById("btnAgregarFilaEval");
        const btnIguales = document.getElementById("btnIgualesEval");
        const btnLimpiarNotas = document.getElementById("btnLimpiarNotas");
        const btnGuardar = document.getElementById("btnGuardarNotas");

        const totalPesoEl = document.getElementById("evalTotalPeso");
        const alertaPesoEl = document.getElementById("alertaPesoEval");
        const resPromedioEl = document.getElementById("resPromedio");
        const resPorcentajeEl = document.getElementById("resPorcentaje");
        const resBarraEl = document.getElementById("resBarra");
        const resAcumuladaEl = document.getElementById("resAcumulada");
        const resNecesariaBox = document.getElementById("resNecesariaBox");

        function aplicarEstadoPersonalizacion(activo) {
            inputPersonalizada.value = activo ? "true" : "false";
            if (barraPersonalizacion) {
                barraPersonalizacion.classList.toggle("d-none", !activo);
            }
            tbody.querySelectorAll(".input-nombre-eval, .input-peso-eval").forEach(inp => {
                inp.readOnly = !activo;
                inp.classList.toggle("bg-light", !activo);
            });
            tbody.querySelectorAll(".col-accion-eval").forEach(td => {
                td.classList.toggle("d-none", !activo);
            });
            const thAccion = document.getElementById("thAccionEval");
            if (thAccion) thAccion.classList.toggle("d-none", !activo);
        }

        function recalcularEvaluacion() {
            const filas = tbody.querySelectorAll("tr");
            let sumaPesos = 0;
            let pesoEvaluado = 0;
            let sumaPonderada = 0;
            let notaAcumulada = 0;
            let hayPesoInvalido = false;
            let hayNotaInvalida = false;

            filas.forEach(tr => {
                const inpPeso = tr.querySelector("input[name='peso']");
                const inpNota = tr.querySelector("input[name='nota']");
                const aporteTd = tr.querySelector(".celda-aporte");

                const peso = parseNum(inpPeso.value);
                const nota = parseNum(inpNota.value);

                if (isNaN(peso) || peso <= 0) {
                    hayPesoInvalido = true;
                } else {
                    sumaPesos += peso;
                }

                if (inpNota.value.trim() !== "") {
                    if (isNaN(nota) || nota < 1.0 || nota > 7.0) {
                        hayNotaInvalida = true;
                        inpNota.classList.add("is-invalid");
                        if (aporteTd) aporteTd.textContent = "—";
                    } else {
                        inpNota.classList.remove("is-invalid");
                        if (!isNaN(peso) && peso > 0) {
                            pesoEvaluado += peso;
                            sumaPonderada += nota * peso;
                            const aporte = nota * (peso / 100.0);
                            notaAcumulada += aporte;
                            if (aporteTd) aporteTd.textContent = fmtDecimal(aporte, 2);
                        }
                    }
                } else {
                    inpNota.classList.remove("is-invalid");
                    if (aporteTd) aporteTd.textContent = "—";
                }
            });

            sumaPesos = Math.round(sumaPesos * 100) / 100;
            pesoEvaluado = Math.round(pesoEvaluado * 100) / 100;
            notaAcumulada = Math.round(notaAcumulada * 100) / 100;

            totalPesoEl.textContent = fmtDecimal(sumaPesos, 2) + " %";
            const pesoOk = Math.abs(sumaPesos - 100.0) <= 0.01 && filas.length > 0 && !hayPesoInvalido;

            if (sumaPesos > 100.01) {
                alertaPesoEl.className = "alert alert-danger py-2 px-3 small mb-3";
                alertaPesoEl.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-2"></i>La suma de porcentajes supera el 100% (actual: ' + fmtDecimal(sumaPesos, 2) + '%).';
                alertaPesoEl.classList.remove("d-none");
            } else if (sumaPesos < 99.99) {
                alertaPesoEl.className = "alert alert-warning py-2 px-3 small mb-3";
                alertaPesoEl.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-2"></i>Los porcentajes deben sumar exactamente 100% (actual: ' + fmtDecimal(sumaPesos, 2) + '%).';
                alertaPesoEl.classList.remove("d-none");
            } else if (hayNotaInvalida) {
                alertaPesoEl.className = "alert alert-danger py-2 px-3 small mb-3";
                alertaPesoEl.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-2"></i>Revisa las notas marcadas en rojo: deben estar entre 1,0 y 7,0.';
                alertaPesoEl.classList.remove("d-none");
            } else {
                alertaPesoEl.classList.add("d-none");
            }

            btnGuardar.disabled = !(pesoOk && !hayNotaInvalida);

            // Actualizar panel de resultados en vivo
            resPorcentajeEl.textContent = fmtDecimal(pesoEvaluado, 1) + "% evaluado";
            resBarraEl.style.width = Math.min(100, pesoEvaluado) + "%";
            resAcumuladaEl.textContent = fmtDecimal(notaAcumulada, 2) + " / 7,00";

            if (pesoEvaluado > 0) {
                const promedio = Math.round((sumaPonderada / pesoEvaluado) * 10) / 10;
                resPromedioEl.textContent = fmtDecimal(promedio, 1);
                resPromedioEl.className = "display-5 fw-bold mb-0 " + (promedio >= 4.0 ? "text-success" : "text-danger");

                const restante = Math.round((100.0 - pesoEvaluado) * 100) / 100;
                if (restante <= 0.01) {
                    if (promedio >= 4.0) {
                        resNecesariaBox.className = "alert alert-success small mb-0";
                        resNecesariaBox.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i><strong>Evaluación completa (100%):</strong> Práctica aprobada con <strong>' + fmtDecimal(promedio, 1) + '</strong>.';
                    } else {
                        resNecesariaBox.className = "alert alert-danger small mb-0";
                        resNecesariaBox.innerHTML = '<i class="bi bi-x-circle-fill me-1"></i><strong>Evaluación completa (100%):</strong> Nota final <strong>' + fmtDecimal(promedio, 1) + '</strong> (bajo 4,0).';
                    }
                } else {
                    const necesaria = Math.round(((4.0 - notaAcumulada) / (restante / 100.0)) * 10) / 10;
                    if (necesaria <= 1.0) {
                        resNecesariaBox.className = "alert alert-success small mb-0";
                        resNecesariaBox.innerHTML = '<i class="bi bi-award-fill me-1"></i>Con <strong>' + fmtDecimal(notaAcumulada, 2) + '</strong> acumulados, ya asegura nota de aprobación (4,0).';
                    } else if (necesaria > 7.0) {
                        resNecesariaBox.className = "alert alert-danger small mb-0";
                        resNecesariaBox.innerHTML = '<i class="bi bi-exclamation-octagon-fill me-1"></i>Necesitaría un <strong>' + fmtDecimal(necesaria, 1) + '</strong> en el ' + fmtDecimal(restante, 1) + '% restante (supera el 7,0).';
                    } else {
                        resNecesariaBox.className = "alert alert-info small mb-0";
                        resNecesariaBox.innerHTML = '<i class="bi bi-calculator me-1"></i>Necesita promedio <strong>' + fmtDecimal(necesaria, 1) + '</strong> en el <strong>' + fmtDecimal(restante, 1) + '%</strong> restante para aprobar con 4,0.';
                    }
                }
            } else {
                resPromedioEl.textContent = "—";
                resPromedioEl.className = "display-5 fw-bold mb-0 text-muted";
                resNecesariaBox.className = "alert alert-light border small mb-0 text-muted";
                resNecesariaBox.innerHTML = '<i class="bi bi-info-circle me-1"></i>Ingresa al menos una nota (1,0 a 7,0) para calcular el promedio ponderado en vivo.';
            }
        }

        if (chkPersonalizar) {
            chkPersonalizar.addEventListener("change", function () {
                aplicarEstadoPersonalizacion(this.checked);
            });
            aplicarEstadoPersonalizacion(chkPersonalizar.checked);
        }

        tbody.addEventListener("input", function () {
            recalcularEvaluacion();
        });

        tbody.addEventListener("click", function (e) {
            const btn = e.target.closest(".btn-eliminar-eval");
            if (!btn || btn.disabled || btn.classList.contains("disabled")) return;
            if (tbody.querySelectorAll("tr").length <= 1) return;
            btn.closest("tr").remove();
            recalcularEvaluacion();
        });

        if (btnAgregar) {
            btnAgregar.addEventListener("click", function () {
                const tr = document.createElement("tr");
                tr.innerHTML =
                    '<td>' +
                        '<input type="hidden" name="uid" value="' + generarUid() + '">' +
                        '<input type="text" name="nombre" class="form-control form-control-sm input-nombre-eval" placeholder="Nombre del componente" required maxlength="160">' +
                    '</td>' +
                    '<td style="width: 135px;">' +
                        '<div class="input-group input-group-sm">' +
                            '<input type="number" name="peso" class="form-control text-end input-peso-eval" step="0.01" min="0.1" max="100" required>' +
                            '<span class="input-group-text">%</span>' +
                        '</div>' +
                    '</td>' +
                    '<td style="width: 120px;">' +
                        '<input type="number" name="nota" class="form-control form-control-sm text-center input-nota-eval" step="0.1" min="1.0" max="7.0" placeholder="1,0 - 7,0">' +
                    '</td>' +
                    '<td class="text-center fw-semibold text-muted celda-aporte" style="width: 90px;">—</td>' +
                    '<td class="text-center col-accion-eval" style="width: 50px;">' +
                        '<button type="button" class="btn btn-link text-danger p-0 btn-eliminar-eval" title="Quitar">' +
                            '<i class="bi bi-x-lg"></i>' +
                        '</button>' +
                    '</td>';
                tbody.appendChild(tr);
                recalcularEvaluacion();
            });
        }

        if (btnIguales) {
            btnIguales.addEventListener("click", function () {
                const inputs = tbody.querySelectorAll("input[name='peso']");
                const n = inputs.length;
                if (n === 0) return;
                const base = Math.floor((10000 / n)) / 100;
                let acumulado = 0;
                inputs.forEach((inp, idx) => {
                    if (idx === n - 1) {
                        inp.value = Math.round((100 - acumulado) * 100) / 100;
                    } else {
                        inp.value = base;
                        acumulado += base;
                    }
                });
                recalcularEvaluacion();
            });
        }

        if (btnLimpiarNotas) {
            btnLimpiarNotas.addEventListener("click", function () {
                if (!confirm("¿Limpiar las notas manuales ingresadas en pantalla? (Las notas provenientes de pautas se mantendrán bloqueadas)")) return;
                tbody.querySelectorAll("input[name='nota']").forEach(inp => {
                    if (!inp.readOnly) {
                        inp.value = "";
                    }
                });
                recalcularEvaluacion();
            });
        }

        recalcularEvaluacion();
    }
})();
