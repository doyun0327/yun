package com.example.yun.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * 실제 흐름
 * 1. 기기를 나눠주기 전에 개발자 도구의 로컬스토리지에 macAddress를 입력한다.
 * 2. fingerprint,macAddress를 db에 저장한다.
 * 3. sse요청이 오면 db에 있는 macAddress값 기준으로 fingerprint값을 비교한다.
 * 사용자한테 배포할때는 로컬스토리지에 mac주소 세팅해서 배포
 * 
 * mac + 핑거프린트 같이 쓰는 이유: 개발자도구에 mac주소 넣어서 쓰면 다른컴에서도 접속가능하니까 fingerprint값을 비교해서 인증
 * 
 * mac이 없이 fp만으로도 충분한지?
 * 수많은 속성들 (브라우저 종류, 언어, 해상도, 시간대, OS, CPU, 렌더링 방식 등)을 조합
 *  다른 기기와 겹칠 확률이 극히 낮음
 * 실제 통계상 수천만 명 중에 동일한 fingerprint를 가지는 경우는 거의 없음
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final AuthRegistry authRegistry;

    //AuthRegistry ����
    public AuthController(AuthRegistry authRegistry) {
        this.authRegistry = authRegistry;
    }

    // ���� API
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
