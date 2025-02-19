let selectedSection = "";
document.addEventListener("DOMContentLoaded", function() {

	if (window.csrf && window.csrf.token && window.csrf.headerName) {
		$.ajaxSetup({
			beforeSend: function(xhr) {
				xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
			},
		});
	}
	cargarDatosDesdeBackend();
	// Event Listeners para las secciones
	document.querySelectorAll(".nav-link").forEach(link => {
		link.addEventListener("click", function(event) {
			event.preventDefault();
			const section = this.getAttribute("onclick").match(/'([^']+)'/)[1];
			console.log("linea 17 section=", section)
			showSection(section);
		});
	});

	// Event Listeners para los botones de cada sección
	document.getElementById("accionesContainer").querySelectorAll("button").forEach(btn => {
		btn.addEventListener("click", function() {
			manejarAccion(this.getAttribute("onclick"))
			console.log("linea 26 this.getAttribute(onclick)=", this.getAttribute("onclick"))

		});
	});

	// Listener para guardar en los formularios
	document.getElementById("dynamicForm").addEventListener("submit", function(event) {
		event.preventDefault();
		guardarDatos();
	});
});



/**
 * Carga datos desde el backend y los almacena en sessionStorage.
 */
function cargarDatosDesdeBackend() {
	const endpoints = {
		"materias": "/admin/materias",
		"tests": "/admin/tests",
		"preguntas": "/admin/preguntas",
		"respuestas": "/admin/respuestas"
	};

	Object.entries(endpoints).forEach(([key, url]) => {
		fetch(url, { method: "GET", headers: { "Content-Type": "application/json" } })
			.then(response => response.json())
			.then(data => {
				sessionStorage.setItem(key, JSON.stringify(data));
				console.log("linea 56 para ${key}=", key, " y url= ", url)
				console.log("linea 57 data =", data)
			})
			.catch(error => console.error(`Error cargando ${key}:`, error));
	});
}
async function sendRequest(url, method = "GET", body = null) {
	console.log(`📡 linea 63 Enviando petición: ${method} ${url}`, body);
	const csrfToken = window.csrf.token;
	if (!csrfToken) {
		console.error("❌ lineaa 66 CSRF Token no encontrado");
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
		console.log(`📩 linea 82  Respuesta de ${url}:`, response);

		if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);

		return response.status === 204 ? null : await response.json();
	} catch (error) {
		console.error(`❌ Error en la petición: ${error.message}`);
		alert(`Error: ${error.message}`);
	}
}


function actualizarSessionStorage(section) {
	const storageKey = section.replace("Section", "");
	console.log(`🔄  linea 96  Actualizando sessionStorage para ${storageKey}`);
	sendRequest(`/${storageKey}`, "GET").then(data => {
		console.log(`📥linea 98  Datos actualizados de ${storageKey}:`, data);
		sessionStorage.setItem(storageKey, JSON.stringify(data));
	});
}
/**
 * Muestra la sección correspondiente y sus botones.
 */
function showSection(sectionId) {
	document.querySelectorAll(".admin-section").forEach(section => {
		section.style.display = "none";
		console.log("linea 108  section.style.display =", section.style.display, " para section ", section)
	});

	document.getElementById(sectionId).style.display = "block";

	console.log("linea 113  section.style.display =", document.getElementById(sectionId).style.display, " para section ", sectionId)

	document.getElementById("accionesContainer").style.display = "block";

	// Ocultar el botón de Mostrar Estado si no es Materias o Tests
	document.querySelector(".btn-info").style.display =
		(sectionId === "materiasSection" || sectionId === "testsSection") ? "inline-block" : "none";

	// ✅ Guardar la sección activa
	selectedSection = sectionId;
	console.log(`🔹 Sección activa: ${selectedSection}`);
}


/**
 * Maneja la acción de cada botón según la sección activa.
 */
function manejarAccion(accion) {
	const section = document.querySelector(".admin-section[style='display: block;']");
	console.log("accion linea 132 ", accion)
	if (!section) return;

	const sectionId = section.id;
	console.log("linea 136  section.id =", sectionId)
	if (accion.includes("openFormModal")) {
		abrirFormulario(sectionId, accion.includes("edit") ? "editar" : "nuevo");
	} else if (accion.includes("eliminarElemento")) {
		abrirEliminarModal(sectionId);
	} else if (accion.includes("mostrarListaEstados")) {
		mostrarListaEstados(sectionId);
	}
}


/**
 * Limpia el formulario antes de abrir el modal.
 */
function limpiarFormulario() {
	let modalLabel = document.getElementById("modalLabel");
	let inputNuevoElemento = document.getElementById("labelNuevoElemento");
	document.getElementById("dynamicForm").reset();
	document.getElementById("materiaSelectContainer").innerHTML = "";
	document.getElementById("testSelectContainer").innerHTML = "";
	document.getElementById("preguntaSelectContainer").innerHTML = "";
	document.getElementById("respuestasLista").innerHTML = "";
	modalLabel.textContent = ""
	inputNuevoElemento.textContent = ""
	console.log("linea 156  Limpiando formulario")
}
/**
 * Abre el formulario modal con los select e inputs adecuados según la sección.
 *    abrirFormulario(sectionId, accion.includes("edit") ? "editar" : "nuevo");
 */
function abrirFormulario(sectionId, tipo) {
	limpiarFormulario();
	let modalLabel = document.getElementById("modalLabel");
	let inputNuevoElemento = document.getElementById("labelNuevoElemento");
	let respuestasListas =document.getElementById("respuestasLista")
	sectionId = (sectionId === 'add' || sectionId === 'edit') ? selectedSection : sectionId;
	console.log("📌 Abriendo formulario en sección:", sectionId);
	console.log("📌 linea 164 Abriendo formulario en sección:", sectionId);
	console.log(" linea 165 tipo ", tipo)
	if (sectionId === "materiasSection") {

		if (tipo === "edit") {

			// Modo editar: cargamos el select de materias
			cargarMateriasEnSelect("materiaSelectContainer", function() {
				console.log("Entrando en la función de cargarMaterias en modo editar");
				// Aseguramos que el contenedor del select se muestre
				document.getElementById("materiaSelectContainer").style.display = "block";
				// Mostramos también el contenedor del input de nombre, pues lo vamos a rellenar
				modalLabel.textContent = "MODIFICAR MATERIA"
				inputNuevoElemento.textContent = "Nuevo nombre de la materia:"
				document.getElementById("nombreElementoContainer").style.display = "block";
				// Obtenemos el select ya cargado
				const materiaSelect = document.getElementById("materiaSelect");
				// Si hay una opción seleccionada, prellenamos los inputs con esos datos
				if (materiaSelect.value !== "") {
					document.getElementById("elementId").value = materiaSelect.value;
					document.getElementById("nombreElemento").value = materiaSelect.options[materiaSelect.selectedIndex].text;
				}
				// Si el usuario cambia la selección, actualizamos los inputs correspondientes
				materiaSelect.addEventListener("change", function() {
					if (this.value !== "") {
						document.getElementById("elementId").value = this.value;
						document.getElementById("nombreElemento").value = this.options[this.selectedIndex].text;
					} else {
						document.getElementById("elementId").value = "";
						document.getElementById("nombreElemento").value = "";
					}
				});
			});
		} else {
			modalLabel.textContent = "NUEVA MATERIA"
			inputNuevoElemento.textContent = "Nombre de la materia:"
			// Modo agregar: ocultamos el select y dejamos visible el input para escribir el nombre
			document.getElementById("materiaSelectContainer").style.display = "none";
			document.getElementById("nombreElementoContainer").style.display = "block";
			document.getElementById("nombreElemento").value = "";
			document.getElementById("elementId").value = "";
		}
	} else if (sectionId === "testsSection") {

		if (tipo === "edit") {
			modalLabel.textContent = "MODIFICAR TEST"
			inputNuevoElemento.textContent = "Nuevo nombre del test:"
			document.getElementById("materiaSelectContainer").style.display = "block";
			// Mostramos también el contenedor del input de nombre, pues lo vamos a rellenar
			document.getElementById("nombreElementoContainer").style.display = "block";
			// Una vez que se cargue el select de materias, añadimos un listener:
			// Modo editar: cargamos el select de materias
			cargarMateriasEnSelect("materiaSelectContainer", function() {
				console.log("Entrando en la función de cargarMaterias en modo editar TEST");
				// Obtenemos el select ya cargado
				// Aseguramos que el contenedor del select se muestre
				document.getElementById("materiaSelectContainer").style.display = "block";
				// Mostramos también el contenedor del input de nombre, pues lo vamos a rellenar
				document.getElementById("nombreElementoContainer").style.display = "block";

				// Una vez que se cargue el select de materias, añadimos un listener:
				document.getElementById("materiaSelect").addEventListener("change", function() {

					let idMateria = this.value;
					console.log("linea 181 idMateria ", idMateria)
					// Aquí llamamos a una función que filtre los tests por la materia seleccionada:
					cargarTestsEnSelectFiltrado("testSelectContainer", idMateria);
					document.getElementById("testSelect").addEventListener("change", function() {
						let idTest = this.value;
						console.log("linea 186 idTest ", idTest)
						if (this.value !== "") {
							document.getElementById("elementId").value = this.value;
							document.getElementById("nombreElemento").value = this.options[this.selectedIndex].text;
						} else {
							document.getElementById("elementId").value = "";
							document.getElementById("nombreElemento").value = "";
						}

					})
				});
			});


		} else {
			modalLabel.textContent = "MODIFICAR TEST"
			inputNuevoElemento.textContent = "Nuevo nombre del test:"
			// Modo agregar: ocultamos el select y dejamos visible el input para escribir el nombre
			cargarMateriasEnSelect("materiaSelectContainer");
			console.log("✅  171 Llamando a cargarMateriasEnSelect para Tests");
			document.getElementById("materiaSelectContainer").style.display = "block";
			document.getElementById("nombreElementoContainer").style.display = "block";
			document.getElementById("nombreElemento").value = "";
			document.getElementById("elementId").value = "";
		}

	} else if (sectionId === "preguntasSection") {

		if (tipo === "edit") {

			modalLabel.textContent = "MODIFICAR PREGUNTA"
			inputNuevoElemento.textContent = "Nuevo texto de la pregunta:"
			document.getElementById("materiaSelectContainer").style.display = "block";
			// Mostramos también el contenedor del input de nombre, pues lo vamos a rellenar
			document.getElementById("nombreElementoContainer").style.display = "block";

			cargarMateriasEnSelect("materiaSelectContainer", function() {
				// Una vez que se cargue el select de materias, añadimos un listener:
				document.getElementById("materiaSelect").addEventListener("change", function() {
					let idMateria = this.value;
					console.log("linea 196 idMateria ", idMateria)
					// Aquí llamamos a una función que filtre los tests por la materia seleccionada:
					cargarTestsEnSelectFiltrado("testSelectContainer", idMateria);
					document.getElementById("testSelect").addEventListener("change", function() {
						let idTest = this.value;
						console.log("linea 201 idTest ", idTest)
						cargarPreguntasEnSelectFiltrado("preguntaSelectContainer", idTest)
						document.getElementById("preguntaSelect").addEventListener("change", function() {
							let idPregunta = this.value;
							console.log("linea 205 idPregunta ", idPregunta)
							if (this.value !== "") {
								document.getElementById("elementId").value = this.value;
								document.getElementById("nombreElemento").value = this.options[this.selectedIndex].text;
							} else {
								document.getElementById("elementId").value = "";
								document.getElementById("nombreElemento").value = "";
							}

						})
					})
				});
			});


		} else {
			// Modo agregar: ocultamos el select y dejamos visible el input para escribir el nombre
			modalLabel.textContent = "NUEVA PREGUNTA"
						inputNuevoElemento.textContent = "Contenido de la pregunta:"
			document.getElementById("materiaSelectContainer").style.display = "block";
			// Mostramos también el contenedor del input de nombre, pues lo vamos a rellenar
			document.getElementById("nombreElementoContainer").style.display = "block";

			cargarMateriasEnSelect("materiaSelectContainer", function() {

				document.getElementById("materiaSelectContainer").style.display = "block";
				// Mostramos también el contenedor del input de nombre, pues lo vamos a rellenar
				document.getElementById("nombreElementoContainer").style.display = "block";
				// Una vez que se cargue el select de materias, añadimos un listener:
				document.getElementById("materiaSelect").addEventListener("change", function() {
					let idMateria = this.value;
					console.log("linea 196 idMateria ", idMateria)
					// Aquí llamamos a una función que filtre los tests por la materia seleccionada:
					cargarTestsEnSelectFiltrado("testSelectContainer", idMateria);
					document.getElementById("testSelect").addEventListener("change", function() {
						let idTest = this.value;
						console.log("linea 201 idTest ", idTest)

						
					})
				});
			});


		}

	} else if (sectionId === "respuestasSection") {
		document.getElementById("respuestasContainer").display = "block"
			document.getElementById("nombreElementoContainer").style.display = "none";
		if (tipo === "edit") {
			modalLabel.textContent = "MODIFICAR RESPUESTA"

			// Cargar select de materias con cascada similar:
			cargarMateriasEnSelect("materiaSelectContainer", function() {
				// Una vez que se cargue el select de materias, añadimos un listener:
				document.getElementById("materiaSelect").addEventListener("change", function() {
					let idMateria = this.value;
					console.log("linea 196 idMateria ", idMateria)
					// Aquí llamamos a una función que filtre los tests por la materia seleccionada:
					cargarTestsEnSelectFiltrado("testSelectContainer", idMateria);
					document.getElementById("testSelect").addEventListener("change", function() {
						let idTest = this.value;
						console.log("linea 201 idTest ", idTest)
						cargarPreguntasEnSelectFiltrado("preguntaSelectContainer", idTest)
						document.getElementById("preguntaSelect").addEventListener("change", function() {
							let idPregunta = this.value;
							console.log("linea 205 idPregunta ", idPregunta)
							cargarRespuestasEnSelectFiltrado("respuestaSelectContainer", idTest)
							document.getElementById("respuestaSelect").addEventListener("change", function() {
								let idRespuesta = this.value;
								console.log("linea 205 idPregunta ", idPregunta)


							})

						})
					})
				});
			});

			// En vez de mostrar el input genérico para "nombreElemento", ocultarlo y mostrar el div de respuestas:
			respuestasListas.style.display = "block";
			let html = `<div id="respuestasDiv">
                  <div class="mb-3">
                    <label>Respuesta modificada:</label>
                    <input type="text" class="form-control" id="textoRespuesta" required>
                  </div>
                  <div class="mb-3">
                    <label>Explicación modificada:</label>
                    <input type="text" class="form-control" id="textoExplicacion">
                  </div>
                  <div class="mb-3">
                    <label>Nota modificada:</label>
                    <select class="form-control" id="nota">
                        <option value="0">0</option>
                        <option value="1">1</option>
                    </select>
                  </div>
                </div>`;
		respuestasListas.innerHTML = html;
			respuestasListas.style.display = "block";
		} else {
			modalLabel.textContent = "NUEVA RESPUESTA"

			document.getElementById("materiaSelectContainer").style.display = "block";
			// Mostramos también el contenedor del input de nombre, pues lo vamos a rellenar
			

			cargarMateriasEnSelect("materiaSelectContainer", function() {

				document.getElementById("materiaSelectContainer").style.display = "block";
				// Mostramos también el contenedor del input de nombre, pues lo vamos a rellenar
				
				// Una vez que se cargue el select de materias, añadimos un listener:
				document.getElementById("materiaSelect").addEventListener("change", function() {
					let idMateria = this.value;
					console.log("linea 196 idMateria ", idMateria)
					// Aquí llamamos a una función que filtre los tests por la materia seleccionada:
					cargarTestsEnSelectFiltrado("testSelectContainer", idMateria);
					document.getElementById("testSelect").addEventListener("change", function() {
						let idTest = this.value;
						console.log("linea 201 idTest ", idTest)
						cargarPreguntasEnSelectFiltrado("preguntaSelectContainer", idTest)
						document.getElementById("preguntaSelect").addEventListener("change", function() {
							let idPregunta = this.value;
							console.log("linea 205 idPregunta ", idPregunta)

						})
					})
				});
			});
		
			let html = `<div id="respuestasDiv">
						              <div class="mb-3">
						                <label>Nueva respuesta:</label>
						                <input type="text" class="form-control" id="textoRespuesta" required>
						              </div>
						              <div class="mb-3">
						                <label>Nueva explicación:</label>
						                <input type="text" class="form-control" id="textoExplicacion">
						              </div>
						              <div class="mb-3">
						                <label>Nueva nota:</label>
						                <select class="form-control" id="nota">
						                    <option value="0">0</option>
						                    <option value="1">1</option>
						                </select>
						              </div>
						            </div>`;
			respuestasListas.innerHTML = html;
			respuestasListas.style.display = "block";


		}
	}


	new bootstrap.Modal(document.getElementById("modalFormulario")).show();
}



/**
 * Obtiene los datos almacenados en sessionStorage y los inserta en los selects.
 */
function cargarMateriasEnSelect(containerId, callback) {
	const materias = JSON.parse(sessionStorage.getItem("materias")) || [];
	if (materias.length === 0) {
		document.getElementById(containerId).innerHTML = "<p class='text-danger'>No hay materias disponibles.</p>";
		return;
	}
	let selectHtml = `<label>Selecciona Materia:</label>
                      <select id="materiaSelect" class="form-control">
                          <option value="">Seleccione una materia</option>`;
	materias.forEach(m => {
		selectHtml += `<option value="${m.idMateria}">${m.nombreMateria}</option>`;
	});
	selectHtml += `</select>`;
	document.getElementById(containerId).innerHTML = selectHtml;
	// ✅ Hacer visible el contenedor si estaba oculto
	document.getElementById(containerId).style.display = "block";
	if (callback) callback();
}

function cargarTestsEnSelectFiltrado(containerId, idMateria, callback) {
	const tests = JSON.parse(sessionStorage.getItem("tests")) || [];
	// Filtrar tests cuya propiedad idMateria coincida
	const testsFiltrados = tests.filter(t => t.idMateria === parseInt(idMateria, 10));
	if (testsFiltrados.length === 0) {
		document.getElementById(containerId).innerHTML = "<p class='text-danger'>No hay tests disponibles para esta materia.</p>";
		return;
	}
	let selectHtml = `<label>Selecciona Test:</label>
                      <select id="testSelect" class="form-control">
                        <option value="">Seleccione un test</option>`;
	testsFiltrados.forEach(t => {
		selectHtml += `<option value="${t.idTest}">${t.nombreTest}</option>`;
	});
	selectHtml += `</select>`;
	console.log(" linea 266 containerId", containerId)
	console.log(" linea 267 select html ", selectHtml)
	document.getElementById(containerId).innerHTML = selectHtml;
	document.getElementById(containerId).style.display = "block";
	if (callback) callback();
}


function cargarTestsEnSelect(containerId, callback) {
	const tests = JSON.parse(sessionStorage.getItem("tests")) || [];
	if (test.length === 0) {
		document.getElementById(containerId).innerHTML = "<p class='text-danger'>No hay test disponibles.</p>";
		return;
	}
	let select = `<label>Selecciona Test:</label>
	<select class="form-control">
	<option value="">Seleccione un test:</option>`;
	tests.forEach(t =>
		select += `<option value="${t.idTest}">${t.nombreTest}</option>`
	);
	select += `</select>`;
	document.getElementById(containerId).innerHTML = select;
	if (callback) callback();
}

function cargarPreguntasEnSelect(containerId, callback) {
	const preguntas = JSON.parse(sessionStorage.getItem("preguntas")) || [];
	let select = `<label>Selecciona Pregunta:</label>
	<select class="form-control">`;
	preguntas.forEach(p => select += `<option value="${p.idPregunta}">${p.textoPregunta}</option>`);
	select += `</select>`;
	document.getElementById(containerId).innerHTML = select;
	if (callback) callback();
}
function cargarPreguntasEnSelectFiltrado(containerId, idTest, callback) {
	// Obtener las preguntas desde el sessionStorage (no desde "tests")
	const preguntas = JSON.parse(sessionStorage.getItem("preguntas")) || [];

	// Filtrar las preguntas cuyo idTest coincida con el parámetro recibido
	const preguntasFiltradas = preguntas.filter(p => p.idTest === parseInt(idTest, 10));
	console.log("linea 318 preguntasFiltradas ", preguntasFiltradas)
	if (preguntasFiltradas.length === 0) {
		document.getElementById(containerId).innerHTML = "<p class='text-danger'>No hay preguntas disponibles para este test.</p>";
		return;
	}

	let selectHtml = `<label>Selecciona Pregunta:</label>
                      <select id="preguntaSelect" class="form-control">
                        <option value="">Seleccione una pregunta</option>`;

	preguntasFiltradas.forEach(p => {
		selectHtml += `<option value="${p.idPregunta}">${p.textoPregunta}</option>`;
	});

	selectHtml += `</select>`;
	console.log("linea 333 containerId ", containerId)
	console.log("linea 334 selectHtml ", selectHtml)
	document.getElementById(containerId).innerHTML = selectHtml;
	document.getElementById(containerId).style.display = "block";

	if (callback) callback();
}

function cargarRespuestasEnSelectFiltrado(containerId, idPregunta, callback) {
	// Obtener las respuestas desde el sessionStorage (pueden ser DTO o entidades)
	const respuestas = JSON.parse(sessionStorage.getItem("respuestas")) || [];

	// Filtrar las respuestas cuyo idPregunta coincida con el parámetro recibido
	const respuestasFiltradas = respuestas.filter(r => r.idPregunta === parseInt(idPregunta, 10));
	console.log("linea X - respuestasFiltradas:", respuestasFiltradas);

	if (respuestasFiltradas.length === 0) {
		document.getElementById(containerId).innerHTML =
			"<p class='text-danger'>No hay respuestas disponibles para esta pregunta.</p>";
		return;
	}

	// Construir el HTML del select
	let selectHtml = `<label>Selecciona Respuesta:</label>
	                      <select id="respuestaSelect" class="form-control">
	                        <option value="">Seleccione una respuesta</option>`;
	respuestasFiltradas.forEach(r => {
		selectHtml += `<option value="${r.idRespuesta}">${r.textoRespuesta}</option>`;
	});
	selectHtml += `</select>`;

	// Insertar el select en el contenedor y hacerlo visible
	document.getElementById(containerId).innerHTML = selectHtml;
	document.getElementById(containerId).style.display = "block";
	console.log("linea 579 containerId ",containerId)

	if (callback) callback();
}


/**
 * Guarda los datos según la sección activa.
 */
window.guardarDatos = function() {
	console.log("📌 Guardando datos en:", selectedSection);
	// Por ejemplo, al abrir el formulario en la sección de respuestas:
	if (selectedSection === "respuestasSection") {
		document.getElementById("nombreElementoContainer").style.display = "none";
		document.getElementById("nombreElemento").removeAttribute("required");
	} else {
		// En otras secciones, asegúrate de que sea visible y requerido
		document.getElementById("nombreElementoContainer").style.display = "block";
		document.getElementById("nombreElemento").setAttribute("required", "required");
	}

	if (!selectedSection) {
		console.error("❌ No hay una sección seleccionada.");
		return;
	}

	const id = document.getElementById("elementId")?.value || null;
	const nombre = document.getElementById("nombreElemento")?.value.trim();
	const activa = document.getElementById("activoCheckbox")?.checked || false;
	const materiaSelect = document.getElementById("materiaSelect") ? parseInt(document.getElementById("materiaSelect").value) : null
	const testSelect = document.getElementById("testSelect") ? parseInt(document.getElementById("testSelect").value) : null

	if (!nombre && selectedSection != "respuestasSection") {
		alert("⚠ Debes ingresar un nombre.");
		return;
	}

	let payload = {};
	let apiUrl = `/admin/${selectedSection.replace('Section', '')}`;

	switch (selectedSection) {
		case "materiasSection":
			payload = { idMateria: id, nombreMateria: nombre, activa };
			break;
		case "testsSection":
			payload = {
				idTest: id,
				nombreTest: nombre,
				idMateria: materiaSelect,
				activa
			};
			break;
		case "preguntasSection":
			payload = {
				idPregunta: id,
				textoPregunta: nombre,
				idTest: testSelect
			};
			console.log(" linea 359 document.getElementById(testSelect)", testSelect)
			console.log("payload line 356", payload)
			break;
		case "respuestasSection":
			payload = {
				idRespuesta: id,
				textoRespuesta: document.getElementById("textoRespuesta")?.value.trim(),
				textoExplicacion: document.getElementById("textoExplicacion")?.value.trim(),
				nota: parseFloat(document.getElementById("nota")?.value) || 0,
				idPregunta: parseInt(document.getElementById("preguntaSelect")?.value)
			};
			break;
		default:
			console.error("❌ Sección no reconocida:", selectedSection);
			return;
	}

	console.log(`📡 linea 376 Enviando datos a ${apiUrl}`, payload);

	if (id) apiUrl += `/${id}`;

	sendRequest(apiUrl, id ? "PUT" : "POST", payload).then(() => {
		cargarDatosDesdeBackend();
		new bootstrap.Modal(document.getElementById("modalFormulario")).hide();
		alert(id ? "Elemento actualizado" : "Elemento creado");
	});
};



window.mostrarListaEstados = function() {
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

window.guardarCambiosEstados = function() {
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
	document.getElementById("btnConfirmarEliminar").onclick = function() {
		confirmarEliminar(modalEliminar);
	};
};

window.confirmarEliminar = function(modalEliminar) {
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


