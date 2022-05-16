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
            String defaultProfileImgUrl = "https://www.kindpng.com/picc/m/24-248253_user-profile-default-image-png-clipart-png-download.png";
            long userIdx = userDao.createUser(postUserReq, defaultProfileImgUrl);
            return userIdx;
        } catch (Exception exception) {
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
