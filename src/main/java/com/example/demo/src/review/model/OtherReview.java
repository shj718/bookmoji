package com.example.demo.src.review.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OtherReview {
    private long reviewIdx;
    private String emoji;
    private String text;
    // 책정보
    private String isbn;
    private String title;
    private String thumbnailUrl;
    private String author;
}
