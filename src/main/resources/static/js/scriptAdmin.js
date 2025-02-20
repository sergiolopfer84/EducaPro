let selectedSection = "";
const modalLabel = document.getElementById("modalLabel");
const inputNuevoElemento = document.getElementById("labelNuevoElemento");
//const respuestasLista = document.getElementById("respuestasLista")
const selectEliminar = document.getElementById("selectEliminar");
const selectEliminarContainer = document.getElementById("selectEliminarContainer");
const listaEstados = document.getElementById("listaEstados");
const materiaSelectContainer = document.getElementById("materiaSelectContainer");
const testSelectContainer = document.getElementById("testSelectContainer");
const preguntaSelectContainer = document.getElementById("preguntaSelectContainer");
const respuestaSelectContainer = document.getElementById("respuestaSelectContainer");
const respuestasContainer = document.getElementById("respuestasContainer")
const textoRespuesta = document.getElementById("textoRespuesta")
const textoExplicacion = document.getElementById("textoExplicacion")
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
		document.getElementById("selectEliminarContainer").innerHTML = "";
		document.getElementById("mensajeEliminar").textContent = "";
		document.getElementById("mensajeEliminar").style.display = "none";
		console.log("Backdrop eliminado tras ocultar el modal.");
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
				console.log("linea 57 data =", data)
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
		console.log(`📥linea 98  Datos actualizados de ${storageKey}:`, data);
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
	console.log(accion)
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
	//respuestasLista.innerHTML = "";
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
			document.getElementById("nombreElementoContainer").style.display = (accion === "editar" || accion === "crear") ? "block" : "none";
			materiaSelectContainer.style.display = (accion === "editar" || accion === "crear") ? "none" : "block";
			break;
		case "testsSection":
			modalLabel.textContent = (accion === "editar") ? "MODIFICAR TEST" : (accion === "eliminar") ? "ELIMINAR TEST" : "NUEVO TEST";
			inputNuevoElemento.textContent = (accion === "editar") ? "Nuevo nombre del test:" : (accion === "crear") ? "Nombre del test:" : "";
			document.getElementById("nombreElementoContainer").style.display = (accion === "editar" || accion === "crear") ? "block" : "none";
			break;
		case "preguntasSection":
			modalLabel.textContent = (accion === "editar") ? "MODIFICAR PREGUNTA" : (accion === "eliminar") ? "ELIMINAR PREGUNTA" : "NUEVA PREGUNTA";
			inputNuevoElemento.textContent = (accion === "editar") ? "Nuevo texto de la pregunta:" : (accion === "crear") ? "Contenido de la pregunta:" : "";
			document.getElementById("nombreElementoContainer").style.display = (accion === "editar" || accion === "crear") ? "block" : "none";
			preguntaSelectContainer.style.display = "block"
			break;
		case "respuestasSection":
			modalLabel.textContent = (accion === "editar") ? "MODIFICAR RESPUESTA" : (accion === "eliminar") ? "ELIMINAR RESPUESTA" : "NUEVA RESPUESTA";
			document.getElementById("nombreElementoContainer").style.display = "none";
			respuestaSelectContainer.style.display = (accion === "editar" || accion === "crear") ? "block" : "none";
			respuestasContainer.style.display = "block"
			respuestaLabel.style.display = "block"
			explicacionLabel.style.display = "block"
			notaLabel.style.display = "block"
			textoRespuesta.style.display = "block"
			textoExplicacion.style.display = "block"
			nota.style.display = "block"
			respuestaLabel.innerText = (accion === "editar" || accion === "crear") ? "Modifica la respuesta" : "Nueva respuesta";
			explicacionLabel.innerText = (accion === "editar" || accion === "crear") ? "Modifica la explicación" : "Nueva explicación";
			notaLabel.innerText = (accion === "editar" || accion === "crear") ? "Modifica la nota" : "Nueva nota";
			//respuestasLista.style.display = "block";


			break;
		default:
			console.error("Sección no reconocida:", sectionId);
	}
	/********************************MUESTRA LOS SELECT EN CASCADA *************************************/
	cargarSelectsEnCascada(accion);
	if (accion === "crear") {
		document.getElementById("elementId").value = "";
		document.getElementById("nombreElemento").value = "";
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
    let containerId = "materiaSelectContainer";
    let selectId = "materiaSelect";
    let inputContainer = document.getElementById("nombreElementoContainer"); // Contenedor del input
    let inputNombre = document.getElementById("nombreElemento"); // Input de texto
    let inputId = document.getElementById("elementId"); // Campo oculto con el ID

    // Configurar vista según la acción
    if (accion === "eliminar") {
        containerId = "selectEliminarContainer";
        selectId = "selectEliminar";
        inputContainer.style.display = "none"; // Ocultamos el input en eliminar
        inputId.value = ""; // No se usa ID en eliminar
    } else if (accion === "editar") {
        inputContainer.style.display = "block"; // Mostrar input en editar
    } else {
        containerId = "materiaSelectContainer";
        inputContainer.style.display = "block"; // Mostrar input en crear
        inputId.value = ""; // Asegurar que no haya ID en crear
    }

    // Llamamos a la función que carga las materias en el select correspondiente
    cargarMateriasEnSelect(containerId, selectId, function () {
        document.getElementById(containerId).style.display = (accion === "crear") ? "none" : "block";

        // Solo en editar, asignamos el ID y nombre al input cuando el usuario selecciona una materia
        if (accion === "editar") {
            document.getElementById(selectId).addEventListener("change", function () {
                let idMateriaSeleccionada = this.value;
                let materiaSeleccionada = this.options[this.selectedIndex].text;
                
                if (idMateriaSeleccionada) {
                    inputId.value = idMateriaSeleccionada;
                    inputNombre.value = materiaSeleccionada;
                } else {
                    inputId.value = "";
                    inputNombre.value = "";
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

    // Limpiar el contenido previo antes de insertar el nuevo select
    container.innerHTML = `<label>Selecciona Materia:</label>
                           <select id="${selectId}" class="form-control"></select>`;
    const selectElement = document.getElementById(selectId);

    const materias = JSON.parse(sessionStorage.getItem("materias")) || [];
    if (materias.length === 0) {
        container.innerHTML = "<p class='text-danger'>No hay materias disponibles.</p>";
        return;
    }

    // Insertar opciones en el select
    selectElement.innerHTML = `<option value="">Seleccione una materia</option>`;
    materias.forEach(m => {
        const option = document.createElement("option");
        option.value = m.idMateria;
        option.textContent = m.nombreMateria;
        selectElement.appendChild(option);
    });

    console.log(`📌 Datos cargados en ${selectId}:`, materias);
    container.style.display = "block";

    if (callback) callback();
}




/***********************PRECARGA UN SELECT CON LOS TEST FILTRADOS POR MATERIA **********************************************/
function cargarSelectsTests(accion) {
    let containerId = "testSelectContainer";
    let selectId = "testSelect";
    let inputContainer = document.getElementById("nombreElementoContainer"); // Contenedor del input
    let inputNombre = document.getElementById("nombreElemento"); // Input de texto
    let inputId = document.getElementById("elementId"); // Campo oculto con el ID
    let materiaContainer = "materiaSelectContainer"; // Contenedor por defecto para materias
    let materiaSelectId = "materiaSelect"; // ID del select de materias

    // 📌 Configurar la vista según la acción
    if (accion === "eliminar") {
        containerId = "selectEliminarContainer";
        selectId = "selectEliminar";
        materiaContainer = "selectEliminarContainer"; // En eliminar, el select de materias va en `selectEliminarContainer`
        materiaSelectId = "selectEliminar";
        inputContainer.style.display = "none"; // Ocultamos el input en eliminar
        inputId.value = ""; // No se usa ID en eliminar
        testSelectContainer.style.display = "none"; // Ocultar el select de tests hasta seleccionar una materia
    } else if (accion === "editar") {
        inputContainer.style.display = "block"; // Mostrar input en editar
    } else { // Caso "crear"
        inputContainer.style.display = "block"; // Mostrar input en crear
        inputId.value = ""; // Asegurar que no haya ID en crear
        testSelectContainer.style.display = "none"; // Ocultar el select de tests en crear
    }

    // ✅ **Cargar el select de materias en el contenedor correcto**
    cargarMateriasEnSelect(materiaContainer, materiaSelectId, function () {
        document.getElementById(materiaSelectId).addEventListener("change", function () {
            let idMateria = this.value;
            if (idMateria) {
                if (accion === "editar" || accion === "eliminar") {
                    testSelectContainer.style.display = "block"; // Mostrar el select de tests
                    cargarTestsEnSelectFiltrado(containerId, selectId, idMateria, function () {
                        document.getElementById(selectId).addEventListener("change", function () {
                            let idTestSeleccionado = this.value;
                            let testSeleccionado = this.options[this.selectedIndex].text;

                            if (idTestSeleccionado) {
                                inputId.value = idTestSeleccionado; // Guardar el ID del test
                                inputNombre.value = testSeleccionado; // Mostrar nombre en el input
                            } else {
                                inputId.value = "";
                                inputNombre.value = "";
                            }
                        });
                    });
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

    console.log(`📌 Datos cargados en ${selectId}:`, testsFiltrados);
    container.style.display = "block";

    if (callback) callback();
}




/***********************PRECARGA UN SELECT CON LAS PREGUNTAS FILTRADAS POR TEST **********************************************/
function cargarSelectsPreguntas(accion) {
    let containerId = "preguntaSelectContainer";
    let selectId = "preguntaSelect";
    let inputContainer = document.getElementById("nombreElementoContainer"); // Contenedor del input
    let inputNombre = document.getElementById("nombreElemento"); // Input de texto
    let inputId = document.getElementById("elementId"); // Campo oculto con el ID
    let materiaContainer = "materiaSelectContainer"; // Contenedor del select de materias
    let materiaSelectId = "materiaSelect"; // ID del select de materias
    let testContainer = "testSelectContainer"; // Contenedor del select de tests
    let testSelectId = "testSelect"; // ID del select de tests

    // 📌 Configuración visual según la acción
    if (accion === "eliminar") {
        containerId = "selectEliminarContainer";
        selectId = "selectEliminar";
        materiaContainer = "selectEliminarContainer"; // En eliminar, el select de materias va en `selectEliminarContainer`
        materiaSelectId = "selectEliminar";
        testContainer = "selectEliminarContainer"; // ✅ Ahora el test también se coloca en el contenedor de eliminar
        testSelectId = "selectEliminarTest"; // ✅ Asegurar un ID distinto para evitar conflictos
        inputContainer.style.display = "none"; // Ocultar el input en eliminar
        inputId.value = ""; // No se usa ID en eliminar
        preguntaSelectContainer.style.display = "none"; // Ocultar select de preguntas hasta seleccionar un test
    } else if (accion === "editar") {
        inputContainer.style.display = "block"; // Mostrar input en editar
        preguntaSelectContainer.style.display = "none"; // Ocultar preguntas hasta seleccionar test
    } else { // Caso "crear"
        inputContainer.style.display = "block"; // Mostrar input en crear
        inputId.value = ""; // Asegurar que no haya ID en crear
        preguntaSelectContainer.style.display = "none"; // ❌ OCULTAR SELECT DE PREGUNTAS EN CREAR
    }

    // ✅ **Cargar el select de materias en el contenedor correcto**
    cargarMateriasEnSelect(materiaContainer, materiaSelectId, function () {
        document.getElementById(materiaSelectId).addEventListener("change", function () {
            let idMateria = this.value;
            if (idMateria) {
               document.getElementById(testContainer).style.display = "block"; // Mostrar el select de tests
                cargarTestsEnSelectFiltrado(testContainer, testSelectId, idMateria, function () {
                    document.getElementById(testSelectId).addEventListener("change", function () {
                        let idTest = this.value;
                        
                        if (idTest) {
                            if (accion === "editar" || accion === "eliminar") {
                                preguntaSelectContainer.style.display = "block"; // ✅ Solo en editar/eliminar se muestra preguntas
                                cargarPreguntasEnSelectFiltrado(containerId, selectId, idTest, function () {
                                    if (accion === "editar") {
                                        document.getElementById(selectId).addEventListener("change", function () {
                                            let idPreguntaSeleccionada = this.value;
                                            let preguntaSeleccionada = this.options[this.selectedIndex].text;

                                            if (idPreguntaSeleccionada) {
                                                inputId.value = idPreguntaSeleccionada; // Guardar el ID de la pregunta
                                                inputNombre.value = preguntaSeleccionada; // Mostrar el nombre en el input
                                            } else {
                                                inputId.value = "";
                                                inputNombre.value = "";
                                            }
                                        });
                                    }
                                });
                            } else {
                                preguntaSelectContainer.style.display = "none"; // ❌ Asegurar que preguntas se oculta en crear
                            }
                        }
                    });
                });
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

    console.log(`📌 Datos cargados en ${selectId}:`, preguntasFiltradas);
    container.style.display = "block";

    if (callback) callback();
}




/***********************PRECARGA UN SELECT CON LAS RESPUESTAS FILTRADAS POR PREGUNTA **********************************************/

function cargarSelectsRespuestas(accion) {
    let containerId = "respuestaSelectContainer";
    let selectId = "respuestaSelect";
    let inputTextoRespuesta = document.getElementById("textoRespuesta"); // Input de texto de la respuesta
    let inputTextoExplicacion = document.getElementById("textoExplicacion"); // Input de texto de la explicación
    let inputNota = document.getElementById("nota"); // Select de nota (0 o 1)
    let inputId = document.getElementById("elementId"); // Campo oculto con el ID
    let materiaContainer = "materiaSelectContainer"; // Contenedor del select de materias
    let materiaSelectId = "materiaSelect"; // ID del select de materias
    let testContainer = "testSelectContainer"; // Contenedor del select de tests
    let testSelectId = "testSelect"; // ID del select de tests
    let preguntaContainer = "preguntaSelectContainer"; // Contenedor del select de preguntas
    let preguntaSelectId = "preguntaSelect"; // ID del select de preguntas

    // 📌 Configuración visual según la acción
    if (accion === "eliminar") {
        containerId = "selectEliminarContainer";
        selectId = "selectEliminar";
        materiaContainer = "selectEliminarContainer"; // En eliminar, el select de materias va en `selectEliminarContainer`
        materiaSelectId = "selectEliminar";
        testContainer = "selectEliminarContainer"; // ✅ Ahora el test también se coloca en el contenedor de eliminar
        testSelectId = "selectEliminarTest"; // ✅ Asegurar un ID distinto para evitar conflictos
        preguntaContainer = "selectEliminarContainer"; // ✅ Ahora la pregunta también se coloca en el contenedor de eliminar
        preguntaSelectId = "selectEliminarPregunta"; // ✅ Asegurar un ID distinto para evitar conflictos
        inputTextoRespuesta.style.display = "none"; // Ocultar el input de respuesta en eliminar
        inputTextoExplicacion.style.display = "none"; // Ocultar el input de explicación en eliminar
        inputNota.style.display = "none"; // Ocultar el select de nota en eliminar
        inputId.value = ""; // No se usa ID en eliminar
        respuestaSelectContainer.style.display = "none"; // Ocultar el select de respuestas hasta seleccionar una pregunta
    } else if (accion === "editar") {
        inputTextoRespuesta.style.display = "block"; // Mostrar input en editar
        inputTextoExplicacion.style.display = "block"; // Mostrar input en editar
        inputNota.style.display = "block"; // Mostrar select de nota en editar
        respuestaSelectContainer.style.display = "none"; // Ocultar respuestas hasta seleccionar pregunta
    } else { // Caso "crear"
        inputTextoRespuesta.style.display = "block"; // Mostrar input en crear
        inputTextoExplicacion.style.display = "block"; // Mostrar input en crear
        inputNota.style.display = "block"; // Mostrar select de nota en crear
        inputId.value = ""; // Asegurar que no haya ID en crear
        respuestaSelectContainer.style.display = "none"; // ❌ OCULTAR SELECT DE RESPUESTAS EN CREAR
    }

    // ✅ **Cargar el select de materias en el contenedor correcto**
    cargarMateriasEnSelect(materiaContainer, materiaSelectId, function () {
        document.getElementById(materiaSelectId).addEventListener("change", function () {
            let idMateria = this.value;
            if (idMateria) {
                document.getElementById(testContainer).style.display = "block"; // Mostrar el select de tests
                cargarTestsEnSelectFiltrado(testContainer, testSelectId, idMateria, function () {
                    document.getElementById(testSelectId).addEventListener("change", function () {
                        let idTest = this.value;
                        if (idTest) {
                            document.getElementById(preguntaContainer).style.display = "block"; // Mostrar el select de preguntas
                            cargarPreguntasEnSelectFiltrado(preguntaContainer, preguntaSelectId, idTest, function () {
                                document.getElementById(preguntaSelectId).addEventListener("change", function () {
                                    let idPregunta = this.value;
                                    if (idPregunta && (accion === "editar" || accion === "eliminar")) {
                                        respuestaSelectContainer.style.display = "block"; // ✅ Solo en editar/eliminar se muestra respuestas
                                        cargarRespuestasEnSelectFiltrado(containerId, selectId, idPregunta, function () {
                                            if (accion === "editar") {
                                                document.getElementById(selectId).addEventListener("change", function () {
                                                    let idRespuestaSeleccionada = this.value;
                                                    let respuestaSeleccionada = this.options[this.selectedIndex].text;

                                                    if (idRespuestaSeleccionada) {
                                                        inputId.value = idRespuestaSeleccionada; // Guardar el ID de la respuesta
                                                        // Precargar valores en los inputs de respuesta
                                                        const respuestas = JSON.parse(sessionStorage.getItem("respuestas")) || [];
                                                        const respuesta = respuestas.find(r => r.idRespuesta == idRespuestaSeleccionada);
                                                        if (respuesta) {
                                                            inputTextoRespuesta.value = respuesta.textoRespuesta || "";
                                                            inputTextoExplicacion.value = respuesta.textoExplicacion || "";
                                                            inputNota.value = respuesta.nota || "0";
                                                        }
                                                    } else {
                                                        inputId.value = "";
                                                        inputTextoRespuesta.value = "";
                                                        inputTextoExplicacion.value = "";
                                                        inputNota.value = "0";
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        respuestaSelectContainer.style.display = "none"; // ❌ Asegurar que respuestas se oculta en crear
                                    }
                                });
                            });
                        }
                    });
                });
            }
        });
    });
}

function cargarSelectsRespuestas(accion) {
    let containerId = "respuestaSelectContainer";
    let selectId = "respuestaSelect";
    let inputTextoRespuesta = document.getElementById("textoRespuesta"); // Input de texto de la respuesta
    let inputTextoExplicacion = document.getElementById("textoExplicacion"); // Input de texto de la explicación
    let inputNota = document.getElementById("nota"); // Select de nota (0 o 1)
    let inputId = document.getElementById("elementId"); // Campo oculto con el ID
    let materiaContainer = "materiaSelectContainer"; // Contenedor del select de materias
    let materiaSelectId = "materiaSelect"; // ID del select de materias
    let testContainer = "testSelectContainer"; // Contenedor del select de tests
    let testSelectId = "testSelect"; // ID del select de tests
    let preguntaContainer = "preguntaSelectContainer"; // Contenedor del select de preguntas
    let preguntaSelectId = "preguntaSelect"; // ID del select de preguntas

    // 📌 Configuración visual según la acción
    if (accion === "eliminar") {
        containerId = "selectEliminarContainer";
        selectId = "selectEliminar";
        materiaContainer = "selectEliminarContainer"; // En eliminar, el select de materias va en `selectEliminarContainer`
        materiaSelectId = "selectEliminar";
        testContainer = "selectEliminarContainer"; // ✅ Ahora el test también se coloca en el contenedor de eliminar
        testSelectId = "selectEliminarTest"; // ✅ Asegurar un ID distinto para evitar conflictos
        preguntaContainer = "selectEliminarContainer"; // ✅ Ahora la pregunta también se coloca en el contenedor de eliminar
        preguntaSelectId = "selectEliminarPregunta"; // ✅ Asegurar un ID distinto para evitar conflictos
        inputTextoRespuesta.style.display = "none"; // Ocultar el input de respuesta en eliminar
        inputTextoExplicacion.style.display = "none"; // Ocultar el input de explicación en eliminar
        inputNota.style.display = "none"; // Ocultar el select de nota en eliminar
        inputId.value = ""; // No se usa ID en eliminar
        respuestaSelectContainer.style.display = "none"; // Ocultar el select de respuestas hasta seleccionar una pregunta
    } else if (accion === "editar") {
        inputTextoRespuesta.style.display = "block"; // Mostrar input en editar
        inputTextoExplicacion.style.display = "block"; // Mostrar input en editar
        inputNota.style.display = "block"; // Mostrar select de nota en editar
        respuestaSelectContainer.style.display = "none"; // Ocultar respuestas hasta seleccionar pregunta
    } else { // Caso "crear"
        inputTextoRespuesta.style.display = "block"; // Mostrar input en crear
        inputTextoExplicacion.style.display = "block"; // Mostrar input en crear
        inputNota.style.display = "block"; // Mostrar select de nota en crear
        inputId.value = ""; // Asegurar que no haya ID en crear
        respuestaSelectContainer.style.display = "none"; // ❌ OCULTAR SELECT DE RESPUESTAS EN CREAR
    }

    // ✅ **Cargar el select de materias en el contenedor correcto**
    cargarMateriasEnSelect(materiaContainer, materiaSelectId, function () {
        document.getElementById(materiaSelectId).addEventListener("change", function () {
            let idMateria = this.value;
            if (idMateria) {
                document.getElementById(testContainer).style.display = "block"; // Mostrar el select de tests
                cargarTestsEnSelectFiltrado(testContainer, testSelectId, idMateria, function () {
                    document.getElementById(testSelectId).addEventListener("change", function () {
                        let idTest = this.value;
                        if (idTest) {
                            document.getElementById(preguntaContainer).style.display = "block"; // Mostrar el select de preguntas
                            cargarPreguntasEnSelectFiltrado(preguntaContainer, preguntaSelectId, idTest, function () {
                                document.getElementById(preguntaSelectId).addEventListener("change", function () {
                                    let idPregunta = this.value;
                                    if (idPregunta && (accion === "editar" || accion === "eliminar")) {
                                        respuestaSelectContainer.style.display = "block"; // ✅ Solo en editar/eliminar se muestra respuestas
                                        cargarRespuestasEnSelectFiltrado(containerId, selectId, idPregunta, function () {
                                            if (accion === "editar") {
                                                document.getElementById(selectId).addEventListener("change", function () {
                                                    let idRespuestaSeleccionada = this.value;
                                                    let respuestaSeleccionada = this.options[this.selectedIndex].text;

                                                    if (idRespuestaSeleccionada) {
                                                        inputId.value = idRespuestaSeleccionada; // Guardar el ID de la respuesta
                                                        // Precargar valores en los inputs de respuesta
                                                        const respuestas = JSON.parse(sessionStorage.getItem("respuestas")) || [];
                                                        const respuesta = respuestas.find(r => r.idRespuesta == idRespuestaSeleccionada);
                                                        if (respuesta) {
                                                            inputTextoRespuesta.value = respuesta.textoRespuesta || "";
                                                            inputTextoExplicacion.value = respuesta.textoExplicacion || "";
                                                            inputNota.value = respuesta.nota || "0";
                                                        }
                                                    } else {
                                                        inputId.value = "";
                                                        inputTextoRespuesta.value = "";
                                                        inputTextoExplicacion.value = "";
                                                        inputNota.value = "0";
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        respuestaSelectContainer.style.display = "none"; // ❌ Asegurar que respuestas se oculta en crear
                                    }
                                });
                            });
                        }
                    });
                });
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

    console.log(`📌 Datos cargados en ${selectId}:`, respuestasFiltradas);
    container.style.display = "block";

    if (callback) callback();
}






/************************FUNCION PARA CREAR MATERIA*********************************************/

function crearMateria() {
	modalLabel.textContent = "NUEVA MATERIA";
	inputNuevoElemento.textContent = "Nombre de la materia:";
	// Borra el contenido interno y oculta el contenedor del select
	materiaSelectContainer.innerHTML = "";
	// Muestra el contenedor del input para escribir el nombre
	document.getElementById("nombreElementoContainer").style.display = "block";
	document.getElementById("nombreElemento").value = "";
	document.getElementById("elementId").value = "";
}
/*************************FUNCION PARA EDITAR********************************************/
function editarMateria() {
	document.getElementById("elementId").value = "";
	cargarSelectsMaterias("editar");
}
/************************************CREA TEST*********************************/

function crearTest() {
	document.getElementById("elementId").value = "";
	modalLabel.textContent = "NUEVO TEST";
	inputNuevoElemento.textContent = "Nombre del test:";
	// Para crear un test, se requiere seleccionar la materia y luego escribir el nombre
	cargarSelectsTests("crear");
}
/****************************EDITA TEST*****************************************/

function editarTest() {

	document.getElementById("elementId").value = "";
	cargarSelectsTests("editar");
}
/*****************************CREA PREGUNTA****************************************/
function crearPregunta() {
	document.getElementById("elementId").value = "";
	modalLabel.textContent = "NUEVA PREGUNTA";
	inputNuevoElemento.textContent = "Contenido de la pregunta:";
	cargarSelectsPreguntas("crear")
}
/****************************EDITAA PREGGUNTA*****************************************/
function editarPregunta() {

	document.getElementById("elementId").value = "";
	cargarSelectsPreguntas("editar");
}
/********************************CREA RESPPUESTA*************************************/
function crearRespuesta() {
	document.getElementById("elementId").value = "";
	modalLabel.textContent = "NUEVA RESPUESTA";
	document.getElementById("nombreElementoContainer").style.display = "none";
	cargarSelectsRespuestas("crear");
	respuestasContainer.style.display = "block"
	respuestaLabel.style.display = "block"
	explicacionLabel.style.display = "block"
	notaLabel.style.display = "block"
	textoRespuesta.style.display = "block"
	textoExplicacion.style.display = "block"
	nota.style.display = "block"
	respuestasLista.style.display = "block";
	document.getElementById("respuestasContainer").style.display = "block";
}
/******************************EDITA RESPUESTAA***************************************/
function editarRespuesta() {
	document.getElementById("elementId").value = "";
	document.getElementById("nombreElementoContainer").style.display = "none";
	cargarSelectsRespuestas("editar");
	respuestasContainer.style.display = "block"
	respuestaLabel.style.display = "block"
	explicacionLabel.style.display = "block"
	notaLabel.style.display = "block"
	textoRespuesta.style.display = "block"
	textoExplicacion.style.display = "block"
	nota.style.display = "block"
	//respuestasLista.style.display = "block";
}

/**************************FUNCION DE GUARDADO EN BASE DE DATOS*******************************************/
window.guardarDatos = function() {
    if (!selectedSection) {
        mostrarMensajeModal("error", "❌ No hay una sección seleccionada.");
        return;
    }

    const id = document.getElementById("elementId")?.value || null;
    const nombre = document.getElementById("nombreElemento")?.value?.trim() || "";

    if (!nombre && selectedSection !== "respuestasSection") {
        mostrarMensajeModal("error", "⚠ Debes ingresar un nombre.");
        return;
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
                textoRespuesta: document.getElementById("textoRespuesta")?.value?.trim() || "",
                textoExplicacion: document.getElementById("textoExplicacion")?.value?.trim() || "",
                nota: parseFloat(document.getElementById("nota")?.value) || 0,
                idPregunta: parseInt(document.getElementById("preguntaSelect")?.value || 0)
            };
            break;
        default:
            mostrarMensajeModal("error", "❌ Sección no reconocida.");
            return;
    }

    console.log(`📡 Enviando datos a ${apiUrl}`, payload);

    if (id) apiUrl += `/${id}`;

    sendRequest(apiUrl, id ? "PUT" : "POST", payload)
        .then(() => {
            mostrarMensajeModal("success", id ? "✅ Elemento actualizado correctamente." : "✅ Elemento creado correctamente.", true);
            setTimeout(() => cargarDatosDesdeBackend(), 2500); // 🔄 Actualizar los datos después de cerrar el modal
        })
        .catch(error => {
            mostrarMensajeModal("error", `❌ Error al guardar: ${error.message}`);
        });
};



/************************MOSTRAR ESTADOS ACTIVO / INACTIVO*********************************************/

window.mostrarListaEstados = function() {

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


/*************************GUARDA TODOS LOS CAMBIOS DE ESTADO ********************************************/
window.guardarCambiosEstados = function() {
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
			console.log(`✅ Estado cambiado para ${cambio.id}: ${cambio.activa}`);
		});
	});

	alert("✅ Cambios guardados correctamente.");
	actualizarSessionStorage(selectedSection);
	document.getElementById("guardarCambiosEstado").style.display = "none";
};



/*************************ABRE EL MODAL PARA ELIMINAR********************************************/
function abrirEliminarModal(sectionId) {
	let storageKey, idField, nameField;
	switch (sectionId) {
		case "materiasSection":
			storageKey = "materias";
			idField = "idMateria";
			nameField = "nombreMateria";
			cargarSelectsMaterias("eliminar");
			break;
		case "testsSection":
			storageKey = "tests";
			idField = "idTest";
			nameField = "nombreTest";
			cargarSelectsTests("eliminar");
			break;
		case "preguntasSection":
			storageKey = "preguntas";
			idField = "idPregunta";
			nameField = "textoPregunta";
			cargarSelectsPreguntas("eliminar");
			break;
		case "respuestasSection":
			storageKey = "respuestas";
			idField = "idRespuesta";
			nameField = "textoRespuesta";
			cargarSelectsRespuestas("eliminar");
			break;
		default:
			console.error("❌ Sección no reconocida:", sectionId);
			return;
	}
	const data = JSON.parse(sessionStorage.getItem(storageKey)) || [];
	if (data.length === 0) {
		selectEliminarContainer.innerHTML = "<p class='text-danger'>No hay elementos disponibles para eliminar.</p>";
		return;
	}
	document.getElementById("mensajeEliminar").textContent = "";
	   document.getElementById("mensajeEliminar").style.display = "none";
	   document.getElementById("advertenciaEliminar").style.display = "block";
	   document.getElementById("btnCancelarEliminar").style.display = "inline-block";
	   document.getElementById("btnConfirmarEliminar").style.display = "inline-block";

	   const modalEliminar = new bootstrap.Modal(document.getElementById("modalEliminar"), {
	       backdrop: "static",
	       keyboard: false
	   });

	   modalEliminar.show();
	   document.getElementById("btnConfirmarEliminar").onclick = function() {
	       confirmarEliminar(modalEliminar);
	   };
	modalEliminar.show();
	document.getElementById("btnConfirmarEliminar").onclick = function() {
		confirmarEliminar(modalEliminar);
	};
}
/**********************ELIMINAR ELEMENTO***********************************************/

window.eliminarElemento = function() {
	console.log("🗑 Eliminando elemento en:", selectedSection);


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

            // **Actualizar datos en sessionStorage**
            actualizarSessionStorage(selectedSection);

            // **Ocultar los botones y mostrar el mensaje de éxito**
            advertenciaEliminar.style.display = "none";
            btnCancelarEliminar.style.display = "none";
            btnConfirmarEliminar.style.display = "none";
            mensajeEliminar.textContent = "✅ Elemento eliminado correctamente.";
            mensajeEliminar.style.display = "block";

            // **Cerrar el modal después de 1.5 segundos**
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

function mostrarMensajeModal(tipo, mensaje, cerrar = false) {
    let modalMensaje = document.getElementById("modalMensaje");
    let modalMensajeTexto = document.getElementById("modalMensajeTexto");
    let modalFormulario = document.getElementById("modalFormulario");
    
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



