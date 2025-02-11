package es.prw.services;

import es.prw.dtos.MateriaProgresoDTO;
import es.prw.dtos.NotaHistorialDTO;
import es.prw.models.Materia;
import es.prw.models.Test;
import es.prw.repositories.PuntuacionRepository;
import es.prw.repositories.TestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ProgresoService {

    @Autowired
    private PuntuacionRepository puntuacionRepository;

    @Autowired
    private TestRepository testRepository;

    public List<MateriaProgresoDTO> obtenerProgresoMaterias(Integer idUsuario) {
        List<Materia> materias = testRepository.findAll().stream()
                .map(Test::getMateria)
                .distinct()
                .toList();

        return materias.stream().map(materia -> {
            int totalTests = testRepository.countByMateria(materia);
            int testsAprobados = puntuacionRepository.countAprobadosByMateria(materia.getIdMateria());
            return new MateriaProgresoDTO(materia.getMateria(), totalTests, testsAprobados);
        }).toList();
    }
    public Map<String, Map<String, List<Double>>> obtenerProgresoTests(Integer idUsuario) {
        Map<String, Map<String, List<Double>>> historialNotas = new HashMap<>();

        // Consultar las notas desde la base de datos
        List<Object[]> resultados = puntuacionRepository.obtenerHistorialNotasPorUsuario(idUsuario);

        for (Object[] fila : resultados) {
            String materiaNombre = (String) fila[0];
            String testNombre = (String) fila[1];
            Double nota = (Double) fila[2];

            // Asegurar que la materia existe en el mapa
            historialNotas.putIfAbsent(materiaNombre, new HashMap<>());

            // Asegurar que el test existe dentro de la materia
            historialNotas.get(materiaNombre).putIfAbsent(testNombre, new ArrayList<>());

            // Agregar la nota al test correspondiente
            historialNotas.get(materiaNombre).get(testNombre).add(nota);
        }

        return historialNotas;
    }

}

