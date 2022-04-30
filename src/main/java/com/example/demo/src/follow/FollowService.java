package com.example.demo.src.follow;

import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.follow.model.*;
// import com.example.demo.utils.AES128;
// import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
public class FollowService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FollowDao followDao;
    private final FollowProvider followProvider;

    @Autowired
    public FollowService(FollowDao followDao, FollowProvider followProvider) {
        this.followDao = followDao;
        this.followProvider = followProvider;
    }


    public void createFollow(PostFollowReq postFollowReq) throws BaseException {
        // 이미 팔로잉하는 유저인지 체크 (디비에서 PK가 idx여서 중복값이 삽입될 수도 있기 때문)
        if(followProvider.checkFollow(postFollowReq.getFromUserId(),postFollowReq.getToUserId()) ==1){
            throw new BaseException(DUPLICATED_FOLLOW);
        }
        try{
            int result = followDao.createFollow(postFollowReq);
            if(result == 0){
                throw new BaseException(DATABASE_ERROR);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }

    }


    public void deleteFollow(PatchFollowReq patchFollowReq) throws BaseException {
        try{
            int result = followDao.deleteFollow(patchFollowReq);
            if(result == 0){
                throw new BaseException(DATABASE_ERROR);
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
