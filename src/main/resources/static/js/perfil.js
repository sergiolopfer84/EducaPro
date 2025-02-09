$(document).ready(function() {

	// ======================= Cargar Perfil de Usuario =======================
	function cargarPerfil() {
		$.ajax({
			url: '/api/perfil',
			type: 'GET',
			success: function(data) {
				$('#perfil-nombre').text(data.nombre);
				$('#perfil-email').text(data.email);
				$('#perfil-rol').text(data.rol);
				$('#welcome-text').text(`Perfil de ${data.nombre}`);
			},
			error: function() {
				alert('Error al cargar perfil');
			}
		});
	}

	// ======================= Cambio de Contraseña =======================
	$('#changePasswordForm').submit(function(e) {
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

	    console.log("Enviando nueva contraseña...");

	    if (!window.csrf || !window.csrf.token || !window.csrf.headerName) {
	        console.error("CSRF token no definido");
	        return;
	    }

	    $.ajax({
	        url: '/api/cambiarPassword',
	        type: 'POST',
	        contentType: 'application/json',
	        data: JSON.stringify({ nuevaPassword: newPassword }),
	        beforeSend: function(xhr) {
	            xhr.setRequestHeader(window.csrf.headerName, window.csrf.token);
	        },
	        success: function(response) {
	            console.log("Respuesta del servidor:", response);
	            $('#passwordMessage').text('Contraseña actualizada con éxito.').css('color', 'green');
	            $('#newPassword, #confirmPassword').val('');
	        },
	        error: function(xhr) {
	            console.log("Error en la petición AJAX:", xhr);
	            $('#passwordMessage').text('Error al cambiar la contraseña.').css('color', 'red');
	        }
	    });
	});


	// ======================= Cargar Progreso de Materias =======================
	function cargarProgresoMaterias() {
		$.ajax({
			url: '/api/progresoMaterias',
			type: 'GET',
			success: function(data) {
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
			error: function() {
				$('#materia-progresos').html('<p style="color: red;">Error al cargar el progreso.</p>');
			}
		});
	};
	// ======================= Cargar Gráfico de Notas =======================
	
	function cargarGraficoNotas() {
	    $.ajax({
	        url: '/api/progresoTests?idUsuario=1', 
	        type: 'GET',
	        success: function (data) {
	            $('#graficos-container').html(''); // Limpiar gráficos anteriores

	            Object.keys(data).forEach((materiaNombre) => {
	                $('#graficos-container').append(`<div class="materia-wrapper"><h2>${materiaNombre}</h2></div>`);

	                let materiaContainer = $('<div class="materia-container"></div>');

	                Object.keys(data[materiaNombre]).forEach((testNombre, index) => {
	                    let canvasId = `graficoNotas-${materiaNombre.replace(/\s+/g, '-')}-${index}`;
	                    materiaContainer.append(`
	                        <div class="grafico-wrapper">
	                            <h3>${testNombre}</h3>
	                            <canvas id="${canvasId}" class="clickable-chart" width="400" height="200"></canvas>
	                        </div>
	                    `);
	                });

	                $('#graficos-container').append(materiaContainer);

	                // Crear gráficos y añadir eventos para abrir el modal
	                Object.keys(data[materiaNombre]).forEach((testNombre, index) => {
	                    let canvasId = `graficoNotas-${materiaNombre.replace(/\s+/g, '-')}-${index}`;
	                    let ctx = document.getElementById(canvasId).getContext('2d');

	                    let chartData = {
	                        labels: data[materiaNombre][testNombre].map((_, i) => `Nota ${i + 1}`),
	                        datasets: [{
	                            label: `Notas de ${testNombre}`,
	                            data: data[materiaNombre][testNombre],
	                            backgroundColor: "rgba(59, 130, 246, 0.7)", // Azul con transparencia
	                            borderColor: "rgba(59, 130, 246, 1)", // Azul sólido
	                            borderWidth: 1,
	                            borderRadius: 5, // Esquinas redondeadas en las barras
	                        }]
	                    };

	                    let chartOptions = {
	                        responsive: true,
	                        animation: {
	                            duration: 1500,
	                            easing: "easeInOutQuart"
	                        },
	                        plugins: {
	                            legend: {
	                                labels: {
	                                    color: "black",
	                                    font: { size: 14, weight: "bold" }
	                                }
	                            },
	                            tooltip: {
	                                backgroundColor: "rgba(0, 0, 0, 0.7)",
	                                titleFont: { size: 16, weight: "bold" },
	                                bodyFont: { size: 14 }
	                            }
	                        },
	                        scales: {
	                            x: {
	                                grid: { display: false },
	                                ticks: { color: "black", font: { size: 12 } }
	                            },
	                            y: {
	                                beginAtZero: true,
	                                max: 10,
	                                grid: { color: "rgba(0,0,0,0.1)" },
	                                ticks: { color: "black", font: { size: 12 } }
	                            }
	                        }
	                    };

	                    let chartInstance = new Chart(ctx, {
	                        type: 'bar', // 🔹 Cambiado de 'line' a 'bar'
	                        data: chartData,
	                        options: chartOptions
	                    });

	                    // Evento para abrir el modal con el gráfico ampliado
	                    $(`#${canvasId}`).click(function () {
	                        abrirModalGrafico(chartData, chartOptions, testNombre);
	                    });
	                });
	            });
	        },
	        error: function () {
	            console.log('Error al cargar el gráfico de notas.');
	        }
	    });
	}


	function abrirModalGrafico(chartData, chartOptions, testNombre) {
	    $('#modalTitulo').text(`Notas de ${testNombre}`);

	    // Limpiar y agregar nuevo canvas
	    $('#modalGraficoContainer').html('<canvas id="modalGraficoCanvas"></canvas>');

	    let ctx = document.getElementById("modalGraficoCanvas").getContext("2d");
	    new Chart(ctx, {
	        type: 'bar',
	        data: chartData,
	        options: chartOptions
	    });

	    // Mostrar el modal correctamente
	    $('#modalGrafico').addClass('show').fadeIn();
	    $('.modal-content').fadeIn();
	}

	// Cerrar modal al hacer clic en la "X" o fuera del modal
	$(document).on('click', '#modalClose, #modalGrafico', function(event) {
	    if (event.target.id === 'modalClose' || event.target.id === 'modalGrafico') {
	        $('#modalGrafico').removeClass('show').fadeOut();
	        $('.modal-content').fadeOut();
	    }
	});





	// ======================= Ejecutar Funciones al Cargar la Página =======================
	cargarPerfil();
	cargarProgresoMaterias();
	cargarGraficoNotas();

});
