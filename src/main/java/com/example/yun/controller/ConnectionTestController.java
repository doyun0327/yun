package com.example.yun.controller;

import org.springframework.web.bind.annotation.RestController;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class ConnectionTestController {
    //produces 없애고 해보기 html로 리턴 되는지.. , produces = MediaType.TEXT_PLAIN_VALUE
    @GetMapping(value ="/api/check-connection") 
    public ResponseEntity<String> checkConnection() {
        System.out.println("Connection test endpoint hit!");
        return ResponseEntity.ok("Backend is online!");
    }



}
