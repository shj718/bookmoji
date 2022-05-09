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
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
public class LikeService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LikeDao likeDao;
    private final LikeProvider likeProvider;
    private final JwtService jwtService;

    @Autowired
    public LikeService(LikeDao likeDao, LikeProvider likeProvider, JwtService jwtService) {
        this.likeDao = likeDao;
        this.likeProvider = likeProvider;
        this.jwtService = jwtService;
    }


    @Transactional
    public long createLike(long userIdx, long reviewIdx) throws BaseException {
        // 존재하는 리뷰인지 검사
        int reviewExists;
        try {
            reviewExists = likeProvider.checkReview(reviewIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(reviewExists == 0) {
            throw new BaseException(TARGET_REVIEW_NOT_EXISTS);
        }
        // 이미 좋아요한 리뷰인지 검사 (좋아요 중복 검사)
        int hasLiked;
        try {
            hasLiked = likeProvider.checkLike(userIdx, reviewIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(hasLiked == 1) {
            throw new BaseException(DUPLICATED_LIKE);
        }
        // 좋아요 생성
        try {
            long likeIdx = likeDao.createLike(userIdx, reviewIdx);
            return likeIdx;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public void deleteLike(long userIdx, long reviewIdx) throws BaseException {
        // 존재하는 리뷰인지 검사
        int reviewExists;
        try {
            reviewExists = likeProvider.checkReview(reviewIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(reviewExists == 0) {
            throw new BaseException(TARGET_REVIEW_NOT_EXISTS);
        }
        // 해당 유저가 해당 리뷰에 좋아요한 적이 있는지 검사
        int hasLiked;
        try {
            hasLiked = likeProvider.checkLike(userIdx, reviewIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(hasLiked == 0) {
            throw new BaseException(FAILED_TO_DELETE_LIKE);
        }
        // 좋아요 삭제
        try {
            int result = likeDao.deleteLike(userIdx, reviewIdx);
            if(result == 0) {
                throw new BaseException(DATABASE_ERROR);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
