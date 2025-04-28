package com.example.yun.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RoomRequest {
    private String nickname; // 닉네임
    private int lanes; // 총 레인 수 
    private String roomId; // 방 번호
}
