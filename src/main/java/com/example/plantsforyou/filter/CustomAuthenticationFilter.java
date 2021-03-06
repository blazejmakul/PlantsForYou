package com.example.plantsforyou.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.plantsforyou.appuser.AppUser;
import com.example.plantsforyou.appuser.LoginCredentials;
import com.example.plantsforyou.oAuth.oAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private oAuthService authService;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        log.info("New login request!");
        if (authService == null) {
            ServletContext servletContext = request.getServletContext();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            authService = webApplicationContext.getBean(oAuthService.class);
        }
        GoogleIdToken.Payload payload = null;
        String payloadEmail;
        try {
            BufferedReader reader = request.getReader();
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            LoginCredentials credentials = objectMapper.readValue(stringBuilder.toString(), LoginCredentials.class);

            String email = credentials.getUsername();
            String password = credentials.getPassword();
            String oAuth = credentials.getOAuth();
            log.info("Email is: {}", email);
            log.info("Password is: {}", password);
            UsernamePasswordAuthenticationToken authenticationToken = null;
            if (oAuth.equals("true")) {
                GoogleIdToken idToken = authService.validate(password);
                if (idToken != null) {
                    payload = idToken.getPayload();
                    payloadEmail = payload.getEmail();
                    String payloadName = (String) payload.get("name");
                    if (!authService.findByEmail(payloadEmail).isPresent()) {
                        authService.singUpUser(payload);
                    }
                    AppUser user = authService.findByEmail(payloadEmail).get();
                    authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), "none");
                }
            } else {
                authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
            }
            return authenticationManager.authenticate(authenticationToken);
        } catch (UsernameNotFoundException e) {
            authService.singUpUser(payload);
            AppUser user = authService.findByEmail(payload.getEmail()).get();
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), "none");
            return authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            log.error("Error on auth");
            throw new IllegalStateException(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException{
        AppUser user = (AppUser) authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        String access_token = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) //60 min
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
        String refresh_token = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 7 * 1440 * 60 * 1000)) //7 days
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }
}
