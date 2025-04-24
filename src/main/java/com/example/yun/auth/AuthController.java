package com.example.yun.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final AuthRegistry authRegistry;

    //AuthRegistry 주입
    public AuthController(AuthRegistry authRegistry) {
        this.authRegistry = authRegistry;
    }

    // 인증 API
    @PostMapping("/auth")
    public ResponseEntity<Map<String,Object>> authenticate (@RequestBody Map<String,String> payload){
        String mac= payload.get("macAddress");
        String fingerprint = payload.get("fingerprint");
        System.out.println("macAddress----------------"+mac);
        System.out.println("fingerprint----------------"+fingerprint);
        boolean success = authRegistry.isRegistered(mac, fingerprint);

        return ResponseEntity.ok(Map.of("success",success));
    }

}
