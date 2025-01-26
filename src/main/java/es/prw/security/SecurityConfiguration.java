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
	        .cors() // Habilitar CORS
            .and()
            .csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            //.csrf().disable()
            .authorizeHttpRequests(authorize -> authorize
	                .requestMatchers(
	                    "/styles/**",
	                    "/bootstrap/**",
	                    "/img/**",
	                    "/js/**",
	                    "/index",
	                    "/register",
	                    "/"
	                ).permitAll() // Permitir acceso a recursos estáticos y la página de inicio
	                .anyRequest().authenticated() // Cualquier otra solicitud requiere autenticación
	            )
	            .formLogin(form -> form
	                .defaultSuccessUrl("/home", true) // Redirigir a /home después del inicio de sesión
	                .permitAll() // Permitir acceso a todos para que puedan ver el formulario de inicio de sesión
	            )
	            .logout(logout -> logout.permitAll()); // Permitir acceso a todos para cerrar sesión

	        return http.build();
	    }

	    @Bean
	    public UserDetailsService userDetailsService() {
	        return username -> {
	            Usuario appUser  = userDao.findByEmail(username)
	                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
	            return User.withUsername(appUser .getEmail())
	                .password(appUser.getPass()) // La contraseña debe estar codificada
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