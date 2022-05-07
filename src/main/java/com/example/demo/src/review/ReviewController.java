package com.example.demo.src.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.review.model.*;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final ReviewProvider reviewProvider;
    @Autowired
    private final ReviewService reviewService;
    @Autowired
    private final JwtService jwtService;


    public ReviewController(ReviewProvider reviewProvider, ReviewService reviewService, JwtService jwtService) {
        this.reviewProvider = reviewProvider;
        this.reviewService = reviewService;
        this.jwtService = jwtService;
    }


    /**
     * 리뷰 생성 API
     * [POST] /reviews
     * @return BaseResponse<Long>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<Long> createReview(@RequestBody PostReviewReq postReviewReq) {
        try {
            long userIdx = postReviewReq.getUserIdx();
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            // 형식적 validation
            if(postReviewReq.getEmoji() == null) {
                return new BaseResponse<>(EMPTY_EMOJI);
            }
            if(postReviewReq.getText() == null) {
                return new BaseResponse<>(EMPTY_REVIEW_TEXT);
            }
            if(postReviewReq.getIsbn() == null) {
                return new BaseResponse<>(EMPTY_ISBN);
            }
            if(postReviewReq.getTitle() == null) {
                return new BaseResponse<>(EMPTY_BOOK_TITLE);
            }
            if(postReviewReq.getThumbnailUrl() == null) {
                return new BaseResponse<>(EMPTY_THUMBNAIL);
            }
            if(postReviewReq.getAuthor() == null) {
                return new BaseResponse<>(EMPTY_AUTHOR);
            }
            if(postReviewReq.getPublisher() == null) {
                return new BaseResponse<>(EMPTY_PUBLISHER);
            }
            if(postReviewReq.getIntroduction() == null) {
                return new BaseResponse<>(EMPTY_BOOK_INTRODUCTION);
            }
            if(postReviewReq.getReleaseYear() == null) {
                return new BaseResponse<>(EMPTY_BOOK_RELEASE_YEAR);
            }
            Long reviewIdx = reviewService.createReview(postReviewReq);
            return new BaseResponse<>(reviewIdx);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 유저의 전체 리뷰 조회 API (감상 글은 안보이고 책 썸네일, 제목, 이모지만 보임)
     * [GET] /reviews
     * @return BaseResponse<List<GetReviewRes>>
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetReviewRes>> getReviews(@RequestParam long userIdx) {
        try {
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }

            List<GetReviewRes> getReviewsRes = reviewProvider.getReviews(userIdx);
            return new BaseResponse<>(getReviewsRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 특정 리뷰 조회 API (이모지, 감상글, 작성 날짜, 좋아요수, 책 정보 보임)
     * [GET] /reviews/details
     * @return BaseResponse<GetReviewDetailRes>
     */
    @ResponseBody
    @GetMapping("/details")
    public BaseResponse<GetReviewDetailRes> getReviewDetail(@RequestParam long userIdx, @RequestParam long reviewIdx) {
        try {
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }

            GetReviewDetailRes getReviewDetailRes = reviewProvider.getReviewDetail(userIdx, reviewIdx);
            return new BaseResponse<>(getReviewDetailRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
