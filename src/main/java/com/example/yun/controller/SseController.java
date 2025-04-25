package com.example.yun.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class SseController {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/sse", produces = "text/event-stream")
    public SseEmitter handleSse(@RequestParam(name = "sessionId", required = false) String sessionIdParam) {
        final String sessionId = (sessionIdParam == null || sessionIdParam.isEmpty())
                ? "unknown_" + System.currentTimeMillis()
                : sessionIdParam;

        final SseEmitter emitter = new SseEmitter(0L);
        emitters.put(sessionId, emitter);

        System.out.println("🟢 연결됨: " + sessionId);
        broadcastSessionCount();

        // 연결 종료 시 처리
        emitter.onCompletion(() -> {
            System.out.println("⚫ 연결 종료: " + sessionId);
            emitters.remove(sessionId);
            broadcastSessionCount();
        });

        // 타임아웃 시 처리
        emitter.onTimeout(() -> {
            System.out.println("⏰ 타임아웃 발생: " + sessionId);
            emitter.complete();
            emitters.remove(sessionId);
            broadcastSessionCount();
        });

        // 에러 발생 시 처리
        emitter.onError(e -> {
            System.out.println("❌ 에러 발생: " + sessionId);
            emitter.completeWithError(e);
            emitters.remove(sessionId);
            broadcastSessionCount();
        });

        // 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data("연결 성공: " + sessionId));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emitter;
    }

    // 모든 클라이언트에게 현재 세션 수 브로드캐스트
    private void broadcastSessionCount() {
        int sessionCount = emitters.size();
        System.out.println("📢 현재 연결된 세션 수: " + sessionCount);

        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name("sessionCount")
                        .data("현재 연결된 세션 수: " + sessionCount)
                        .id("sessionCount"));
            } catch (IOException e) {
                entry.getValue().completeWithError(e);
            }
        }
    }
}
