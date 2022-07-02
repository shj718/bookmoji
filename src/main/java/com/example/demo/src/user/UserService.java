package com.example.demo.src.user;



import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.AES128;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Random;

import javax.transaction.Transactional;

import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;
    private JavaMailSender javaMailSender;

    @Autowired
    public UserService(UserDao userDao, UserProvider userProvider, JwtService jwtService, JavaMailSender javaMailSender) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
        this.javaMailSender = javaMailSender;
    }

    @Transactional
    public long createUser(PostUserReq postUserReq) throws BaseException {
        //이메일 중복 검사
        if(userProvider.checkEmail(postUserReq.getEmail()) ==1){
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }

        String pwd;
        try{
            //암호화
            pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(postUserReq.getPassword());
            postUserReq.setPassword(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try{
            // 프로필 이미지 디폴트로 세팅
            String defaultProfileImgUrl = " ";
            long userIdx = userDao.createUser(postUserReq, defaultProfileImgUrl);
            return userIdx;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public String getKakaoAccessToken (String code) {
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //POST 요청을 위해 기본값이 false인 setDoOutput을 true로
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=9d2e5171737c5341662bd9d51b2b634c"); // TODO REST_API_KEY 입력
            sb.append("&redirect_uri=http://localhost:3000/oauth"); // TODO 인가코드 받은 redirect_uri 입력
            sb.append("&code=" + code);
            bw.write(sb.toString());
            bw.flush();

            //결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            //Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            access_Token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("access_token : " + access_Token);
            System.out.println("refresh_token : " + refresh_Token);

            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return access_Token;
    }

    @Transactional
    public PostLoginRes kakaoSocialLogin(String token) throws BaseException {
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        long kakaoId;
        String email = "";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + token); //전송할 header 작성, access_token전송

            //결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            //Gson 라이브러리로 JSON파싱
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            kakaoId = element.getAsJsonObject().get("id").getAsLong();
            boolean hasEmail = element.getAsJsonObject().get("kakao_account").getAsJsonObject().get("has_email").getAsBoolean();
            if(hasEmail){
                email = element.getAsJsonObject().get("kakao_account").getAsJsonObject().get("email").getAsString();
            }

            System.out.println("id : " + kakaoId);
            System.out.println("email : " + email);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new BaseException(KAKAO_SOCIAL_LOGIN_ERROR);
        }

        // 가입된 카카오회원인지 아닌지 중복 체크 (신규면 회원가입 후 로그인, 아니면 그냥 로그인)
        try {
            long userIdx;
            String jwt;
            User user = new User();

            if(userProvider.checkKakaoEmail(email, kakaoId) == 0) { // 기존에 카카오로 가입한 이력이 없는 경우 회원가입 진행
                userIdx = createKakaoUser(email, kakaoId);
                user = userProvider.getKakaoUserIdx(email, kakaoId);
            }
            else { // 기존에 카카오로 가입한 이력이 있는 경우
                user = userProvider.getKakaoUserIdx(email, kakaoId);
                userIdx = user.getUserIdx();
            }

            System.out.println("userIdx : " + userIdx);
            // JWT 발급
            jwt = jwtService.createJwt(userIdx);
            System.out.println("jwt : " + jwt);

            return new PostLoginRes(userIdx, jwt, user.getProfileImgUrl());
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public long createKakaoUser(String email, long kakaoId) throws BaseException {
        try {
            // 닉네임은 이메일 앞부분으로 세팅
            String nickname;
            nickname = email.substring(0, email.indexOf("@"));
            // 디폴트 프로필 이미지 세팅
            String defaultProfileImgUrl = " ";

            long userIdx;
            PostUserReq postUserReq = new PostUserReq(email, " ", nickname);
            userIdx = userDao.createKakaoUser(postUserReq, defaultProfileImgUrl, kakaoId);
            return userIdx;
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public void modifyNickname(PatchNicknameReq patchNicknameReq) throws BaseException {
        // 변경하려는 닉네임이 현재 닉네임과 다른지 검사
        String currentNickname;
        try {
            currentNickname = userProvider.getUserInfo(patchNicknameReq.getUserIdx()).getNickname();
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
        if(currentNickname.equals(patchNicknameReq.getNickname())) {
            throw new BaseException(FAILED_TO_MODIFY_NICKNAME);
        }
        // 닉네임 변경
        try {
            int result = userDao.modifyNickname(patchNicknameReq);
            if(result == 0) {
                throw new BaseException(DATABASE_ERROR);
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public void modifyPassword(PatchPasswordReq patchPasswordReq) throws BaseException {
        // 카카오 소셜로그인 회원이면 비밀번호 변경 불가
        long kakaoId;
        try {
            kakaoId = userProvider.checkKakaoUser(patchPasswordReq.getUserIdx());
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
        if(kakaoId != 0) {
            throw new BaseException(KAKAO_USER_MODIFY_PASSWORD_FAIL);
        }

        String encryptedPassword; // 암호화된 현재 비밀번호
        try {
            long userIdx = patchPasswordReq.getUserIdx();
            encryptedPassword = userDao.getPwdByUserIdx(userIdx);
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
        // 현재 비밀번호 맞는지 검사
        String currentPassword;
        try {
            currentPassword = new AES128(Secret.USER_INFO_PASSWORD_KEY).decrypt(encryptedPassword); // 복호화
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }
        if(!currentPassword.equals(patchPasswordReq.getCurrentPassword())) { // 현재 비밀번호가 틀리면 에러
            throw new BaseException(WRONG_CURRENT_PASSWORD);
        }

        // 변경하려는 비밀번호가 현재 비밀번호와 다른지 검사
        String newPassword;
        newPassword = patchPasswordReq.getNewPassword();
        if(currentPassword.equals(newPassword)) {
            throw new BaseException(FAILED_TO_MODIFY_PASSWORD);
        }

        // 변경할 비밀번호 암호화
        String pwd;
        try{
            //암호화
            pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(newPassword);
            patchPasswordReq.setNewPassword(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try {
            int result = userDao.modifyPassword(patchPasswordReq); // 비밀번호 변경
            if(result == 0) {
                throw new BaseException(DATABASE_ERROR);
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public void modifyProfileImage(PatchProfileImageReq patchProfileImageReq) throws BaseException {
        try {
            int result = userDao.modifyProfileImage(patchProfileImageReq);
            if(result == 0) {
                throw new BaseException(DATABASE_ERROR);
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public void deleteUser(PatchStatusReq patchStatusReq) throws BaseException {
        // 카카오 소셜로그인 회원이면 회원탈퇴 불가
        long kakaoId;
        try {
            kakaoId = userProvider.checkKakaoUser(patchStatusReq.getUserIdx());
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
        if(kakaoId != 0) {
            throw new BaseException(KAKAO_USER_DELETE_FAIL);
        }

        // 이미 탈퇴한 유저인지 검사
        String userStatus;
        try {
            userStatus = userProvider.getUserStatus(patchStatusReq.getUserIdx());
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
        if(!userStatus.equals("A")) {
            throw new BaseException(DELETE_USER_FAIL);
        }
        try {
            int result = userDao.deleteUser(patchStatusReq);
            if(result == 0) {
                throw new BaseException(DATABASE_ERROR);
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void mailSend(Mail mail) throws BaseException {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(mail.getTo());
            message.setSubject(mail.getTitle());
            message.setText(mail.getMessage());

            javaMailSender.send(message);
        } catch(Exception exception) {
            exception.printStackTrace();
            throw new BaseException(MAIL_SEND_ERROR);
        }
    }

    @Transactional
    public String sendAuthMail(String email) throws BaseException {
        try {
            String to = email;
            String title = "북모지(Bookmoji) 이메일 인증 코드";
            // 8자리 인증 코드 생성하기
            String authCode = "";
            Random random = new Random();
            for(int i=0;i<8;i++) {
                int x = random.nextInt(3);
                switch(x) {
                    case 0:
                        authCode += (char)(random.nextInt(26) + 97); // a ~ z
                        break;
                    case 1:
                        authCode += (char)(random.nextInt(26) + 65); // A ~ Z
                        break;
                    case 2:
                        authCode += random.nextInt(10); // 0 ~ 9
                        break;
                }
            }
            String message = "인증 코드는  " + authCode + "  입니다.";

            Mail mail = new Mail(to, title, message);
            mailSend(mail);

            return authCode;
        } catch(Exception exception){
            throw new BaseException(MAIL_SEND_ERROR);
        }
    }
}
