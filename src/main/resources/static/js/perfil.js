$(document).ready(function () {
    
    // ======================= Cargar Perfil de Usuario =======================
    function cargarPerfil() {
        $.ajax({
            url: '/api/perfil',
            type: 'GET',
            success: function (data) {
                $('#perfil-nombre').text(data.nombre);
                $('#perfil-email').text(data.email);
                $('#perfil-rol').text(data.rol);
                $('#welcome-text').text(`Perfil de ${data.nombre}`);
            },
            error: function () {
                alert('Error al cargar perfil');
            }
        });
    }

    // ======================= Cambio de Contraseña =======================
    $('#changePasswordForm').submit(function (e) {
        e.preventDefault();
        let newPassword = $('#newPassword').val().trim();
        let confirmPassword = $('#confirmPassword').val().trim();

        if (newPassword.length < 6) {
            $('#passwordMessage').text('La contraseña debe tener al menos 6 caracteres.').css('color', 'red');
            return;
        }
        if (newPassword !== confirmPassword) {
            $('#passwordMessage').text('Las contraseñas no coinciden.').css('color', 'red');
            return;
        }

        $.ajax({
            url: '/api/cambiarPassword',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ nuevaPassword: newPassword }),
            success: function () {
                $('#passwordMessage').text('Contraseña actualizada con éxito.').css('color', 'green');
                $('#newPassword, #confirmPassword').val('');
            },
            error: function () {
                $('#passwordMessage').text('Error al cambiar la contraseña.').css('color', 'red');
            }
        });
    });

    // ======================= Cargar Progreso de Materias =======================
	function cargarProgresoMaterias() {
	       $.ajax({
	           url: '/api/progresoMaterias',
	           type: 'GET',
	           success: function (data) {
	               let html = '';
				  
	               data.forEach(materia => {
	                   let nombre = materia.materia || materia.materia || 'Desconocido';
	                   let totalTests = materia.totalTests || materia.total_tests || 0;
	                   let aprobados = materia.testsAprobados || materia.tests_aprobados || 0;
	                   
	                   // Evitar divisiones por 0
	                   let porcentaje = totalTests > 0 ? (aprobados / totalTests) * 100 : 0;

	                   html += `
	                       <div class="materia-item">
	                           <p><strong>${nombre}</strong> - ${porcentaje.toFixed(2)}%</p>
	                           <div class="progress-bar">
	                               <div class="progress" style="width: ${porcentaje}%; background: ${porcentaje >= 50 ? 'green' : 'red'};"></div>
	                           </div>
	                       </div>
	                   `;
	               });
	               $('#materia-progresos').html(html);
	           },
	           error: function () {
	               $('#materia-progresos').html('<p style="color: red;">Error al cargar el progreso.</p>');
	           }
	       });
		   };
    // ======================= Cargar Gráfico de Notas =======================
	function cargarGraficoNotas() {
	    $.ajax({
	        url: '/api/progresoTests?idUsuario=1', // Ajusta esto según cómo pases el idUsuario
	        type: 'GET',
	        success: function (data) {
	            $('#graficos-container').html(''); // Limpiar gráficos anteriores

	            Object.keys(data).forEach((testNombre, index) => {
	                let canvasId = `graficoNotas-${index}`;
	                $('#graficos-container').append(`
	                    <h3>${testNombre}</h3>
	                    <canvas id="${canvasId}" width="400" height="200"></canvas>
	                `);

	                let ctx = document.getElementById(canvasId).getContext('2d');
	                new Chart(ctx, {
	                    type: 'line',
	                    data: {
	                        labels: data[testNombre].map((_, i) => `Intento ${i + 1}`),
	                        datasets: [{
	                            label: `Notas de ${testNombre}`,
	                            data: data[testNombre],
	                            borderColor: 'blue',
	                            borderWidth: 2
	                        }]
	                    },
	                    options: {
	                        scales: {
	                            y: { min: 0, max: 10 }
	                        }
	                    }
	                });
	            });
	        },
	        error: function () {
	            console.log('Error al cargar el gráfico de notas.');
	        }
	    });
	}


    // ======================= Ejecutar Funciones al Cargar la Página =======================
    cargarPerfil();
    cargarProgresoMaterias();
    cargarGraficoNotas();

});
