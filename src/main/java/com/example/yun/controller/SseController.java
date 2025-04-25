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

    // 모든 클라이언트의 세션 ID와 SseEmitter를 저장하는 맵
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/sse", produces = "text/event-stream")
    public SseEmitter handleSse(@RequestParam(name = "sessionId", required = false) String sessionIdParam) {
        // sessionId가 없을 경우 기본값 설정
        final String sessionId = (sessionIdParam == null || sessionIdParam.isEmpty())
                ? "unknown_" + System.currentTimeMillis()
                : sessionIdParam;

        final SseEmitter emitter = new SseEmitter(60_000L);
        emitters.put(sessionId, emitter);

        // 연결 시 현재 세션 수 브로드캐스트
        broadcastSessionCount();

        // 클라이언트에 데이터 전송 쓰레드
        new Thread(() -> {
            try {
                for (int i = 5; i >= 0; i--) {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("보내는 데이터 " + i)
                            .id(String.valueOf(i)));
                    Thread.sleep(1000);
                }

                emitter.send(SseEmitter.event()
                        .name("close")
                        .data("정상 종료")
                        .id("end"));

                // emitter 종료
                emitter.complete();

            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            } finally {
                emitters.remove(sessionId); // 세션 제거
                broadcastSessionCount();    // 모든 클라이언트에게 최신 세션 수 알림
            }
        }).start();

        return emitter;
    }

    // 모든 클라이언트에게 현재 세션 수 전송
    private void broadcastSessionCount() {
        int sessionCount = emitters.size();

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
