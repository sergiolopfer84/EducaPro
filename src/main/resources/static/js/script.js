$(document).ready(function() {
	
	$.get("/api/current-user", function(usuario) {
	         // Aquí cambiamos el texto "Bienvenido Usuario" por "Bienvenido + nombre del usuario"
	         $("#welcome-text").text("Bienvenido " + usuario.nombre);
	     }).fail(function() {
	         console.error("Error al obtener datos del usuario");
	     });

    // ================== PARTE DE CARGAR PREGUNTAS ==================
	$('#loadTest').click(function() {
	    console.log("Botón de cargar test clickeado");
	    const materia = $('#materias').val();
	    const test = $('#tests').val();


        questionsContainer.empty();
        if (!materia || !test) {
            alert('Por favor, selecciona una materia y un test.');
            return;
        }

        // Crear 10 preguntas de ejemplo
        for (let i = 1; i <= 10; i++) {
            const questionDiv = $(`
                <div class="question">
                    <h3>Pregunta ${i}: ¿Cuál es la respuesta correcta?</h3>
                    <div class="options">
                        <label><input type="radio" name="question${i}" value="A" class="radio-btn"> Opción A</label>
                        <label><input type="radio" name="question${i}" value="B" class="radio-btn"> Opción B</label>
                        <label><input type="radio" name="question${i}" value="C" class="radio-btn"> Opción C</label>
                        <label><input type="radio" name="question${i}" value="D" class="radio-btn"> Opción D</label>
                    </div>
                </div>
            `);
            questionsContainer.append(questionDiv);
        }

        // Botón de enviar respuestas
        if (!$('#submitAnswers').length) {
            const submitButton = $('<button id="submitAnswers" class="btn btn-primary">Enviar respuestas</button>');
            questionsContainer.append(submitButton);
            submitButton.click(function() {
                alert('Respuestas enviadas. ¡Gracias por participar!');
            });
        }
    });


    // ================== LOGIN FORM-BASED ==================
    $('#loginBtn').click(function(e) {
        e.preventDefault(); // Evita un submit accidental si es <button type="submit">

        // Usaremos username y password,
        // OJO: si en tu input usas "loginUsername" e "loginPassword"
        // y en Security config .usernameParameter("username"), .passwordParameter("password")
        // entonces "username" y "password" deben matchear
        const email = $('#loginUsername').val();
        const password = $('#loginPassword').val();
		console.log(email,password)


        if (!email || !password) {
            alert('Por favor, completa todos los campos.');
            return;
        }

        // Enviar solicitud de inicio de sesión con parámetros tipo "form-data"
        $.ajax({
            url: '/login',  // loginProcessingUrl en SecurityConfiguration
            type: 'POST',
            data: {
                email: email, // .usernameParameter("username")
                password: password  // .passwordParameter("password")
            },
            beforeSend: function(xhr) {
				console.log(email,password)
                // Adjuntar cabecera CSRF si es necesario
                if (window.csrf.headerName && window.csrf.token) {
                    xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
                }
            },
            success: function() {
                // Si las credenciales son correctas, Spring redirige a /home.
                // Pero como es AJAX, forzamos la redirección:
                window.location.href = '/home';
            },
            error: function(xhr) {
                // Si credenciales inválidas => 401 o 403
                alert('Error al iniciar sesión.');
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
});
