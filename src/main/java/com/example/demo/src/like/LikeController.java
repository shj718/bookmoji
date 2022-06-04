package com.example.demo.src.like;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
// import com.example.demo.src.like.model.*;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/likes")
@CrossOrigin(origins = {"http://localhost:3000", "https://bookmoji.netlify.app"})
public class LikeController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final LikeProvider likeProvider;
    @Autowired
    private final LikeService likeService;
    @Autowired
    private final JwtService jwtService;

    public LikeController(LikeProvider likeProvider, LikeService likeService, JwtService jwtService) {
        this.likeProvider = likeProvider;
        this.likeService = likeService;
        this.jwtService = jwtService;
    }


    /**
     * 좋아요 생성 API
     * [POST] /likes
     * @return BaseResponse<Long>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<Long> createLike(@RequestParam long userIdx, @RequestParam long reviewIdx) {
        try {
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }

            Long likeIdx = likeService.createLike(userIdx, reviewIdx);
            return new BaseResponse<>(likeIdx);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 좋아요 삭제 API
     * [PATCH] /likes
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("")
    public BaseResponse<String> deleteLike(@RequestParam long userIdx, @RequestParam long reviewIdx) {
        try {
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }

            likeService.deleteLike(userIdx, reviewIdx);
            String result = "좋아요 삭제 성공";
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 특정 리뷰의 좋아요 개수 조회 API
     * [GET] /likes/count
     * @return BaseResponse<Integer>
     */
    @ResponseBody
    @GetMapping("/count")
    public BaseResponse<Integer> getLikeCount(@RequestParam long userIdx, @RequestParam long reviewIdx) {
        try {
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }

            Integer likeCount = likeProvider.getLikeCount(userIdx, reviewIdx);
            return new BaseResponse<>(likeCount);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
