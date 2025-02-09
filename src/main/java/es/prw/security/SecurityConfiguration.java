package es.prw.security;

import jakarta.servlet.http.HttpServletResponse;
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
import es.prw.services.LoginAttemptService;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@CrossOrigin(origins = "http://localhost:8080")
@Configuration
public class SecurityConfiguration {
    private final UserDao userDao;
    private final LoginAttemptService loginAttemptService;

    public SecurityConfiguration(@Lazy UserDao userDao, LoginAttemptService loginAttemptService) {
        this.userDao = userDao;
        this.loginAttemptService = loginAttemptService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/chat")
        )
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/styles/**", "/img/**", "/js/**").permitAll()
                .requestMatchers("/api/chat").permitAll()
                .requestMatchers("/chat.html").permitAll()
                .anyRequest().authenticated()
        )
        .formLogin(form -> form
                .loginPage("/")
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
