package com.example.yun.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.example.yun.dto.RoomInfo;
import com.example.yun.dto.RoomRequest;
import com.example.yun.dto.RoomResponse;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class LadderController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class); 

    // 방 정보를 저장할 Map
    private Map<Integer, RoomInfo> roomInfoMap = new HashMap<>();

    // 순차적인 방 번호를 위한 변수
    private int roomNumber = 1;

     // roomId와 당첨 레일을 반환하는 API
    @PostMapping(value ="/create/room") 
    public ResponseEntity<RoomResponse> checkConnection(@RequestBody RoomRequest roomRequest) {
   int currentRoomNumber = roomNumber++;
     // 방 ID 생성
    String roomId = generateRoomId();

    // 랜덤 당첨 레일 생성
    int winRailNo = (int) (Math.random() * roomRequest.getLanes()) + 1;

     // 방 정보를 RoomInfo 객체에 저장
    RoomInfo roomInfo = new RoomInfo(roomId, roomRequest.getLanes(), winRailNo,roomRequest.getNickname());

     // Map에 방 정보 저장
     roomInfoMap.put(currentRoomNumber, roomInfo);

     //roominfomap 정보 출력
    for (Map.Entry<Integer, RoomInfo> entry : roomInfoMap.entrySet()) {
        Integer key = entry.getKey();
        RoomInfo value = entry.getValue();
        System.out.println("Key: " + key + ", 방아이디: " + value.getRoomId() + ",총 레인 수 : " + value.getLanes() + ",당첨 레인:  " + value.getWinRailNo() + ", 호스트 닉네임 :  " + value.getHostId());
    }   

    RoomResponse response = new RoomResponse(roomId,winRailNo);

    return ResponseEntity.ok(response);
    }

     // 현재 시간 기반으로 방 ID 생성 (yyyyMMddHHmmss 형식)
    private String generateRoomId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }
    

    @GetMapping(value = "/sse", produces = "text/event-stream")    
     public SseEmitter connect(@RequestParam(name = "roomId", required = true) String roomId) {
        logger.info("[SseController] SSE 연결 요청: roomId = {}", roomId);
        SseEmitter emitter = new SseEmitter(0L); // 임시 무제한 설정
        logger.info("[SseController] SSE 연결 성공: 클라이언트에 '연결 성공!' 메시지를 보냈습니다.");
        try {
            emitter.send("SSE 연결 성공!");
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    // roomInfoMap에서 방 정보를 가져오는 API
  @GetMapping("/rooms") 
    public ResponseEntity<List<RoomInfo>> getRooms() {
    // Map에 저장된 모든 방 정보 리스트로 반환
    List<RoomInfo> roomList = new ArrayList<>(roomInfoMap.values());
    return ResponseEntity.ok(roomList);
}
    
}
