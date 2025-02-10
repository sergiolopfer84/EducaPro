$(document).ready(function() {

    /*************************************
     * 1. Función para "transformar" <select>
     *    en un dropdown personalizado
     *************************************/
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

            // Evitar añadir si la opción está vacía (p.e. "Elige una materia")
            if (!val) return;

            const $item = $('<div class="dropdown-item"></div>')
                .attr('data-value', val)
                .text(text);

            // Evento click en cada item
            $item.on('click', function(e) {
                e.stopPropagation();
                // Setear el <select> real y disparar 'change'
                $select.val(val).trigger('change');
                // Cambiar el texto mostrado en .dropdown-selected
                $selected.text(text);
                // Cerrar el menú
                $dropdown.removeClass('open');
            });
            $itemsContainer.append($item);
        });

        // Si el <select> actual tiene algo seleccionado, mostrarlo
        const currentValue = $select.val();
        if (currentValue) {
            const currentText = $select
                .find('option[value="' + currentValue + '"]')
                .text();
            if (currentText) {
                $selected.text(currentText);
            }
        }

        // Evento para abrir/cerrar el dropdown
        $selected.off('click').on('click', function(e) {
            e.stopPropagation();
            // Toggle la clase "open"
            $dropdown.toggleClass('open');
        });
    }

    /*************************************
     * 2. Cerrar dropdown si se hace click
     *    en cualquier lugar fuera de él
     *************************************/
    $(document).on('click', function() {
        $('.custom-dropdown.open').removeClass('open');
    });

    // ================== PERFIL ==================
    $('#perfilBtn').click(function () {
        window.location.href = 'perfil';
    });

    // ================== LOGIN FORM-BASED ==================
    $('#loginBtn').click(function(e) {
        e.preventDefault();
        const email = $('#loginUsername').val();
        const password = $('#loginPassword').val();

        if (!email || !password) {
            $('#loginError').html('<span style="color: red;">Por favor, completa todos los campos.</span>');
            return;
        }

        $.ajax({
            url: '/login',
            type: 'POST',
            data: { email: email, password: password },
            beforeSend: function(xhr) {
                if (window.csrf.headerName && window.csrf.token) {
                    xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
                }
            },
            success: function() {
                window.location.href = '/home';
            },
            error: function(xhr) {
                if (xhr.status === 401) {
                    $('#loginError').html('<span style="color: red;">Nombre o contraseña incorrectos</span>');
                } else if (xhr.status === 403) {
                    $('#loginError').html('<span style="color: red;">Demasiados intentos fallidos. Espere 1 minuto.</span>');
                } else {
                    $('#loginError').html('<span style="color: red;">Error al iniciar sesión.</span>');
                }
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

    // ================== FORZAR QUE EL MODAL SIEMPRE SE ABRA ==================
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

    // ================== REGISTRO ==================
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
            $('#registerError').html('<span style="color: red;">Inserte un email válido</span>');
            return;
        }
        const passwordRegex = /^(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{4,12}$/;
        if (!passwordRegex.test(password)) {
            $('#registerError').html('<span style="color: red;">La contraseña debe contener una mayúscula y un número</span>');
            return;
        }

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

    // ================== MOSTRAR/OCULTAR formularios modal ==================
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
    $.get("/api/current-user", function(usuario) {
        $("#welcome-text").text("Bienvenido/a " + usuario.nombre);
        if (usuario && usuario.nombre) {
            $("#loginTrigger").hide();
            $("#logoutBtn").show();
        }
    }).fail(function() {
        console.error("Error al obtener datos del usuario.");
    });

    const currentPath = window.location.pathname;

    // Configuración global CSRF
    if (window.csrf && window.csrf.token && window.csrf.headerName) {
        $.ajaxSetup({
            beforeSend: function(xhr) {
                xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
            },
        });
    }

    // ================== LÓGICA DE HOME ==================
    if (currentPath === '/home') {

        // ===== CARGAR MATERIAS =====
        $.ajax({
            url: '/materias',
            type: 'GET',
            success: function(data) {
                let options = '<option value="">Elige una materia</option>';
                data.forEach(materia => {
                    options += `<option value="${materia.idMateria}">${materia.materia}</option>`;
                });
                $('#materias').html(options);

                // Llamar a nuestra función para "convertir" <select> en dropdown
                transformSelectToDropdown('materias', 'materiasDropdown');
            },
            error: function(xhr) {
                console.error('Error al cargar las materias:', xhr);
            }
        });

        // ===== CARGAR TESTS al cambiar materia =====
        $('#materias').on('change', function() {
            const idMateria = $(this).val();
            if (!idMateria) {
                $('#tests').html('<option value="">Elige un test</option>');
                transformSelectToDropdown('tests', 'testsDropdown'); // Reseteamos
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
                    // Actualizar dropdown de tests
                    transformSelectToDropdown('tests', 'testsDropdown');
                },
                error: function(xhr) {
                    console.error('Error al cargar los tests:', xhr);
                }
            });
        });

        // ===== CARGAR PREGUNTAS al cambiar test =====
		$('#tests').on('change', function() {
		    const idTest = $(this).val();
		    if (!idTest) {
		        $('#questions-container').html('');
		        $('#ultima-nota').hide(); // Ocultar si no hay test seleccionado
		        $('#nota-obtenida').hide(); // Ocultar nota obtenida también
		        return;
		    }

		    $('#questions-container').html('');
		    $('#nota-obtenida').html('').hide();
		    $('#ultima-nota').html('').hide();

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
		                        <div class="respuesta">
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

		    // CARGAR ÚLTIMA PUNTUACIÓN Y MOSTRAR #ultima-nota SOLO SI TIENE CONTENIDO
		    $.ajax({
		        url: '/ultimaPuntuacion',
		        type: 'GET',
		        data: { idTest: idTest },
		        success: function(response) {
		            let ultimaNota = response.ultimaNota ?? null;
		            if (ultimaNota !== null) {
		                $('#ultima-nota')
		                    .html(`<p><strong>Última nota obtenida:</strong> ${ultimaNota}</p>`)
		                    .fadeIn(); // Solo mostrar si hay contenido
		            }
		        },
		        error: function(xhr) {
		            console.error("Error al obtener la última puntuación:", xhr);
		        }
		    });
		});


        // ===== FINALIZAR TEST =====
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

		    $.ajax({
		        url: '/calcularNota',
		        type: 'POST',
		        contentType: 'application/json',
		        data: JSON.stringify({ idTest: idTest, respuestas: respuestasSeleccionadas }),
		        success: function(response) {
		            window.scrollTo({ top: 0, behavior: 'smooth' });

		            let notaObtenida = response.nota;

		            // Obtener la última nota antes de esta prueba
		            $.ajax({
		                url: '/ultimaPuntuacion',
		                type: 'GET',
		                data: { idTest: idTest },
		                success: function(resp) {
		                    let notaAnterior = resp.penultimaNota !== null ? resp.penultimaNota : 'Sin registros previos';

		                    $('#ultima-nota').html(`<p><strong>Última nota obtenida:</strong> ${notaAnterior}</p>`);

		                    if (notaObtenida !== null && notaObtenida !== undefined) {
		                        $('#nota-obtenida')
		                            .html(`<p><strong>Nota obtenida en este test:</strong> ${notaObtenida}</p>`)
		                            .fadeIn(); // Mostrar con efecto
		                    }
		                }
		            });

		            // Respuestas correctas
		            $.ajax({
		                url: '/obtenerRespuestasSesion',
		                type: 'GET',
		                data: { idTest: idTest },
		                success: function(data) {
		                    let respuestasCorrectas = [];
		                    data.forEach(r => {
		                        if (r.nota === 1) {
		                            respuestasCorrectas.push(r.idRespuesta);
		                        }
		                    });

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
		                    console.error("Error en la petición AJAX:", xhr.responseText);
		                }
		            });
		        },
		        error: function(xhr) {
		            console.error("Error en la petición AJAX:", xhr.responseText);
		        }
		    });
		});

        

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
		
		/***************************************************

								ASISTENTE
		 ***************************************************/

		    const openAssistantBtn = document.getElementById("openAssistantBtn");
		    const modal = document.getElementById("assistantModal");
		    const closeBtn = document.querySelector(".close-btn");
		    const chatBox = document.getElementById("chat-box");
		    const userInput = document.getElementById("user-input");
		    const sendMessageBtn = document.getElementById("sendMessageBtn");

		    // Mostrar la modal al hacer clic en el botón
		    openAssistantBtn.addEventListener("click", function() {
		        modal.style.display = "flex";
		    });

		    // Cerrar la modal al hacer clic en la 'X'
		    closeBtn.addEventListener("click", function() {
		        modal.style.display = "none";
		    });

		    // Enviar mensaje al Asistente de Estudio
		    sendMessageBtn.addEventListener("click", function() {
		        let mensaje = userInput.value.trim();
				console.log(mensaje);
		        if (mensaje === "") return;

		        chatBox.innerHTML += `<p><strong>Tú:</strong> ${mensaje}</p>`;
		        userInput.value = "";

		        // Petición a la API
		        fetch('/api/chat', {
		            method: 'POST',
		            headers: { 'Content-Type': 'application/json' },
		            body: JSON.stringify(mensaje)
		        })
		        .then(response => response.text())
		        .then(data => {
		            chatBox.innerHTML += `<p><strong>IA:</strong> ${data}</p>`;
		            chatBox.scrollTop = chatBox.scrollHeight;
		        });
		    });

		    // Cerrar la modal si el usuario hace clic fuera del contenido
		    window.addEventListener("click", function(event) {
		        if (event.target === modal) {
		            modal.style.display = "none";
		        }
		    });
		
    } // Fin if /home

}); // Fin document.ready


/***************************************************
 * FUNCIÓN finalizarTest():
 * Bloquea respuestas y muestra botón "Intentar de nuevo"
 ***************************************************/
function finalizarTest() {
    $('#resetTest').show();
    let inputs = document.querySelectorAll("input[type='radio'], input[type='checkbox']");
    inputs.forEach(input => {
        input.disabled = true;
    });
    $('#finalizar').prop('disabled', true);
}





