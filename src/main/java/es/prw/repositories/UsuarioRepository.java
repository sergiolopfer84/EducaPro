package es.prw.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.prw.models.Usuario;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
    boolean existsByEmail(String email);
    Optional<Usuario> findByIdUsuario(Integer idUsuario);

}

