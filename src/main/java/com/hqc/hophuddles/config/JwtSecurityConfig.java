//package com.hqc.hophuddles.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
//import org.springframework.security.oauth2.core.OAuth2TokenValidator;
//import org.springframework.security.oauth2.jwt.*;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
//import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//public class JwtSecurityConfig {
//
//    @Value("${auth0.audience}")
//    private String audience;
//
//    @Value("${auth0.domain}")
//    private String issuer;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt
//                                .decoder(jwtDecoder())
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
//                        )
//                )
//                .authorizeHttpRequests(auth -> auth
//                        // Public endpoints - keep your existing H2 console access
//                        .requestMatchers("/health/**", "/h2-console/**", "/api/v1/auth/**").permitAll()
//                        .requestMatchers("/api/v1/test/**").permitAll() // Keep test endpoints public for now
//
//                        // Protected endpoints - will be secured once Auth0 is configured
//                        .requestMatchers("/api/v1/agencies/**").permitAll() // Temporary - will secure later
//                        .requestMatchers("/api/v1/users/**").permitAll()    // Temporary - will secure later
//                        .requestMatchers("/api/v1/sequences/**").permitAll() // Temporary - will secure later
//                        .requestMatchers("/api/v1/huddles/**").permitAll()  // Temporary - will secure later
//                        .requestMatchers("/api/v1/progress/**").permitAll() // Temporary - will secure later
//                        .requestMatchers("/api/v1/analytics/**").permitAll() // Temporary - will secure later
//
//                        .anyRequest().authenticated()
//                )
//                .headers(headers -> headers
//                        .frameOptions().deny() // Keep your existing frame options
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        String issuerUri = String.format("https://%s/", issuer);
//
//        try {
//            NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
//
//            OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
//            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
//            OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
//
//            jwtDecoder.setJwtValidator(withAudience);
//            return jwtDecoder;
//        } catch (Exception e) {
//            // Fallback for development when Auth0 is not configured
//            return NimbusJwtDecoder.withJwkSetUri("https://your-tenant.auth0.com/.well-known/jwks.json").build();
//        }
//    }
//
//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
//        converter.setAuthoritiesClaimName("permissions");
//        converter.setAuthorityPrefix("SCOPE_");
//
//        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
//        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
//        return jwtConverter;
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}