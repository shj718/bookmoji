package com.example.demo.src.user;


import com.example.demo.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }



    public long createUser(PostUserReq postUserReq, String defaultProfileImgUrl){
        String createUserQuery = "insert into User (email, password, nickname, profileImgUrl) VALUES (?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getEmail(), postUserReq.getPassword(), postUserReq.getNickname(), defaultProfileImgUrl};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,long.class);
    }

    public long createKakaoUser(PostUserReq postUserReq, String defaultProfileImgUrl, long kakaoId) {
        String createKakaoUserQuery = "insert into User (email, password, nickname, profileImgUrl, kakaoId) VALUES (?,?,?,?,?)";
        Object[] createKakaoUserParams = new Object[]{postUserReq.getEmail(), postUserReq.getPassword(), postUserReq.getNickname(), defaultProfileImgUrl, kakaoId};
        this.jdbcTemplate.update(createKakaoUserQuery, createKakaoUserParams);

        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,long.class);
    }

    public int checkEmail(String email){
        String checkEmailQuery = "select exists(select email from User where email = ? and kakaoId is null)";
        String checkEmailParams = email;

        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);

    }

    public int checkKakaoEmail(String email, long kakaoId) {
        String checkKakaoEmailQuery = "select exists(select email from User where email = ? and kakaoId = ?)";
        Object[] checkKakaoEmailParams = new Object[]{email, kakaoId};

        return this.jdbcTemplate.queryForObject(checkKakaoEmailQuery,
                int.class,
                checkKakaoEmailParams);
    }

    public GetUserInfoRes getUserInfo(long userIdx) {
        String getUserInfoQuery = "select id, email, password, nickname, profileImgUrl from User where id = ?";
        long getUserInfoParams = userIdx;
        return this.jdbcTemplate.queryForObject(getUserInfoQuery,
                (rs, rowNum) -> new GetUserInfoRes(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("nickname"),
                        rs.getString("profileImgUrl")
                ), getUserInfoParams);
    }

    public int modifyNickname(PatchNicknameReq patchNicknameReq) {
        String modifyNicknameQuery = "update User set nickname = ? where id = ?";
        Object[] modifyNicknameParams = new Object[]{patchNicknameReq.getNickname(), patchNicknameReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyNicknameQuery, modifyNicknameParams);
    }

    public int modifyPassword(PatchPasswordReq patchPasswordReq) {
        String modifyPasswordQuery = "update User set password = ? where id = ?";
        Object[] modifyPasswordParams = new Object[]{patchPasswordReq.getNewPassword(), patchPasswordReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyPasswordQuery, modifyPasswordParams);
    }

    public int modifyProfileImage(PatchProfileImageReq patchProfileImageReq) {
        String modifyProfileImageQuery = "update User set profileImgUrl = ? where id = ?";
        Object[] modifyProfileImageParams = new Object[]{patchProfileImageReq.getProfileImgUrl(), patchProfileImageReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyProfileImageQuery, modifyProfileImageParams);
    }

    public String getUserStatus(long userIdx) {
        String getUserStatusQuery = "select status from User where id = ?";
        long getUserStatusParams = userIdx;

        return this.jdbcTemplate.queryForObject(getUserStatusQuery,
                String.class,
                getUserStatusParams);
    }

    public int deleteUser(PatchStatusReq patchStatusReq) {
        String deleteUserQuery = "update User set status = ? where id = ?";
        String statusWithQuitReason = "D-" + patchStatusReq.getQuitReason();
        Object[] deleteUserParams = new Object[]{statusWithQuitReason, patchStatusReq.getUserIdx()};

        return this.jdbcTemplate.update(deleteUserQuery, deleteUserParams);
    }

    public User getPwd(PostLoginReq postLoginReq){
        String getPwdQuery = "select id, email, password, nickname, status from User where email = ?";
        String getPwdParams = postLoginReq.getEmail();

        return this.jdbcTemplate.queryForObject(getPwdQuery,
                (rs,rowNum)-> new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("nickname"),
                        rs.getString("status")
                ),
                getPwdParams
                );

    }

    public String getPwdByUserIdx(long userIdx) {
        String getPwdByUserIdxQuery = "select password from User where id = ?";
        long getPwdByUserIdxParams = userIdx;

        return this.jdbcTemplate.queryForObject(getPwdByUserIdxQuery,
                String.class,
                getPwdByUserIdxParams);
    }

    public long getKakaoUserIdx(String email, long kakaoId) {
        String getKakaoUserIdxQuery = "select id from User where email = ? and kakaoId = ? and status = 'A'";
        Object[] getKakaoUserParams = new Object[]{email, kakaoId};

        return this.jdbcTemplate.queryForObject(getKakaoUserIdxQuery,
                long.class,
                getKakaoUserParams);
    }

    public long checkKakaoUser(long userIdx) {
        String checkKakaoUserQuery = "select ifnull(kakaoId, 0) as kakaoId from User where id = ?";
        long checkKakaoUserParams = userIdx;

        return this.jdbcTemplate.queryForObject(checkKakaoUserQuery,
                long.class,
                checkKakaoUserParams);
    }
}
