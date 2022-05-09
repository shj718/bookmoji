package com.example.demo.src.like;

import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
// import com.example.demo.src.like.model.*;
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
public class LikeProvider {

    private final LikeDao likeDao;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public LikeProvider(LikeDao likeDao, JwtService jwtService) {
        this.likeDao = likeDao;
        this.jwtService = jwtService;
    }


    public int checkReview(long reviewIdx) throws BaseException {
        try {
            int result = likeDao.checkReview(reviewIdx);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int checkLike(long userIdx, long reviewIdx) throws BaseException {
        try {
            int result = likeDao.checkLike(userIdx, reviewIdx);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int checkUserReview(long userIdx, long reviewIdx) throws BaseException {
        try {
            int result = likeDao.checkUserReview(userIdx, reviewIdx);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int getLikeCount(long userIdx, long reviewIdx) throws BaseException {
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
        // 있으면 좋아요 개수 조회
        try {
            int result = likeDao.getLikeCount(reviewIdx);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
