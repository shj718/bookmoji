package com.example.demo.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private long userIdx;
    private String email;
    private String password;
    private String nickname;
    private String status;
    private String profileImgUrl;

    public User() {

    }
}
