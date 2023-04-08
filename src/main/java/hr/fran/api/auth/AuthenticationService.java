package hr.fran.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fran.api.config.JwtService;
import hr.fran.api.token.Token;
import hr.fran.api.token.TokenRepository;
import hr.fran.api.token.TokenType;
import hr.fran.api.user.Role;
import hr.fran.api.user.User;
import hr.fran.api.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request,HttpServletResponse response) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        var savedUser=userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        var refreshToken = jwtService.generateRefreshToken(user);

        Cookie cookie = new Cookie("refresh_token",refreshToken);
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);

        cookie.setPath("/");
        response.addCookie(cookie);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .userRole(Role.USER)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }



    public AuthenticationResponse authenticate(AuthenticationRequest request,HttpServletResponse response) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        Role userRole= user.getRole();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        Cookie cookie = new Cookie("refresh_token",refreshToken);
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .userRole(userRole)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    private void saveUserToken(User savedUser, String jwtToken) {
        var token= Token.builder()
                .user(savedUser)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();

        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }


    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String userEmail;

        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            return;
        }

        refreshToken = authHeader.substring(7);
        userEmail=jwtService.extractUserEmail(refreshToken);

        if(userEmail!=null){
            var userDetails =  this.userRepository.findByEmail(userEmail).orElseThrow();

            if(jwtService.isTokenValid(refreshToken,userDetails)){
                var accesToken = jwtService.generateToken(userDetails);
                revokeAllUserTokens(userDetails);
                saveUserToken(userDetails,accesToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accesToken)
                        .build();

                Cookie cookie = new Cookie("refresh_token",refreshToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                cookie.setPath("/");
                response.addCookie(cookie);

                new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
            }
        }
    }
}
