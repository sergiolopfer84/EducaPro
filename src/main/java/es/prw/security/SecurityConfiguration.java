package es.prw.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.CrossOrigin;

import es.prw.daos.UserDao;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletResponse;

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

        http.csrf(csrf -> 
            csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers("/api/chat")
            
        )
        .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/", "/register", "/styles/**", "/img/**", "/js/**").permitAll()
                // Permitir el acceso anónimo a /api/chat
                .requestMatchers("/api/chat").permitAll()
                // Permitir también /chat.html, si lo tuvieras como archivo estático
                .requestMatchers("/chat.html").permitAll()
                // Cualquier otra requiere autenticación
                .anyRequest().authenticated()
        )
        .formLogin(form -> form
                // Indica dónde está tu “formulario” (tu página custom con el modal)
                .loginPage("/")
                
                // Indica la URL a la que se hace POST para loguear (Spring Security la procesa)
                .loginProcessingUrl("/login") 
                
                // El name del input para el usuario en tu formulario
                .usernameParameter("email")

                // El name del input para la contraseña en tu formulario
                .passwordParameter("password")
                
                // Configuramos un successHandler que devuelve 200
                .successHandler((request, response, authentication) -> {
                    // Podemos responder OK y dejar que el front (Ajax) redirija a /home
                    response.setStatus(HttpServletResponse.SC_OK);
                })
                
                // Configuramos un failureHandler que devuelve 401
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                })
                
                .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .permitAll()
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
        );

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
        authenticationManagerBuilder
                .userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}
