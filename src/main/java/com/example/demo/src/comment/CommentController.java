package com.example.demo.src.comment;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.comment.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/app/comments")
public class CommentController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final CommentProvider commentProvider;
    @Autowired
    private final CommentService commentService;

    public CommentController(CommentProvider commentProvider, CommentService commentService) {
        this.commentProvider = commentProvider;
        this.commentService = commentService;
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
            int result = commentProvider.getCommentsCount(postId);
            return new BaseResponse<>(result);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 댓글 삭제 API
     * [PATCH] /comments/:userId/:commentId
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userId}/{commentId}")
    public BaseResponse<String> deleteComment(@PathVariable("userId") long userId, @PathVariable("commentId") long commentId) {
        try{
            commentService.deleteComment(userId, commentId);
            String result = "";
            return new BaseResponse<>(result);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
