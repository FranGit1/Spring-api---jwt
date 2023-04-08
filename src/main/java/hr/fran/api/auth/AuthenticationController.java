package hr.fran.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register (@RequestBody RegisterRequest request,HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.register(request,response));
    }




    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate (@RequestBody AuthenticationRequest request,HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.authenticate(request,response));

    }


    @PostMapping("/refresh-token")
    public void refreshToken (  HttpServletRequest request,
    HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request,response);


    }
}
