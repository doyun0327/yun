package com.example.yun.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class SseController {

    private final Set<String> activeSessions = new HashSet<>(); // 활성 세션을 관리하는 Set

    @GetMapping(value = "/sse", produces = "text/event-stream")
    public SseEmitter handleSse(@RequestParam(name = "sessionId", required = false) String sessionId) {

        SseEmitter emitter = new SseEmitter(60_000L);

        if (sessionId != null && !activeSessions.contains(sessionId)) {
            activeSessions.add(sessionId);
        }

        try {
            System.out.println("현재 연결된 세션 수1: " + activeSessions.size());
            emitter.send(SseEmitter.event()
                    .name("sessionCount")
                    .data("현재 연결된 세션 수1: " + activeSessions.size())
                    .id("sessionCount_1")
            );

            new Thread(() -> {
                try {
                    for (int i = 5; i >= 0; i--) {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data("보내는 데이터 " + i)
                                .id(String.valueOf(i))
                        );
                        Thread.sleep(1000);
                    }

                    emitter.send(SseEmitter.event()
                            .name("close")
                            .data("정상 종료")
                            .id("2")
                    );

                    // 세션 제거 및 최신 세션 수 전송 (보내고 나서 emitter 종료)
                    activeSessions.remove(sessionId);
                    System.out.println("현재 연결된 세션 수2: " + activeSessions.size());

                    emitter.send(SseEmitter.event()
                            .name("sessionCount")
                            .data("현재 연결된 세션 수2: " + activeSessions.size())
                            .id("sessionCount_2")
                    );

                   // emitter.complete();
                    System.out.println("SSE emitter complete.");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }).start();

        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
