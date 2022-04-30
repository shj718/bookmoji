package com.example.demo.src.post.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetPostRes {
    private long postId;
    private String firstContentUrlPerPost;
    private String firstContentTypePerPost;
}
