package com.example.demo.src.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;



    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService){
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }


    /**
     * 회원 정보 조회 API
     * [GET] /users/info
     * @return BaseResponse<GetUserInfoRes>
     */
    @ResponseBody
    @GetMapping("/info")
    public BaseResponse<GetUserInfoRes> getUserInfo(@RequestParam long userIdx) {
        try {
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            GetUserInfoRes getUserInfoRes = userProvider.getUserInfo(userIdx);
            return new BaseResponse<>(getUserInfoRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 회원가입 API
     * [POST] /users/signup
     * @return BaseResponse<Long>
     */
    // Body
    @ResponseBody
    @PostMapping("/signup")
    public BaseResponse<Long> createUser(@RequestBody PostUserReq postUserReq) {
        // TODO: email 관련한 짧은 validation 예시입니다. 그 외 더 부가적으로 추가해주세요!
        if(postUserReq.getEmail() == null) {
            return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
        }
        if(postUserReq.getPassword() == null) {
            return new BaseResponse<>(POST_USERS_EMPTY_PASSWORD);
        }
        if(postUserReq.getNickname() == null) {
            return new BaseResponse<>(POST_USERS_EMPTY_NICKNAME);
        }
        //이메일 정규표현
        if(!isRegexEmail(postUserReq.getEmail())){
            return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        }
        try{
            Long userIdx = userService.createUser(postUserReq);
            return new BaseResponse<>(userIdx);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 로그인 API
     * [POST] /users/login
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/login")
    public BaseResponse<PostLoginRes> logIn(@RequestBody PostLoginReq postLoginReq){
        try{
            // TODO: 로그인 값들에 대한 형식적인 validation 처리해주셔야합니다!
            if(postLoginReq.getEmail() == null) {
                return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
            }
            if(postLoginReq.getPassword() == null) {
                return new BaseResponse<>(POST_USERS_EMPTY_PASSWORD);
            }
            //이메일 정규표현
            if(!isRegexEmail(postLoginReq.getEmail())){
                return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            }
            // TODO: 유저의 status ex) 비활성화된 유저, 탈퇴한 유저 등을 관리해주고 있다면 해당 부분에 대한 validation 처리도 해주셔야합니다.
            PostLoginRes postLoginRes = userProvider.logIn(postLoginReq);
            return new BaseResponse<>(postLoginRes);
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 소셜로그인(카카오) API
     * [POST] /users/oauth/kakao
     * @return BaseResponse<String>
     */



    /**
     * 닉네임 변경 API
     * [PATCH] /users/info/nickname
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/info/nickname")
    public BaseResponse<String> modifyNickname(@RequestBody PatchNicknameReq patchNicknameReq) {
        try {
            long userIdx = patchNicknameReq.getUserIdx();
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            // 형식적 validation
            if(patchNicknameReq.getNickname() == null || patchNicknameReq.getNickname().isEmpty()) {
                return new BaseResponse<>(PATCH_EMPTY_NICKNAME);
            }

            userService.modifyNickname(patchNicknameReq);
            String result = "닉네임 변경 성공";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 비밀번호 변경 API
     * [PATCH] /users/info/password
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/info/password")
    public BaseResponse<String> modifyPassword(@RequestBody PatchPasswordReq patchPasswordReq) {
        try {
            long userIdx = patchPasswordReq.getUserIdx();
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            // 형식적 validation
            if(patchPasswordReq.getCurrentPassword() == null || patchPasswordReq.getCurrentPassword().isEmpty()) {
                return new BaseResponse<>(PATCH_EMPTY_CURRENT_PASSWORD);
            }
            if(patchPasswordReq.getNewPassword() == null || patchPasswordReq.getNewPassword().isEmpty()) {
                return new BaseResponse<>(PATCH_EMPTY_NEW_PASSWORD);
            }

            userService.modifyPassword(patchPasswordReq);
            String result = "비밀번호 변경 성공";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 프로필 사진 변경 API
     * [PATCH] /users/info/image
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/info/image")
    public BaseResponse<String> modifyProfileImage(@RequestBody PatchProfileImageReq patchProfileImageReq) {
        try {
            long userIdx = patchProfileImageReq.getUserIdx();
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            // 형식적 validation
            if(patchProfileImageReq.getProfileImgUrl() == null || patchProfileImageReq.getProfileImgUrl().isEmpty()) {
                return new BaseResponse<>(EMPTY_PROFILE_IMAGE);
            }

            userService.modifyProfileImage(patchProfileImageReq);
            String result = "프로필 사진 변경 성공";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 회원 탈퇴 API
     * [PATCH] /users/account
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/account")
    public BaseResponse<String> deleteUser(@RequestBody PatchStatusReq patchStatusReq) {
        try {
            long userIdx = patchStatusReq.getUserIdx();
            //jwt에서 idx 추출.
            long userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            // 형식적 validation
            if(patchStatusReq.getQuitReason() == null) {
                patchStatusReq.setQuitReason("");
            }
            userService.deleteUser(patchStatusReq);
            String result = "회원 탈퇴 성공";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
