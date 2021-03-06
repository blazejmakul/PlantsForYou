package com.example.plantsforyou.appuser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.plantsforyou.registration.token.ConfirmationToken;
import com.example.plantsforyou.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG = "USER WITH EMAIL %s NOT FOUND";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(()
                        -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG,email)));
    }

    public String signUpUser(AppUser appUser){
        boolean userExist = appUserRepository.findByEmail(appUser.getEmail()).isPresent();

        if(userExist){
            throw new IllegalStateException("Email already taken");
        }
        String encodedPassword = passwordEncoder.encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        //TODO: SEND EMAIL
        return token;
    }
    public List<AppUser> getAppUsers(){ return appUserRepository.findAll(); }

    public AppUser getAppUser(String email){ return appUserRepository.findByEmail(email).orElseThrow(() ->  new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG,email)));
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }

    public AppUser getUserFromToken(String token){
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256("secret".getBytes())).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return getAppUser((String) decodedJWT.getSubject());
    }
}
