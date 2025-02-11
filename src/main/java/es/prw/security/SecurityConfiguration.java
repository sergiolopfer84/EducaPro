package es.prw.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.CrossOrigin;

import es.prw.repositories.UsuarioRepository;
import es.prw.services.LoginAttemptService;

@CrossOrigin(origins = "http://localhost:8080")
@Configuration
public class SecurityConfiguration {
    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService;

    public SecurityConfiguration(@Lazy UsuarioRepository usuarioRepository, LoginAttemptService loginAttemptService) {
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/chat")
        )
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/styles/**", "/img/**", "/js/**", "/login").permitAll()
                .requestMatchers("/api/asistente").permitAll()
                .requestMatchers("/chat.html").permitAll()
                .anyRequest().authenticated()
        )
        .formLogin(form -> form
                .loginPage("/") // Evitamos redirección infinita
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    String email = request.getParameter("email");
                    loginAttemptService.loginSucceeded(email);
                    response.sendRedirect("/home"); // Redirección tras login exitoso
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
                .logoutUrl("/auth/logout")
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
            var appUser = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
            return User.withUsername(appUser.getEmail())
                    .password(appUser.getPass())
                    .roles("USER") // Asignar roles correctamente
                    .build();
        };
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
