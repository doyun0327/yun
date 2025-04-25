package com.example.yun.auth;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/** MACï¿½Ö¼Ò¿ï¿½ FingerPrint ï¿½ï¿½ï¿? */
@Component
public class AuthRegistry {

    private final Map<String,String> registeredMap  = new HashMap<>();

    public AuthRegistry(){
        //MACï¿½ï¿½ FingerPrint 34-C9-3D-EB-B8-3C ì§? 9ffef1292c7f55f874142ac1b69ff5ff
        registeredMap.put("34-C9-3D-EB-B8-3C", "9ffef1292c7f55f874142ac1b69ff5ff") ;
    }

    public boolean isRegistered(String mac,String fingerprint){
        return registeredMap.containsKey(mac) && registeredMap.get(mac).equals(fingerprint);
    }
    
}
