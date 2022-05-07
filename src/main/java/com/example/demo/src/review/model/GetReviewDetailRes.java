package com.example.demo.src.review.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetReviewDetailRes {
    private long reviewIdx;
    private String emoji;
    private String text;
    private String reviewTime; // 리뷰 최초 작성 시간 (포맷 맞춰줌)
    // 책정보
    private String isbn;
    private String title;
    private String thumbnailUrl;
    private String author;
    private String publisher;
    private String introduction;
    private String releaseYear;
}
