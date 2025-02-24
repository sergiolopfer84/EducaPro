let selectedSection = "";
const modalLabel = document.getElementById("modalLabel");

///NUEVO ELEMENTO
const inputNuevoElemento = document.getElementById("labelNuevoElemento");
const nombreElementoContainer = document.getElementById("nombreElementoContainer")
const nombreElemento = document.getElementById("nombreElemento");
const elementId = document.getElementById("elementId");

//MODAL
const modalMensaje = document.getElementById("modalMensaje");
const modalMensajeTexto = document.getElementById("modalMensajeTexto");
const modalFormulario = document.getElementById("modalFormulario");
///ELIMINAR
const selectEliminar = document.getElementById("selectEliminar");
const selectEliminarContainer = document.getElementById("selectEliminarContainer");
const mensajeEliminar = document.getElementById("mensajeEliminar")
const btnConfirmarEliminar = document.getElementById("btnConfirmarEliminar")
const advertenciaEliminar = document.getElementById("advertenciaEliminar")
const btnCancelarEliminar = document.getElementById("btnCancelarEliminar")

///ESTADOS
const listaEstados = document.getElementById("listaEstados");
const guardarCambiosEstado = document.getElementById("guardarCambiosEstado")

//MATERIAS
const materiaSelectContainer = document.getElementById("materiaSelectContainer");

//TEST
const testSelectContainer = document.getElementById("testSelectContainer");

//PREGUNTAS
const preguntaSelectContainer = document.getElementById("preguntaSelectContainer");

//RESPUESTAS
const respuestaSelectContainer = document.getElementById("respuestaSelectContainer");
const respuestasContainer = document.getElementById("respuestasContainer")
const inputTextoRespuesta = document.getElementById("textoRespuesta")
const inputTextoExplicacion = document.getElementById("textoExplicacion")
const notaLabel = document.getElementById("notaLabel")
const nota = document.getElementById("nota")
const explicacionLabel = document.getElementById("explicacionLabel")
const respuestaLabel = document.getElementById("respuestaLabel")

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
			showSection(section);
		});
	});

	// Event Listeners para los botones de cada sección
	document.getElementById("accionesContainer").querySelectorAll("button").forEach(btn => {
		btn.addEventListener("click", function() {
			manejarAccion(this.getAttribute("onclick"))

		});
	});

	// Listener para guardar en los formularios
	document.getElementById("dynamicForm").addEventListener("submit", function(event) {
		event.preventDefault();
		guardarDatos();
	});

	const modalEliminarEl = document.getElementById("modalEliminar");
	modalEliminarEl.addEventListener("hidden.bs.modal", function() {
		document.querySelectorAll('.modal-backdrop').forEach(el => el.remove());
		selectEliminarContainer.innerHTML = "";
		mensajeEliminar.textContent = "";
		mensajeEliminar.style.display = "none";

	});
});


/*******************************ALMACENAR SESIÓN DDBB*************************************************** */
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

			})
			.catch(error => console.error(`Error cargando ${key}:`, error));
	});
}

/************************SOLICITUD AL BACKEND PARA ALMACENAR DATOS******************************************** */
async function sendRequest(url, method = "GET", body = null) {
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
		if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);

		return response.status === 204 ? null : await response.json();
	} catch (error) {
		console.error(`❌ Error en la petición: ${error.message}`);
		alert(`Error: ${error.message}`);
	}
}
////////////////////////////////////////////////////////////////////////
/*************************ACTUALIZA LA SESION PERO PENDIENTE DE VER ******************************************* */
///////////////////////////revisar necesidad/////////////////////////
function actualizarSessionStorage(section) {
	const storageKey = section.replace("Section", "");
	sendRequest(`/${storageKey}`, "GET").then(data => {

		sessionStorage.setItem(storageKey, JSON.stringify(data));
	});
}

/*****************************MUESTRA LA SECCIÓN Y LOS BOTONES QUE CORRESPONDEN A LA MISMA****************************/
function showSection(sectionId) {
	document.querySelectorAll(".admin-section").forEach(section => {
		section.style.display = "none";
	});

	document.getElementById(sectionId).style.display = "block";

	document.getElementById("accionesContainer").style.display = "block";

	document.querySelector(".btn-info").style.display =
		(sectionId === "materiasSection" || sectionId === "testsSection") ? "inline-block" : "none";

	selectedSection = sectionId;
}

/***************************ACCIÓN A REALIZAR SEGÚN EL BOTÓN PULSADO******************************************/

function manejarAccion(accion) {
	const section = document.querySelector(".admin-section[style='display: block;']");
	if (!section) return;

	const sectionId = section.id;
	if (accion.includes("openFormModal")) {
		abrirFormulario(sectionId, accion.includes("edit") ? "editar" : "nuevo");
	} else if (accion.includes("eliminarElemento")) {
		abrirEliminarModal(sectionId);
	} else if (accion.includes("mostrarListaEstados")) {
		mostrarListaEstados(sectionId);
	}
}

/*************************LIMMPIA EL FORMULAARIO PARA LOS NUEVOS DATOS********************************************/

function limpiarFormulario() {

	document.getElementById("dynamicForm").reset();
	materiaSelectContainer.innerHTML = "";
	testSelectContainer.innerHTML = "";
	preguntaSelectContainer.innerHTML = "";
	modalLabel.textContent = ""
	inputNuevoElemento.textContent = ""
}

/******************************CONFIGURA LAS LABEL DEL MODAL************************************************** */
function configurarModal(sectionId, accion, cargarSelectsEnCascada) {
	limpiarFormulario();

	switch (sectionId) {
		case "materiasSection":
			modalLabel.textContent = (accion === "editar") ? "MODIFICAR MATERIA" : (accion === "eliminar") ? "ELIMINAR MATERIA" : "NUEVA MATERIA";
			inputNuevoElemento.textContent = (accion === "editar") ? "Nuevo nombre de la materia:" : (accion === "crear") ? "Nombre de la materia:" : "";
			nombreElementoContainer.style.display = (accion === "editar" || accion === "crear") ? "block" : "none";
			materiaSelectContainer.style.display = (accion === "editar" || accion === "crear") ? "none" : "block";
			break;
		case "testsSection":
			modalLabel.textContent = (accion === "editar") ? "MODIFICAR TEST" : (accion === "eliminar") ? "ELIMINAR TEST" : "NUEVO TEST";
			inputNuevoElemento.textContent = (accion === "editar") ? "Nuevo nombre del test:" : (accion === "crear") ? "Nombre del test:" : "";
			nombreElementoContainer.style.display = (accion === "editar" || accion === "crear") ? "block" : "none";
			testSelectContainer.style.display = (accion === "crear") ? "none" : "block";
			break;
		case "preguntasSection":
			modalLabel.textContent = (accion === "editar") ? "MODIFICAR PREGUNTA" : (accion === "eliminar") ? "ELIMINAR PREGUNTA" : "NUEVA PREGUNTA";
			inputNuevoElemento.textContent = (accion === "editar") ? "Nuevo texto de la pregunta:" : (accion === "crear") ? "Contenido de la pregunta:" : "";
			nombreElementoContainer.style.display = (accion === "eliminar") ? "none" : "block";
			preguntaSelectContainer.style.display = (accion === "crear") ? "none" : "block";
			break;
		case "respuestasSection":
			modalLabel.textContent = (accion === "editar") ? "MODIFICAR RESPUESTA" : (accion === "eliminar") ? "ELIMINAR RESPUESTA" : "NUEVA RESPUESTA";
			nombreElementoContainer.style.display = "none";
			respuestaSelectContainer.style.display = (accion === "editar" || accion === "crear") ? "block" : "none";
			respuestasContainer.style.display = (accion === "eliminar") ? "none" : "block";
			respuestaLabel.style.display = "block"
			explicacionLabel.style.display = "block"
			notaLabel.style.display = "block"
			inputTextoRespuesta.style.display = "block"
			inputTextoExplicacion.style.display = "block"
			nota.style.display = "block"
			respuestaLabel.textContent = (accion === "editar") ? "Modifica la respuesta" : "Nueva respuesta";
			explicacionLabel.textContent = (accion === "editar") ? "Modifica la explicación" : "Nueva explicación";
			notaLabel.textContent = (accion === "editar") ? "Modifica la nota" : "Nueva nota";
			break;
		default:
			console.error("Sección no reconocida:", sectionId);
	}
	/********************************MUESTRA LOS SELECT EN CASCADA *************************************/
	cargarSelectsEnCascada(accion);
	if (accion === "crear") {
		elementId.value = "";
		nombreElemento.value = "";
	}
	new bootstrap.Modal(document.getElementById("modalFormulario")).show();
}

/***************************ABRE EL MODAAL SEGUN NECESIDAD******************************************/

function abrirFormulario(sectionId, tipo) {
	limpiarFormulario();
	let accion = (tipo === "edit") ? "editar" : "crear";
	if (sectionId === "materiasSection") {
		configurarModal("materiasSection", accion, cargarSelectsMaterias);
	} else if (sectionId === "testsSection") {
		configurarModal("testsSection", accion, cargarSelectsTests);
	} else if (sectionId === "preguntasSection") {
		configurarModal("preguntasSection", accion, cargarSelectsPreguntas);
	} else if (sectionId === "respuestasSection") {
		configurarModal("respuestasSection", accion, cargarSelectsRespuestas);
	}
}


/*************************CARGGA LOS DATOS DE SESION EN EL SELECT********************************************/



/***********************PRECARGA UN SELECT CON LAS MATERIAS **********************************************/
function cargarSelectsMaterias(accion) {
	const containerId = (accion === "eliminar") ? "selectEliminarContainer" : "materiaSelectContainer";
	const selectId = (accion === "eliminar") ? "selectEliminar" : "materiaSelect";

	// Si no es "editar", vaciamos el elementId
	if (accion !== "editar") {
		elementId.value = "";
	}

	// Cargamos el select
	cargarMateriasEnSelect(containerId, selectId, function() {
		const containerEl = document.getElementById(containerId);
		const selectEl = document.getElementById(selectId);

		containerEl.style.display = (accion === "crear") ? "none" : "block";

		if (accion === "editar") {
			// Deshabilitamos el botón de guardar al inicio
			const btnGuardar = document.querySelector("#dynamicForm button[type='submit']");
			if (btnGuardar) {
				btnGuardar.disabled = true;
			}

			selectEl.addEventListener("change", function() {
				const idMateriaSeleccionada = this.value;
				const materiaSeleccionada = this.options[this.selectedIndex].text;

				if (idMateriaSeleccionada) {
					elementId.value = idMateriaSeleccionada;
					nombreElemento.value = materiaSeleccionada;
					// Al haber seleccionado algo válido, habilitas "Guardar"
					if (btnGuardar) btnGuardar.disabled = false;
				} else {
					elementId.value = "";
					nombreElemento.value = "";
					// Si quita la selección, bloqueas el guardado otra vez
					if (btnGuardar) btnGuardar.disabled = true;
				}
			});
		}
	});
}


function cargarMateriasEnSelect(containerId, selectId, callback) {
	const container = document.getElementById(containerId);
	if (!container) {
		console.error(`❌ El contenedor con ID "${containerId}" no existe.`);
		return;
	}

	container.innerHTML = `<label>Selecciona Materia:</label>
                           <select id="${selectId}" class="form-control"></select>`;
	const selectElement = document.getElementById(selectId);
	const materias = JSON.parse(sessionStorage.getItem("materias")) || [];
	if (materias.length === 0) {
		container.innerHTML = "<p class='text-danger'>No hay materias disponibles.</p>";
		return;
	}
	selectElement.innerHTML = `<option value="">Seleccione una materia</option>`;
	materias.forEach(m => {
		const option = document.createElement("option");
		option.value = m.idMateria;
		option.textContent = m.nombreMateria;
		selectElement.appendChild(option);
	});
	container.style.display = "block";
	if (callback) callback();
}


/***********************PRECARGA UN SELECT CON LOS TEST FILTRADOS POR MATERIA **********************************************/
function cargarSelectsTests(accion) {
	const containerId = (accion === "eliminar") ? "selectEliminarContainer" : "testSelectContainer";
	const selectId = (accion === "eliminar") ? "selectEliminar" : "testSelect";
	let materiaContainer = (accion === "eliminar") ? "selectEliminarContainer" : "materiaSelectContainer";
	let materiaSelectId = (accion === "eliminar") ? "selectEliminar" : "materiaSelect";

	if (accion !== "editar") {
		elementId.value = "";
	}
	const btnGuardar = document.querySelector("#dynamicForm button[type='submit']");
	if (btnGuardar) {
		btnGuardar.disabled = true;
	}

	cargarMateriasEnSelect(materiaContainer, materiaSelectId, function() {
		document.getElementById(materiaSelectId).addEventListener("change", function() {
			let idMateria = this.value;

			if (idMateria) {
				if (accion === "crear" && btnGuardar) {
					btnGuardar.disabled = false;
				}
				if (accion === "editar" || accion === "eliminar") {
					cargarTestsEnSelectFiltrado(containerId, selectId, idMateria, function() {
						const selectTest = document.getElementById(selectId);

						selectTest.addEventListener("change", function() {
							let idTestSeleccionado = this.value;
							let testSeleccionado = this.options[this.selectedIndex]?.text || "";

							if (idTestSeleccionado) {
								elementId.value = idTestSeleccionado;
								nombreElemento.value = testSeleccionado;

								if (accion === "editar" && btnGuardar) {
									btnGuardar.disabled = false;
								}
							} else {
								elementId.value = "";
								nombreElemento.value = "";

								if (accion === "editar" && btnGuardar) {
									btnGuardar.disabled = true;
								}
							}
						});
					});
				}
			} else {
				elementId.value = "";
				nombreElemento.value = "";
				if (btnGuardar) {
					btnGuardar.disabled = true;
				}
			}
		});
	});
}


function cargarTestsEnSelectFiltrado(containerId, selectId, idMateria, callback) {
	const container = document.getElementById(containerId);

	if (!container) {
		console.error(`❌ El contenedor con ID "${containerId}" no existe.`);
		return;
	}
	// 📌 Limpiar el contenido previo antes de insertar el nuevo select
	container.innerHTML = `<label>Selecciona Test:</label>
                           <select id="${selectId}" class="form-control"></select>`;
	const selectElement = document.getElementById(selectId);

	const tests = JSON.parse(sessionStorage.getItem("tests")) || [];
	const testsFiltrados = tests.filter(t => t.idMateria === parseInt(idMateria, 10));

	if (testsFiltrados.length === 0) {
		container.innerHTML = "<p class='text-danger'>No hay tests disponibles para esta materia.</p>";
		return;
	}
	// 📌 Insertar opciones en el select
	selectElement.innerHTML = `<option value="">Seleccione un test</option>`;
	testsFiltrados.forEach(t => {
		const option = document.createElement("option");
		option.value = t.idTest;
		option.textContent = t.nombreTest;
		selectElement.appendChild(option);
	});

	container.style.display = "block";

	if (callback) callback();
}




/***********************PRECARGA UN SELECT CON LAS PREGUNTAS FILTRADAS POR TEST **********************************************/
function cargarSelectsPreguntas(accion) {
	const containerId = (accion === "eliminar") ? "selectEliminarContainer" : "preguntaSelectContainer";
	const selectId = (accion === "eliminar") ? "selectEliminar" : "preguntaSelect";
	const materiaContainer = (accion === "eliminar") ? "selectEliminarContainer" : "materiaSelectContainer";
	const materiaSelectId = (accion === "eliminar") ? "selectEliminar" : "materiaSelect";
	const testContainer = (accion === "eliminar") ? "selectEliminarContainer" : "testSelectContainer";
	const testSelectId = (accion === "eliminar") ? "selectEliminar" : "testSelect";
	if (accion !== "editar") {
		elementId.value = "";
	}

	const btnGuardar = document.querySelector("#dynamicForm button[type='submit']");

	if (btnGuardar) {
		btnGuardar.disabled = true;
	}

	cargarMateriasEnSelect(materiaContainer, materiaSelectId, function() {
		const selectMateria = document.getElementById(materiaSelectId);

		selectMateria.addEventListener("change", function() {
			let idMateria = this.value;

			if (idMateria) {
				document.getElementById(testContainer).style.display = "block";

				cargarTestsEnSelectFiltrado(testContainer, testSelectId, idMateria, function() {
					const selectTest = document.getElementById(testSelectId);

					selectTest.addEventListener("change", function() {
						let idTest = this.value;

						if (idTest) {
							if (accion === "crear" && btnGuardar) {
								btnGuardar.disabled = false;
							}
							if (accion === "editar" || accion === "eliminar") {
								cargarPreguntasEnSelectFiltrado(containerId, selectId, idTest, function() {
									const selectPregunta = document.getElementById(selectId);
									if (accion === "editar") {
										selectPregunta.addEventListener("change", function() {
											const idPreguntaSeleccionada = this.value;
											const preguntaSeleccionada = this.options[this.selectedIndex]?.text || "";

											if (idPreguntaSeleccionada) {
												elementId.value = idPreguntaSeleccionada;
												nombreElemento.value = preguntaSeleccionada;

												if (btnGuardar) {
													btnGuardar.disabled = false;
												}

											} else {
												elementId.value = "";
												nombreElemento.value = "";

												if (btnGuardar) {
													btnGuardar.disabled = true;
												}
											}
										});
									}
								});
							}
						} else {
							elementId.value = "";
							nombreElemento.value = "";

							if (accion === "editar" && btnGuardar) {
								btnGuardar.disabled = true;
							}
						}
					});
				});
			} else {
				elementId.value = "";
				nombreElemento.value = "";
				document.getElementById(testContainer).style.display = "none";

				if (accion === "editar" && btnGuardar) {
					btnGuardar.disabled = true;
				}
			}
		});
	});
}



function cargarPreguntasEnSelectFiltrado(containerId, selectId, idTest, callback) {
	const container = document.getElementById(containerId);

	if (!container) {
		console.error(`❌ El contenedor con ID "${containerId}" no existe.`);
		return;
	}
	// 📌 Limpiar el contenido previo antes de insertar el nuevo select
	container.innerHTML = `<label>Selecciona Pregunta:</label>
                           <select id="${selectId}" class="form-control"></select>`;
	const selectElement = document.getElementById(selectId);

	const preguntas = JSON.parse(sessionStorage.getItem("preguntas")) || [];
	const preguntasFiltradas = preguntas.filter(p => p.idTest === parseInt(idTest, 10));

	if (preguntasFiltradas.length === 0) {
		container.innerHTML = "<p class='text-danger'>No hay preguntas disponibles para este test.</p>";
		return;
	}
	// 📌 Insertar opciones en el select
	selectElement.innerHTML = `<option value="">Seleccione una pregunta</option>`;
	preguntasFiltradas.forEach(p => {
		const option = document.createElement("option");
		option.value = p.idPregunta;
		option.textContent = p.textoPregunta;
		selectElement.appendChild(option);
	});
	container.style.display = "block";

	if (callback) callback();
}

/***********************PRECARGA UN SELECT CON LAS RESPUESTAS FILTRADAS POR PREGUNTA **********************************************/

function cargarSelectsRespuestas(accion) {
	const containerId = (accion === "eliminar") ? "selectEliminarContainer" : "respuestaSelectContainer";
	const selectId = (accion === "eliminar") ? "selectEliminar" : "respuestaSelect";
	const materiaContainer = (accion === "eliminar") ? "selectEliminarContainer" : "materiaSelectContainer";
	const materiaSelectId = (accion === "eliminar") ? "selectEliminar" : "materiaSelect";
	const testContainer = (accion === "eliminar") ? "selectEliminarContainer" : "testSelectContainer";
	const testSelectId = (accion === "eliminar") ? "selectEliminar" : "testSelect";
	let preguntaContainer = (accion === "eliminar") ? "selectEliminarContainer" : "preguntaSelectContainer";
	let preguntaSelectId = (accion === "eliminar") ? "selectEliminar" : "preguntaSelect";
	if (accion !== "editar") {
		elementId.value = "";
	}
	const btnGuardar = document.querySelector("#dynamicForm button[type='submit']");

	if (btnGuardar) {
		btnGuardar.disabled = true;
	}
	// ✅ **Cargar el select de materias en el contenedor correcto**
	cargarMateriasEnSelect(materiaContainer, materiaSelectId, function() {
		document.getElementById(materiaSelectId).addEventListener("change", function() {
			const idMateria = this.value;

			if (idMateria) {
				// Cargamos Tests filtrados
				cargarTestsEnSelectFiltrado(testContainer, testSelectId, idMateria, function() {
					document.getElementById(testSelectId).addEventListener("change", function() {
						const idTest = this.value;
						if (idTest) {
							// Cargamos Preguntas filtradas
							cargarPreguntasEnSelectFiltrado(preguntaContainer, preguntaSelectId, idTest, function() {
								document.getElementById(preguntaSelectId).addEventListener("change", function() {
									const idPregunta = this.value;
									if (accion === "crear" && btnGuardar) {
										btnGuardar.disabled = false;
									}
									if (idPregunta && (accion === "editar" || accion === "eliminar")) {
										// Finalmente, cargamos Respuestas filtradas
										cargarRespuestasEnSelectFiltrado(containerId, selectId, idPregunta, function() {
											if (accion === "editar") {
												// Escuchamos el cambio en el select de Respuestas
												document.getElementById(selectId).addEventListener("change", function() {
													const idRespuestaSeleccionada = this.value;
													const respuestaSeleccionada = this.options[this.selectedIndex]?.text || "";

													if (idRespuestaSeleccionada) {
														// Se ha seleccionado una respuesta existente
														elementId.value = idRespuestaSeleccionada;

														// Rellenamos el texto de Respuesta, Explicación y Nota
														const respuestas = JSON.parse(sessionStorage.getItem("respuestas")) || [];
														const respuesta = respuestas.find(
															r => r.idRespuesta == idRespuestaSeleccionada
														);
														if (respuesta) {
															inputTextoRespuesta.value = respuesta.textoRespuesta || "";
															inputTextoExplicacion.value = respuesta.textoExplicacion || "";
															nota.value = respuesta.nota || "0";
														}

														// Habilitamos el botón Guardar
														if (btnGuardar) {
															btnGuardar.disabled = false;
														}
													} else {
														// No se seleccionó ninguna respuesta
														elementId.value = "";
														inputTextoRespuesta.value = "";
														inputTextoExplicacion.value = "";
														nota.value = "0";

														// Volvemos a deshabilitar Guardar
														if (btnGuardar) {
															btnGuardar.disabled = true;
														}
													}
												});
											}
										});
									} else {
										// El usuario ha cambiado algo que anula la selección de respuesta
										elementId.value = "";
										inputTextoRespuesta.value = "";
										inputTextoExplicacion.value = "";
										nota.value = "0";
										if (accion === "editar" && btnGuardar) {
											btnGuardar.disabled = true;
										}
									}
								});
							});
						} else {
							// No se seleccionó Test
							elementId.value = "";
							if (accion === "editar" && btnGuardar) {
								btnGuardar.disabled = true;
							}
						}
					});
				});
			} else {
				// No se seleccionó Materia
				elementId.value = "";
				if (accion === "editar" && btnGuardar) {
					btnGuardar.disabled = true;
				}
			}
		});
	});
}

function cargarRespuestasEnSelectFiltrado(containerId, selectId, idPregunta, callback) {
	const container = document.getElementById(containerId);

	if (!container) {
		console.error(`❌ El contenedor con ID "${containerId}" no existe.`);
		return;
	}

	// 📌 Limpiar el contenido previo antes de insertar el nuevo select
	container.innerHTML = `<label>Selecciona Respuesta:</label>
                           <select id="${selectId}" class="form-control"></select>`;
	const selectElement = document.getElementById(selectId);

	const respuestas = JSON.parse(sessionStorage.getItem("respuestas")) || [];
	const respuestasFiltradas = respuestas.filter(r => r.idPregunta === parseInt(idPregunta, 10));

	if (respuestasFiltradas.length === 0) {
		container.innerHTML = "<p class='text-danger'>No hay respuestas disponibles para esta pregunta.</p>";
		return;
	}

	// 📌 Insertar opciones en el select
	selectElement.innerHTML = `<option value="">Seleccione una respuesta</option>`;
	respuestasFiltradas.forEach(r => {
		const option = document.createElement("option");
		option.value = r.idRespuesta;
		option.textContent = r.textoRespuesta;
		selectElement.appendChild(option);
	});
	container.style.display = "block";

	if (callback) callback();
}


/************************FUNCION PARA CREAR MATERIA*********************************************/

function crearMateria() {
	materiaSelectContainer.innerHTML = "";
	nombreElemento.value = "";
	elementId.value = "";
}
/*************************FUNCION PARA EDITAR********************************************/
function editarMateria() {
	elementId.value = "";
	cargarSelectsMaterias("editar");
}
/************************************CREA TEST*********************************/

function crearTest() {
	elementId.value = "";
	cargarSelectsTests("crear");
}
/****************************EDITA TEST*****************************************/

function editarTest() {
	elementId.value = "";
	cargarSelectsTests("editar");
}
/*****************************CREA PREGUNTA****************************************/
function crearPregunta() {
	elementId.value = "";
	cargarSelectsPreguntas("crear")
}
/****************************EDITAA PREGGUNTA*****************************************/
function editarPregunta() {
	elementId.value = "";
	cargarSelectsPreguntas("editar");
}
/********************************CREA RESPPUESTA*************************************/
function crearRespuesta() {
	elementId.value = "";
	cargarSelectsRespuestas("crear");
}
/******************************EDITA RESPUESTAA***************************************/
function editarRespuesta() {
	elementId.value = "";
	cargarSelectsRespuestas("editar");
}

/**************************FUNCION DE GUARDADO EN BASE DE DATOS*******************************************/
window.guardarDatos = function() {
	if (!selectedSection) {
		mostrarMensajeModal("error", "❌ No hay una sección seleccionada.");
		return;
	}

	const id = elementId?.value || null;
	const nombre = nombreElemento?.value?.trim() || "";

	if (!nombre && selectedSection !== "respuestasSection") {
		mostrarMensajeModal("error", "⚠ Debes ingresar un nombre.");
		return;
	}
	if (selectedSection === "respuestasSection") {
		const respuestaVal = textoRespuesta.value.trim();
		const explicacionVal = textoExplicacion.value.trim();
		const notaVal = nota.value;

		if (!respuestaVal) {
			mostrarMensajeModal("error", "⚠ Debes ingresar un texto de respuesta.");
			return;
		}

		if (!explicacionVal) {
			mostrarMensajeModal("error", "⚠ Debes ingresar una explicación.");
			return;
		}

		if (notaVal === "" || isNaN(parseFloat(notaVal))) {
			mostrarMensajeModal("error", "⚠ Debes seleccionar una nota válida (0 o 1).");
			return;
		}
	}

	let payload = {};
	let apiUrl = `/admin/${selectedSection.replace('Section', '')}`;

	switch (selectedSection) {
		case "materiasSection":
			payload = { idMateria: id, nombreMateria: nombre };
			break;
		case "testsSection":
			payload = { idTest: id, nombreTest: nombre, idMateria: parseInt(document.getElementById("materiaSelect")?.value || 0) };
			break;
		case "preguntasSection":
			payload = { idPregunta: id, textoPregunta: nombre, idTest: parseInt(document.getElementById("testSelect")?.value || 0) };
			break;
		case "respuestasSection":
			payload = {
				idRespuesta: id,
				textoRespuesta: inputTextoRespuesta?.value?.trim() || "",
				textoExplicacion: inputTextoExplicacion?.value?.trim() || "",
				nota: parseFloat(nota?.value) || 0,
				idPregunta: parseInt(document.getElementById("preguntaSelect")?.value || 0)
			};
			break;
		default:
			mostrarMensajeModal("error", "❌ Sección no reconocida.");
			return;
	}



	if (id) apiUrl += `/${id}`;

	sendRequest(apiUrl, id ? "PUT" : "POST", payload)
		.then(() => {
			console.log("Estaammos gguardadno ")
			mostrarMensajeModal("success", id ? "✅ Elemento actualizado correctamente." : "✅ Elemento creado correctamente.", true);
			setTimeout(() => cargarDatosDesdeBackend(), 2500); // 🔄 Actualizar los datos después de cerrar el modal
		})
		.catch(error => {
			mostrarMensajeModal("error", `❌ Error al guardar: ${error.message}`);
		});
};



/************************MOSTRAR ESTADOS ACTIVO / INACTIVO*********************************************/
window.mostrarListaEstados = function() {
	let estadoContainer = document.getElementById("estadoContainer");
	let filtroMateriaContainer = document.getElementById("materiaSelectContainer");
	let selectMateria = document.getElementById("filterMateria");
	let listaEstados = document.getElementById("listaEstados");
	let filterMateriaLabel = document.getElementById("filterMateriaLabel")
	let filterMateriaContainer = document.getElementById("filterMateriaContainer")

	if (!estadoContainer || !filtroMateriaContainer || !selectMateria || !listaEstados) {
		console.error("❌ No se encontraron los elementos necesarios en el DOM.");
		return;
	}

	estadoContainer.style.display = "block";
	listaEstados.innerHTML = "";

	if (selectedSection === "testsSection") {
		filtroMateriaContainer.style.display = "block";
		filterMateriaLabel.style.display = "block";
		selectMateria.style.display = "block";
		filterMateriaContainer.style.margin = "10px"
		cargarMateriasEstados();
		cargarListaEstados();
	} else {
		// Si estamos en Materias, ocultamos el filtro y mostramos las materias directamente
		filterMateriaLabel.style.display = "none";
		selectMateria.style.display = "none";
		cargarListaEstados();
	}
};

function cargarMateriasEstados() {
	const selectMateria = document.getElementById("filterMateria");

	if (!selectMateria) {
		console.error("❌ No se encontró el select de materias.");
		return;
	}

	selectMateria.innerHTML = `<option value="">Todas las Materias</option>`; // Opción por defecto

	const materias = JSON.parse(sessionStorage.getItem("materias")) || [];

	if (materias.length === 0) {
		selectMateria.innerHTML = "<option value=''>No hay materias disponibles</option>";
		return;
	}

	materias.forEach(m => {
		const option = document.createElement("option");
		option.value = m.idMateria;
		option.textContent = m.nombreMateria;
		option.style.margin = "10px"
		selectMateria.appendChild(option);
	});

	// Evento para actualizar la lista de tests al cambiar la materia seleccionada
	selectMateria.addEventListener("change", function() {
		let idMateriaSeleccionada = this.value;
		cargarListaEstados(idMateriaSeleccionada);
	});
}

function cargarListaEstados(idMateria = "") {
	let listaEstados = document.getElementById("listaEstados");
	listaEstados.innerHTML = "";

	let storageKey, idField, nameField;

	if (selectedSection === "testsSection") {
		storageKey = "tests";
		idField = "idTest";
		nameField = "nombreTest";
	} else {
		storageKey = "materias";
		idField = "idMateria";
		nameField = "nombreMateria";
	}

	let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];

	// Si estamos en "Tests" y hay una materia seleccionada, filtramos los tests por esa materia
	if (selectedSection === "testsSection" && idMateria) {
		data = data.filter(test => test.idMateria == idMateria);
	}

	if (data.length === 0) {
		listaEstados.innerHTML = "<p class='text-danger'>⚠ No hay elementos disponibles.</p>";
		return;
	}

	let estadosIniciales = {};

	data.forEach(item => {
		estadosIniciales[item[idField]] = item.activa;
		const estado = item.activa ? "checked" : "";

		let div = document.createElement("div");
		div.classList.add("list-group-item", "d-flex", "justify-content-between", "align-items-center");
		div.innerHTML = `
            <span>${item[nameField]}</span>
            <div class="form-check form-switch">
                <input class="form-check-input estado-toggle" type="checkbox" data-id="${item[idField]}" ${estado}>
            </div>
        `;

		listaEstados.appendChild(div);
	});

	document.getElementById("estadoContainer").style.display = "block";

	document.querySelectorAll(".estado-toggle").forEach(toggle => {
		toggle.addEventListener("change", function() {
			const id = this.getAttribute("data-id");
			const nuevoEstado = this.checked;

			if (nuevoEstado !== estadosIniciales[id]) {
				guardarCambiosEstado.style.display = "block";
			} else {
				let algunCambio = [...document.querySelectorAll(".estado-toggle")].some(t => t.checked !== estadosIniciales[t.getAttribute("data-id")]);
				guardarCambiosEstado.style.display = algunCambio ? "block" : "none";
			}
		});
	});

	window.estadosIniciales = estadosIniciales;
}

window.guardarCambiosEstados = function() {
	let cambios = [];

	document.querySelectorAll(".estado-toggle").forEach(toggle => {
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

	let promesas = cambios.map(cambio => {
		let apiUrl = selectedSection === "materiasSection" ? "/admin/materias/" : "/admin/tests/";
		apiUrl += cambio.id + "/toggle-activa";

		return sendRequest(apiUrl, "PUT", { activa: cambio.activa }).then(() => {

		});
	});

	Promise.all(promesas).then(() => {
		alert("✅ Cambios guardados correctamente.");
		cargarDatosDesdeBackend()
		guardarCambiosEstado.style.display = "none";
	}).catch(error => {
		console.error("❌ Error al actualizar estados:", error);
		alert("❌ Ocurrió un error al guardar los cambios.");
	});
};

function getSectionConfig(sectionId) {
	let config = {};

	switch (sectionId) {
		case "materiasSection":
			config = {
				storageKey: "materias",
				idField: "idMateria",
				nameField: "nombreMateria",
				loadFn: cargarSelectsMaterias
			};
			break;
		case "testsSection":
			config = {
				storageKey: "tests",
				idField: "idTest",
				nameField: "nombreTest",
				loadFn: cargarSelectsTests
			};
			break;
		case "preguntasSection":
			config = {
				storageKey: "preguntas",
				idField: "idPregunta",
				nameField: "textoPregunta",
				loadFn: cargarSelectsPreguntas
			};
			break;
		case "respuestasSection":
			config = {
				storageKey: "respuestas",
				idField: "idRespuesta",
				nameField: "textoRespuesta",
				loadFn: cargarSelectsRespuestas
			};
			break;
		default:
			console.error("❌ Sección no reconocida:", sectionId);
			return null;
	}

	return config;
}



/*************************ABRE EL MODAL PARA ELIMINAR********************************************/
function abrirEliminarModal(sectionId) {
	const apartado = getSectionConfig(sectionId);
	if (!apartado) return;

	const { storageKey, idField, nameField, loadFn } = apartado;

	if (typeof loadFn === "function") {
		loadFn("eliminar");
	}
	const data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
	if (data.length === 0) {
		selectEliminarContainer.innerHTML = "<p class='text-danger'>No hay elementos disponibles para eliminar.</p>";
		return;
	}
	mensajeEliminar.textContent = "";
	mensajeEliminar.style.display = "none";
	advertenciaEliminar.style.display = "block";
	btnCancelarEliminar.style.display = "inline-block";
	btnConfirmarEliminar.style.display = "inline-block";
	const modalEliminar = new bootstrap.Modal(document.getElementById("modalEliminar"), {
		backdrop: "static",
		keyboard: false
	});

	modalEliminar.show();
	btnConfirmarEliminar.onclick = function() {
		confirmarEliminar(modalEliminar);
	};

}
/**********************ELIMINAR ELEMENTO***********************************************/

window.eliminarElemento = function() {

	const apartado = getSectionConfig(sectionId);
	if (!apartado) return;

	const { storageKey, idField, nameField, loadFn } = apartado;
	let data = JSON.parse(sessionStorage.getItem(storageKey)) || [];

	if (data.length === 0) {
		alert("⚠ No hay elementos disponibles para eliminar.");
		return;
	}

	// Llenar select con elementos disponibles
	llenarSelect("selectEliminar", storageKey, idField, nameField);

	// Mostrar el contenedor del select
	selectEliminarContainer.style.display = "block";

	// Mostrar modal de eliminación
	const modalEliminar = new bootstrap.Modal(document.getElementById("modalEliminar"), {
		backdrop: "static", // No se cierra al hacer clic fuera
		keyboard: false // No se cierra con ESC
	});

	modalEliminar.show();

	// Asegurar que el botón de confirmación está vinculado correctamente
	btnConfirmarEliminar.onclick = function() {
		confirmarEliminar(modalEliminar);
	};
};

window.confirmarEliminar = function(modalEliminar) {
	const selectEliminar = document.getElementById("selectEliminar");
	const idSeleccionado = selectEliminar.value;
	console.log(idSeleccionado)
	if (!idSeleccionado) {
		advertenciaEliminar.innerHTML = "<p>⚠ Debes seleccionar un elemento para eliminar. </p>" +
			"<p>IMPORTANTE:: Recuerda que esta acción no se puede deshacer.</p>";

		return;
	}
	advertenciaEliminar.textContent = "⚠ Importante: Esta acción no se puede deshacer.";
	advertenciaEliminar.style.textDecorationLine = "underline"

	const apiUrl = `/admin/${selectedSection.replace("Section", "")}/${idSeleccionado}`;

	sendRequest(apiUrl, "DELETE").then((response) => {
		if (response !== undefined) {


			// **Actualizar datos en sessionStorage**
			cargarDatosDesdeBackend()

			// **Ocultar los botones y mostrar el mensaje de éxito**
			advertenciaEliminar.style.display = "none";
			btnCancelarEliminar.style.display = "none";
			btnConfirmarEliminar.style.display = "none";
			mensajeEliminar.textContent = "✅ Elemento eliminado correctamente.";
			mensajeEliminar.style.display = "block";

			// **Cerrar el modal después de 1.5 segundos**
			setTimeout(() => {
				modalEliminar.hide();

			}, 1500);
		} else {
			console.error("❌ No se pudo eliminar el elemento.");
		}
	}).catch(error => {
		console.error("❌ Error al eliminar:", error);
	});
};

function mostrarMensajeModal(tipo, mensaje, cerrar = false) {


	// Capturar los elementos dentro del modal
	let modalBody = modalFormulario ? modalFormulario.querySelector(".modal-body") : null;
	let modalFooter = modalFormulario ? modalFormulario.querySelector(".modal-footer") : null;

	if (!modalMensaje || !modalMensajeTexto) {
		console.error("❌ No se encontró el contenedor del mensaje en el modal.");
		return;
	}

	// ✅ Mostrar el mensaje antes de cerrar el modal
	modalMensajeTexto.textContent = mensaje;
	modalMensaje.style.display = "block";
	modalMensaje.style.color = tipo === "error" ? "red" : "green";

	// Ocultar el contenido del formulario solo si el tipo es "success"
	if (tipo === "success" && modalBody && modalFooter) {
		modalBody.style.display = "none";
		modalFooter.style.display = "none";
	}

	// ✅ Esperar unos segundos antes de cerrar el modal
	if (cerrar) {
		setTimeout(() => {
			// Mostrar el mensaje de éxito/fracaso un poco antes de cerrar
			modalMensaje.style.display = "block";

			// Ocultar el modal después de 2 segundos
			setTimeout(() => {
				let modal = bootstrap.Modal.getInstance(modalFormulario);
				if (modal) {
					modal.hide();
				}

				// Restaurar los estilos solo si existen
				if (modalBody) modalBody.style.display = "block";
				if (modalFooter) modalFooter.style.display = "block";
				modalMensaje.style.display = "none"; // Ocultar mensaje después de cerrar el modal
			}, 2000);
		}, 500); // ⏳ Mostrar el mensaje durante 0.5s antes de ocultarlo
	}
}



