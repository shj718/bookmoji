package com.example.demo.src.post;

import com.example.demo.src.post.PostDao;
import com.example.demo.src.post.PostProvider;
import com.example.demo.src.post.model.PostPostRes;
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
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class PostService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PostDao postDao;
    private final PostProvider postProvider;

    @Autowired
    public PostService(PostDao postDao, PostProvider postProvider) {
        this.postDao = postDao;
        this.postProvider = postProvider;
    }


    // 게시물 생성
    public PostPostRes createPost(PostPostReq postPostReq) throws BaseException {
        try{
            int postIdx = postDao.createPost(postPostReq);
            int numOfContents = 0;
            for(int i=0; i<postPostReq.getPostContents().size(); i++) {
                numOfContents += postDao.createContent(postIdx, postPostReq.getPostContents().get(i),postPostReq.getContentType());
            }

            // int contentIdx = postDao.createContent(postIdx, postPostReq);

            return new PostPostRes((long)postIdx, numOfContents);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 게시물 수정
    public void modifyPostText(int userId, PatchPostReq patchPostReq) throws BaseException {
        try{
            int result = postDao.modifyPostText(userId, patchPostReq);
            if(result == 0){
                throw new BaseException(DATABASE_ERROR);
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 좋아요 생성
    public void createLike(Like like) throws BaseException {
        try {
            if(postProvider.checkPostLike(like) == 1) { // 이미 해당 게시물에 좋아요를 누른 경우
                throw new BaseException(DATABASE_ERROR);
            }
            int result = postDao.createLike(like);
            if(result == 0){
                throw new BaseException(DATABASE_ERROR); // 게시물이 존재하지 않는 경우
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }

    }

    // 좋아요 삭제
    public void deleteLike(Like like) throws BaseException {
        try {
            if(postProvider.checkPostLike(like) == 0) { // 해당 게시물에 좋아요를 누른적이 없는 경우
                throw new BaseException(DATABASE_ERROR);
            }
            int result = postDao.deleteLike(like);
            if(result == 0) {
                throw new BaseException(DATABASE_ERROR);
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
