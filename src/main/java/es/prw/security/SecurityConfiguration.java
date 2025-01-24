package es.prw.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Autorización de peticiones
            .authorizeHttpRequests(auth -> auth
                // Permitimos el acceso a la raíz, /home y /public/**
            		.requestMatchers(
            			    "/styles/**",
            			    "/bootstrap/**",
            			    "/img/**",
            			    "/js/**",
            			    "/login",
            			    "/",
            			    "/home",
            			    "/public/**"
            			).permitAll()

                .requestMatchers("/", "/home", "/public/**").permitAll()
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // 2. Configuración del formulario de login
            .formLogin(form -> form
            .defaultSuccessUrl("/home",true)
                // Ruta al formulario de inicio de sesión
               // .loginPage("/login")
                // Permitir acceso a todos para que puedan ver el formulario
                .permitAll()
            )

            // 3. Configuración del logout
            .logout(logout -> logout.permitAll());

        // 4. Deshabilitar CSRF si fuese necesario (por ejemplo, para APIs),
        // pero no lo hagas en aplicaciones web si no es necesario.
        // http.csrf().disable();

        // Construimos el objeto de seguridad
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Ejemplo de usuario en memoria:
        @SuppressWarnings("deprecation")
		UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password") // en uso real: usar contraseñas encriptadas
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}