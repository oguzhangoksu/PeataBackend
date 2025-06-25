package peata.backend.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import peata.backend.utils.JwtAuthenticationEntryPoint;
import peata.backend.utils.JwtAuthenticationFilter;
import peata.backend.utils.RateLimitFilter;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

     @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter; // Inject the RateLimitFilter
    private static final String[] PUBLIC_URLS = {
        "/api/user/auth/login",
        "/api/user/auth/register",
        "/api/admin/auth/login",
        "/api/user/getPasswordCode",
        "/api/user/deneme",
        "/api/user/changePassword",
        "/api/add/getPaginatedAdds",
        "/api/user/emailVerification",
        "/api/user/getEmailVerificationCode",
        "/api/suggestions/suggest",
        "/api/add/findAddByPcode",
        "/api/game/scoreBoard",
        "/api/district/getAll",
        "/api/district/getById/**",
        "/api/country/getById/**",
        "/api/country/getAll",
        "/api/city/getAll",
        "/api/city/getById/**",
        "/api/city/getCitiesByCountryId/**",
        "/api/district/getDistrictsByCityId",
        "/api/district/getDistrictsByCountryId",
        "/api/add/getPaginatedAddsByCountryId",
        "/api/add/findAddByPcode",
        "/api/version/validate/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "adminpanel/*"

    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers(PUBLIC_URLS).permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/panel/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .exceptionHandling(management-> management.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)  // Add RateLimitFilter before JwtAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable()); // Disable CSRF using Lambda DSL
                              
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Add your frontend origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }



    
}