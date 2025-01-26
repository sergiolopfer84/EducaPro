$(document).ready(function() {
	const csrfToken = window.csrf.token;
	const csrfHeader = window.csrf.headerName;
    // Manejar el clic en el botón de carga de preguntas
    $('#loadTest').click(function() {
        const materia = $('#materias').val();
        const test = $('#tests').val();
        const questionsContainer = $('#questions-container');

        // Limpiar preguntas previas
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

        // Agregar botón de enviar respuestas si no existe
        if (!$('#submitAnswers').length) {
            const submitButton = $('<button id="submitAnswers" class="btn btn-primary">Enviar respuestas</button>');
            questionsContainer.append(submitButton);

            submitButton.click(function() {
                alert('Respuestas enviadas. ¡Gracias por participar!');
            });
        }
    });

    // Manejar el clic en el botón de inicio de sesión
    $('#loginBtn').click(function() {
        const username = $('#loginUsername').val();
        const password = $('#loginPassword').val();

        if (!username || !password) {
            alert('Por favor, introduce usuario y contraseña.');
            return;
        }

        // Enviar solicitud de inicio de sesión
        $.ajax({
            url: '/login', // Cambia esto a la URL de tu endpoint de inicio de sesión
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ username, password }),
            success: function(response) {
                alert('Inicio de sesión exitoso');
                window.location.href = '/home'; // Cambia esto a la URL de tu página de inicio
            },
            error: function(xhr) {
                alert('Error al iniciar sesión: ' + xhr.responseText);
            }
        });
    });

    // Manejar el clic en el botón de registro
    $('#registerBtn').click(function() {
        const name = $('#registerName').val();
		console.log(name)
        const email = $('#registerEmail').val();
		console.log(email)
        const password = $('#registerPassword').val();
		console.log(password)
		console.log("entrando en registro")
        if (!name || !email || !password) {
            alert('Por favor, completa todos los campos.');
            return;
        }

        // Enviar solicitud de registro
        $.ajax({			
            url: '/register', // Cambia esto a la URL de tu endpoint de registro
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ nombre: name, email: email, pass: password }),
			beforeSend: function(xhr) {
				console.log(name, email, password)
				console.log("JSON.stringify({ nombre: name, email: email, pass: password }) ",JSON.stringify({ nombre: name, email: email, pass: password }))
				if (window.csrf.headerName && window.csrf.token) { // Verifica que no sean null
				            xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
				        } else {
				            console.error("CSRF header or token is null");
				        }
			        },
            success: function(response) {
				console.log(response)
                alert('Registro exitoso');
                $('#authModal').modal('hide'); // Cerrar el modal
            },
            error: function(xhr) {
				console.error("Error al registrarse: ", xhr); // Imprime el objeto xhr completo
				    const errorMessage = xhr.responseText || 'Error al registrarse.';
				    $('#error-message').text(errorMessage).removeClass('d-none');
            }
        });
    });

    // Alternar entre formularios de inicio de sesión y registro
    $('#showRegister').click(function() {
        $('#loginForm').hide();
        $('#registerForm').show();
    });

    $('#showLogin').click(function() {
        $('#registerForm').hide();
        $('#loginForm').show();
    });
});