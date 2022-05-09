package com.example.demo.src.review.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PatchReviewReq {
    private long userIdx;
    private long reviewIdx;
    private String emoji;
    private String text;
}
