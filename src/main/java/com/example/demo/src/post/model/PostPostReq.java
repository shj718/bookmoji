package com.example.demo.src.post.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostPostReq {
    private int userId;
    private String text;
    private List<String> postContents;
    private String contentType;
}
