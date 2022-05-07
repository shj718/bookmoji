package com.example.demo.src.review.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetReviewRes {
    private long reviewIdx;
    private String title; // 책 제목
    private String thumbnailUrl;
    private String emoji;
}
