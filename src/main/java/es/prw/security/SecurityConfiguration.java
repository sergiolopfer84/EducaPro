package es.prw.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import es.prw.daos.UserDao;
import es.prw.models.Usuario;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
@CrossOrigin(origins = "http://localhost:8080")
@Configuration
public class SecurityConfiguration {
	 private final UserDao userDao;

	    public SecurityConfiguration(@Lazy UserDao userDao) {
	        this.userDao = userDao;
	    }

	    @SuppressWarnings("removal")
	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	        http
	            .csrf(csrf -> csrf
	                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
	            )
	            .authorizeHttpRequests(auth -> auth
	                // Rutas públicas
	                .requestMatchers("/", "/register", "/styles/**", "/img/**", "/js/**").permitAll()
	                // Cualquier otra requiere autenticación
	                .anyRequest().authenticated()
	            )
	            .formLogin(form -> form
	                // Indica dónde está tu “formulario” (tu página custom con el modal)
	                .loginPage("/")                       // aquí se renderea tu index.html

	                // Indica la URL a la que se hace POST para loguear
	                .loginProcessingUrl("/login")         // Spring recibirá las credenciales por aquí

	                // El nombre de parámetro que usas para el usuario
	                // (si en tu formulario pones `name="username"`)
	                .usernameParameter("email")

	                // El nombre de parámetro que usas para la contraseña
	                .passwordParameter("password")

	                // Dónde redirigir tras login exitoso
	                .defaultSuccessUrl("/home", true)

	                .permitAll() // permite a todos acceder al formulario
	            )
	            .logout(logout -> logout.permitAll());

	        return http.build();
	    }


	    @Bean
	    public UserDetailsService userDetailsService() {
	    	return email -> {
	    	    var appUser = userDao.findByEmail(email)
	    	        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
	    	    return User.withUsername(appUser.getEmail())
	    	        .password(appUser.getPass())
	    	        .build();
	    	};

	    }


	    @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }

	    @Bean
	    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
	        AuthenticationManagerBuilder authenticationManagerBuilder = 
	            http.getSharedObject(AuthenticationManagerBuilder.class);
	        authenticationManagerBuilder.userDetailsService(userDetailsService())
	            .passwordEncoder(passwordEncoder());
	        return authenticationManagerBuilder.build();
	    }
}