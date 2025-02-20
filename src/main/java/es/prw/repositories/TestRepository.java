package es.prw.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.prw.models.Materia;
import es.prw.models.Test;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer> {

	
	 @Transactional(readOnly = true)
	    Optional<Test> findByNombreTest(String nombreTest); 
	 
	@Transactional(readOnly = true)
    int countByMateria(Materia materia);
//devuelve todos los tests asociados a una materia específica
    @Transactional(readOnly = true)
    List<Test> findByMateriaIdMateria(Integer idMateria);

    // Nuevo método para obtener una materia por su ID
    @Query("SELECT t.materia FROM Test t WHERE t.materia.idMateria = :idMateria")
    Optional<Materia> findMateriaById(@Param("idMateria") Integer idMateria);

	List<Test> findByActivaTrue();
	List<Test> findByMateria_IdMateriaAndActivaTrue(int idMateria);


}
