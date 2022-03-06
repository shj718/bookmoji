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
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class); // 잘되는지 체크하기
    }

    public int createContent(int postIdx, PostPostReq postPostReq) {
        //postIdx에 해당하는 컨텐츠 넣기
        String createContentQuery = "insert into Content (contentUrl, contentType) VALUES (?,?)";
        Object[] createContentParams = new Object[]{postPostReq.getPostContents(), postPostReq.getContentType()};
        return this.jdbcTemplate.update(createContentQuery,createContentParams);
    }

    public List<GetPostInfoRes> getPostInfos() {
        String getPostInfosQuery = "select p_id as postId, u_id as userId, text from Post";
        return this.jdbcTemplate.query(getPostInfosQuery,
                (rs,rowNum) -> new GetPostInfoRes(
                        rs.getLong("postId"),
                        rs.getLong("userId"),
                        rs.getString("text"))
        );
    }

    public List<GetPostRes> getPosts() {
        String getPostsQuery = "select contentUrl as firstContentUrlPerPost, contentType as firstContentTypePerPost " +
                "from Content " +
                "where cont_id in (select min(cont_id) " +
                "                  from Content " +
                "                  where targetType = 'post' " +
                "                  group by target_id)";
        return this.jdbcTemplate.query(getPostsQuery,
                (rs,rowNum) -> new GetPostRes(
                        rs.getString("firstContentUrlPerPost"),
                        rs.getString("firstContentTypePerPost"))
        );
    }

    public List<GetPostRes> getFeedPosts(long userId) {
        String getFeedPostsQuery = "select contentUrl as firstContentUrlPerPost, contentType as firstContentTypePerPost " +
                "from Content " +
                "where cont_id in (select min(cont_id) " +
                "                  from Content " +
                "                  where targetType = 'post' " +
                "                    and target_id in (select p_id from Post where u_id = ?) " +
                "                  group by target_id)";
        long getFeedPostsParams = userId;
        return this.jdbcTemplate.query(getFeedPostsQuery,
                (rs,rowNum) -> new GetPostRes(
                        rs.getString("firstContentUrlPerPost"),
                        rs.getString("firstContentTypePerPost")),
                getFeedPostsParams);
    }

    public int modifyPostText(PatchPostReq patchPostReq) {
        String modifyPostTextQuery = "update Post set text = ? where u_id = ? AND p_id = ? ";
        Object[] modifyPostTextParams = new Object[]{patchPostReq.getUserId(), patchPostReq.getPostId()};

        return this.jdbcTemplate.update(modifyPostTextQuery,modifyPostTextParams);
    }
}
