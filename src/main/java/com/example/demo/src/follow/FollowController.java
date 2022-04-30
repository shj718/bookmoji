package com.example.demo.src.follow;

import com.example.demo.src.follow.FollowProvider;
import com.example.demo.src.follow.FollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.follow.model.*;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/app/follows")
public class FollowController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final FollowProvider followProvider;
    @Autowired
    private final FollowService followService;
    @Autowired
    private final JwtService jwtService;

    public FollowController(FollowProvider followProvider, FollowService followService, JwtService jwtService) {
        this.followProvider = followProvider;
        this.followService = followService;
        this.jwtService = jwtService;
    }


    /**
     * 팔로우 생성 API
     * [POST] /follows
     * @return BaseResponse<String>
     */
    // Body
    @ResponseBody
    @PostMapping("")
    public BaseResponse<String> createFollow(@RequestBody PostFollowReq postFollowReq) {
        try {
            if(postFollowReq.getFromUserId() == 0 || postFollowReq.getToUserId() == 0) {
                return new BaseResponse<>(USERS_EMPTY_USER_ID);
            }
            int userId = postFollowReq.getFromUserId();
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            followService.createFollow(postFollowReq);
            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 팔로워 조회 API
     * [GET] /follows/followed-by
     * @return BaseResponse<List<GetFollowerRes>>
     */
    @ResponseBody
    @GetMapping("/followed-by")
    public BaseResponse<List<GetFollowerRes>> getFollowers(@RequestParam int userId) {
        try{
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            List<GetFollowerRes> getFollowersRes = followProvider.getFollowers(userId);
            return new BaseResponse<>(getFollowersRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
    * 팔로잉 조회 API
    * [GET] /follows/following
    * @return BaseResponse<List<GetFollowingRes>>
    */
    @ResponseBody
    @GetMapping("/following")
    public BaseResponse<List<GetFollowingRes>> getFollowings(@RequestParam int userId) {
        try{
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            List<GetFollowingRes> getFollowingsRes = followProvider.getFollowings(userId);
            return new BaseResponse<>(getFollowingsRes);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 팔로잉 삭제(언팔로우) API
     * [PATCH] /follows
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("")
    public BaseResponse<String> deleteFollow(@RequestBody PatchFollowReq patchFollowReq) {
        try {
            if(patchFollowReq.getFromUserId() == 0 || patchFollowReq.getToUserId() == 0) {
                return new BaseResponse<>(USERS_EMPTY_USER_ID);
            }
            int userId = patchFollowReq.getFromUserId();
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            followService.deleteFollow(patchFollowReq);
            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
