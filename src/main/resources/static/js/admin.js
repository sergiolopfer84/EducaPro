document.addEventListener("DOMContentLoaded", function () {
    function getCsrfTokenFromCookie() {
        const cookies = document.cookie.split("; ");
        for (let cookie of cookies) {
            if (cookie.startsWith("XSRF-TOKEN=")) {
                return cookie.split("=")[1];
            }
        }
        return null;
    }

    window.csrf = {
        token: getCsrfTokenFromCookie(),
        headerName: "X-XSRF-TOKEN"
    };

    console.log("CSRF Token obtenido:", window.csrf.token);
	
	function toggleSection(sectionId) {
		    let section = document.getElementById(sectionId);
		    if (section) {
		        section.style.display = section.style.display === "none" ? "block" : "none";
		    }
		}
    document.getElementById("toggleView").addEventListener("click", function () {
        let button = this;
        let isAdmin = button.textContent.includes("Usuario");
        button.textContent = isAdmin ? "Modo Administrador" : "Modo Usuario";
    });

    document.getElementById("tipoGestion").addEventListener("change", function () {
        let tipo = this.value;
        let formContainer = document.getElementById("gestionForm");
        formContainer.innerHTML = "";

        if (tipo === "materia") {
            formContainer.innerHTML = `
                <label for="nombreMateria">Nombre de la Materia:</label>
                <input type="text" class="form-control" id="nombreMateria">
                <button class="btn btn-success mt-2" id="btnCrearMateria">Crear Materia</button>
            `;
        } else if (tipo === "test") {
            formContainer.innerHTML = `
                <label for="nombreTest">Nombre del Test:</label>
                <input type="text" class="form-control" id="nombreTest">
                <label for="idMateria">ID de la Materia:</label>
                <input type="number" class="form-control" id="idMateria">
                <button class="btn btn-success mt-2" id="btnCrearTest">Crear Test</button>
            `;
        }

        asignarEventosBotones();
    });

    function asignarEventosBotones() {
        if (document.getElementById("btnCrearMateria")) {
            document.getElementById("btnCrearMateria").addEventListener("click", crearMateria);
        }
        if (document.getElementById("btnCrearTest")) {
            document.getElementById("btnCrearTest").addEventListener("click", crearTest);
        }
    }

    async function sendRequest(url, method = "GET", body = null) {
        const options = {
            method,
            headers: {
                "Content-Type": "application/json",
                [window.csrf.headerName]: window.csrf.token
            }
        };

        if (body) {
            options.body = JSON.stringify(body);
        }

        try {
            const response = await fetch(url, options);
            if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
            return await response.json();
        } catch (error) {
            console.error("Error en la solicitud:", error);
            alert(`Error: ${error.message}`);
        }
    }
	

    window.crearMateria = async function () {
        const nombreMateria = document.getElementById("nombreMateria").value;
        if (nombreMateria) {
            const nuevaMateria = await sendRequest("/admin/materias", "POST", { nombreMateria });
            if (nuevaMateria) alert(`Materia creada: ${nuevaMateria.nombreMateria}`);
        }
    };

    window.crearTest = async function () {
        const nombreTest = document.getElementById("nombreTest").value;
        const idMateria = document.getElementById("idMateria").value;
        if (nombreTest && idMateria) {
            const nuevoTest = await sendRequest("/admin/tests", "POST", { nombreTest, idMateria });
            if (nuevoTest) alert(`Test creado: ${nuevoTest.nombreTest}`);
        }
    };
});
