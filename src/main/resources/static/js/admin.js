document.addEventListener("DOMContentLoaded", function() {
	console.log("✅ admin.js cargado correctamente");

	let selectedSection = "";
	let selectedElementId = null;

	// Cargar datos en sesión si no existen
	function cargarDatosDesdeBackend() {
		if (!sessionStorage.getItem("materias")) {
			sendRequest("/materias", "GET").then(materias => {
				sessionStorage.setItem("materias", JSON.stringify(materias));
			});
		}
		if (!sessionStorage.getItem("tests")) {
			sendRequest("/tests", "GET").then(tests => {
				sessionStorage.setItem("tests", JSON.stringify(tests));
			});
		}
		if (!sessionStorage.getItem("preguntas")) {
			sendRequest("/preguntas", "GET").then(preguntas => {
				sessionStorage.setItem("preguntas", JSON.stringify(preguntas));
			});
		}
		if (!sessionStorage.getItem("respuestas")) {
			sendRequest("/respuestas", "GET").then(respuestas => {
				sessionStorage.setItem("respuestas", JSON.stringify(respuestas));
			});
		}
	}

	cargarDatosDesdeBackend(); // Ejecutar al inicio

	window.showSection = function(sectionId) {
		const allSections = document.querySelectorAll(".admin-section");
		allSections.forEach(section => section.style.display = "none");

		const targetSection = document.getElementById(sectionId);
		if (targetSection) {
			targetSection.style.display = "block";
			document.getElementById("accionesContainer").style.display = "block";
			selectedSection = sectionId; // Guardar la sección activa
		}
	};
	window.openFormModal = function(actionType) {
		const modalLabel = document.getElementById("modalLabel");
		const inputNombre = document.getElementById("nombreElemento");
		const inputId = document.getElementById("elementId");
		const selectElemento = document.getElementById("selectElemento");

		inputNombre.value = "";
		inputId.value = "";
		selectElemento.innerHTML = "";

		let data = JSON.parse(sessionStorage.getItem(selectedSection.replace("Section", ""))) || [];
		console.log("Datos cargados desde sesión:", data);

		// Determinar dinámicamente los nombres de los campos de ID y nombre
		let idField = selectedSection === "materiasSection" ? "idMateria" :
			selectedSection === "testsSection" ? "idTest" :
				selectedSection === "preguntasSection" ? "idPregunta" :
					selectedSection === "respuestasSection" ? "idRespuesta" : "id";

		let nameField = selectedSection === "materiasSection" ? "nombreMateria" :
			selectedSection === "testsSection" ? "nombreTest" :
				selectedSection === "preguntasSection" ? "textoPregunta" :
					selectedSection === "respuestasSection" ? "textoRespuesta" : "nombre";

		if (actionType === "edit") {
			// Cargar el select con los elementos de la sección actual
			selectElemento.innerHTML = data.map(item => `<option value="${item[idField]}">${item[nameField]}</option>`).join("");
			document.getElementById("selectElementoContainer").style.display = "block";

			// Cuando se seleccione un elemento, cargar sus datos en los inputs
			selectElemento.addEventListener("change", function() {
				let selectedItem = data.find(item => item[idField] == this.value);
				if (selectedItem) {
					inputId.value = selectedItem[idField];
					inputNombre.value = selectedItem[nameField];

					// Si el elemento tiene estado activo, mostrar el checkbox
					if (selectedItem.activa !== undefined || selectedItem.activo !== undefined) {
						document.getElementById("activoCheckbox").checked = selectedItem.activa || selectedItem.activo;
						document.getElementById("activoContainer").style.display = "block";
					} else {
						document.getElementById("activoContainer").style.display = "none";
					}
				}
			});
		} else {
			document.getElementById("selectElementoContainer").style.display = "none";
			document.getElementById("activoContainer").style.display = "none";
		}

		// Mostrar selects según la sección activa y precargar datos si se edita
		if (selectedSection === "testsSection") {
			document.getElementById("materiaSelectContainer").style.display = "block";
			llenarSelect("materiaSelect", "materias", "idMateria", "nombreMateria");
			selectElemento.addEventListener("change", function() {
				let selectedTest = data.find(item => item[idField] == this.value);
				if (selectedTest) {
					document.getElementById("materiaSelect").value = selectedTest.materia.idMateria;
				}
			});
		} else {
			document.getElementById("materiaSelectContainer").style.display = "none";
		}

		if (selectedSection === "preguntasSection") {
			document.getElementById("testSelectContainer").style.display = "block";
			llenarSelect("testSelect", "tests", "idTest", "nombreTest");

			selectElemento.addEventListener("change", function() {
				let selectedPregunta = data.find(item => item[idField] == this.value);
				if (selectedPregunta) {
					document.getElementById("testSelect").value = selectedPregunta.test.idTest;
				}
			});
		} else {
			document.getElementById("testSelectContainer").style.display = "none";
		}

		if (selectedSection === "respuestasSection") {
			document.getElementById("preguntaSelectContainer").style.display = "block";
			llenarSelect("preguntaSelect", "preguntas", "idPregunta", "textoPregunta");

			selectElemento.addEventListener("change", function() {
				let selectedRespuesta = data.find(item => item[idField] == this.value);
				if (selectedRespuesta) {
					document.getElementById("preguntaSelect").value = selectedRespuesta.pregunta.idPregunta;
				}
			});
		} else {
			document.getElementById("preguntaSelectContainer").style.display = "none";
		}

		document.getElementById("dynamicForm").onsubmit = function(event) {
			event.preventDefault();
			guardarElemento();
		};

		new bootstrap.Modal(document.getElementById("modalFormulario")).show();
	};


	function llenarSelect(selectId, storageKey, idField, textField) {
		let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
		let select = document.getElementById(selectId);
		select.innerHTML = `<option value="">Seleccione...</option>` +
			data.map(item => `<option value="${item[idField]}">${item[textField]}</option>`).join("");
	}

	window.guardarElemento = function() {
		const id = document.getElementById("elementId").value;
		const nombre = document.getElementById("nombreElemento").value.trim();
		const activa = document.getElementById("activoCheckbox").checked;
		if (!nombre) return;

		let payload = {};
		let apiUrl = `/admin/${selectedSection.replace('Section', '')}`;

		if (selectedSection === "materiasSection") {
			payload = { idMateria: id || null, nombreMateria: nombre, activa };

			console.log("Payload en materiasSection  ", payload)
			console.log("apiUrl en materiasSection ", apiUrl)
		} else if (selectedSection === "testsSection") {
			payload = {
				idTest: id || null,
				nombreTest: nombre,
				materia: { idMateria: document.getElementById("materiaSelect").value },
				activa
			};
			console.log("Payload en testsSection ", payload)
			console.log("apiUrl en testsSection ", apiUrl)
		} else if (selectedSection === "preguntasSection") {
			payload = {
				idPregunta: id || null,
				textoPregunta: nombre,
				test: { idTest: document.getElementById("testSelect").value }
			};
			console.log("Payload en preguntasSection ", payload)
			console.log("apiUrl en preguntasSection ", apiUrl)
		} else if (selectedSection === "respuestasSection") {
			const nota = document.querySelector("input[name='respuestaCorrecta']:checked") ? 1.0 : 0.0;
			payload = {
				idRespuesta: id || null,
				textoRespuesta: nombre,
				textoExplicacion: document.getElementById("explicacionRespuesta").value.trim(),
				nota: nota,
				pregunta: { idPregunta: document.getElementById("preguntaSelect").value }
			};
			console.log("Payload en respuestasSection ", payload)
			console.log("apiUrl en respuestasSection ", apiUrl)
		}

		if (id) apiUrl += `/${id}`;

		sendRequest(apiUrl, id ? "PUT" : "POST", payload).then((result) => {
			console.log("Payload en sendRequest ", payload)
			console.log("apiUrl en sendRequest ", apiUrl)
			if (result) {
				actualizarSessionStorage(selectedSection);
				setTimeout(() => {
					new bootstrap.Modal(document.getElementById("modalFormulario")).hide();
				}, 300);
				alert(id ? "Elemento actualizado" : "Elemento creado");
			}
		});
	};


	/**
	 * 🔹 Función para actualizar los datos en `sessionStorage` después de cada cambio
	 */
	function actualizarSessionStorage(section) {
		let endpoint = "";
		let storageKey = "";

		if (section === "materiasSection") {
			endpoint = "/materias";
			storageKey = "materias";
		} else if (section === "testsSection") {
			endpoint = "/tests";
			storageKey = "tests";
		} else if (section === "preguntasSection") {
			endpoint = "/preguntas";
			storageKey = "preguntas";
		} else if (section === "respuestasSection") {
			endpoint = "/respuestas";
			storageKey = "respuestas";
		}

		if (endpoint && storageKey) {
			sendRequest(endpoint, "GET").then(data => {
				sessionStorage.setItem(storageKey, JSON.stringify(data));
			});
		}
	}

	window.eliminarElemento = function () {
	    const selectEliminar = document.getElementById("selectEliminar");
	    const selectEliminarContainer = document.getElementById("selectEliminarContainer");

	    selectEliminar.innerHTML = "";

	    let storageKey, idField, nameField;

	    switch (selectedSection) {
	        case "materiasSection":
	            storageKey = "materias";
	            idField = "idMateria";
	            nameField = "nombreMateria";
	            break;
	        case "testsSection":
	            storageKey = "tests";
	            idField = "idTest";
	            nameField = "nombreTest";
	            break;
	        case "preguntasSection":
	            storageKey = "preguntas";
	            idField = "idPregunta";
	            nameField = "textoPregunta";
	            break;
	        case "respuestasSection":
	            storageKey = "respuestas";
	            idField = "idRespuesta";
	            nameField = "textoRespuesta";
	            break;
	        default:
	            console.error("❌ Sección no reconocida:", selectedSection);
	            return;
	    }

	    let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
	    console.log("🔹 Datos cargados desde sesión para eliminar:", data);

	    if (data.length === 0) {
	        alert("⚠ No hay elementos disponibles para eliminar.");
	        return;
	    }

	    // Usamos la función de llenado de select
	    llenarSelect("selectEliminar", storageKey, idField, nameField);

	    // 🔹 Forzar el repaint del select para asegurar que se actualiza en la UI
	    selectEliminar.style.display = "block";
	    selectEliminarContainer.style.display = "block";
		console.log(selectEliminar.innerHTML);

	    // 🔹 Asegurar que el modal se abre después de llenar el select
	    setTimeout(() => {
	        new bootstrap.Modal(document.getElementById("modalEliminar")).show();
	    }, 300);
	};

	window.confirmarEliminar = function () {
	    const selectEliminar = document.getElementById("selectEliminar");
	    const idSeleccionado = selectEliminar.value;

	    if (!idSeleccionado) {
	        alert("⚠ Debes seleccionar un elemento para eliminar.");
	        return;
	    }

	    if (!confirm("❗ ¿Estás seguro de que quieres eliminar este elemento?")) return;
	    if (!confirm("⚠ Esta acción es irreversible. ¿Confirmas eliminar?")) return;

	    const apiUrl = `/admin/${selectedSection.replace("Section", "")}/${idSeleccionado}`;

	    sendRequest(apiUrl, "DELETE").then((response) => {
	        if (response !== undefined) {
	            alert("✅ Elemento eliminado correctamente.");
	            actualizarSessionStorage(selectedSection);

	            // 🔹 Esconder el modal correctamente después de eliminar
	            const modal = bootstrap.Modal.getInstance(document.getElementById("modalEliminar"));
	            if (modal) modal.hide();
	        }
	    });
	};

	window.mostrarListaEstados = function () {
	    const listaEstados = document.getElementById("listaEstados");
	    listaEstados.innerHTML = "";

	    let storageKey, idField, nameField, apiUrl;

	    switch (selectedSection) {
	        case "materiasSection":
	            storageKey = "materias";
	            idField = "idMateria";
	            nameField = "nombreMateria";
	            apiUrl = "/materias/";
	            break;
	        case "testsSection":
	            storageKey = "tests";
	            idField = "idTest";
	            nameField = "nombreTest";
	            apiUrl = "/tests/";
	            break;
	        default:
	            alert("⚠ No puedes modificar el estado de este tipo de elemento.");
	            return;
	    }

	    let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
	    console.log("🔹 Datos cargados para cambiar estado:", data);

	    if (data.length === 0) {
	        listaEstados.innerHTML = "<p class='text-danger'>⚠ No hay elementos disponibles.</p>";
	        return;
	    }

	    // Guardar estados iniciales
	    let estadosIniciales = {};

	    // Crear la lista con switches
	    data.forEach(item => {
	        estadosIniciales[item[idField]] = item.activa; // Guardamos el estado original
	        const estado = item.activa ? "checked" : "";
	        listaEstados.innerHTML += `
	            <div class="list-group-item d-flex justify-content-between align-items-center">
	                <span>${item[nameField]}</span>
	                <div class="form-check form-switch">
	                    <input class="form-check-input estado-toggle" type="checkbox" data-id="${item[idField]}" ${estado}>
	                </div>
	            </div>
	        `;
	    });

	    // Mostrar el contenedor y el botón de guardar
	    document.getElementById("estadoContainer").style.display = "block";

	    // Detectar cambios y mostrar botón de guardar solo si hay cambios reales
	    document.querySelectorAll(".estado-toggle").forEach(toggle => {
	        toggle.addEventListener("change", () => {
	            const id = toggle.getAttribute("data-id");
	            if (toggle.checked !== estadosIniciales[id]) {
	                document.getElementById("guardarCambiosEstado").style.display = "block";
	            } else {
	                // Si no hay cambios en ningún elemento, ocultar el botón
	                if (![...document.querySelectorAll(".estado-toggle")].some(t => t.checked !== estadosIniciales[t.getAttribute("data-id")])) {
	                    document.getElementById("guardarCambiosEstado").style.display = "none";
	                }
	            }
	        });
	    });

	    // Guardar el estado inicial en una variable global para verificar cambios
	    window.estadosIniciales = estadosIniciales;
	};

	window.guardarCambiosEstados = function () {
	    const toggles = document.querySelectorAll(".estado-toggle");
	    let cambios = [];

	    toggles.forEach(toggle => {
	        const id = toggle.getAttribute("data-id");
	        const estadoNuevo = toggle.checked;

	        // Verificamos si ha cambiado respecto al estado inicial
	        if (estadoNuevo !== window.estadosIniciales[id]) {
	            cambios.push({ id, activa: estadoNuevo });
	        }
	    });

	    if (cambios.length === 0) {
	        alert("⚠ No hay cambios para guardar.");
	        return;
	    }

	    // Enviar peticiones solo por los elementos que cambiaron
	    cambios.forEach(cambio => {
	        let apiUrl = selectedSection === "materiasSection" ? "/materias/" : "/tests/";
	        apiUrl += cambio.id + "/toggle-activa";

	        sendRequest(apiUrl, "PUT").then(() => {
	            console.log(`✅ Estado cambiado para ${cambio.id}: ${cambio.activa}`);
	        });
	    });

	    alert("✅ Cambios guardados correctamente.");
	    actualizarSessionStorage(selectedSection);
	    document.getElementById("guardarCambiosEstado").style.display = "none";
	};






	window.addRespuesta = function() {
		let div = document.createElement("div");
		div.className = "input-group mb-2";
		div.innerHTML = `
            <input type="text" class="form-control respuesta-input" placeholder="Texto de la respuesta">
            <input type="text" class="form-control explicacion-input" placeholder="Explicación">
            <input type="radio" name="respuestaCorrecta">
            <button class="btn btn-danger" onclick="this.parentElement.remove()">✖</button>
        `;
		document.getElementById("respuestasLista").appendChild(div);
	};

	function getRespuestas() {
		let respuestas = [];
		document.querySelectorAll(".respuesta-input").forEach((input, index) => {
			respuestas.push({
				textoRespuesta: input.value,
				textoExplicacion: document.querySelectorAll(".explicacion-input")[index].value,
				nota: document.querySelectorAll("input[name='respuestaCorrecta']")[index].checked ? 1.0 : 0.0
			});
		});
		return respuestas;
	}

	async function sendRequest(url, method = "GET", body = null) {
	    const csrfToken = window.csrf.token;
	    if (!csrfToken) return;

	    const options = {
	        method,
	        headers: {
	            "Content-Type": "application/json",
	            [window.csrf.headerName]: csrfToken
	        }
	    };

	    if (body) options.body = JSON.stringify(body);

	    try {
	        const response = await fetch(url, options);

	        if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);

	        // 🔹 Si la respuesta es 204 (No Content), no intentar parsear JSON
	        if (response.status === 204) {
	            return null; // No hay contenido que procesar
	        }

	        return await response.json();
	    } catch (error) {
	        alert(`Error: ${error.message}`);
	    }
	}
	

});
