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
@CrossOrigin(origins = {"http://localhost:3000", "https://bookmoji.netlify.app"})
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
            return new BaseResponse<>(USERS_INVALID_EMAIL);
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
            // 로그인 값들에 대한 형식적인 validation 처리
            if(postLoginReq.getEmail() == null) {
                return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
            }
            if(postLoginReq.getPassword() == null) {
                return new BaseResponse<>(POST_USERS_EMPTY_PASSWORD);
            }
            //이메일 정규표현
            if(!isRegexEmail(postLoginReq.getEmail())){
                return new BaseResponse<>(USERS_INVALID_EMAIL);
            }
            // Provider 에서 탈퇴한 유저에 대한 validation 처리
            PostLoginRes postLoginRes = userProvider.logIn(postLoginReq);
            return new BaseResponse<>(postLoginRes);
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 중복 이메일 조회 API
     * [GET] /users/email/duplication
     * @return BaseResponse<Integer>
     */
    @ResponseBody
    @GetMapping("/email/duplication")
    public BaseResponse<Integer> checkDuplicateEmail(@RequestParam String email) {
        try {
            //이메일 정규표현
            if(!isRegexEmail(email)){
                return new BaseResponse<>(USERS_INVALID_EMAIL);
            }
            Integer isDuplicate = userProvider.checkEmail(email);
            return new BaseResponse<>(isDuplicate);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 이메일 인증을 위한 메일 발송 API
     * [POST] /users/auth/mail
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/auth/mail")
    public BaseResponse<String> sendAuthMail(@RequestParam String email) {
        try {
            //이메일 정규표현
            if(!isRegexEmail(email)){
                return new BaseResponse<>(USERS_INVALID_EMAIL);
            }
            String authCode = userService.sendAuthMail(email);
            return new BaseResponse<>(authCode);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 소셜로그인(카카오) API
     * [POST] /users/oauth/kakao
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/oauth/kakao")
    public BaseResponse<PostLoginRes> kakaoSocialLogin(@RequestParam String token) {
        try {
            if(token == null) {
                return new BaseResponse<>(EMPTY_ACCESS_TOKEN);
            }
            PostLoginRes postLoginRes = userService.kakaoSocialLogin(token);
            return new BaseResponse<>(postLoginRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 카카오 callback (카카오 로그인시 정보를 받는 주소)
     * [GET] /users/oauth/kakao/callback
     */
    @ResponseBody
    @GetMapping("/oauth/kakao/callback")
    public BaseResponse<PostLoginRes> kakaoCallback(@RequestParam String code) {
        System.out.println("authorization code : " + code);
        String accessToken;
        try {
            accessToken = userService.getKakaoAccessToken(code);
            PostLoginRes postLoginRes = userService.kakaoSocialLogin(accessToken);
            return new BaseResponse<>(postLoginRes);
        } catch (BaseException exception) {
            // 에러 메세지 출력
            System.out.println("소셜 로그인 에러 발생");
            return new BaseResponse<>(exception.getStatus());
        }
    }


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
