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
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
public class ReviewService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ReviewDao reviewDao;
    private final ReviewProvider reviewProvider;
    private final JwtService jwtService;

    @Autowired
    public ReviewService(ReviewDao reviewDao, ReviewProvider reviewProvider, JwtService jwtService) {
        this.reviewDao = reviewDao;
        this.reviewProvider = reviewProvider;
        this.jwtService = jwtService;
    }

    @Transactional
    public long createBook(PostReviewReq postReviewReq) throws BaseException {
        try {
            long bookIdx = reviewDao.createBook(postReviewReq);
            return bookIdx;
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public long createReview(PostReviewReq postReviewReq) throws BaseException {
        try {
            long bookIdx;
            // DB에 없는 책일 경우 책 정보 삽입
            if(reviewProvider.checkBook(postReviewReq.getIsbn()) == 0) {
                bookIdx = createBook(postReviewReq);
            }
            else { // DB에 있는 책일 경우
                bookIdx = reviewProvider.getBookIdx(postReviewReq.getIsbn());
            }

            // 리뷰 생성
            long reviewIdx = reviewDao.createReview(postReviewReq, bookIdx);
            return reviewIdx;
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
