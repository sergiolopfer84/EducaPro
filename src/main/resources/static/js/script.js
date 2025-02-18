document.addEventListener("DOMContentLoaded", function() {
	 // Seleccionamos todos los botones de "pregunta rápida"
	    const quickQuestionButtons = document.querySelectorAll('.quick-question-btn');
	    const userInput = document.getElementById('user-input');
	    const sendMessageBtn = document.getElementById('sendMessageBtn');

	    // Al hacer clic en cada botón, enviamos la pregunta directamente
	    quickQuestionButtons.forEach(button => {
	        button.addEventListener('click', () => {
	            // Tomamos la pregunta del atributo data-question
	            const question = button.getAttribute('data-question');
	            
	            // Si prefieres rellenar el input y que el usuario le dé a "Enviar" manualmente:
	            // userInput.value = question;

	            // O si prefieres enviar directamente:
	            userInput.value = question;
	            sendMessageBtn.click(); // Llamamos al clic del botón "Enviar" para que vaya al backend
	        });
	    });
	
    const currentPath = window.location.pathname;
    console.log("Ruta actual:", currentPath);

    const inicioBtn = document.querySelector("#Inicio");
    const irATestsBtn = document.querySelector("#IrATests");
    const perfilBtn = document.querySelector("#perfilBtn");
    const adminBtn = document.querySelector("#adminBtn");

    if (currentPath === "/index" && inicioBtn) {
		inicioBtn.style.display = "none";
    }

    if (currentPath === "/home" && irATestsBtn) {
        irATestsBtn.style.display = "none";
    }

    if (currentPath === "/perfil" && perfilBtn) {
        perfilBtn.style.display = "none";
    }

    if (currentPath === "/admin" && adminBtn) {
        adminBtn.style.display = "none";
    }
});

/***************************************************
 * Botón de admin oculto para user *
 ***************************************************/


$(document).ready(function() {

	const currentPath = window.location.pathname;
	
	/***************************************************
	 * 1. Dropdown personalizado para selects
	 ***************************************************/
	function transformSelectToDropdown(selectId, dropdownId) {
		const $select = $('#' + selectId);
		const $dropdown = $('#' + dropdownId);
		const $selected = $dropdown.find('.dropdown-selected');
		const $itemsContainer = $dropdown.find('.dropdown-items');

		// Limpiar items previos
		$itemsContainer.empty();

		// Crear un .dropdown-item por cada <option>
		$select.find('option').each(function() {
			const val = $(this).attr('value');
			const text = $(this).text();
			if (!val) return;

			const $item = $('<div class="dropdown-item"></div>')
				.attr('data-value', val)
				.text(text)
				.on('click', function(e) {
					e.stopPropagation();
					$select.val(val).trigger('change');
					$selected.text(text);
					$dropdown.removeClass('open');
				});
			$itemsContainer.append($item);
		});

		// Mostrar la opción seleccionada si la hubiera
		const currentValue = $select.val();
		if (currentValue) {
			const currentText = $select.find('option[value="' + currentValue + '"]').text();
			if (currentText) {
				$selected.text(currentText);
			}
		}

		// Evento para abrir/cerrar el dropdown
		$selected.off('click').on('click', function(e) {
			e.stopPropagation();
			$dropdown.toggleClass('open');
		});
	}

	$(document).on('click', function() {
		$('.custom-dropdown.open').removeClass('open');
	});

	// Botón de perfil
	$('#perfilBtn').click(function() {
		window.location.href = '/perfil';  // o la ruta que uses
	});

	// LOGIN
	$('#loginBtn').click(function(e) {
		e.preventDefault();
		const email = $('#loginUsername').val();
		const password = $('#loginPassword').val();

		if (!email || !password) {
			$('#loginError').html('<span style="color: red;">Por favor, completa todos los campos.</span>');
			return;
		}

		$.ajax({
			url: '/auth/login',
			type: 'POST',
			data: { email: email, password: password },
			beforeSend: function(xhr) {
				if (window.csrf?.headerName && window.csrf?.token) {
					xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
				}
			},
			success: function() {
				// Si la sesión se crea, se guardará el usuario en el server
				// Asegúrate de que luego las peticiones GET usen la misma cookie
				window.location.href = '/home';
			},
			error: function(xhr) {
				let mensaje = 'Error al iniciar sesión.';
				if (xhr.status === 401) {
					mensaje = 'Nombre o contraseña incorrectos.';
				} else if (xhr.status === 403) {
					mensaje = 'Demasiados intentos fallidos. Espere 1 minuto.';
				}
				$('#loginError').html(`<span style="color: red;">${mensaje}</span>`);
				$('#authModal').modal({ backdrop: 'static', keyboard: false });
			}
		});
	});

	// LOGOUT
	$('#logoutBtn').click(function(e) {
		e.preventDefault();
		$.ajax({
			url: '/auth/logout',
			type: 'POST',
			beforeSend: function(xhr) {
				if (window.csrf?.headerName && window.csrf?.token) {
					xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
				}
			},
			success: function() {
				// Se invalidará la sesión en el server (si tu config lo hace)
				window.location.href = '/';
			},
			error: function() {
				alert('Error al cerrar sesión.');
			}
		});
	});

	// Forzar modal login/register limpio
	$('#authModal').on('show.bs.modal', function() {
		$('#loginUsername').val('');
		$('#loginPassword').val('');
		$('#loginError').html('');
		$('#registerName').val('');
		$('#registerEmail').val('');
		$('#registerPassword').val('');
		$('#registerError').html('');
		$('#registerForm').hide();
		$('#loginForm').show();
	});

	// REGISTRO
	$('#registerBtn').click(function() {
		$('#registerError').html('');
		const name = $('#registerName').val().trim();
		const email = $('#registerEmail').val().trim();
		const password = $('#registerPassword').val().trim();

		if (!name || !email || !password) {
			$('#registerError').html('<span style="color: red;">Faltan campos por rellenar</span>');
			return;
		}
		if (!email.includes('@') || !email.includes('.')) {
			$('#registerError').html('<span style="color: red;">Inserta un email válido</span>');
			return;
		}
		const passwordRegex = /^(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{4,12}$/;
		if (!passwordRegex.test(password)) {
			$('#registerError').html('<span style="color: red;">La contraseña debe contener una mayúscula y un número (4-12)</span>');
			return;
		}

		$.ajax({
			url: '/auth/register',
			type: 'POST',
			contentType: 'application/json',
			data: JSON.stringify({ nombre: name, email: email, pass: password }),
			beforeSend: function(xhr) {
				if (window.csrf?.headerName && window.csrf?.token) {
					xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
				}
			},
			success: function() {
				console.log("registro ok")
				$('#registerForm').hide();
				$('#loginForm').show();
				$('#registerName').val('');
				$('#registerEmail').val('');
				$('#registerPassword').val('');
				$('#registerError').html('');
			},
			error: function(xhr) {
				console.error("Error al registrarse:", xhr);
				$('#registerError').html(
					`<span style="color: red;">${xhr.responseText || 'Error al registrarse.'}</span>`
				);
			}
		});
	});

	// Mostrar/ocultar formularios en el modal
	$('#showRegister').click(function() {
		$('#loginForm').hide();
		$('#registerForm').show();
		$('#loginUsername').val('');
		$('#loginPassword').val('');
		$('#loginError').html('');
	});
	$('#showLogin').click(function() {
		$('#registerForm').hide();
		$('#loginForm').show();
		$('#registerName').val('');
		$('#registerEmail').val('');
		$('#registerPassword').val('');
		$('#registerError').html('');
	});
	// ================== BIENVENIDA ==================
	$.get("/usuarios/api/current-user", function(usuario) {
		$("#welcome-text").text("Bienvenido/a " + usuario.nombre);
		if (usuario && usuario.nombre) {
			$("#loginTrigger").hide();
			$("#logoutBtn").show();
		}
	}).fail(function() {
		console.error("Error al obtener datos del usuario.");
	});



	// Configuración global CSRF
	if (window.csrf && window.csrf.token && window.csrf.headerName) {
		$.ajaxSetup({
			beforeSend: function(xhr) {
				xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
			},
		});
	}

	if (currentPath === '/home') {

		/*document.getElementById("adminBtn").addEventListener("click", function () {
			window.location.href = "/admin"; // Asegúrate de que esta sea la URL correcta del panel de administración
		});*/

		// Cargar materias
		$.get('/materias', function(data) {
			let options = '<option value="">Elige una materia</option>';
			data.forEach(materia => {
				options += `<option value="${materia.idMateria}">${materia.nombreMateria || materia.materia}</option>`;
			});
			$('#materias').html(options);
			transformSelectToDropdown('materias', 'materiasDropdown');
		});

		// Al cambiar materia -> cargar tests
		$('#materias').on('change', function() {
			const idMateria = $(this).val();
			if (!idMateria) {
				$('#tests').html('<option value="">Elige un test</option>');
				transformSelectToDropdown('tests', 'testsDropdown');
				return;
			}
			$.get(`/tests/materia/${idMateria}`, function(data) {
				let options = '<option value="">Elige un test</option>';
				data.forEach(test => {
					options += `<option value="${test.idTest}">${test.nombreTest || test.test}</option>`;
				});
				$('#tests').html(options).prop('disabled', false);
				transformSelectToDropdown('tests', 'testsDropdown');
			});
		});

		// Al cambiar test -> cargar preguntas + última nota
		$('#tests').on('change', function() {
			const idTest = $(this).val();
			if (!idTest) {
				$('#questions-container').html('');
				$('#ultima-nota').hide();
				$('#nota-obtenida').hide();
				return;
			}
			$('#questions-container').html('');
			$('#nota-obtenida').html('').hide();
			$('#ultima-nota').html('').hide();

			// 1. Cargar preguntas (y en el backend se guardan en sesión)
			$.get(`/preguntas/test/${idTest}`, function(data) {
				let questionsHTML = '';
				data.forEach(p => {
					questionsHTML += `
                        <div class="question">
                            <h3>${p.pregunta}</h3>
                            <div class="options">
                                ${p.respuestas.map(r => `
                                    <div class="respuesta">
                                        <label>
                                            <input type="radio" name="pregunta${p.idPregunta}" value="${r.idRespuesta}">
                                            ${r.textoRespuesta}
                                        </label>
                                        <div class="explicacion" style="display: none;">${r.textoExplicacion}</div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    `;
				});
				questionsHTML += `<button type='submit' id='finalizar' class='btn-finalizar'>Finalizar</button>`;
				$('#questions-container').html(questionsHTML);
			});

			// 2. Cargar última puntuación
			$.ajax({
				url: '/puntuaciones/ultimaPuntuacion',
				type: 'GET',
				data: { idTest: idTest },
				success: function(response) {

					let ultimaNota = response.ultimaNota ?? null;
					if (ultimaNota !== null) {
						$('#ultima-nota')
							.html(`<p><strong>Última nota obtenida:</strong> ${ultimaNota}</p>`)
							.fadeIn();
					} else {
						$('#ultima-nota')
							.html(`<p><strong>No existen registros de nota para este test.</p>`)
							.fadeIn();
					}
				},
				error: function(xhr) {
					console.error("Error al obtener la última puntuación:", xhr);
				}
			});
		});

		// FINALIZAR TEST
		$(document).on('click', '#finalizar', function() {
			let respuestasSeleccionadas = [];
			let idTest = $('#tests').val();

			$('input[type=radio]:checked').each(function() {
				respuestasSeleccionadas.push(parseInt($(this).val()));
			});

			if (!idTest || respuestasSeleccionadas.length === 0) {
				window.scrollTo({ top: 0, behavior: 'smooth' });
				return;
			}

			// Enviamos al backend para calcular la nota y guardar en BD
			$.ajax({
				url: '/puntuaciones/calcularNota',
				type: 'POST',
				contentType: 'application/json',
				beforeSend: function(xhr) {
					if (window.csrf?.headerName && window.csrf?.token) {
						xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
					}
				},
				data: JSON.stringify({ idTest: idTest, respuestas: respuestasSeleccionadas }),
				success: function(response) {
					window.scrollTo({ top: 0, behavior: 'smooth' });
					let notaObtenida = response.nota;

					// Obtener la penúltima nota (o "sin registros previos")
					$.ajax({
						url: '/puntuaciones/ultimaPuntuacion',
						type: 'GET',
						data: { idTest: idTest },
						success: function(resp) {
							let notaAnterior = (resp.penultimaNota !== null) ? resp.penultimaNota : 'Sin registros previos';
							$('#ultima-nota').html(`<p><strong>Última nota obtenida:</strong> ${notaAnterior}</p>`);

							if (notaObtenida !== null && notaObtenida !== undefined) {
								$('#nota-obtenida')
									.html(`<p><strong>Nota obtenida en este test:</strong> ${notaObtenida}</p>`)
									.fadeIn();
							}
						}
					});

					// Resaltar correctas/incorrectas
					$.ajax({
						url: '/respuestas/obtenerRespuestasSesion', // Ajusta al path real si es "/respuestas/obtenerRespuestasSesion"
						type: 'GET',
						data: { idTest: idTest },
						success: function(data) {
							// data es la lista de Respuesta con su nota=1 o 0
							let respuestasCorrectas = data
								.filter(r => r.nota === 1)
								.map(r => r.idRespuesta);

							$('.respuesta').each(function() {
								let input = $(this).find('input[type=radio]');
								let explicacion = $(this).find('.explicacion');
								let respuestaId = parseInt(input.val());

								if (respuestasSeleccionadas.includes(respuestaId)) {
									if (respuestasCorrectas.includes(respuestaId)) {
										$(this).addClass('respuesta-correcta');
									} else {
										$(this).addClass('respuesta-incorrecta');
									}
									explicacion.show();
								}
							});
							finalizarTest();
						},
						error: function(xhr) {
							console.error("Error en la petición AJAX (obtenerRespuestasSesion):", xhr.responseText);
						}
					});
				},
				error: function(xhr) {
					console.error("Error en la petición AJAX (calcularNota):", xhr.responseText);
				}
			});
		});

		// RESET TEST
		$('#resetTest').click(function() {
			let inputs = document.querySelectorAll("input[type='radio'], input[type='checkbox']");
			inputs.forEach(input => {
				input.checked = false;
				input.disabled = false;
			});
			$('.respuesta').removeClass('respuesta-correcta respuesta-incorrecta');
			$('.explicacion').hide();
			$('#resetTest').hide();
			$('#finalizar').prop('disabled', false);
		});

		
		// ASISTENTE
		const openAssistantBtn = document.getElementById("openAssistantBtn");
		const modal = document.getElementById("assistantModal");
		const closeBtn = document.querySelector(".close-btn");
		const chatBox = document.getElementById("chat-box");
		const userInput = document.getElementById("user-input");
		const sendMessageBtn = document.getElementById("sendMessageBtn");

		// Función para enviar mensaje
		function sendMessage() {
		    let mensaje = userInput.value.trim();
		    if (mensaje === "") return;

		    chatBox.innerHTML += `<p><strong>Tú:</strong> ${mensaje}</p>`;
		    userInput.value = "";

		    // Petición al backend
		    fetch('/api/asistente', {
		        method: 'POST',
		        headers: {
		            'Content-Type': 'application/json',
		            [window.csrf.headerName]: window.csrf.token
		        },
		        body: JSON.stringify(mensaje)
		    })
		        .then(response => response.text())
		        .then(data => {
		            chatBox.innerHTML += `<p><strong>IA:</strong> ${data}</p>`;
		            chatBox.scrollTop = chatBox.scrollHeight;
		        });
		}

		// Evento para abrir el asistente
		openAssistantBtn.addEventListener("click", function() {
		    modal.style.display = "flex";
		});

		// Evento para cerrar el asistente
		closeBtn.addEventListener("click", function() {
		    modal.style.display = "none";
		});

		// Evento para enviar mensaje con el botón de enviar
		sendMessageBtn.addEventListener("click", sendMessage);

		// Evento para enviar mensaje al presionar Enter
		userInput.addEventListener("keydown", function(event) {
		    if (event.key === "Enter" && !event.shiftKey) {
		        event.preventDefault(); // Evita el salto de línea en el input
		        sendMessage(); // Llama a la función de enviar mensaje
		    }
		});

		// Evento para cerrar el asistente al hacer clic fuera del modal
		window.addEventListener("click", function(event) {
		    if (event.target === modal) {
		        modal.style.display = "none";
		    }
		});
		// Función para mostrar animación de carga
		function mostrarCargando() {
		    const chatBox = document.getElementById("chat-box");
		    const loadingDiv = document.createElement("div");
		    loadingDiv.id = "loading-indicator";
		    loadingDiv.innerHTML = `<p><strong>IA:</strong> <span class="loading-dots"></span></p>`;
		    chatBox.appendChild(loadingDiv);
		    chatBox.scrollTop = chatBox.scrollHeight;
		}

		// Función para eliminar la animación de carga
		function ocultarCargando() {
		    const loadingDiv = document.getElementById("loading-indicator");
		    if (loadingDiv) {
		        loadingDiv.remove();
		    }
		}

		// Función para enviar mensaje con animación de carga
		function sendMessage() {
		    let mensaje = userInput.value.trim();
		    if (mensaje === "") return;

		    // Agregar el mensaje del usuario con una clase personalizada
		    chatBox.innerHTML += `<p class="mensaje-usuario"><strong>Tú:</strong> ${mensaje}</p>`;
		    userInput.value = "";

		    // Muestra el indicador de carga
		    mostrarCargando();

		    // Petición al backend
		    fetch('/api/asistente', {
		        method: 'POST',
		        headers: {
		            'Content-Type': 'application/json',
		            [window.csrf.headerName]: window.csrf.token
		        },
		        body: JSON.stringify(mensaje)
		    })
		    .then(response => response.text())
		    .then(data => {
		        ocultarCargando(); // Oculta el indicador de carga
		        chatBox.innerHTML += `<p class="mensaje-ia"><strong>IA:</strong> ${data}</p>`;
		        chatBox.scrollTop = chatBox.scrollHeight;
		    })
		    .catch(error => {
		        ocultarCargando();
		        chatBox.innerHTML += `<p class="mensaje-ia error"><strong>IA:</strong> Hubo un error al procesar la respuesta.</p>`;
		        console.error(error);
		    });
		}


		// Evento para enviar mensaje con botón
		sendMessageBtn.addEventListener("click", sendMessage);

		// Evento para enviar mensaje con Enter
		userInput.addEventListener("keydown", function(event) {
		    if (event.key === "Enter" && !event.shiftKey) {
		        event.preventDefault();
		        sendMessage();
		    }
		});
		document.addEventListener("DOMContentLoaded", function() {
		    const modal = document.getElementById("resizableModal");
		    const resizeHandle = document.querySelector(".resize-handle");

		    let isResizing = false;

		    resizeHandle.addEventListener("mousedown", function(e) {
		        isResizing = true;
		        document.addEventListener("mousemove", resizeModal);
		        document.addEventListener("mouseup", () => {
		            isResizing = false;
		            document.removeEventListener("mousemove", resizeModal);
		        });
		    });

		    function resizeModal(e) {
		        if (!isResizing) return;
		        let newWidth = e.clientX - modal.offsetLeft;
		        let newHeight = e.clientY - modal.offsetTop;

		        // Asegurar que el tamaño no sea demasiado pequeño
		        if (newWidth > 300) modal.style.width = newWidth + "px";
		        if (newHeight > 300) modal.style.height = newHeight + "px";
		    }
		});
		document.addEventListener("DOMContentLoaded", function() {
		    const modal = document.querySelector(".modal-content2");
		    const chatBox = document.getElementById("chat-box");

		    new ResizeObserver(() => {
		        const modalPadding = 40; // Ajuste para evitar que toque los bordes
		        chatBox.style.height = (modal.clientHeight - modalPadding) + "px";
		    }).observe(modal);
		});
		document.addEventListener("DOMContentLoaded", function() {
		    const modal = document.querySelector(".modal-content2");
		    const chatBox = document.getElementById("chat-box");
		    const header = document.querySelector(".modal-header");

		    let isDragging = false;
		    let offsetX = 0, offsetY = 0;

		    // ✅ Hacemos la ventana redimensionable
		    new ResizeObserver(() => {
		        const modalPadding = 40;
		        chatBox.style.height = (modal.clientHeight - modalPadding) + "px";
		    }).observe(modal);

		    // ✅ Evento para iniciar el arrastre
		    header.addEventListener("mousedown", (e) => {
		        isDragging = true;
		        offsetX = e.clientX - modal.offsetLeft;
		        offsetY = e.clientY - modal.offsetTop;
		        modal.style.cursor = "grabbing";
		    });

		    // ✅ Evento para mover la ventana
		    document.addEventListener("mousemove", (e) => {
		        if (!isDragging) return;
		        modal.style.left = e.clientX - offsetX + "px";
		        modal.style.top = e.clientY - offsetY + "px";
		    });

		    // ✅ Evento para soltar la ventana
		    document.addEventListener("mouseup", () => {
		        isDragging = false;
		        modal.style.cursor = "grab";
		    });
		});


	} // Fin if /home

}); // Fin document.ready

/***************************************************
 * Bloquear respuestas al finalizar
 ***************************************************/
function finalizarTest() {
	$('#resetTest').show();
	let inputs = document.querySelectorAll("input[type='radio'], input[type='checkbox']");
	inputs.forEach(input => {
		input.disabled = true;
	});
	$('#finalizar').prop('disabled', true);
}