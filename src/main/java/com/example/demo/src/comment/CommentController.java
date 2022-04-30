package com.example.demo.src.comment;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.comment.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/app/comments")
public class CommentController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final CommentProvider commentProvider;
    @Autowired
    private final CommentService commentService;
    @Autowired
    private final JwtService jwtService;

    public CommentController(CommentProvider commentProvider, CommentService commentService, JwtService jwtService) {
        this.commentProvider = commentProvider;
        this.commentService = commentService;
        this.jwtService = jwtService;
    }


    /**
     * 댓글 생성 API
     * [POST] /comments
     * @return BaseResponse<PostCommentRes>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostCommentRes> createComment(@RequestBody PostCommentReq postCommentReq) {
        try{
            if(postCommentReq.getUserId() == 0) {
                return new BaseResponse<>(USERS_EMPTY_USER_ID);
            }
            if(postCommentReq.getText() == null) {
                return new BaseResponse<>(REQUEST_ERROR);
            }
            if(postCommentReq.getPostId() == 0L) {
                return new BaseResponse<>(REQUEST_ERROR);
            }
            int userId = postCommentReq.getUserId();
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }

            PostCommentRes postCommentRes = commentService.createComment(postCommentReq);
            return new BaseResponse<>(postCommentRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 댓글 조회 API
     * [GET] /comments/:postId
     * @return BaseResponse<List<GetCommentRes>>
     */
    @ResponseBody
    @GetMapping("/{postId}")
    public BaseResponse<List<GetCommentRes>> getComments(@PathVariable("postId") long postId) {
        try{
            List<GetCommentRes> getCommentsRes = commentProvider.getComments(postId);
            return new BaseResponse<>(getCommentsRes);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 게시물 댓글 개수 조회 API
     * [GET] /comments/:postId/count
     * @return BaseResponse<Integer>
     */
    @ResponseBody
    @GetMapping("/{postId}/count")
    public BaseResponse<Integer> getCommentsCount(@PathVariable("postId") long postId) {
        try{
            int commentsCount = commentProvider.getCommentsCount(postId);
            Integer result = commentsCount;
            return new BaseResponse<>(result);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 댓글 삭제 API
     * [PATCH] /comments
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userId}/{commentId}")
    public BaseResponse<String> deleteComment(@RequestBody PatchCommentReq patchCommentReq) {
        try{
            if(patchCommentReq.getUserId() == 0) {
                return new BaseResponse<>(USERS_EMPTY_USER_ID);
            }
            if(patchCommentReq.getCommentId() == 0L) {
                return new BaseResponse<>(REQUEST_ERROR);
            }
            int userId = patchCommentReq.getUserId();
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            commentService.deleteComment(patchCommentReq);
            String result = "";
            return new BaseResponse<>(result);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
