package com.example.yun.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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

    // 방마다 클라이언트 연결을 관리할 Map
    private final Map<String, List<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

     // roomId와 당첨 레일을 반환하는 API
    @PostMapping(value ="/create/room") 
    public ResponseEntity<RoomResponse> checkConnection(@RequestBody RoomRequest roomRequest) {
   int currentRoomNumber = roomNumber++;
     // 방 ID 생성
    String roomId ="20250428163504";// generateRoomId();

    // 랜덤 당첨 레일 생성
    int winRailNo = (int) (Math.random() * roomRequest.getLanes()) + 1;

    //방장을 참여자 리스트에 추가
    List<String> Arra = new ArrayList<>();
    Arra.add(roomRequest.getNickname());  


    RoomInfo roomInfo = new RoomInfo(roomId, roomRequest.getLanes(), winRailNo, roomRequest.getNickname(),Arra);

     // Map에 방 정보 저장
     roomInfoMap.put(currentRoomNumber, roomInfo);


    RoomResponse response = new RoomResponse(roomId,winRailNo);

    return ResponseEntity.ok(response);
    }

    @PostMapping("join/room")
    public ResponseEntity<String> joinRoom(@RequestBody RoomRequest roomRequest) {
        // 방 ID와 참여자 닉네임을 가져옴
        String roomId = roomRequest.getRoomId();
        String nickname = roomRequest.getNickname();
        logger.info("[LadderController] 방 참여 요청: roomId = {}, nickname = {}", roomId, nickname);
        RoomInfo roomInfo = null;
        for (RoomInfo info : roomInfoMap.values()) {
            if (info.getRoomId().equals(roomId)) {
                roomInfo = info;
                break;
            }
        }

        // 방이 존재하지 않으면 에러 응답
        if (roomInfo == null) {
            return ResponseEntity.notFound().build();
        }

        // 방에 참여자 추가
        roomInfo.getParticipants().add(nickname);

        // 방에 연결된 모든 클라이언트에게 브로드캐스트
        broadcastMessageToRoom(roomId, nickname + "님이 참여하셨습니다.");

        for (Map.Entry<Integer, RoomInfo> entry : roomInfoMap.entrySet()) {
            Integer key = entry.getKey();
            RoomInfo value = entry.getValue();
            System.out.println("=============================");
            System.out.println("Key: " + key + ", 방아이디: " + value.getRoomId() + ", 총 레인 수 : " + value.getLanes() + ", 당첨 레인: " + value.getWinRailNo() + ", 호스트 닉네임 : " + value.getHostId() + ", 참여자 : " + value.getParticipants());
        }  

        // 응답 본문을 포함한 JSON 반환
        return ResponseEntity.ok("참여완료");
    }


    
     // 현재 시간 기반으로 방 ID 생성 (yyyyMMddHHmmss 형식)
    private String generateRoomId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }
    

 // 클라이언트와 연결을 위한 SSE 엔드포인트
    @GetMapping(value = "/sse", produces = "text/event-stream")
    public SseEmitter connect(@RequestParam(name = "roomId", required = true) String roomId) {
        logger.info("[SseController] SSE 연결 요청: roomId = {}", roomId);

        // 방에 대한 기존 연결된 클라이언트 목록을 가져오기
        List<SseEmitter> emitters = sseEmitters.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());
        
        // 새로운 SSE 연결을 추가
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        // 연결 성공 메시지 보내기
        try {
            emitter.send("SSE 연결 성공!");
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        // 연결이 끊어지면 해당 클라이언트를 제거
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        for (Map.Entry<Integer, RoomInfo> entry : roomInfoMap.entrySet()) {
            Integer key = entry.getKey();
            RoomInfo value = entry.getValue();
            System.out.println("Key: " + key + ", 방아이디: " + value.getRoomId() + ",총 레인 수 : " + value.getLanes() + ",당첨 레인:  " + value.getWinRailNo() + ", 호스트 닉네임 :  " + value.getHostId() + ", 참여자 :  " +value.getParticipants());
        }   

        // 클라이언트에 대한 연결 성공 로그
        logger.info("[SseController] SSE 연결 성공: 클라이언트에 '연결 성공!' 메시지를 보냈습니다.");

        return emitter;
    }

        // 방에 연결된 모든 클라이언트에게 메시지 브로드캐스트
    private void broadcastMessageToRoom(String roomId, String message) {
        List<SseEmitter> emitters = sseEmitters.get(roomId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(message);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        }
    }

    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessageToRoom(@RequestParam String roomId, @RequestParam String message) {
        List<SseEmitter> emitters = sseEmitters.get(roomId); // 방 ID에 해당하는 클라이언트들 가져오기

        if (emitters != null) {
            // 방에 연결된 모든 클라이언트에게 메시지 전송
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send("방 메시지: " + message); // 해당 방의 모든 클라이언트에게 메시지 전송
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
            return ResponseEntity.ok("메시지 전송 성공!");
        }
        return ResponseEntity.status(404).body("방을 찾을 수 없습니다.");
    }

    // roomInfoMap에서 방 정보를 가져오는 API
  @GetMapping("/rooms") 
    public ResponseEntity<List<RoomInfo>> getRooms() {
    // Map에 저장된 모든 방 정보 리스트로 반환
    List<RoomInfo> roomList = new ArrayList<>(roomInfoMap.values());
    return ResponseEntity.ok(roomList);
}
    
}
