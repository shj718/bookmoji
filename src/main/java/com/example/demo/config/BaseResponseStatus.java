package com.example.demo.config;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),


    /**
     * 2000 : Request 오류
     */
    // Common
    REQUEST_ERROR(false, 2000, "입력값을 확인해주세요."),
    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false,2003,"권한이 없는 유저의 접근입니다."),

    // users
    USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력해주세요."),
    USERS_INVALID_EMAIL(false, 2016, "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(false,2017,"중복된 이메일입니다."),
    POST_USERS_EMPTY_PASSWORD(false,2018,"비밀번호를 입력해주세요."),
    POST_USERS_EMPTY_NICKNAME(false,2019,"닉네임을 입력해주세요."),
    PATCH_EMPTY_NICKNAME(false,2020,"변경할 닉네임을 입력해주세요."),
    PATCH_EMPTY_CURRENT_PASSWORD(false,2021,"현재 비밀번호를 입력해주세요."),
    PATCH_EMPTY_NEW_PASSWORD(false,2022,"새 비밀번호를 입력해주세요."),
    EMPTY_PROFILE_IMAGE(false,2023,"프로필 이미지 Url을 입력해주세요."),
    EMPTY_EMOJI(false,2024,"이모지를 입력해주세요."), // 차후에 영화에도 사용 가능
    EMPTY_REVIEW_TEXT(false,2025,"감상 글을 입력해주세요."), // 차후에 영화에도 사용 가능
    EMPTY_ISBN(false,2026,"책의 ISBN을 입력해주세요."),
    EMPTY_BOOK_TITLE(false,2027,"책 제목을 입력해주세요."),
    EMPTY_THUMBNAIL(false,2028,"썸네일 Url을 입력해주세요."), // 차후에 영화에도 사용 가능
    EMPTY_AUTHOR(false,2029,"지은이를 입력해주세요."),
    EMPTY_PUBLISHER(false,2030,"출판사를 입력해주세요."),
    EMPTY_BOOK_INTRODUCTION(false,2031,"책 소개를 입력해주세요."),
    EMPTY_BOOK_RELEASE_YEAR(false,2032,"출판 연도를 입력해주세요."),




    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    // [POST] /users
    DUPLICATED_EMAIL(false, 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(false,3014,"비밀번호가 틀렸습니다."),
    EMAIL_NOT_EXISTS(false,3015,"가입되지 않은 이메일입니다."),
    INVALID_USER_STATUS(false,3016,"탈퇴한 회원입니다."),
    WRONG_CURRENT_PASSWORD(false,3017,"현재 비밀번호가 틀렸습니다."),
    FAILED_TO_MODIFY_PASSWORD(false,3018,"변경하려는 비밀번호가 현재 비밀번호와 일치합니다."),
    REVIEW_NOT_EXISTS(false,3019,"유저가 작성한 감상이 없습니다."),
    INVALID_USER_REVIEW_INDEX(false,3020,"해당 유저 또는 감상 고유번호의 정보가 존재하지 않습니다."),
    TARGET_REVIEW_NOT_EXISTS(false,3021,"존재하지 않는 감상입니다."),
    DUPLICATED_LIKE(false,3022,"이미 공감한 감상입니다."),
    FAILED_TO_DELETE_LIKE(false,3023,"유저가 감상에 공감하지 않았습니다."),
    FAILED_TO_MODIFY_NICKNAME(false,3024,"변경하려는 닉네임이 현재 닉네임과 일치합니다."),
    GET_OTHER_REVIEWS_FAIL(false,3025,"다른 유저들의 감상이 존재하지 않습니다."),
    DELETE_USER_FAIL(false,3026,"이미 탈퇴한 유저입니다."),
    MAIL_SEND_ERROR(false,3027,"이메일 인증 코드 발송 실패"),



    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/{userIdx}
    MODIFY_FAIL_USERNAME(false,4014,"유저네임 수정 실패"),

    PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다.");


    // 5000 : 필요시 만들어서 쓰세요
    // 6000 : 필요시 만들어서 쓰세요


    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
