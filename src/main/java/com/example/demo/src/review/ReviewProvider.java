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

import java.util.ArrayList;
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

    public int checkOtherReview(long userIdx) throws BaseException {
        try {
            int result = reviewDao.checkOtherReview(userIdx);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetOtherReviewRes> getOtherReviews(long userIdx) throws BaseException {
        // 디비에 리뷰가 하나라도 있는지 검사
        int reviewExists;
        try {
            reviewExists = checkOtherReview(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(reviewExists == 0) {
            throw new BaseException(GET_OTHER_REVIEWS_FAIL);
        }
        // 있으면 조회
        try {
            List<OtherReview> otherReviews = reviewDao.getOtherReviewsTmp(userIdx); // 좋아요 여부를 제외한 나머지 정보
            List<GetOtherReviewRes> getOtherReviewsRes = new ArrayList<GetOtherReviewRes>();
            long otherReviewIdx;
            int hasLiked;
            int numOfOtherReviews = otherReviews.size();
            for(int i = 0; i < numOfOtherReviews; i++) {
                otherReviewIdx = otherReviews.get(i).getReviewIdx();

                hasLiked = reviewDao.getLikedOrNot(userIdx, otherReviewIdx);

                getOtherReviewsRes.add(new GetOtherReviewRes(otherReviews.get(i).getReviewIdx(), otherReviews.get(i).getEmoji(),
                        otherReviews.get(i).getText(), otherReviews.get(i).getIsbn(), otherReviews.get(i).getTitle(),
                        otherReviews.get(i).getThumbnailUrl(), otherReviews.get(i).getAuthor(), hasLiked));
            }
            return getOtherReviewsRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int checkYearReview(long userIdx, int year) throws BaseException {
        try {
            int result = reviewDao.checkYearReview(userIdx, year);
            return result;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetEmojiPercentageRes> getEmojiPercentage(long userIdx, int year) throws BaseException {
        // 해당 년도에 유저가 작성한 리뷰가 하나라도 존재하는지 확인
        int reviewExists;
        try {
            reviewExists = checkYearReview(userIdx, year);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(reviewExists == 0) { // 없으면 에러
            throw new BaseException(YEAR_REVIEW_NOT_EXISTS);
        }

        try {
            // 이모지별 퍼센트 조회
            List<GetEmojiPercentageRes> getEmojiPercentageRes = reviewDao.getEmojiPercentage(userIdx, year);
            return getEmojiPercentageRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetMonthlyCountRes> getMonthlyCount(long userIdx, int year) throws BaseException {
        // 해당 년도에 유저가 작성한 리뷰가 하나라도 존재하는지 확인
        int reviewExists;
        try {
            reviewExists = checkYearReview(userIdx, year);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }

        // 해당 년도에 작성한 감상이 아예 없는 경우
        try {
            if(reviewExists == 0) {
                List<GetMonthlyCountRes> getMonthlyCountRes = new ArrayList<GetMonthlyCountRes>();
                for(int i = 1; i <= 12; i++) { // 1월 ~ 12월
                    getMonthlyCountRes.add(i - 1, new GetMonthlyCountRes(i, 0));
                }
                return getMonthlyCountRes;
            }
        } catch (Exception exception) {
            throw new BaseException(SERVER_ERROR);
        }

        // 해당 년도에 작성한 감상이 1개 이상인 경우 (감상이 존재하지 않는 달의 데이터도 표시되어야함)
        try {
            List<GetMonthlyCountRes> monthlyCountTmp = reviewDao.getMonthlyCount(userIdx, year);
            // month 저장
            ArrayList<Integer> month = new ArrayList<>();
            for(int i = 0; i < monthlyCountTmp.size(); i++) {
                month.add(monthlyCountTmp.get(i).getMonth());
            }

            List<GetMonthlyCountRes> getMonthlyCountRes = new ArrayList<GetMonthlyCountRes>();

            for(int i = 1; i <= 12; i++) { // 1월 ~ 12월
                // i 에 해당하는 달이 monthlyCount 에 포함되어 있는지 검사
                int monthIdx = month.indexOf(i);
                if(monthIdx != -1) {
                    // 있다면 monthlyCount 값을 넣고
                    getMonthlyCountRes.add(i - 1, new GetMonthlyCountRes(i, monthlyCountTmp.get(monthIdx).getMonthlyCount()));
                }
                else {
                    // 없다면 0을 넣기
                    getMonthlyCountRes.add(i - 1, new GetMonthlyCountRes(i, 0));
                }
            }

            return getMonthlyCountRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
