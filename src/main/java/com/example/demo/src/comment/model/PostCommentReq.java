package com.example.demo.src.comment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostCommentReq {
    private String text;
    private int userId;
    private long postId;
    private long prevCommentId; // 요청으로 널값 가능
}
