package com.example.demo.src.review.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostReviewReq {
    private String emoji;
    private String text;
    private String isbn;
    private long userIdx;
    private String title; // 책 제목
    private String thumbnailUrl;
    private String author;
    private String publisher;
    private String introduction; // 책 소개
    private String releaseYear; // 출판 연도
}
