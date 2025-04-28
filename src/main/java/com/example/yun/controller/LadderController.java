package com.example.yun.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.yun.dto.RoomRequest;
import com.example.yun.dto.RoomResponse;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class LadderController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class); 

     // roomId와 당첨 레일을 반환하는 API
    @PostMapping(value ="/create/room") 
    public ResponseEntity<RoomResponse> checkConnection(@RequestBody RoomRequest roomRequest) {
  
     // 방 ID 생성
    String roomId = generateRoomId();

    // 랜덤 당첨 레일 생성
    int winningLane = (int) (Math.random() * roomRequest.getLanes()) + 1;

    RoomResponse response = new RoomResponse(roomId,winningLane);

    return ResponseEntity.ok(response);
    }

     // 현재 시간 기반으로 방 ID 생성 (yyyyMMddHHmmss 형식)
    private String generateRoomId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }
    


    @GetMapping(value = "/sse/roomId", produces = "text/event-stream")    
     public SseEmitter connect(@RequestParam("roomId") String roomId) {
        SseEmitter emitter = new SseEmitter(0L); // 임시 무제한 설정
        logger.info("[SseController] SSE 연결 성공: 클라이언트에 '연결 성공!' 메시지를 보냈습니다.");
        try {
            emitter.send("연결 성공!");
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
