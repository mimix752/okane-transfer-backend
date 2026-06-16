package com.okanetransfer.config;

import com.okanetransfer.security.JwtAuthFilter;
import com.okanetransfer.security.RateLimitingFilter;
import com.okanetransfer.security.SecurityHeadersFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired private UserDetailsService userDetailsService;
    @Autowired private JwtAuthFilter jwtAuthFilter;
    @Autowired private RateLimitingFilter rateLimitingFilter;
    @Autowired private SecurityHeadersFilter securityHeadersFilter;
    @Autowired private PasswordEncoder passwordEncoder;


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/auth/**"),
                                new AntPathRequestMatcher("/v3/api-docs/**"),
                                new AntPathRequestMatcher("/**", "OPTIONS"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/swagger-ui.html"),
                                new AntPathRequestMatcher("/swagger.html"),
                                new AntPathRequestMatcher("/webjars/**"),
                                new AntPathRequestMatcher("/favicon.ico"),
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/*.html"),
                                new AntPathRequestMatcher("/*.jsp"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/images/**")
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/client/**")).hasRole("CLIENT")
                        .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/agencies/**")).hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(new AntPathRequestMatcher("/api/transfers/**")).hasAnyRole("ADMIN", "AGENT", "CLIENT")
                        .requestMatchers(new AntPathRequestMatcher("/api/envoi/**")).hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(new AntPathRequestMatcher("/api/retrait/**")).hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(new AntPathRequestMatcher("/api/caisse/**")).hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(new AntPathRequestMatcher("/api/currencies/**")).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reports/**")).hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(new AntPathRequestMatcher("/api/audit/agents/**")).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/mobile-money/**")).hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(new AntPathRequestMatcher("/api/kyc-aml/**")).hasAnyRole("ADMIN", "AGENT")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://okane-transfer-frontend-roan.vercel.app"
        ));        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}