

package es.prw.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.prw.services.LoginAttemptService;

import java.util.Map;

@Configuration
public class SecurityConfiguration {
    
    private final LoginAttemptService loginAttemptService;

    public SecurityConfiguration(@Lazy LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
				
				  .csrf(csrf -> csrf
				  .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				  .ignoringRequestMatchers("/api/asistente") )
				 
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/","/home","/auth/register", "/auth/login", "/styles/**", "/img/**", "/js/**").permitAll()
                .requestMatchers("/api/asistente", "/chat.html").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/usuarios/api/current-user").authenticated()
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
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    if (loginAttemptService.isBlocked(email)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        new ObjectMapper().writeValue(response.getWriter(),
                                Map.of("error", "Demasiados intentos fallidos. Espere 1 minuto."));
                    } else {
                        loginAttemptService.loginFailed(email);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        new ObjectMapper().writeValue(response.getWriter(),
                                Map.of("error", "Credenciales incorrectas."));
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
