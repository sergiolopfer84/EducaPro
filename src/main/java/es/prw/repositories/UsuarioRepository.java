package es.prw.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import es.prw.models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Transactional(readOnly = true)
    Optional<Usuario> findByEmail(String email);

    @Transactional(readOnly = true)
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    @Transactional(readOnly = true)
    boolean existsByEmail(String email);

    @Transactional(readOnly = true)
    Optional<Usuario> findByIdUsuario(Integer idUsuario); // Solo si es necesario
}
