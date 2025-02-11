package es.prw.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import es.prw.repositories.UsuarioRepository;
import es.prw.services.CustomUserDetailsService;
import es.prw.services.LoginAttemptService;

@CrossOrigin(origins = "http://localhost:8080")
@Configuration
public class SecurityConfiguration {

    private final CustomUserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;

    public SecurityConfiguration(@Lazy CustomUserDetailsService userDetailsService, LoginAttemptService loginAttemptService) {
        this.userDetailsService = userDetailsService;
        this.loginAttemptService = loginAttemptService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/chat")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/styles/**", "/img/**", "/js/**").permitAll()
                .requestMatchers("/api/chat").permitAll()
                .requestMatchers("/chat.html").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
            	    .loginPage("/") // <-- REDIRECCIONA AL INICIO CUANDO NO ESTÁ AUTENTICADO
            	    .loginProcessingUrl("/login")
            	    .usernameParameter("email")
            	    .passwordParameter("password")
            	    .successHandler((request, response, authentication) -> {
            	        String email = request.getParameter("email");
            	        loginAttemptService.loginSucceeded(email);
            	        response.setStatus(HttpServletResponse.SC_OK);
            	    })
            	    .failureHandler((request, response, exception) -> {
            	        String email = request.getParameter("email");
            	        if (loginAttemptService.isBlocked(email)) {
            	            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            	            response.getWriter().write("Demasiados intentos fallidos. Espere 1 minuto.");
            	        } else {
            	            loginAttemptService.loginFailed(email);
            	            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            	        }
            	    })
            	    .permitAll()
            	)

            .logout(logout -> logout
            	    .logoutUrl("/logout")
            	    .logoutSuccessUrl("/login?logout") // <-- REDIRECCIONA AL LOGIN TRAS CERRAR SESIÓN
            	    .invalidateHttpSession(true)
            	    .clearAuthentication(true)
            	    .permitAll()
            	    );


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
