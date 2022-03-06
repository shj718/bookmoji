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

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

//Provider : Read의 비즈니스 로직 처리
@Service
public class FollowProvider {

    private final FollowDao followDao;

    @Autowired
    public FollowProvider(FollowDao followDao) {
        this.followDao = followDao;
    }

    final Logger logger = LoggerFactory.getLogger(this.getClass());


    public int checkFollow(long fromUserId, long toUserId) throws BaseException {
        try{
            return followDao.checkFollow(fromUserId, toUserId);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetFollowerRes> getFollowers(long userId) throws BaseException {
        try{
            List<GetFollowerRes> getFollowersRes = followDao.getFollowers(userId);
            return getFollowersRes;
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetFollowingRes> getFollowings(long userId) throws BaseException {
        try{
            List<GetFollowingRes> getFollowingsRes = followDao.getFollowings(userId);
            return getFollowingsRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
