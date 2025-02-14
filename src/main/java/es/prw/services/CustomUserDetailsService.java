/*
 * package es.prw.services;
 * 
 * import java.util.List; import java.util.stream.Collectors;
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.security.core.GrantedAuthority; import
 * org.springframework.security.core.authority.SimpleGrantedAuthority; import
 * org.springframework.security.core.userdetails.UserDetails; import
 * org.springframework.security.core.userdetails.UserDetailsService; import
 * org.springframework.security.core.userdetails.UsernameNotFoundException;
 * import org.springframework.stereotype.Service;
 * 
 * import es.prw.models.Usuario; import es.prw.repositories.UsuarioRepository;
 * 
 * @Service public class CustomUserDetailsService implements UserDetailsService
 * {
 * 
 * private final UsuarioRepository usuarioRepository;
 * 
 * public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
 * this.usuarioRepository = usuarioRepository; }
 * 
 * @Override public UserDetails loadUserByUsername(String email) throws
 * UsernameNotFoundException { Usuario usuario =
 * usuarioRepository.findByEmail(email) .orElseThrow(() -> new
 * UsernameNotFoundException("Usuario no encontrado"));
 * 
 * List<GrantedAuthority> authorities = usuario.getRoles().stream() .map(rol ->
 * new SimpleGrantedAuthority(rol.getNombre())) .map(rol -> new
 * SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
 * 
 * .collect(Collectors.toList());
 * 
 * return new org.springframework.security.core.userdetails.User(
 * usuario.getEmail(), usuario.getPass(), authorities ); } }
 */

package es.prw.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.prw.models.Usuario;
import es.prw.repositories.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre())) // Se asume que en la BBDD ya tienen "ROLE_"
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPass(),
                authorities
        );
    }
}


