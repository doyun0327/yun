package com.example.yun.auth;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/** MAC�ּҿ� FingerPrint ��� */
@Component
public class AuthRegistry {

    private final Map<String,String> registeredMap  = new HashMap<>();

    public AuthRegistry(){
        //MAC�� FingerPrint
        registeredMap.put("34-C9-3D-EB-B8-3C", "9ffef1292c7f55f874142ac1b69ff5ff");
    }

    public boolean isRegistered(String mac,String fingerprint){
        // mac �ּҿ� fingerprint �� ���� ���������� ��
        return registeredMap.containsKey(mac) && registeredMap.get(mac).equals(fingerprint);
    }
    
}
