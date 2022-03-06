package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.post.model.*;
import com.example.demo.src.post.PostProvider;
import com.example.demo.src.post.PostService;
// import com.example.demo.utils.JwtService;
import com.example.demo.src.user.model.GetUserRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/app/posts")
public class PostController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PostProvider postProvider;
    @Autowired
    private final PostService postService;

    public PostController(PostProvider postProvider, PostService postService){
        this.postProvider = postProvider;
        this.postService = postService;
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
    public BaseResponse<List<GetPostInfoRes>> getPostInfos() {
        try{
            List<GetPostInfoRes> getPostInfosRes = postProvider.getPostInfos();
            return new BaseResponse<>(getPostInfosRes);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 게시물 전체 조회 API
     * [GET] /posts
     * @return BaseResponse<List<GetPostRes>>
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetPostRes>> getPosts() {
        try{
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
    public BaseResponse<List<GetPostRes>> getFeedPosts(@PathVariable("userId") long userId) {
        try{
            List<GetPostRes> getFeedPostsRes = postProvider.getFeedPosts(userId);
            return new BaseResponse<>(getFeedPostsRes);

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
    public BaseResponse<String> modifyPostText(@PathVariable("userId") long userId, @RequestBody PatchPostReq patchPostReq) {
        try {
            patchPostReq.setUserId(userId);
            postService.modifyPostText(patchPostReq);

            String result = "";
            return new BaseResponse<>(result);
        }
        catch(BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
