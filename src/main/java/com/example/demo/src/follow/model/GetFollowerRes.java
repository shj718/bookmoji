package com.example.demo.src.follow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetFollowerRes {
    private String userName;
    private String name;
    private String profileImageUrl;
    private String seenOrNotSeenLatestStory;
    private String followEachOther;
}
