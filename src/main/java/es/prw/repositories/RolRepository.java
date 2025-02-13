package es.prw.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import es.prw.models.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> { // ID cambiado de Long a Integer
    @Transactional(readOnly = true)
    Optional<Rol> findByNombre(String nombre);
}
