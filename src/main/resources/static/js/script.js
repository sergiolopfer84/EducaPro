$(document).ready(function () {
    // ================== LOGIN FORM-BASED ==================
    $('#loginBtn').click(function (e) {
        e.preventDefault();
        const email = $('#loginUsername').val();
        const password = $('#loginPassword').val();

        if (!email || !password) {
            alert('Por favor, completa todos los campos.');
            return;
        }

        $.ajax({
            url: '/login',
            type: 'POST',
            data: { email: email, password: password },
            beforeSend: function (xhr) {
                if (window.csrf.headerName && window.csrf.token) {
                    xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
                }
            },
            success: function () {
                window.location.href = '/home';
            },
            error: function (xhr) {
                alert('Error al iniciar sesión.');
            }
        });
    });

    // ================== LOGOUT ==================
    $('#logoutBtn').click(function (e) {
        e.preventDefault();
        $.ajax({
            url: '/logout',
            type: 'POST',
            beforeSend: function (xhr) {
                if (window.csrf.headerName && window.csrf.token) {
                    xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
                }
            },
            success: function () {
                window.location.href = '/login';
            },
            error: function (xhr) {
                alert('Error al cerrar sesión.');
            }
        });
    });

    // ================== REGISTRO ==================
    $('#registerBtn').click(function () {
        const name = $('#registerName').val();
        const email = $('#registerEmail').val();
        const password = $('#registerPassword').val();

        if (!name || !email || !password) {
            alert('Por favor, completa todos los campos.');
            return;
        }

        $.ajax({
            url: '/register',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ nombre: name, email: email, pass: password }),
            beforeSend: function (xhr) {
                if (window.csrf.headerName && window.csrf.token) {
                    xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
                }
            },
            success: function () {
                $('#authModal').modal('hide');
            },
            error: function (xhr) {
                console.error("Error al registrarse:", xhr);
                alert(xhr.responseText || 'Error al registrarse.');
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

		if (currentPath === '/home') {
    // ================== SELECCIÓN DE MATERIAS ==================
    $.ajax({
        url: '/materias',
        type: 'GET',
        success: function (data) {
            let options = '<option value="">Elige una materia</option>';
            data.forEach(materia => {
                options += `<option value="${materia.idMateria}">${materia.materia}</option>`;
            });
            $('#materias').html(options);
        },
        error: function (xhr) {
            console.error('Error al cargar las materias:', xhr);
        }
    });

    // ================== CARGAR TESTS ==================
    $('#materias').on('change', function () {
        const idMateria = $(this).val();
        if (!idMateria) {
            $('#tests').html('<option value="">Elige un test</option>');
            return;
        }

        $.ajax({
            url: '/tests',
            type: 'GET',
            data: { idMateria: idMateria },
            success: function (data) {
                let options = '<option value="">Elige un test</option>';
                data.forEach(test => {
                    options += `<option value="${test.idTest}">${test.test}</option>`;
                });
                $('#tests').html(options);
            },
            error: function (xhr) {
                console.error('Error al cargar los tests:', xhr);
            }
        });
    });

    // ================== CARGAR PREGUNTAS ==================
    $('#tests').on('change', function () {
        const idTest = $(this).val();
        if (!idTest) {
            $('#questions-container').html('');
            return;
        }

        $.ajax({
            url: '/preguntas',
            type: 'GET',
            data: { idTest: idTest },
            success: function (data) {
                let questionsHTML = '';
                data.forEach(pregunta => {
                    questionsHTML += `
                        <div class="question">
                            <h3>${pregunta.pregunta}</h3>
                            <div class="options">`;

                    pregunta.respuestas.forEach(respuesta => {
                        questionsHTML += `
                            <label>
                                <input type="radio" name="pregunta${pregunta.idPregunta}" value="${respuesta.idRespuesta}">
                                ${respuesta.respuesta}
                            </label>`;
                    });

                    questionsHTML += `
                            </div>
                        </div>`;
                });
                questionsHTML += `<button type='submit' id='finalizar'>Finalizar</button>`;
                $('#questions-container').html(questionsHTML);
            },
            error: function (xhr) {
                console.error('Error al cargar las preguntas:', xhr);
            }
        });

        // ================== CARGAR ÚLTIMA PUNTUACIÓN ==================
        $.ajax({
            url: '/ultimaPuntuacion',
            type: 'GET',
            data: { idTest: idTest },
            success: function (response) {
                let ultimaNota = response.ultimaNota ?? 'Sin registros previos';
                let penultimaNota = response.penultimaNota ?? 'Sin registros previos';

                $('#ultima-nota').html(`
                    <p><strong>Última nota obtenida:</strong> ${ultimaNota}</p>
                    <p><strong>Nota anterior:</strong> ${penultimaNota}</p>
                `);
            },
            error: function (xhr) {
                console.error("Error al obtener la última puntuación:", xhr);
            }
        });
    });

    // ================== FINALIZAR TEST ==================
	$(document).on('click', '#finalizar', function() {
	    let respuestasSeleccionadas = [];
	    let idTest = $('#tests').val();

	    // Recoger las respuestas seleccionadas
	    $('input[type=radio]:checked').each(function() {
	        respuestasSeleccionadas.push(parseInt($(this).val()));
	    });

	    if (!idTest || respuestasSeleccionadas.length === 0) {
	        alert("Error: Debes seleccionar un test y al menos una respuesta.");
	        return;
	    }

	    console.log("Datos que se enviarán:", { idTest: idTest, respuestas: respuestasSeleccionadas });

	    // Enviar datos al backend
	    $.ajax({
	        url: '/calcularNota',
	        type: 'POST',
	        contentType: 'application/json',
	        data: JSON.stringify({
	            idTest: idTest,
	            respuestas: respuestasSeleccionadas
	        }),
	        success: function(response) {
	            console.log("Nota calculada:", response.nota);
	            let notaObtenida = response.nota;

	            // ✅ Volver a cargar la última puntuación desde sesión
	            $.ajax({
	                url: '/ultimaPuntuacion',
	                type: 'GET',
	                data: { idTest: idTest },
	                success: function(response) {
	                    console.log("Última nota después de actualización:", response);
	                    
	                    let notaAnterior = response.penultimaNota !== null ? response.penultimaNota : 'Sin registros previos';

	                    $('#nota-obtenida').html(`<p><strong>Nota obtenida en este test:</strong> ${notaObtenida}</p>`);
	                    $('#ultima-nota').html(`<p><strong>Nota anterior:</strong> ${notaAnterior}</p>`);
	                }
	            });
	        },
	        error: function(xhr, status, error) {
	            console.error("Error en la petición AJAX:", status, error);
	            console.error("Detalles del error:", xhr.responseText);
	        }
	    });
	});

	}
});
