package com.example.demo.src.comment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PatchCommentReq { // 댓글 수정 추가하기
    private String text;
    private long userId;
    private long postId;
    private long prevCommentId; // 널값 가능
}
