package com.princeworks.blogger.security;

import com.princeworks.blogger.security.jwt.AuthEntryPointJwt;
import com.princeworks.blogger.security.jwt.AuthTokenFilter;
import com.princeworks.blogger.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Autowired private AuthEntryPointJwt unauthorizedHandler;
  @Autowired private UserDetailsServiceImpl userDetailsService;
  @Autowired private FrameOptionsRemovalFilter frameOptionsRemovalFilter;

  @Value("${cors.allowed-origins}")
  private String[] corsAllowedOrigins;

  @Value("${cors.allowed-methods}")
  private String[] corsAllowedMethods;

  @Value("${cors.allowed-headers}")
  private String[] corsAllowedHeaders;

  @Value("${cors.allow-credentials}")
  private boolean corsAllowCredentials;

  @Value("${cors.max-age}")
  private long corsMaxAge;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(java.util.Arrays.asList(corsAllowedOrigins));
    configuration.setAllowedMethods(java.util.Arrays.asList(corsAllowedMethods));
    configuration.setAllowedHeaders(java.util.Arrays.asList(corsAllowedHeaders));
    configuration.setAllowCredentials(corsAllowCredentials);
    configuration.setMaxAge(corsMaxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/signin", "/api/auth/signup", "/api/auth/me")
                    .permitAll()
                    .requestMatchers("/api/blogs/**")
                    .permitAll()
                    .requestMatchers("/api/auth/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/auth/profile")
                    .authenticated()
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll()
                    .requestMatchers("/api/public/**")
                    .permitAll()
                    .requestMatchers("/api/test/**")
                    .permitAll()
                    .requestMatchers("/pdfs/**")
                    .permitAll()
                    .requestMatchers("/author-images/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated());
    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(
        authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    // Disable default frame options - we'll handle it in the filter
    http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
    // Add filter at the end to intercept X-Frame-Options for PDFs/images
    // The response wrapper will intercept header setting from Spring Security's header writers
    http.addFilterAfter(frameOptionsRemovalFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web ->
        web.ignoring()
            .requestMatchers(
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "configuration/security",
                "/swagger-ui.html",
                "/webjars/**"));
  }
}
