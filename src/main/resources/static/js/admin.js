document.addEventListener("DOMContentLoaded", function() {
	console.log("✅ admin.js cargado correctamente");

	let selectedSection = "";
	let selectedElementId = null;

	function cargarDatosDesdeBackend() {
		const endpoints = {
			"materias": "/admin/materias",
			"tests": "/admin/tests",
			"preguntas": "/admin/preguntas",
			"respuestas": "/admin/respuestas"
		};

		Object.entries(endpoints).forEach(([key, url]) => {
			console.log(`🔄 Cargando datos desde: ${url}`);
			sendRequest(url, "GET").then(data => {
				console.log(`📥 Datos recibidos de ${url}:`, data);
				sessionStorage.setItem(key, JSON.stringify(data));
			});
		});
	}

	cargarDatosDesdeBackend();

	function actualizarSessionStorage(section) {
		const storageKey = section.replace("Section", "");
		console.log(`🔄 Actualizando sessionStorage para ${storageKey}`);
		sendRequest(`/${storageKey}`, "GET").then(data => {
			console.log(`📥 Datos actualizados de ${storageKey}:`, data);
			sessionStorage.setItem(storageKey, JSON.stringify(data));
		});
	}

	async function sendRequest(url, method = "GET", body = null) {
		console.log(`📡 Enviando petición: ${method} ${url}`, body);
		const csrfToken = window.csrf.token;
		if (!csrfToken) {
			console.error("❌ CSRF Token no encontrado");
			return;
		}

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
			console.log(`📩 Respuesta de ${url}:`, response);

			if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);

			return response.status === 204 ? null : await response.json();
		} catch (error) {
			console.error(`❌ Error en la petición: ${error.message}`);
			alert(`Error: ${error.message}`);
		}
	}
	
	window.showSection = function(sectionId) {
			document.querySelectorAll(".admin-section").forEach(section => section.style.display = "none");

			const targetSection = document.getElementById(sectionId);
			if (targetSection) {
				targetSection.style.display = "block";
				document.getElementById("accionesContainer").style.display = "block";
				selectedSection = sectionId;
			}
		};


//	function llenarSelect(selectId, storageKey, idField, textField, filterField = null, filterValue = null) {
//	    console.log("FilterField en select:", filterField);
//	    console.log("FilterValue en select:", filterValue);
//	    console.log(`🎯 Llenando select #${selectId} desde ${storageKey} con filtro: ${filterField}=${filterValue}`);
//
//	    const selectElement = document.getElementById(selectId);
//	    if (!selectElement) {
//	        console.error(`❌ Error: No se encontró el elemento select con ID '${selectId}'`);
//	        return;
//	    }
//
//	    selectElement.innerHTML = `<option value="">Seleccione...</option>`;
//
//	    let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
//	    console.log(`🔍 Datos cargados desde sessionStorage (${storageKey}):`, data);
//		if (filterField && filterValue !== null) {
//			console.log ("data dentro del if de filterFieeld y filterValue: ", data)
//		    data = data.filter(item => {
//		        let fieldValue;
//console.log("item dentro de data.filter: ", item)
//		        if (filterField.includes(".")) {
//		            // ✅ Acceder correctamente a propiedades anidadas (ejemplo: "materia.idMateria")
//		            const fieldParts = filterField.split(".");
//					console.log("fieldParts dentro del includes de filterField: ", fieldParts)
//		            fieldValue = fieldParts.reduce((obj, key) => obj && obj[key] !== undefined ? obj[key] : undefined, item);
//					
//					console.log("FieldValaaue dentro de includes filterfield: ", fieldValue)
//		        } else {
//		            fieldValue = item[filterField];
//					console.log("itemm dentro del else: ", item)
//					console.log("filterfield denntro del else: ", filterField)
//					console.log("fieldValue dentro del else: ", fieldValue )
//		        }
//
//		        console.log(`🔎 Comprobando item:`, item);
//		        console.log(`📌 Comparando ${fieldValue} con ${filterValue}`);
//
//		        return fieldValue == filterValue; // Comparación correcta
//		    });
//		}
//
//
//	    console.log(`📌 Datos después de filtrar (${selectId}):`, data);
//
//	    data.forEach(item => {
//	        const option = document.createElement("option");
//	        option.value = item[idField];
//	        option.textContent = item[textField];
//	        selectElement.appendChild(option);
//			console.log("Antes de cargar opciones :", selectElement)
//	    });
//
//	    console.log(`📌 Opciones cargadas en ${selectId}:`, [...selectElement.options].map(opt => opt.text));
//		// 🔹 Verificar si el `select` es visible, si no, mostrarlo
//		 const containerId = `${selectId}Container`;
//		 const containerElement = document.getElementById(containerId);
//		 if (containerElement) {
//		     containerElement.style.display = "block";
//		     console.log(`📌 Mostrando ${containerId}`);
//		 }
//	}


	function llenarSelect(selectId, storageKey, idField, textField, filterField = null, filterValue = null) {
	    console.log(`🎯 Llenando select #${selectId} desde ${storageKey} con filtro: ${filterField}=${filterValue}`);

	    const selectElement = document.getElementById(selectId);
	    if (!selectElement) {
	        console.error(`❌ Error: No se encontró el elemento select con ID '${selectId}'`);
	        return;
	    }

	    // Limpiar el select antes de llenarlo
	    selectElement.innerHTML = `<option value="">Seleccione...</option>`;

	    let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
	    console.log(`🔍 Datos cargados desde sessionStorage (${storageKey}):`, data);

	    // Filtrar si hay filterField y filterValue
	    if (filterField && filterValue !== null) {
	        console.log("📌 Filtrando datos...");
	        data = data.filter(item => {
	            let fieldValue = filterField.split(".").reduce((obj, key) => obj && obj[key] !== undefined ? obj[key] : undefined, item);
	            console.log(`🔎 Comprobando item:`, item);
	            console.log(`📌 Comparando ${fieldValue} con ${filterValue}`);
	            return fieldValue == filterValue;
	        });
	    }

	    console.log(`📌 Datos después de filtrar (${selectId}):`, data);

	    // Poblar el select con los datos filtrados
	    data.forEach(item => {
	        const option = document.createElement("option");
	        option.value = item[idField];
	        option.textContent = item[textField];
	        selectElement.appendChild(option);
	    });

	    console.log(`📌 Opciones cargadas en ${selectId}:`, [...selectElement.options].map(opt => opt.text));

	    // 🔹 Verificar si el `select` es visible, si no, mostrarlo
	    const containerId = `${selectId}Container`;
	    const containerElement = document.getElementById(containerId);
	    if (containerElement) {
	        containerElement.style.display = "block";
	        console.log(`📌 Mostrando ${containerId}`);
	    }
	}




	window.showSection = function(sectionId) {
		console.log(`📌 Mostrando sección: ${sectionId}`);
		document.querySelectorAll(".admin-section").forEach(section => section.style.display = "none");

		const targetSection = document.getElementById(sectionId);
		if (targetSection) {
			targetSection.style.display = "block";
			document.getElementById("accionesContainer").style.display = "block";
			selectedSection = sectionId;
		}
	};

	document.getElementById("materiaSelect").addEventListener("change", function() {
		console.log(`🔄 Cambio en materiaSelect: ${this.value}`);
		llenarSelect("testSelect", "tests", "idTest", "nombreTest", "materia.idMateria", this.value);
	});

	document.getElementById("testSelect").addEventListener("change", function() {
		console.log(`🔄 Cambio en testSelect: ${this.value}`);
		llenarSelect("preguntaSelect", "preguntas", "idPregunta", "textoPregunta", "idTest", this.value);
	});

	document.getElementById("preguntaSelect").addEventListener("change", function() {
		console.log(`🔄 Cambio en preguntaSelect: ${this.value}`);
		llenarSelect("respuestaSelect", "respuestas", "idRespuesta", "textoRespuesta", "idPregunta", this.value);
	});

	window.openFormModal = function(actionType) {
		console.log(`📌 Abriendo modal para: ${actionType} en ${selectedSection}`);

		const inputNombre = document.getElementById("nombreElemento");
		const inputId = document.getElementById("elementId");
		const selectElemento = document.getElementById("selectElemento");
		const activoCheckboxContainer = document.getElementById("activoContainer");
		const activoCheckbox = document.getElementById("activoCheckbox");

		inputNombre.value = "";
		inputId.value = "";
		selectElemento.innerHTML = "";

		let idField, nameField, storageKey;

		switch (selectedSection) {
			case "materiasSection":
				idField = "idMateria";
				nameField = "nombreMateria";
				storageKey = "materias";
				break;
			case "testsSection":
				idField = "idTest";
				nameField = "nombreTest";
				storageKey = "tests";
				llenarSelect("materiaSelect", "materias", "idMateria", "nombreMateria");
				document.getElementById("materiaSelectContainer").style.display = "block"; 

				break;
			case "preguntasSection":
				idField = "idPregunta";
				nameField = "textoPregunta";
				storageKey = "preguntas";
				llenarSelect("materiaSelect", "materias", "idMateria", "nombreMateria");
				llenarSelect("testSelect", "tests", "idTest", "nombreTest");
				break;
			case "respuestasSection":
				idField = "idRespuesta";
				nameField = "textoRespuesta";
				storageKey = "respuestas";
				llenarSelect("materiaSelect", "materias", "idMateria", "nombreMateria");
				llenarSelect("testSelect", "tests", "idTest", "nombreTest");
				llenarSelect("preguntaSelect", "preguntas", "idPregunta", "textoPregunta");
				break;
		}

		let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
		console.log(`📥 Datos cargados para edición en ${selectedSection}:`, data);

		if (actionType === "edit") {
		       // Cargar el select con los elementos de la sección actual
		       //selectElemento.innerHTML = data.map(item => `<option value="${item[idField]}">${item[nameField]}</option>`).join("");
		      // document.getElementById("selectElementoContainer").style.display = "block";
			   console.log("action type ", actionType)
		       // Cuando se seleccione un elemento, cargar sus datos en los inputs
		       selectElemento.addEventListener("change", function() {
				console.log(" Entrando en evento change de selectElemento ", selectElemento)
		           let selectedItem = data.find(item => item[idField] == this.value);
		           console.log(`📌 Elemento seleccionado para edición:`, selectedItem);

		           if (selectedItem) {
		               inputId.value = selectedItem[idField];
		               inputNombre.value = selectedItem[nameField];

					   console.log("dentro del ", selectedItem)
		             

		               // 🔹 SI ES UN TEST, FILTRAR LOS TESTS POR MATERIA
		               if (selectedSection === "testsSection") {
						console.log("selectedSection ",selectedSection)
						console.log(selectedItem)
		                   console.log(`🔄 Filtrando tests para la materia ID: ${selectedItem.materia.idMateria}`);
		                  llenarSelect("testSelect", "tests", "idTest", "nombreTest", "idMateria", selectedItem.idMateria);
		               }
					   
					   // Si el elemento tiene estado activo, mostrar el checkbox
					   if (selectedItem.activa !== undefined || selectedItem.activo !== undefined) {
					   	                   activoCheckbox.checked = selectedItem.activa || selectedItem.activo;
					   	                   activoCheckboxContainer.style.display = "block";
					   	               } else {
					   	                   activoCheckboxContainer.style.display = "none";
					   	               }
		               // 🔹 SI ES UNA PREGUNTA, FILTRAR LOS TESTS Y LAS PREGUNTAS
		               if (selectedSection === "preguntasSection") {
		                   console.log(`🔄 Filtrando tests y preguntas para la materia ID: ${selectedItem.test.idMateria}`);
		                   llenarSelect("testSelect", "tests", "idTest", "nombreTest", "idMateria", selectedItem.test.idMateria);
		                   llenarSelect("preguntaSelect", "preguntas", "idPregunta", "textoPregunta", "idTest", selectedItem.test.idTest);
		               }
		           }
		       });
		   } else {
		       document.getElementById("selectElementoContainer").style.display = "none";
		       document.getElementById("activoContainer").style.display = "none";
		   }
		document.getElementById("dynamicForm").onsubmit = function(event) {
			event.preventDefault();
			guardarElemento();
		};

		new bootstrap.Modal(document.getElementById("modalFormulario")).show();
	};


	window.guardarElemento = function() {
		const id = document.getElementById("elementId").value;
		const nombre = document.getElementById("nombreElemento").value.trim();
		const activa = document.getElementById("activoCheckbox").checked;
		if (!nombre) return;

		let payload = {};
		let apiUrl = `/admin/${selectedSection.replace('Section', '')}`;

		if (selectedSection === "materiasSection") {
			payload = { idMateria: id || null, nombreMateria: nombre, activa };
		} else if (selectedSection === "testsSection") {
			payload = {
				idTest: id || null,
				nombreTest: nombre,
				idMateria: document.getElementById("materiaSelect").value,
				activa
			};
		} else if (selectedSection === "preguntasSection") {
			payload = {
				idPregunta: id || null,
				textoPregunta: nombre,
				test: { idTest: document.getElementById("testSelect").value }
			};
		} else if (selectedSection === "respuestasSection") {
			payload = {
				idRespuesta: id || null,
				textoRespuesta: nombre,
				textoExplicacion: document.getElementById("explicacionRespuesta").value.trim(),
				nota: document.querySelector("input[name='respuestaCorrecta']:checked") ? 1.0 : 0.0,
				pregunta: { idPregunta: document.getElementById("preguntaSelect").value }
			};
		}

		console.log(`📡 Guardando elemento en ${apiUrl}`, payload);

		if (id) apiUrl += `/${id}`;

		sendRequest(apiUrl, id ? "PUT" : "POST", payload).then(() => {
			actualizarSessionStorage(selectedSection);
			new bootstrap.Modal(document.getElementById("modalFormulario")).hide();
			alert(id ? "Elemento actualizado" : "Elemento creado");
		});
	};

	window.eliminarElemento = function() {
	    console.log("🗑 Eliminando elemento en:", selectedSection);

	    const selectEliminar = document.getElementById("selectEliminar");
	    const selectEliminarContainer = document.getElementById("selectEliminarContainer");
	    selectEliminar.innerHTML = ""; // Limpiar opciones previas

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

	    // Llenar select con elementos disponibles
	    llenarSelect("selectEliminar", storageKey, idField, nameField);
	    console.log("📌 Opciones cargadas en selectEliminar:", data);

	    // Mostrar el contenedor del select
	    selectEliminarContainer.style.display = "block";

	    // Mostrar modal de eliminación
	    const modalEliminar = new bootstrap.Modal(document.getElementById("modalEliminar"), {
	        backdrop: "static", // No se cierra al hacer clic fuera
	        keyboard: false // No se cierra con ESC
	    });

	    modalEliminar.show();
	    console.log("📌 Modal de eliminación abierto correctamente.");

	    // Asegurar que el botón de confirmación está vinculado correctamente
	    document.getElementById("btnConfirmarEliminar").onclick = function () {
	        confirmarEliminar(modalEliminar);
	    };
	};

	window.confirmarEliminar = function (modalEliminar) {
	    const selectEliminar = document.getElementById("selectEliminar");
	    const idSeleccionado = selectEliminar.value;
	    const mensajeEliminar = document.getElementById("mensajeEliminar");
		const advertenciaEliminar = document.getElementById("advertenciaEliminar");
		const btnCancelarEliminar = document.getElementById("btnCancelarEliminar");
		const btnConfirmarEliminar = document.getElementById("btnConfirmarEliminar");
	    if (!idSeleccionado) {
	        alert("⚠ Debes seleccionar un elemento para eliminar.");
	        return;
	    }

	    console.log("🗑 Confirmando eliminación de ID:", idSeleccionado);
	    const apiUrl = `/admin/${selectedSection.replace("Section", "")}/${idSeleccionado}`;

	    sendRequest(apiUrl, "DELETE").then((response) => {
	        if (response !== undefined) {
	            console.log(`✅ Elemento ${idSeleccionado} eliminado correctamente.`);

	            // Actualizar datos en sessionStorage
	            actualizarSessionStorage(selectedSection);
				advertenciaEliminar.style.display = "none";
				btnCancelarEliminar.style.display = "none";
				btnConfirmarEliminar.style.display = "none";
	            // Mostrar mensaje de éxito
	            mensajeEliminar.textContent = "✅ Elemento eliminado correctamente.";
	            mensajeEliminar.style.display = "block"; // Mostrar el mensaje

	            // Cerrar el modal después de un breve retraso
	            setTimeout(() => {
	                modalEliminar.hide();
	                console.log("📌 Modal de eliminación cerrado.");
	            }, 1500);
	        } else {
	            console.error("❌ No se pudo eliminar el elemento.");
	        }
	    }).catch(error => {
	        console.error("❌ Error al eliminar:", error);
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
			console.log(cambio)
	        let apiUrl = selectedSection === "materiasSection" ? "/admin/materias/" : "/admin/tests/";
	        apiUrl += cambio.id + "/toggle-activa";

	        sendRequest(apiUrl, "PUT").then(() => {
	            console.log(`✅ Estado cambiado para ${cambio.id}: ${cambio.activa}`);
	        });
	    });

	    alert("✅ Cambios guardados correctamente.");
	    actualizarSessionStorage(selectedSection);
	    document.getElementById("guardarCambiosEstado").style.display = "none";
	};














});
