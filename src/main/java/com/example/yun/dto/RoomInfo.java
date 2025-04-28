package com.example.yun.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomInfo {
    //방 아이디디
    private String roomId;
    //전체 레인 수수
    private int lanes;
    //당첨된 레인 번호호
    private int winRailNo;
    //방장 아이디
    private String hostId;
    // 참여자 목록 
    private List<String> participants;  

     // 참여자 추가 메서드
     public void addParticipant(String nickname) {
        participants.add(nickname);
    }
}
