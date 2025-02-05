$(document).ready(function() {
	
	$('#perfilBtn').click(function () {
						
						window.location.href = 'perfil';
						});
	// ================== LOGIN FORM-BASED ==================
	$('#loginBtn').click(function(e) {
		e.preventDefault();
		const email = $('#loginUsername').val();
		const password = $('#loginPassword').val();

		// Verificar campos vacíos
		if (!email || !password) {
			$('#loginError').html('<span style="color: red;">Por favor, completa todos los campos.</span>');
			return;
		}

		$.ajax({
			url: '/login',           // La URL donde Spring Security escucha
			type: 'POST',            // Petición POST
			data: { email: email, password: password },
			beforeSend: function(xhr) {
				// CSRF Token si fuera necesario
				if (window.csrf.headerName && window.csrf.token) {
					xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
				}
			},
			success: function() {
				// Si el login es exitoso, redirigimos a /home
				window.location.href = '/home';
			},
			error: function(xhr) {
				// Manejar diferentes errores
				if (xhr.status === 401) {
					// Usuario/contraseña incorrectos
					$('#loginError').html(
						'<span style="color: red;">Nombre o contraseña incorrectos</span>'
					);
				} else if (xhr.status === 403) {
					// Cuenta bloqueada
					$('#loginError').html(
						'<span style="color: red;">Cuenta bloqueada. Inténtalo más tarde.</span>'
					);
				} else {
					// Error genérico
					$('#loginError').html(
						'<span style="color: red;">Error al iniciar sesión.</span>'
					);
				}

				// ⛔ EVITAR QUE SE CIERRE EL MODAL
				// Volvemos a forzar la apertura para asegurarnos de que se quede en pantalla.
				$('#authModal').modal({ backdrop: 'static', keyboard: false });
			}
		});
	});

	// ================== LOGOUT ==================
	$('#logoutBtn').click(function(e) {
		e.preventDefault();
		$.ajax({
			url: '/logout',
			type: 'POST',
			beforeSend: function(xhr) {
				if (window.csrf.headerName && window.csrf.token) {
					xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
				}
			},
			success: function() {
				window.location.href = '/login';
			},
			error: function(xhr) {
				alert('Error al cerrar sesión.');
			}
		});
	});

	// ================== FORZAR QUE EL MODAL SIEMPRE SE ABRA EN INICIO DE SESIÓN ==================
	$('#authModal').on('show.bs.modal', function() {

		// Limpiar campos de login
		$('#loginUsername').val('');
		$('#loginPassword').val('');
		$('#loginError').html('');

		// Limpiar campos de registro
		$('#registerName').val('');
		$('#registerEmail').val('');
		$('#registerPassword').val('');
		$('#registerError').html('');
		$('#registerForm').hide();
		$('#loginForm').show();
	});

	// ================== REGISTRO ==================
	$('#registerBtn').click(function() {
		// Limpiar cualquier mensaje de error anterior
		$('#registerError').html('');

		// Recogemos los valores de los inputs
		const name = $('#registerName').val().trim();
		const email = $('#registerEmail').val().trim();
		const password = $('#registerPassword').val().trim();

		// 1. Comprobar si falta algún campo
		if (!name || !email || !password) {
			$('#registerError').html('<span style="color: red;">Faltan campos por rellenar</span>');
			return;  // Salimos sin hacer el AJAX
		}

		// 2. Validar el email (debe contener al menos una "@" y un ".")
		if (!email.includes('@') || !email.includes('.')) {
			$('#registerError').html('<span style="color: red;">Inserte un email válido</span>');
			return;
		}

		// 3. Validar la contraseña
		// Debe tener entre 4 y 12 caracteres, al menos una mayúscula y al menos un número
		const passwordRegex = /^(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{4,12}$/;
		if (!passwordRegex.test(password)) {
			$('#registerError').html('<span style="color: red;">La contraseña debe contener una mayúscula y un número</span>');
			return;
		}

		// Si pasa todas las validaciones, continuamos con la petición AJAX
		$.ajax({
			url: '/register',
			type: 'POST',
			contentType: 'application/json',
			data: JSON.stringify({ nombre: name, email: email, pass: password }),
			beforeSend: function(xhr) {
				if (window.csrf.headerName && window.csrf.token) {
					xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
				}
			},
			success: function() {
				// Aquí podemos mostrar el login o un mensaje de éxito
				$('#registerForm').hide();
				$('#loginForm').show();

				// Limpiar campos
				$('#registerName').val('');
				$('#registerEmail').val('');
				$('#registerPassword').val('');
				$('#registerError').html(''); // Limpiamos el error al registrar correctamente
			},
			error: function(xhr) {
				// En caso de error, podríamos mostrarlo también aquí
				console.error("Error al registrarse:", xhr);
				$('#registerError').html(
					`<span style="color: red;">${xhr.responseText || 'Error al registrarse.'}</span>`
				);
			}
		});
	});


	// ================== MOSTRAR/OCULTAR formularios del modal ==================

	$('#showRegister').click(function() {
		$('#loginForm').hide();
		$('#registerForm').show();

		// Limpiamos los campos de login
		$('#loginUsername').val('');
		$('#loginPassword').val('');
		$('#loginError').html('');
	});

	$('#showLogin').click(function() {
		$('#registerForm').hide();
		$('#loginForm').show();

		// Limpiamos los campos de registro
		$('#registerName').val('');
		$('#registerEmail').val('');
		$('#registerPassword').val('');
		$('#registerError').html('');
	});

	// ================== BIENVENIDA ==================
	$.get("/api/current-user", function(usuario) {
		// Aquí cambiamos el texto "Bienvenido Usuario" por "Bienvenido + nombre del usuario"
		$("#welcome-text").text("Bienvenido/a " + usuario.nombre);

		// Si el usuario está logueado, ocultamos "Iniciar sesión" y mostramos "Cerrar sesión"
		if (usuario && usuario.nombre) {
			$("#loginTrigger").hide();
			$("#logoutBtn").show();
		}
	}).fail(function() {
		console.error("Error al obtener datos del usuario.");
	});

	const currentPath = window.location.pathname; // Obtener la ruta actual

	// Configuración global para el token CSRF
	if (window.csrf && window.csrf.token && window.csrf.headerName) {
		$.ajaxSetup({
			beforeSend: function(xhr) {
				xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
			},
		});
	}



	if (currentPath === '/home') {

		// ================== SELECCIÓN DE MATERIAS ==================
		$.ajax({
			url: '/materias',
			type: 'GET',
			success: function(data) {
				let options = '<option value="">Elige una materia</option>';
				data.forEach(materia => {
					options += `<option value="${materia.idMateria}">${materia.materia}</option>`;
				});
				$('#materias').html(options);
			},
			error: function(xhr) {
				console.error('Error al cargar las materias:', xhr);
			}
		});



		// ================== CARGAR TESTS ==================
		$('#materias').on('change', function() {
			const idMateria = $(this).val();
			if (!idMateria) {
				$('#tests').html('<option value="">Elige un test</option>');
				return;
			}

			$.ajax({
				url: '/tests',
				type: 'GET',
				data: { idMateria: idMateria },
				success: function(data) {
					let options = '<option value="">Elige un test</option>';
					data.forEach(test => {
						options += `<option value="${test.idTest}">${test.test}</option>`;
					});
					$('#tests').html(options);
				},
				error: function(xhr) {
					console.error('Error al cargar los tests:', xhr);
				}
			});
		});

		// ================== CARGAR PREGUNTAS ==================
		$('#tests').on('change', function() {
			const idTest = $(this).val();
			if (!idTest) {
				$('#questions-container').html('');
				return;
			}
			$('#questions-container').html('');
			// 🔴 LIMPIAR NOTA AL CAMBIAR DE TEST
			$('#nota-obtenida').html('');
			$('#ultima-nota').html('');

			$.ajax({
				url: '/preguntas',
				type: 'GET',
				data: { idTest: idTest },
				success: function(data) {
					let questionsHTML = '';
					data.forEach(pregunta => {
						questionsHTML += `
		                    <div class="question">
		                        <h3>${pregunta.pregunta}</h3>
		                        <div class="options">`;

						pregunta.respuestas.forEach(respuesta => {
							questionsHTML += `
							<div class="respuesta"> <!-- Contenedor para agrupar respuesta + explicación -->
							                            <label>
							                                <input type="radio" name="pregunta${pregunta.idPregunta}" value="${respuesta.idRespuesta}">
							                                ${respuesta.respuesta}
							                            </label>
							                            <div class="explicacion" style="display: none;">${respuesta.explicacion}</div> 
							                        </div>`;
						});

						questionsHTML += `</div></div>`;
					});

					questionsHTML += `<button type='submit' id='finalizar' class='btn-finalizar'>Finalizar</button>`;
					$('#questions-container').html(questionsHTML);
				},
				error: function(xhr) {
					console.error('Error al cargar las preguntas:', xhr);
				}
			});



			// ================== CARGAR ÚLTIMA PUNTUACIÓN ==================
			$.ajax({
				url: '/ultimaPuntuacion',
				type: 'GET',
				data: { idTest: idTest },
				success: function(response) {
					let ultimaNota = response.ultimaNota ?? 'Sin registros previos';

					$('#ultima-nota').html(`
	                        <p><strong>Última nota obtenida:</strong> ${ultimaNota}</p>
	                    `);
				},
				error: function(xhr) {
					console.error("Error al obtener la última puntuación:", xhr);
				}
			});
		});

		// ================== FINALIZAR TEST ==================
		$(document).on('click', '#finalizar', function() {
			let respuestasSeleccionadas = [];
			let idTest = $('#tests').val();

			$('input[type=radio]:checked').each(function() {
				respuestasSeleccionadas.push(parseInt($(this).val()));
			});

			// Validar si el usuario ha seleccionado al menos una respuesta
			if (!idTest || respuestasSeleccionadas.length === 0) {
				window.scrollTo({ top: 0, behavior: 'smooth' });
				return;
			}

			$.ajax({
				url: '/calcularNota',
				type: 'POST',
				contentType: 'application/json',
				data: JSON.stringify({ idTest: idTest, respuestas: respuestasSeleccionadas }),
				success: function(response) {
					window.scrollTo({ top: 0, behavior: 'smooth' });
					let notaObtenida = response.nota;

					// ✅ Mostrar la nota
					$.ajax({
						url: '/ultimaPuntuacion',
						type: 'GET',
						data: { idTest: idTest },
						success: function(response) {
							let notaAnterior = response.penultimaNota !== null ? response.penultimaNota : 'Sin registros previos';

							$('#nota-obtenida').html(`<p><strong>Nota obtenida en este test:</strong> ${notaObtenida}</p>`);
							$('#ultima-nota').html(`<p><strong>Nota anterior:</strong> ${notaAnterior}</p>`);
						}
					});

					// ✅ Obtener respuestas correctas
					$.ajax({
						url: '/obtenerRespuestasSesion',
						type: 'GET',
						data: { idTest: idTest },
						success: function(data) {
							let respuestasCorrectas = [];

							data.forEach(respuesta => {
								if (respuesta.nota === 1) {
									respuestasCorrectas.push(respuesta.idRespuesta);
								}
							});

							// ✅ Aplicar estilos y mostrar explicaciones
							$('.respuesta').each(function() {
								let input = $(this).find('input[type=radio]');
								let explicacion = $(this).find('.explicacion'); // Captura el div de explicación
								let respuestaId = parseInt(input.val());

								if (respuestasSeleccionadas.includes(respuestaId)) {
									if (respuestasCorrectas.includes(respuestaId)) {
										$(this).addClass('respuesta-correcta');
									} else {
										$(this).addClass('respuesta-incorrecta');
										/*respuestasCorrectas.forEach(idCorrecto => {
											let correcta = $(`input[value="${idCorrecto}"]`).closest('.respuesta');
											correcta.addClass('respuesta-correcta');
											correcta.find('.explicacion').show();
										});*/
									}
									explicacion.show();
								}
							});
						},
						error: function(xhr) {
							console.error("Error en la petición AJAX:", xhr.responseText);
						}
					});
				},
				error: function(xhr) {
					console.error("Error en la petición AJAX:", xhr.responseText);
				}
			});
		});
		

	}


});