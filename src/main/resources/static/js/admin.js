document.addEventListener("DOMContentLoaded", function() {
	

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
			sendRequest(url, "GET").then(data => {
				sessionStorage.setItem(key, JSON.stringify(data));
			});
		});
	}

	cargarDatosDesdeBackend();

	function actualizarSessionStorage(section) {
		const storageKey = section.replace("Section", "");
		sendRequest(`/${storageKey}`, "GET").then(data => {
			sessionStorage.setItem(storageKey, JSON.stringify(data));
		});
	}

	async function sendRequest(url, method = "GET", body = null) {
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
			if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
			return response.status === 204 ? null : await response.json();
		} catch (error) {
		//console.error(`Error: ${error.message}`)
			
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



	function llenarSelect(selectId, storageKey, idField, textField, filterField = null, filterValue = null) {
	    const selectElement = document.getElementById(selectId);
	    if (!selectElement) {
	        return;
	    }
	    selectElement.innerHTML = `<option value="">Seleccione...</option>`;
	    let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
	    if (filterField && filterValue !== null) {
	        data = data.filter(item => {
	            let fieldValue = filterField.split(".").reduce((obj, key) => obj && obj[key] !== undefined ? obj[key] : undefined, item);
	            return fieldValue == filterValue;
	        });
	    }
	    data.forEach(item => {
	        const option = document.createElement("option");
	        option.value = item[idField];
	        option.textContent = item[textField];
	        selectElement.appendChild(option);
	    });

	    const containerId = `${selectId}Container`;
	    const containerElement = document.getElementById(containerId);
	    if (containerElement) {
	        containerElement.style.display = "block";
	        
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

	document.getElementById("materiaSelect").addEventListener("change", function() {
		
		llenarSelect("testSelect", "tests", "idTest", "nombreTest", "materia.idMateria", this.value);
	});

	document.getElementById("testSelect").addEventListener("change", function() {
		
		llenarSelect("preguntaSelect", "preguntas", "idPregunta", "textoPregunta", "idTest", this.value);
	});

	document.getElementById("preguntaSelect").addEventListener("change", function() {
		
		llenarSelect("respuestaSelect", "respuestas", "idRespuesta", "textoRespuesta", "idPregunta", this.value);
	});

	window.openFormModal = function(actionType) {
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
		

		if (actionType === "edit") {
		       selectElemento.addEventListener("change", function() {
		           let selectedItem = data.find(item => item[idField] == this.value);
		           if (selectedItem) {
		               inputId.value = selectedItem[idField];
		               inputNombre.value = selectedItem[nameField];
		               if (selectedSection === "testsSection") {
		                  llenarSelect("testSelect", "tests", "idTest", "nombreTest", "idMateria", selectedItem.idMateria);
		               }
					   if (selectedItem.activa !== undefined || selectedItem.activo !== undefined) {
					   	                   activoCheckbox.checked = selectedItem.activa || selectedItem.activo;
					   	                   activoCheckboxContainer.style.display = "block";
					   	               } else {
					   	                   activoCheckboxContainer.style.display = "none";
					   	               }
		               if (selectedSection === "preguntasSection") {
		                   
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

		

		if (id) apiUrl += `/${id}`;

		sendRequest(apiUrl, id ? "PUT" : "POST", payload).then(() => {
			actualizarSessionStorage(selectedSection);
			new bootstrap.Modal(document.getElementById("modalFormulario")).hide();
		});
	};

	window.eliminarElemento = function() {
	    

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

	    if (data.length === 0) {
	        alert("⚠ No hay elementos disponibles para eliminar.");
	        return;
	    }

	    llenarSelect("selectEliminar", storageKey, idField, nameField);
	    selectEliminarContainer.style.display = "block";

	    const modalEliminar = new bootstrap.Modal(document.getElementById("modalEliminar"), {
	        backdrop: "static", 
	        keyboard: false 
	    });

	    modalEliminar.show();
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
	        console.log("⚠ Debes seleccionar un elemento para eliminar.");
	        return;
	    }

	    
	    const apiUrl = `/admin/${selectedSection.replace("Section", "")}/${idSeleccionado}`;

	    sendRequest(apiUrl, "DELETE").then((response) => {
	        if (response !== undefined) {
	            

	            actualizarSessionStorage(selectedSection);
				advertenciaEliminar.style.display = "none";
				btnCancelarEliminar.style.display = "none";
				btnConfirmarEliminar.style.display = "none";
	            mensajeEliminar.textContent = "✅ Elemento eliminado correctamente.";
	            mensajeEliminar.style.display = "block"; 

	            setTimeout(() => {
	                modalEliminar.hide();
	                
	            }, 1500);
	        } else {
	            
	        }
	    }).catch(error => {
	        
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
	    

	    if (data.length === 0) {
	        listaEstados.innerHTML = "<p class='text-danger'>⚠ No hay elementos disponibles.</p>";
	        return;
	    }

	    let estadosIniciales = {};

	    data.forEach(item => {
	        estadosIniciales[item[idField]] = item.activa; 
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

	    document.getElementById("estadoContainer").style.display = "block";

	    document.querySelectorAll(".estado-toggle").forEach(toggle => {
	        toggle.addEventListener("change", () => {
	            const id = toggle.getAttribute("data-id");
	            if (toggle.checked !== estadosIniciales[id]) {
	                document.getElementById("guardarCambiosEstado").style.display = "block";
	            } else {
	                if (![...document.querySelectorAll(".estado-toggle")].some(t => t.checked !== estadosIniciales[t.getAttribute("data-id")])) {
	                    document.getElementById("guardarCambiosEstado").style.display = "none";
	                }
	            }
	        });
	    });

	    window.estadosIniciales = estadosIniciales;
	};

	window.guardarCambiosEstados = function () {
	    const toggles = document.querySelectorAll(".estado-toggle");
	    let cambios = [];

	    toggles.forEach(toggle => {
	        const id = toggle.getAttribute("data-id");
	        const estadoNuevo = toggle.checked;

	        if (estadoNuevo !== window.estadosIniciales[id]) {
	            cambios.push({ id, activa: estadoNuevo });
	        }
	    });

	    if (cambios.length === 0) {
	        alert("⚠ No hay cambios para guardar.");
	        return;
	    }

	    cambios.forEach(cambio => {
			
	        let apiUrl = selectedSection === "materiasSection" ? "/admin/materias/" : "/admin/tests/";
	        apiUrl += cambio.id + "/toggle-activa";

	        sendRequest(apiUrl, "PUT").then(() => {
	            
	        });
	    });

	   
	    actualizarSessionStorage(selectedSection);
	    document.getElementById("guardarCambiosEstado").style.display = "none";
	};



});