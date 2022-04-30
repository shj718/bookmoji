package com.example.demo.src.post;

import com.example.demo.src.post.PostDao;
import com.example.demo.src.post.model.GetPostInfoRes;
import org.springframework.stereotype.Service;
import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.post.model.*;
// import com.example.demo.utils.AES128;
// import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class PostProvider {
    private final PostDao postDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public PostProvider(PostDao postDao) {
        this.postDao = postDao;
    }


    public List<GetPostInfoRes> getPostInfos() throws BaseException{
        try{
            List<GetPostInfoRes> getPostInfosRes = postDao.getPostInfos();
            return getPostInfosRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetPostRes> getPosts() throws BaseException{
        try{
            List<GetPostRes> getPostsRes = postDao.getPosts();
            return getPostsRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetPostRes> getFeedPosts(int userId) throws BaseException{
        try{
            List<GetPostRes> getFeedPostsRes = postDao.getFeedPosts(userId);
            return getFeedPostsRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetPostRes> getFollowPosts(int userId) throws BaseException{
        try{
            List<GetPostRes> getFollowPostsRes = postDao.getFollowPosts(userId);
            return getFollowPostsRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int checkPostLike (Like like) throws BaseException {
        try {
            return postDao.checkPostLike(like);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public GetLikeCountRes getLikeCount(long postId) throws BaseException {
        try {
            GetLikeCountRes getLikeCountRes = postDao.getLikeCount(postId);
            return getLikeCountRes;
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
