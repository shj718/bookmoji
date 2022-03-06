package com.example.demo.src.comment;

import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.comment.CommentDao;
import com.example.demo.src.comment.model.*;
import com.example.demo.utils.AES128;
// import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

//Provider : Read의 비즈니스 로직 처리
@Service
public class CommentProvider {

    private final CommentDao commentDao;
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public CommentProvider(CommentDao commentDao) {
        this.commentDao = commentDao;
    }

    // 댓글 조회
    public List<GetCommentRes> getComments(long postId) throws BaseException{
        try{
            List<GetCommentRes> getCommentsRes = commentDao.getComments(postId);
            return getCommentsRes;
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 댓글 개수 조회
    public int getCommentsCount(long postId) throws BaseException{
        try{
            int result = commentDao.getCommentsCount(postId);
            return result;

        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
