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
            int contentNum = postDao.createContent(postIdx, postPostReq);

            return new PostPostRes((long)postIdx, contentNum);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 게시물 수정
    public void modifyPostText(PatchPostReq patchPostReq) throws BaseException {
        try{
            int result = postDao.modifyPostText(patchPostReq);
            if(result == 0){
                throw new BaseException(FAILED_TO_MODIFY_POST); // 사용자가 게시물 작성자가 아니라면 에러
            }
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
