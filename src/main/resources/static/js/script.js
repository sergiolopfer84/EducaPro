document.getElementById('loadTest').addEventListener('click', () => {
    const materia = document.getElementById('materias').value;
    const test = document.getElementById('tests').value;

    const questionsContainer = document.getElementById('questions-container');

    // Limpiar preguntas previas
    questionsContainer.innerHTML = '';

    if (!materia || !test) {
        alert('Por favor, selecciona una materia y un test.');
        return;
    }

    // Crear 10 preguntas de ejemplo
    for (let i = 1; i <= 10; i++) {
        const questionDiv = document.createElement('div');
        questionDiv.classList.add('question');

        questionDiv.innerHTML = `
            <h3>Pregunta ${i}: ¿Cuál es la respuesta correcta?</h3>
            <div class="options">
                <label><input type="radio" name="question${i}" value="A" class="radio-btn"> Opción A</label>
                <label><input type="radio" name="question${i}" value="B" class="radio-btn"> Opción B</label>
                <label><input type="radio" name="question${i}" value="C" class="radio-btn"> Opción C</label>
                <label><input type="radio" name="question${i}" value="D" class="radio-btn"> Opción D</label>
            </div>
        `;

        questionsContainer.appendChild(questionDiv);
    }

    // Hacer que los botones de opción (radio buttons) se puedan desmarcar
    document.querySelectorAll('.radio-btn').forEach((radio) => {
        let previousState = false; // Almacena el estado previo del botón
        radio.addEventListener('click', (event) => {
            if (previousState) {
                event.target.checked = false; // Desmarcar si estaba previamente marcado
            }
            previousState = event.target.checked; // Actualizar el estado
        });
    });

    // Agregar botón de enviar respuestas si no existe
    if (!document.getElementById('submitAnswers')) {
        const submitButton = document.createElement('button');
        submitButton.id = 'submitAnswers';
        submitButton.textContent = 'Enviar respuestas';
        questionsContainer.appendChild(submitButton);

        submitButton.addEventListener('click', () => {
            alert('Respuestas enviadas. ¡Gracias por participar!');
        });
    }
});

// Manejar inicio de sesión
document.getElementById('loginBtn').addEventListener('click', () => {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!username || !password) {
        alert('Por favor, introduce usuario y contraseña.');
        return;
    }

    // Aquí puedes manejar el proceso de login
   // alert(`Usuario: ${username}\nContraseña: ${password}`);
});
