package com.example.demo.src.review;

import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.review.model.*;
import com.example.demo.utils.AES128;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

//Provider : Read의 비즈니스 로직 처리
@Service
public class ReviewProvider {

    private final ReviewDao reviewDao;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ReviewProvider(ReviewDao reviewDao, JwtService jwtService) {
        this.reviewDao = reviewDao;
        this.jwtService = jwtService;
    }

    public int checkBook(String isbn) throws BaseException {
        try {
            int result = reviewDao.checkBook(isbn);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public long getBookIdx(String isbn) throws BaseException {
        try {
            long bookIdx = reviewDao.getBookIdx(isbn);
            return bookIdx;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int checkReview(long userIdx) throws BaseException {
        try {
            int result = reviewDao.checkReview(userIdx);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetReviewRes> getReviews(long userIdx) throws BaseException {
        // 유저가 작성한 리뷰가 하나라도 존재하는지 확인
        int reviewExists;
        try {
            reviewExists = checkReview(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(reviewExists == 0) { // 없으면 에러
            throw new BaseException(REVIEW_NOT_EXISTS);
        }
        // 있으면 조회
        try {
            List<GetReviewRes> getReviewsRes = reviewDao.getReviews(userIdx);
            return getReviewsRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int checkUserReview(long userIdx, long reviewIdx) throws BaseException {
        try {
            int result = reviewDao.checkUserReview(userIdx, reviewIdx);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public GetReviewDetailRes getReviewDetail(long userIdx, long reviewIdx) throws BaseException {
        // 해당 유저의 해당 리뷰가 존재하는지 체크
        int userReviewExists;
        try {
            userReviewExists = checkUserReview(userIdx, reviewIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(userReviewExists == 0) { // 없으면 에러
            throw new BaseException(INVALID_USER_REVIEW_INDEX);
        }
        // 있으면 조회
        try {
            GetReviewDetailRes getReviewDetailRes = reviewDao.getReviewDetail(reviewIdx);
            return getReviewDetailRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
