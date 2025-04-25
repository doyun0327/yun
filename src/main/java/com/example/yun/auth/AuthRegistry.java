package com.example.yun.auth;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/** MAC�ּҿ� FingerPrint ��� */
@Component
public class AuthRegistry {

    private final Map<String,String> registeredMap  = new HashMap<>();

    public AuthRegistry(){
        //MAC�� FingerPrint 34-C9-3D-EB-B8-3C 집 9ffef1292c7f55f874142ac1b69ff5ff
        registeredMap.put("E4-C7-67-A4-B3-56", "adecbbb9ff684a06f01a7e8f24e4ff4b") ;
    }

    public boolean isRegistered(String mac,String fingerprint){
        return registeredMap.containsKey(mac) && registeredMap.get(mac).equals(fingerprint);
    }
    
}
