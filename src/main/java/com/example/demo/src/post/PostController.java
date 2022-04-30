package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.post.model.*;
import com.example.demo.src.post.PostProvider;
import com.example.demo.src.post.PostService;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/app/posts")
public class PostController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PostProvider postProvider;
    @Autowired
    private final PostService postService;
    @Autowired
    private final JwtService jwtService;

    public PostController(PostProvider postProvider, PostService postService, JwtService jwtService){
        this.postProvider = postProvider;
        this.postService = postService;
        this.jwtService = jwtService;
    }

    /**
     * 게시물 생성 API
     * [POST] /posts
     * @return BaseResponse<PostPostRes>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostPostRes> createPost(@RequestBody PostPostReq postPostReq) {
        try{
            if(postPostReq.getUserId() == 0) {
                return new BaseResponse<>(USERS_EMPTY_USER_ID);
            }
            if(postPostReq.getPostContents() == null) {
                return new BaseResponse<>(REQUEST_ERROR);
            }
            if(postPostReq.getContentType() == null) {
                return new BaseResponse<>(REQUEST_ERROR);
            }
            int userId = postPostReq.getUserId();
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }

            PostPostRes postPostRes = postService.createPost(postPostReq);
            return new BaseResponse<>(postPostRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 게시물 정보 전체 조회 API
     * [GET] /posts/info
     * @return BaseResponse<List<GetPostInfoRes>>
     */
    @ResponseBody
    @GetMapping("/info")
    public BaseResponse<List<GetPostInfoRes>> getPostInfos(@RequestParam int userId) {
        try{
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            List<GetPostInfoRes> getPostInfosRes = postProvider.getPostInfos();
            return new BaseResponse<>(getPostInfosRes);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 게시물 전체 조회 API (검색 화면)
     * [GET] /posts
     * @return BaseResponse<List<GetPostRes>>
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetPostRes>> getPosts(@RequestParam int userId) {
        try{
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            List<GetPostRes> getPostsRes = postProvider.getPosts();
            return new BaseResponse<>(getPostsRes);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 특정 유저의 피드 게시물 조회 API
     * [GET] /posts/:userId
     * @return BaseResponse<List<GetPostRes>>
     */
    @ResponseBody
    @GetMapping("/{userId}")
    public BaseResponse<List<GetPostRes>> getFeedPosts(@PathVariable("userId") int userId, @RequestParam int myUserId) {
        try{
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(myUserId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            List<GetPostRes> getFeedPostsRes = postProvider.getFeedPosts(userId);
            return new BaseResponse<>(getFeedPostsRes);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 팔로잉하는 게시물 조회 API (메인 화면)
     * [GET] /posts/follow-posts
     * @return BaseResponse<List<GetPostRes>>
     */
    @ResponseBody
    @GetMapping("/follow-posts")
    public BaseResponse<List<GetPostRes>> getFollowPosts(@RequestParam int userId) {
        try{
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            List<GetPostRes> getFollowPostsRes = postProvider.getFollowPosts(userId);
            return new BaseResponse<>(getFollowPostsRes);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 게시물 수정 API
     * [PATCH] /posts/:userId
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userId}")
    public BaseResponse<String> modifyPostText(@PathVariable("userId") int userId, @RequestBody PatchPostReq patchPostReq) {
        try {
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            if(patchPostReq.getPostId() == 0L || patchPostReq.getText() == null) {
                return new BaseResponse<>(REQUEST_ERROR);
            }

            postService.modifyPostText(userId, patchPostReq);
            String result = "";
            return new BaseResponse<>(result);
        }
        catch(BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /** 게시물 좋아요 생성 API
     * [POST] /posts/likes
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/likes")
    public BaseResponse<String> createLike(@RequestBody Like like) {
        try {
            if(like.getUserId() == 0) {
                return new BaseResponse<>(USERS_EMPTY_USER_ID);
            }
            if(like.getPostId() == 0L) {
                return new BaseResponse<>(REQUEST_ERROR);
            }
            int userId = like.getUserId();
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }

            postService.createLike(like);
            String result = "";
            return new BaseResponse<>(result);
        }
        catch(BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /** 게시물 좋아요수 조회 API
     * [GET] /posts/likes
     * @return BaseResponse<GetLikeCountRes>
     */
    @ResponseBody
    @GetMapping("/likes")
    public BaseResponse<GetLikeCountRes> getLikeCount(@RequestParam long postId, @RequestParam int userId) {
        try{
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }
            GetLikeCountRes getLikeCountRes = postProvider.getLikeCount(postId);
            return new BaseResponse<>(getLikeCountRes);
        }
        catch(BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /** 게시물 좋아요 삭제 API
     * [PATCH] /posts/likes
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/likes")
    public BaseResponse<String> deleteLike(@RequestBody Like like) {
        try {
            if(like.getUserId() == 0) {
                return new BaseResponse<>(USERS_EMPTY_USER_ID);
            }
            if(like.getPostId() == 0L) {
                return new BaseResponse<>(REQUEST_ERROR);
            }
            int userId = like.getUserId();
            //jwt 에서 idx 추출.
            int userIdByJwt = jwtService.getUserIdx();
            if(userId != userIdByJwt){
                return new BaseResponse<>(INVALID_USER_JWT); //userId와 접근한 유저가 같은지 확인
            }

            postService.deleteLike(like);
            String result = "";
            return new BaseResponse<>(result);
        }
        catch(BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
