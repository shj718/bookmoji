package com.example.demo.src.post;

import com.example.demo.src.post.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public int createPost(PostPostReq postPostReq) {
        String createPostQuery = "insert into Post (u_id, text) VALUES (?,?)";
        Object[] createPostParams = new Object[]{postPostReq.getUserId(), postPostReq.getText()};
        this.jdbcTemplate.update(createPostQuery, createPostParams); // update()는 추가/변경한 행 개수를 리턴하는데 우리는 지금 이 값은 필요 없음

        String lastInserIdQuery = "select last_insert_id()"; // 마지막으로 인서트한 포스트의 idx를 다시 쿼리문으로 받아옴
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class);
    }

    public int createContent(int postIdx, String postContent, String contentType) {
        //postIdx에 해당하는 컨텐츠 넣기
        String createContentQuery = "insert into Content (contentUrl, contentType, target_id, targetType) VALUES (?,?,?,'post')";
        Object[] createContentParams = new Object[]{postContent, contentType, postIdx};
        return this.jdbcTemplate.update(createContentQuery,createContentParams);
    }

    public List<GetPostInfoRes> getPostInfos() {
        String getPostInfosQuery = "select p_id as postId, u_id as userId, text from Post";
        return this.jdbcTemplate.query(getPostInfosQuery,
                (rs,rowNum) -> new GetPostInfoRes(
                        rs.getLong("postId"),
                        rs.getInt("userId"),
                        rs.getString("text"))
        );
    }

    public List<GetPostRes> getPosts() {
        String getPostsQuery = "select target_id as postId, contentUrl as firstContentUrlPerPost, contentType as firstContentTypePerPost " +
                "from Content " +
                "where cont_id in (select min(cont_id) " +
                "                  from Content " +
                "                  where targetType = 'post' " +
                "                  group by target_id)";
        return this.jdbcTemplate.query(getPostsQuery,
                (rs,rowNum) -> new GetPostRes(
                        rs.getLong("postId"),
                        rs.getString("firstContentUrlPerPost"),
                        rs.getString("firstContentTypePerPost"))
        );
    }

    public List<GetPostRes> getFeedPosts(int userId) {
        String getFeedPostsQuery = "select target_id as postId, contentUrl as firstContentUrlPerPost, contentType as firstContentTypePerPost " +
                "from Content " +
                "where cont_id in (select min(cont_id) " +
                "                  from Content " +
                "                  where targetType = 'post' " +
                "                    and target_id in (select p_id from Post where u_id = ?) " +
                "                  group by target_id)";
        int getFeedPostsParams = userId;
        return this.jdbcTemplate.query(getFeedPostsQuery,
                (rs,rowNum) -> new GetPostRes(
                        rs.getLong("postId"),
                        rs.getString("firstContentUrlPerPost"),
                        rs.getString("firstContentTypePerPost")),
                getFeedPostsParams);
    }

    public List<GetPostRes> getFollowPosts(int userId) {
        String getFollowPostsQuery = "select target_id as postId, contentUrl as firstContentUrlPerPost, contentType as firstContentTypePerPost " +
                "from Content " +
                "where cont_id in (select min(cont_id) " +
                "                  from Content " +
                "                  where targetType = 'post' " +
                "                    and target_id in (select p_id " +
                "                                      from Post " +
                "                                      where u_id in (select following " +
                "                                                     from Follow " +
                "                                                     where follower = ? " +
                "                                                       and status = 'active')) " +
                "                  group by target_id)";
        int getFollowPostsParams = userId;
        return this.jdbcTemplate.query(getFollowPostsQuery,
                (rs,rowNum) -> new GetPostRes(
                        rs.getLong("postId"),
                        rs.getString("firstContentUrlPerPost"),
                        rs.getString("firstContentTypePerPost")),
                getFollowPostsParams);
    }

    public int modifyPostText(int userId, PatchPostReq patchPostReq) {
        String modifyPostTextQuery = "update Post set text = ? where u_id = ? and p_id = ? ";
        Object[] modifyPostTextParams = new Object[]{patchPostReq.getText(), userId, patchPostReq.getPostId()};
        return this.jdbcTemplate.update(modifyPostTextQuery,modifyPostTextParams);
    }

    public int checkPostLike(Like like) { // 해당 게시물에 좋아요를 누른적이 있는지 확인
        String checkPostLikeQuery = "select exists(select l_id from Likes where p_id = ? and u_id = ?)";
        Object[] checkPostLikeParams = new Object[]{like.getPostId(), like.getUserId()};
        return this.jdbcTemplate.queryForObject(checkPostLikeQuery,int.class,checkPostLikeParams);
    }

    public int createLike(Like like) {
        String createLikeQuery = "insert into Likes (p_id, u_id) VALUES (?,?)";
        Object[] createLikeParams = new Object[]{like.getPostId(), like.getUserId()};
        return this.jdbcTemplate.update(createLikeQuery, int.class, createLikeParams);
    }

    public GetLikeCountRes getLikeCount(long postId) {
        String getLikeCountQuery = "select p_id, count(l_id) as likeCount from Likes where p_id = ? and status = 'active'";
        long getLikeCountParams = postId;
        return this.jdbcTemplate.queryForObject(getLikeCountQuery,
                (rs,rowNum) -> new GetLikeCountRes(
                        rs.getLong("p_id"),
                        rs.getInt("likeCount")),
                getLikeCountParams);
    }

    public int deleteLike(Like like) {
        String deleteLikeQuery = "update Likes set status = 'deleted' where p_id = ? and u_id = ?";
        Object[] deleteLikeParams = new Object[]{like.getPostId(), like.getUserId()};
        return this.jdbcTemplate.update(deleteLikeQuery, int.class, deleteLikeParams);
    }
}
