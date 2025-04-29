package com.example.yun.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    String roomId =  generateRoomId(); //20250428163504

    // 랜덤 당첨 레일 생성
    int winRailNo = (int) (Math.random() * roomRequest.getLanes()) + 1;

    //방장을 참여자 리스트에 추가
    Map<String, Integer> participants = new HashMap<>();
    participants.put(roomRequest.getNickname(), null);  // 초기 선택 레인은 null

    RoomInfo roomInfo = new RoomInfo(
        roomId,
        roomRequest.getLanes(),
        winRailNo,
        roomRequest.getNickname(),
        participants, 
        roomRequest.getRoomName()
    );

     // Map에 방 정보 저장
     roomInfoMap.put(currentRoomNumber, roomInfo);

    RoomResponse response = new RoomResponse(roomId,winRailNo);

    return ResponseEntity.ok(response);
    }

    // 참가자 등록
    @PostMapping("/add/participants")
    public String addParticipants(@RequestBody RoomRequest roomRequest) {
        String roomId = roomRequest.getRoomId();
        String nickname = roomRequest.getNickname();
    
        // 방에 참여자 추가
        RoomInfo roomInfo = null;
        for (RoomInfo info : roomInfoMap.values()) {
            if (info.getRoomId().equals(roomId)) {
                roomInfo = info;
                break;
            }
        }
        roomInfo.addParticipant(nickname);
    
        // participantsList 준비
        List<Map<String, Object>> participantsList = new ArrayList<>();
        for (Map.Entry<String, Integer> participant : roomInfo.getParticipants().entrySet()) {
            Map<String, Object> participantInfo = new HashMap<>();
            participantInfo.put("nickname", participant.getKey());
            participantInfo.put("selectedLane", participant.getValue());
            participantsList.add(participantInfo);
        }
    
          // roomData 만들기
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("roomId", roomInfo.getRoomId());
        roomData.put("participants", participantsList);

        logger.info("[LadderController] JSON 응답 객체: {}", roomData);

        // 방에 연결된 모든 클라이언트에게 참가자 리스트를 브로드캐스트
        broadcastMessageToRoom(roomId, roomData); // `broadcastMessageToRoom` 사용
    
        return "참여자 등록 완료";
    }
    
    

    @PostMapping("/join/room")
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
        roomInfo.addParticipant(nickname);

        // 방에 연결된 모든 클라이언트에게 브로드캐스트
        broadcastMessageToRoom(roomId, nickname + "님이 참여하셨습니다.");

        // for (Map.Entry<Integer, RoomInfo> entry : roomInfoMap.entrySet()) {
        //     Integer key = entry.getKey();
        //     RoomInfo value = entry.getValue();
        //     System.out.println("=============================");
        //     System.out.println("Key: " + key + ", 방아이디: " + value.getRoomId() + ", 총 레인 수 : " + value.getLanes() + ", 당첨 레인: " + value.getWinRailNo() + ", 호스트 닉네임 : " + value.getHostId() + ", 참여자 : " + value.getParticipants());
        // }  

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
    private void broadcastMessageToRoom(String roomId, Object message) {
        List<SseEmitter> emitters = sseEmitters.get(roomId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    // 데이터 타입에 맞게 처리
                    if (message instanceof String) {
                        emitter.send(SseEmitter.event().name("message").data(message)); // 참가 메시지,퇴장 메시지
                    } else if (message instanceof Map) {
                        emitter.send(SseEmitter.event().name("participants").data(message)); // 참가자 리스트
                    }
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        }
    }

    // roomInfoMap에서 방 정보를 가져오는 API
    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String, Object>>> getRooms() {
        List<Map<String, Object>> roomList = new ArrayList<>();
    
        for (Map.Entry<Integer, RoomInfo> entry : roomInfoMap.entrySet()) {
            RoomInfo value = entry.getValue();
    
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("roomId", value.getRoomId());
            roomData.put("roomName", value.getRoomName());
            roomData.put("lanes", value.getLanes());
            roomData.put("attendeeCount", value.getParticipants().size());
    
            roomList.add(roomData);
    
            // 로그 출력
            System.out.println("=============================");
            System.out.println("방아이디: " + value.getRoomId() +
                               ", 룸 네임: " + value.getRoomName() +
                               ", 총 레인 수: " + value.getLanes() +
                               ", 참여자 수: " + value.getParticipants().size());
        }
    
        return ResponseEntity.ok(roomList);
    }

    // 선택 레인 등록(테스트 필요)
    @GetMapping("/add/lane")
    public String setSelectedLane(
            @RequestParam("roomId") String roomId,
            @RequestParam("nickname") String nickname,
            @RequestParam("selectedLane") int selectedLane
    ) {
        // roomId로 해당 방을 찾기
        RoomInfo roomInfo = null;
        for (RoomInfo info : roomInfoMap.values()) {
            if (info.getRoomId().equals(roomId)) {
                roomInfo = info;
                break;
            }
        }
    
        if (roomInfo == null) {
            return "해당 roomId를 찾을 수 없습니다.";
        }
    
        // participants Map에서 nickname이 존재하는지 확인 후 selectedLane 설정
        Map<String, Integer> participants = roomInfo.getParticipants();
        if (!participants.containsKey(nickname)) {
            return "해당 nickname이 참가자 목록에 없습니다.";
        }
    
        participants.put(nickname, selectedLane); 

        for (Map.Entry<Integer, RoomInfo> entry : roomInfoMap.entrySet()) {
            Integer key = entry.getKey();
            RoomInfo value = entry.getValue();
            System.out.println("=============================");
            System.out.println("Key: " + key + ", 방아이디: " + value.getRoomId() + ", 총 레인 수 : " + value.getLanes() + ", 당첨 레인: " + value.getWinRailNo() + ", 호스트 닉네임 : " + value.getHostId() + ", 참여자 : " + value.getParticipants());
        }  
    
        logger.info("[LadderController] 선택 레인 등록 완료: roomId = {}, nickname = {}, selectedLane = {}", roomId, nickname, selectedLane);

         // participants 정보를 JSON 형식으로 변환하여 전송할 준비
            List<Map<String, Object>> participantsList = new ArrayList<>();
            for (Map.Entry<String, Integer> participant : roomInfo.getParticipants().entrySet()) {
                Map<String, Object> participantInfo = new HashMap<>();
                participantInfo.put("nickname", participant.getKey());
                participantInfo.put("selectedLane", participant.getValue());
                participantsList.add(participantInfo);
            }

            // JSON 형식으로 결과 생성
            String jsonResponse = String.format("{\"roomId\": \"%s\", \"participants\": %s}",
                    roomInfo.getRoomId(), participantsList.toString());

            logger.info("[LadderController] JSON 응답: {}", jsonResponse);

      
            broadcastMessageToRoom(roomId, jsonResponse);

        return "레인 등록 완료";
    }
    
    // 방 정보 조회 API (roomId로 특정 방 조회)
    @GetMapping("/search/rooms")
    public ResponseEntity<Map<String, Object>> getRoomById(@RequestParam("roomId") String roomId) {   
        for (Map.Entry<Integer, RoomInfo> entry : roomInfoMap.entrySet()) {
            RoomInfo room = entry.getValue();
            
            // roomId가 일치하는 방 찾기
            if (room.getRoomId().equals(roomId)) {
                Map<String, Object> roomData = new HashMap<>();
                roomData.put("roomId", room.getRoomId());
                roomData.put("roomName", room.getRoomName());
                roomData.put("attendeeCount", room.getParticipants().size());
                roomData.put("lanes", room.getLanes());
                roomData.put("winRailNo", room.getWinRailNo());

                // participants 리스트 생성
                List<Map<String, Object>> participantsList = new ArrayList<>();
                for (Map.Entry<String, Integer> participantEntry : room.getParticipants().entrySet()) {
                    Map<String, Object> participantData = new HashMap<>();
                    participantData.put("nickname", participantEntry.getKey());
                    participantData.put("selectedLane", participantEntry.getValue());
                    participantsList.add(participantData);
                }
                roomData.put("participants", participantsList);

                logger.info("[Room Search] 찾은 방 정보: {}", roomData);
                return ResponseEntity.ok(roomData);
            }
        }

        // 못 찾았을 때
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Collections.singletonMap("message", "Room not found"));
    }

    // 방 퇴장 API
    @PostMapping("/leave/room")
    public String leaveRoom(@RequestBody RoomRequest roomRequest) {
    String roomId = roomRequest.getRoomId();
    String nickname = roomRequest.getNickname();

    RoomInfo roomInfo = null;
    // 방을 찾음
    for (RoomInfo info : roomInfoMap.values()) {
        if (info.getRoomId().equals(roomId)) {
            roomInfo = info;
            break;
        }
    }

    if (roomInfo != null) {
        // 참가자 삭제
        roomInfo.getParticipants().remove(nickname);

        logger.info("[방 퇴장] {} 님이 방 {} 에서 나갔습니다.", nickname, roomId);

        // 만약 참가자가 없으면 방 삭제
        if (roomInfo.getParticipants().isEmpty()) {
            roomInfoMap.values().removeIf(r -> r.getRoomId().equals(roomId));
            logger.info("[방 삭제] {} 방 참가자 0명 -> 삭제", roomId);
        }

        // 퇴장한 사람을 알리는 메시지 준비
        String leaveMessage = nickname + " 님이 퇴장하셨습니다 👋";

        // 방에 연결된 모든 클라이언트에게 메시지 브로드캐스트
        broadcastMessageToRoom(roomId, leaveMessage);

        // 남은 사람들에게 업데이트 알림 (SSE)
        List<Map<String, Object>> participantsList = new ArrayList<>();
        for (Map.Entry<String, Integer> participant : roomInfo.getParticipants().entrySet()) {
            Map<String, Object> participantData = new HashMap<>();
            participantData.put("nickname", participant.getKey());
            participantData.put("selectedLane", participant.getValue());
            participantsList.add(participantData);
        }

        // roomId와 함께 participants 데이터를 보낼 준비
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("roomId", roomInfo.getRoomId());
        roomData.put("participants", participantsList);

        // 남은 사람들에게 SSE로 업데이트된 참가자 리스트 전송
        broadcastMessageToRoom(roomId, roomData);

        return "방 퇴장 완료";
    } else {
        return "방을 찾을 수 없습니다.";
    }
}
    

}
