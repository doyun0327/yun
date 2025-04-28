package com.example.yun.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomResponse {
    private String roomId; // 방 고유 키
    private int winRailNo;

    public RoomResponse(String roomId, int winRailNo) {
        this.roomId = roomId;
        this.winRailNo = winRailNo;
    }

}
