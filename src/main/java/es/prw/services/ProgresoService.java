package es.prw.services;

import es.prw.dtos.MateriaProgresoDTO;
import es.prw.models.Materia;
import es.prw.models.Puntuacion;
import es.prw.models.Test;
import es.prw.repositories.PuntuacionRepository;
import es.prw.repositories.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProgresoService {

	private final PuntuacionRepository puntuacionRepository;
	private final TestRepository testRepository;

	// Inyección de dependencias por constructor
	public ProgresoService(PuntuacionRepository puntuacionRepository, TestRepository testRepository) {
		this.puntuacionRepository = puntuacionRepository;
		this.testRepository = testRepository;
	}

	@Transactional(readOnly = true)
	public List<MateriaProgresoDTO> obtenerProgresoMaterias(Integer idUsuario) {
		// Obtenemos todas las puntuaciones del usuario ordenadas por fecha descendente
		List<Puntuacion> puntuaciones = puntuacionRepository.findPuntuacionesByUsuario(idUsuario);

		// Agrupar las puntuaciones por test. Al usar toMap con merge function, se
		// conserva la primera ocurrencia de cada test,
		// que es la más reciente debido al orden descendente.
		Map<Test, Puntuacion> ultimaPuntuacionPorTest = puntuaciones.stream()
				.collect(Collectors.toMap(Puntuacion::getTest, Function.identity(), (p1, p2) -> p1));

		// Obtenemos todas las materias (suponiendo que existen en cada test)
		List<Materia> materias = testRepository.findAll().stream().map(Test::getMateria).distinct().filter(Materia::isActiva)
				.collect(Collectors.toList());

		// Para cada materia, contamos el total de tests y cuántos tienen la última nota
		// aprobada (>= 5)
		return materias.stream().map(materia -> {
			// Filtramos los tests de la materia actual
			List<Test> testsMateria = testRepository.findByMateriaIdMateria(materia.getIdMateria());
			int totalTests = testsMateria.size();

			// Contamos los tests aprobados basándonos en la última puntuación del test para
			// el usuario
			int testsAprobados = (int) testsMateria.stream().map(ultimaPuntuacionPorTest::get) // obtenemos la última
																								// puntuación para cada
																								// test, si existe
					.filter(Objects::nonNull) // descartamos los tests sin puntuación
					.filter(p -> p.getNotaObtenida() >= 5).count();

			return new MateriaProgresoDTO(materia.getNombreMateria(), totalTests, testsAprobados);
		}).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Map<String, Map<String, List<Double>>> obtenerProgresoTests(Integer idUsuario) {
		Map<String, Map<String, List<Double>>> historialNotas = new HashMap<>();

		// Consultar las notas desde la base de datos
		List<Object[]> resultados = puntuacionRepository.obtenerHistorialNotasPorUsuario(idUsuario);

		for (Object[] fila : resultados) {
			String materiaNombre = (String) fila[0];
			String testNombre = (String) fila[1];
			Double nota = (Double) fila[2];

			// Usar `computeIfAbsent` para optimizar la inicialización de los mapas
			historialNotas.computeIfAbsent(materiaNombre, k -> new HashMap<>())
					.computeIfAbsent(testNombre, k -> new ArrayList<>()).add(nota);
		}

		return historialNotas;
	}

	@Transactional(readOnly = true)
	public Map<String, Map<String, List<Double>>> obtenerProgresoMateriaEspecifica(Integer idUsuario,
			Integer idMateria) {
		Map<String, Map<String, List<Double>>> historialNotas = new HashMap<>();

		// Consultar las notas desde la base de datos
		List<Object[]> resultados = puntuacionRepository.obtenerHistorialNotasPorUsuarioYMateria(idUsuario, idMateria);

		for (Object[] fila : resultados) {
			String testNombre = (String) fila[0];
			Double nota = (Double) fila[1];

			// Usar `computeIfAbsent` para inicializar la estructura
			historialNotas.computeIfAbsent("Materia Consultada", k -> new HashMap<>())
					.computeIfAbsent(testNombre, k -> new ArrayList<>()).add(nota);
		}

		System.out.println("Notas procesadas: " + historialNotas);
		return historialNotas;
	}

}
