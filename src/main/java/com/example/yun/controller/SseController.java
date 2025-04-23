package com.example.yun.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class SseController {
    
    @GetMapping(value = "/sse", produces = "text/event-stream")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(60_000L);
        
        try {
            emitter.send(SseEmitter.event()
                .data("단일 메시지 테스트")
                .id("1")
            );
            
            // 1초 지연 후 종료 (클라이언트가 onopen을 처리할 시간 확보)
            new Thread(() -> {
                try {
                    for (int i = 10; i >= 0; i--) {
                        emitter.send(SseEmitter.event()
                            .name("message") // 일반 메시지 이벤트 이름
                            .data("보내는 데이터 " + i)
                            .id(String.valueOf(i))
                        );
        
                        Thread.sleep(1000); // 1초 간격
                    }
        
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    try {
                        emitter.send(SseEmitter.event()
                            .name("close") // 이벤트 이름을 "close"로 지정
                            .data("정상 종료")
                            .id("2")
                        );
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                        return;
                    }
                
                    System.out.println("SSE emitter complete.");
                    emitter.complete(); // 메시지 전송 후 연결 종료
                }
            }).start();
            
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
}
