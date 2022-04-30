package com.example.demo.src.comment;

import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.comment.CommentDao;
import com.example.demo.src.comment.CommentProvider;
import com.example.demo.src.comment.model.*;
import com.example.demo.utils.AES128;
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
public class CommentService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommentDao commentDao;
    private final CommentProvider commentProvider;

    @Autowired
    public CommentService(CommentDao commentDao, CommentProvider commentProvider) {
        this.commentDao = commentDao;
        this.commentProvider = commentProvider;
    }

    // 댓글 생성
    public PostCommentRes createComment(PostCommentReq postCommentReq) throws BaseException {
        // 존재하는 게시물인지 확인 (존재하지 않으면 에러)
        if(commentProvider.checkCommentTarget(postCommentReq.getPostId()) == 0) {
            throw new BaseException(POST_NOT_EXISTS);
        }
        try{
            int commentIdx = commentDao.createComment(postCommentReq);
            return new PostCommentRes(postCommentReq.getPostId(),(long)commentIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 댓글 삭제
    public void deleteComment(PatchCommentReq patchCommentReq) throws BaseException {
        try{
            int result = commentDao.deleteComment(patchCommentReq);
            if(result == 0) {
                throw new BaseException(FAILED_TO_DELETE_COMMENT); // 댓글 삭제 에러
            }
        } catch (Exception exception) {
            throw new BaseException(FAILED_TO_DELETE_COMMENT);
        }
    }

}
