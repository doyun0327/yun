package com.example.yun.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomInfo {
    //방 아이디
    private String roomId;
    //전체 레인 수
    private int lanes;
    //당첨된 레인 번호
    private int winRailNo;
    //방장 아이디
    private String hostId;
    // 참여자 목록 
    private Map<String, Integer> participants = new HashMap<>();
    // 방이름
    private String roomName;

    // 참여자(닉네임만만) 추가
    public void addParticipant(String nickname) {
        participants.putIfAbsent(nickname, null); // 선택 레인은 아직 없음
    }

    // 선택 레인 등록
    public void updateSelectedLane(String nickname, Integer selectedLane) {
        if (participants.containsKey(nickname)) {
            participants.put(nickname, selectedLane);
        }
}   
}
