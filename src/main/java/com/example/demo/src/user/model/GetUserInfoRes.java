package com.example.demo.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetUserInfoRes {
    private long userIdx;
    private String email;
    private String password;
    private String nickname;
    private String profileImgUrl;
}
