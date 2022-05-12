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
            if(postReviewReq.getEmoji() == null || postReviewReq.getEmoji().isEmpty()) {
                return new BaseResponse<>(EMPTY_EMOJI);
            }
            if(postReviewReq.getText() == null || postReviewReq.getText().isEmpty()) {
                return new BaseResponse<>(EMPTY_REVIEW_TEXT);
            }
            if(postReviewReq.getIsbn() == null || postReviewReq.getIsbn().isEmpty()) {
                return new BaseResponse<>(EMPTY_ISBN);
            }
            if(postReviewReq.getTitle() == null || postReviewReq.getTitle().isEmpty()) {
                return new BaseResponse<>(EMPTY_BOOK_TITLE);
            }
            if(postReviewReq.getThumbnailUrl() == null || postReviewReq.getThumbnailUrl().isEmpty()) {
                // 썸네일 없는 책은 디폴트 썸네일 Url 삽입
                postReviewReq.setThumbnailUrl("https://img.ypbooks.co.kr/ypbooks/images/empty70x100.gif");
            }
            if(postReviewReq.getAuthor() == null || postReviewReq.getAuthor().isEmpty()) {
                return new BaseResponse<>(EMPTY_AUTHOR);
            }
            if(postReviewReq.getPublisher() == null || postReviewReq.getPublisher().isEmpty()) {
                return new BaseResponse<>(EMPTY_PUBLISHER);
            }
            if(postReviewReq.getIntroduction() == null || postReviewReq.getIntroduction().isEmpty()) {
                return new BaseResponse<>(EMPTY_BOOK_INTRODUCTION);
            }
            if(postReviewReq.getReleaseYear() == null || postReviewReq.getReleaseYear().isEmpty()) {
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

    /**
     * 리뷰 수정 API
     * [PATCH] /reviews/contents
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/contents")
    public BaseResponse<String> modifyReview(@RequestBody PatchReviewReq patchReviewReq) {
        try {
            long userIdx = patchReviewReq.getUserIdx();
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            // 형식적 validation
            if(patchReviewReq.getEmoji() == null || patchReviewReq.getEmoji().isEmpty()) {
                return new BaseResponse<>(EMPTY_EMOJI);
            }
            if(patchReviewReq.getText() == null || patchReviewReq.getText().isEmpty()) {
                return new BaseResponse<>(EMPTY_REVIEW_TEXT);
            }
            reviewService.modifyReview(patchReviewReq);
            String result = "감상 수정 성공";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 리뷰 삭제 API
     * [PATCH] /reviews
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("")
    public BaseResponse<String> deleteReview(@RequestParam long userIdx, @RequestParam long reviewIdx) {
        try {
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            reviewService.deleteReview(userIdx, reviewIdx);
            String result = "감상 삭제 성공";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 다른 유저들의 리뷰 조회 API (유저가 해당 리뷰에 좋아요했는지 여부가 표시되어야함)
     * [GET] /reviews/others
     * @return BaseResponse<List<GetOtherReviewRes>>
     */
    @ResponseBody
    @GetMapping("/others")
    public BaseResponse<List<GetOtherReviewRes>> getOtherReviews(@RequestParam long userIdx) {
        try {
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            List<GetOtherReviewRes> getOtherReviewsRes = reviewProvider.getOtherReviews(userIdx);
            return new BaseResponse<>(getOtherReviewsRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
