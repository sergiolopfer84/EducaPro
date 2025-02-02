$(document).ready(function() {

	// ================== LOGIN FORM-BASED ==================
		
		let lockTimer = null;

		function blockLogin(seconds) {
		    $('#loginButton').prop('disabled', true); // Desactiva el botón
		    lockTimer = setInterval(() => {
		        seconds--;
		        $('#loginButton').text(`Bloqueado (${seconds}s)`);

		        if (seconds <= 0) {
		            clearInterval(lockTimer);
		            $('#loginButton').prop('disabled', false).text('Iniciar sesión');
		        }
		    }, 1000); // Actualiza cada segundo
		}
		$('#loginBtn').click(function(e) {
			e.preventDefault(); // Evita un submit accidental si es <button type="submit">

			// Usaremos username y password,
			// OJO: si en tu input usas "loginUsername" e "loginPassword"
			// y en Security config .usernameParameter("username"), .passwordParameter("password")
			// entonces "username" y "password" deben matchear
			const email = $('#loginUsername').val();
			const password = $('#loginPassword').val();


			if (!email || !password) {
				alert('Por favor, completa todos los campos.');
				return;
			}

			// Enviar solicitud de inicio de sesión con parámetros tipo "form-data"
			$('#loginButton').on('click', function () {
			    const email = $('#email').val(); // Captura el email ingresado por el usuario
			    const password = $('#password').val(); // Captura la contraseña ingresada

			    $.ajax({
			        url: '/login',  // URL del controlador de login en el backend
			        type: 'POST',   // Tipo de petición
			        data: {
			            email: email,
			            password: password
			        },
			        beforeSend: function (xhr) {
			            // Adjuntar cabecera CSRF si es necesario
			            if (window.csrf?.headerName && window.csrf?.token) {
			                xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
			            }
			        },
			        success: function () {
			            // Si las credenciales son correctas
			            window.location.href = '/home'; // Redirigir al usuario a /home
			        },
			        error: function (xhr) {
			            // Manejo de errores
			            if (xhr.status === 403) {
			                // Caso en que la cuenta esté bloqueada
			                const match = xhr.responseText.match(/(\d+) segundos/); // Busca el tiempo en el mensaje
			                const seconds = match ? parseInt(match[1], 10) : 30;   // Usa el tiempo o por defecto 30s
			                blockLogin(seconds); // Bloquea el botón durante el tiempo especificado
			                alert(xhr.responseText); // Muestra el mensaje recibido del backend
			            } else if (xhr.status === 401) {
			                // Caso de credenciales incorrectas
			                alert('Credenciales incorrectas. Intenta nuevamente.');
			            } else {
			                // Otros errores (por ejemplo, problemas en el servidor)
			                alert('Error al intentar iniciar sesión.');
			            }
			        }
			    });
			});

		
		//Logout 

		$('#logoutBtn').click(function (e) {
		    e.preventDefault(); // Evita el comportamiento por defecto del botón

		    $.ajax({
		        url: '/logout',
		        type: 'POST',
		        beforeSend: function (xhr) {
		            // Agregar el token CSRF
		            if (window.csrf && window.csrf.headerName && window.csrf.token) {
		                xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
		            }
		        },
		        success: function () {
		            window.location.href = '/login'; // Redirigir tras el logout
		        },
		        error: function (xhr) {
		            alert('Error al cerrar sesión: ' + xhr.status);
		        }
		    });
		});


		// ================== REGISTRO (JSON) ==================
		$('#registerBtn').click(function() {
			const name = $('#registerName').val();
			const email = $('#registerEmail').val();
			const password = $('#registerPassword').val();
			if (!name || !email || !password) {
				alert('Por favor, completa todos los campos.');
				return;
			}

			// Enviar solicitud de registro (esto es tu endpoint custom, /register)
			// Aquí sí podemos usar JSON
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
				success: function(response) {
					$('#authModal').modal('hide'); // Cerrar el modal
				},
				error: function(xhr) {
					console.error("Error al registrarse: ", xhr);
					const errorMessage = xhr.responseText || 'Error al registrarse.';
					alert(errorMessage);
				}
			});
		});


		// ================== MOSTRAR/OCULTAR formularios del modal ==================
		$('#showRegister').click(function() {
			$('#loginForm').hide();
			$('#registerForm').show();
		});

		$('#showLogin').click(function() {
			$('#registerForm').hide();
			$('#loginForm').show();
		});

		// ================== BIENVENIDA ==================
		$.get("/api/current-user", function(usuario) {
			// Aquí cambiamos el texto "Bienvenido Usuario" por "Bienvenido + nombre del usuario"
			$("#welcome-text").text("Bienvenido/a " + usuario.nombre);
		}).fail(function() {
			console.error("Error al obtener datos del usuario");
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

		// ================== Lógica para /home.html ==================
		if (currentPath === '/home') {

			$.ajax({
				url: '/materias',
				type: 'GET',
				success: function(data) {
					let options = '<option value="">Elige una materia</option>'; // Opción inicial
					data.forEach(materia => {
						options += `<option value="${materia.idMateria}">${materia.materia}</option>`;
					});
					$('#materias').html(options); // Carga las opciones en el select
				},
				error: function(xhr, status, error) {
					console.error('Error al cargar las materias:', error);
				}
			});

			// Evento para cargar tests cuando se selecciona una materia
			$('#materias').on('change', function() {
				const idMateria = $(this).val(); // Obtener el valor seleccionado
				if (!idMateria) {
					// Si no se selecciona una materia, vaciar el select de tests
					$('#tests').html('<option value="">Elige un test</option>');
					return;
				}

				// Hacer una petición AJAX para cargar los tests de la materia seleccionada
				$.ajax({
					url: '/tests', // Endpoint en el backend
					type: 'GET',
					data: { idMateria: idMateria }, // Parámetro idMateria enviado al backend
					success: function(data) {
						let options = '<option value="">Elige un test</option>';
						data.forEach(test => {
							options += `<option value="${test.idTest}">${test.test}</option>`;
						});
						$('#tests').html(options); // Cargar los tests en el select
					},
					error: function(xhr, status, error) {
						console.error('Error al cargar los tests:', error);
					}
				});
			});

			// Evento para cargar preguntas cuando se selecciona un test
			$('#tests').on('change', function() {
				const idTest = $(this).val(); // Obtener el test seleccionado
				if (!idTest) {
					$('#questions-container').html(''); // Limpiar si no hay test seleccionado
					return;
				}

				// Hacer una petición AJAX al backend
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
						                        <div class="options">
						                `;

							pregunta.respuestas.forEach(respuesta => {

								questionsHTML += `
						                        <label>
						                            <input type="radio" name="pregunta${pregunta.idPregunta}" value="${respuesta.idRespuesta}">
						                            ${respuesta.respuesta}
						                        </label>
						                    `;
							});

							questionsHTML += `
						                        </div>
						                    </div>
						                `;
						});

						$('#questions-container').html(questionsHTML); // Mostrar las preguntas y respuestas
					},
					error: function(xhr, status, error) {
						console.error('Error al cargar las preguntas:', error);
					}
				});
			});

		}
	});

});


