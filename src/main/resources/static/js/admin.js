document.addEventListener("DOMContentLoaded", function () {
    console.log("✅ admin.js cargado correctamente");

    // 1️⃣ Obtener el token CSRF de la cookie

	
	/**
	 * Oculta todas las secciones con clase .admin-section
	 * y muestra únicamente la que coincida con el ID.
	 */
	 window.showSection = function(sectionId) {
	  // Obtener todas las secciones que comparten la clase .admin-section
	  const allSections = document.querySelectorAll(".admin-section");
	  
	  // Ocultar todas las secciones
	  allSections.forEach(section => {
	    section.style.display = "none";
	  });

	  // Mostrar solo la sección que queremos
	  const targetSection = document.getElementById(sectionId);
	  if (targetSection) {
	    targetSection.style.display = "block";
	  }
	}

	
	console.log("CSRF Token:", window.csrf);

	// Función genérica
	async function sendRequest(url, method = "GET", body = null) {
	    const csrfToken = window.csrf.token;
	    if (!csrfToken) {
	        console.error("❌ No se encontró el CSRF token.");
	        alert("Error: No se encontró el CSRF token.");
	        return;
	    }else{
			console.log(csrfToken)
		}

	    const options = {
	        method,
	        credentials: "include", // Para enviar cookies de sesión/CSRF
	        headers: {
	            "Content-Type": "application/json",
	            [window.csrf.headerName]: csrfToken // Cabecera CSRF
	        }
	    };

	    if (body) {
	        options.body = JSON.stringify(body);
	    }

	    try {
	        const response = await fetch(url, options);
	        if (!response.ok) {
	            throw new Error(`Error ${response.status}: ${response.statusText}`);
	        }
	        return await response.json().catch(() => null); 
	    } catch (error) {
	        console.error("❌ Error en la solicitud:", error);
	        alert(`Error: ${error.message}`);
	    }
	}
    /**
     * ───────────────────────────────────────────────
     *     MATERIA: Crear, Actualizar, Eliminar
     * ───────────────────────────────────────────────
     */

    // Crear materia (POST /admin/materias)
    window.crearMateria = async function () {
        console.log("🟢 Intentando crear materia...");
        const nombreMateria = document.getElementById("materiaNombre")?.value.trim();

        if (!nombreMateria) {
            alert("⚠️ Debes ingresar un nombre para la materia.");
            return;
        }

		const result = await sendRequest("/admin/materias", "POST", {
		     nombreMateria: nombreMateria,
		     activa: true,
		   });
        if (result) {
            alert(`✅ Materia creada: ${result.toString()}`);
            location.reload(); 
        }
    };

    // Actualizar materia (PUT /admin/materias/{id})
    window.actualizarMateria = async function (idMateria) {
        // Ejemplo: la vista podría pedirte el nuevo nombre en un input
        const nombreMateria = prompt("Nuevo nombre de la materia:");
        if (!nombreMateria) {
            return;
        }
        // También podría ser si deseas cambiar el flag activa o no
        // const activa = confirm("¿La materia está activa?");
        const payload = { nombreMateria };  //, activa

        const materiaActualizada = await sendRequest(`/admin/materias/${idMateria}`, "PUT", payload);
        if (materiaActualizada) {
            alert(`Materia actualizada: ${materiaActualizada.nombreMateria}`);
            location.reload();
        }
    };

    // Eliminar materia (DELETE /admin/materias/{id})
    window.eliminarMateria = async function (idMateria) {
        if (!confirm("¿Estás seguro de eliminar la materia?")) {
            return;
        }
        const resp = await sendRequest(`/admin/materias/${idMateria}`, "DELETE");
        if (resp !== undefined) {
            alert("Materia eliminada correctamente.");
            location.reload();
        }
    };

    // Cambiar estado materia (PATCH /admin/materias/{id}/estado?estado=...)
    window.cambiarEstadoMateria = async function (idMateria, estado) {
        // "estado" es true o false
        const materia = await sendRequest(`/admin/materias/${idMateria}/estado?estado=${estado}`, "PATCH");
        if (materia) {
            alert(`Se cambió el estado de la materia a: ${materia.activa}`);
            location.reload();
        }
    };


    /**
     * ───────────────────────────────────────────────
     *     TEST: Crear, Actualizar, Eliminar
     * ───────────────────────────────────────────────
     */

    // Crear test (POST /admin/tests)
    window.crearTest = async function () {
        console.log("🔵 Intentando crear test...");
        const nombreTest = document.getElementById("nombreTest")?.value.trim();
        const idMateria = document.getElementById("idMateria")?.value.trim();

        if (!nombreTest || !idMateria) {
            alert("⚠️ Debes completar todos los campos para el test.");
            return;
        }

        // IMPORTANTE: Si en tu modelo "Test" hay un campo "materia" de tipo objeto,
        // deberás mandar algo como { nombreTest, materia: { idMateria: parseInt(idMateria) } }
        // En lugar de un simple "idMateria".
        const payload = {
            nombreTest,
            // Por ejemplo: materia: { idMateria: parseInt(idMateria, 10) }
            // O si el backend solo requiere un "idMateria" suelto:
            idMateria: parseInt(idMateria, 10)
        };

        const nuevoTest = await sendRequest("/admin/tests", "POST", payload);
        if (nuevoTest) {
            alert(`✅ Test creado: ${nuevoTest.nombreTest}`);
            location.reload();
        }
    };

    // Actualizar test (PUT /admin/tests/{id})
    window.actualizarTest = async function (idTest) {
        // Ejemplo: 
        const nombreTest = prompt("Nuevo nombre del test:");
        if (!nombreTest) {
            return;
        }
        // Podrías pedir la materia también: let idMateria = ...
        const payload = {
            nombreTest
            // materia: { idMateria: ... }
        };

        const testActualizado = await sendRequest(`/admin/tests/${idTest}`, "PUT", payload);
        if (testActualizado) {
            alert(`Test actualizado: ${testActualizado.nombreTest}`);
            location.reload();
        }
    };

    // Eliminar test (DELETE /admin/tests/{id})
    window.eliminarTest = async function (idTest) {
        if (!confirm("¿Estás seguro de eliminar el test?")) {
            return;
        }
        const resp = await sendRequest(`/admin/tests/${idTest}`, "DELETE");
        if (resp !== undefined) {
            alert("Test eliminado correctamente.");
            location.reload();
        }
    };

    // Cambiar estado test (PATCH /admin/tests/{id}/estado?estado=...)
    window.cambiarEstadoTest = async function (idTest, estado) {
        const test = await sendRequest(`/admin/tests/${idTest}/estado?estado=${estado}`, "PATCH");
        if (test) {
            alert(`Se cambió el estado del test a: ${test.activo}`);
            location.reload();
        }
    };


    /**
     * ───────────────────────────────────────────────
     *     PREGUNTAS: Crear, Actualizar, Eliminar
     * ───────────────────────────────────────────────
     */

    // Crear pregunta (POST /admin/preguntas)
    window.crearPregunta = async function () {
        const enunciado = prompt("Enunciado de la pregunta:");
        if (!enunciado) return;

        // En tu modelo backend: public Pregunta crearPregunta(@RequestBody Pregunta pregunta)
        // Requiere: { enunciado, test: { idTest: X } } por ejemplo
        const idTest = prompt("ID del test al que pertenece:");
        if (!idTest) return;

        const payload = {
            enunciado,
            test: {
                idTest: parseInt(idTest, 10)
            }
        };

        const pregunta = await sendRequest("/admin/preguntas", "POST", payload);
        if (pregunta) {
            alert(`Pregunta creada: ${pregunta.enunciado}`);
        }
    };

    // Actualizar pregunta (PUT /admin/preguntas/{id})
    window.actualizarPregunta = async function (idPregunta) {
        const nuevoEnunciado = prompt("Nuevo enunciado de la pregunta:");
        if (!nuevoEnunciado) return;

        const payload = {
            enunciado: nuevoEnunciado
            // si deseas cambiar el test, podrías hacerlo
        };

        const preg = await sendRequest(`/admin/preguntas/${idPregunta}`, "PUT", payload);
        if (preg) {
            alert(`Pregunta actualizada: ${preg.enunciado}`);
            location.reload();
        }
    };

    // Eliminar pregunta (DELETE /admin/preguntas/{id})
    window.eliminarPregunta = async function (idPregunta) {
        if (!confirm("¿Estás seguro de eliminar esta pregunta?")) return;
        const resp = await sendRequest(`/admin/preguntas/${idPregunta}`, "DELETE");
        if (resp !== undefined) {
            alert("Pregunta eliminada correctamente.");
            location.reload();
        }
    };


    /**
     * ───────────────────────────────────────────────
     *     RESPUESTAS: Crear, Actualizar, Eliminar
     * ───────────────────────────────────────────────
     */

    // Crear respuesta (POST /admin/respuestas)
    window.crearRespuesta = async function () {
        const texto = prompt("Texto de la respuesta:");
        if (!texto) return;

        const correcta = confirm("¿Es la respuesta correcta?");
        const idPregunta = prompt("¿ID de la pregunta a la que pertenece?");
        if (!idPregunta) return;

        const payload = {
            texto,
            correcta,
            pregunta: {
                idPregunta: parseInt(idPregunta, 10)
            }
        };

        const resp = await sendRequest("/admin/respuestas", "POST", payload);
        if (resp) {
            alert(`Respuesta creada: ${resp.texto}`);
        }
    };

    // Actualizar respuesta (PUT /admin/respuestas/{id})
    window.actualizarRespuesta = async function (idRespuesta) {
        const texto = prompt("Nuevo texto de la respuesta:");
        if (!texto) return;

        const correcta = confirm("¿Es la respuesta correcta?");
        // Cambia la pregunta si lo deseas
        const payload = {
            texto,
            correcta
        };

        const resp = await sendRequest(`/admin/respuestas/${idRespuesta}`, "PUT", payload);
        if (resp) {
            alert(`Respuesta actualizada: ${resp.texto}`);
            location.reload();
        }
    };

    // Eliminar respuesta (DELETE /admin/respuestas/{id})
    window.eliminarRespuesta = async function (idRespuesta) {
        if (!confirm("¿Estás seguro de eliminar esta respuesta?")) return;
        const r = await sendRequest(`/admin/respuestas/${idRespuesta}`, "DELETE");
        if (r !== undefined) {
            alert("Respuesta eliminada correctamente.");
            location.reload();
        }
    };


    // Solo para debug
    console.log("📌 Funciones disponibles en window:", Object.keys(window));
});
