package com.example.demo.src.comment;

import com.example.demo.src.comment.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class CommentDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public int createComment(PostCommentReq postCommentReq) {
        String createCommentQuery = "insert into Comment (text, u_id, target_id, previous) VALUES (?,?,?,?)";
        Object[] createCommentParams = new Object[]{postCommentReq.getText(),postCommentReq.getUserId(), postCommentReq.getPostId(), postCommentReq.getPrevCommentId()};
        this.jdbcTemplate.update(createCommentQuery, createCommentParams); // update()는 추가/변경한 행 개수를 리턴하는데 우리는 지금 이 값은 필요 없음

        String lastInserIdQuery = "select last_insert_id()"; // 마지막으로 인서트한 댓글의 idx를 다시 쿼리문으로 받아옴
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class); // 잘되는지 체크하기
    }

    public List<GetCommentRes> getComments(long postId) {
        String getCommentsQuery = "select c_id, u_id, text from Comment where status = 'active' and target_id = ?";
        long getCommentsParams = postId;
        return this.jdbcTemplate.query(getCommentsQuery,
                (rs,rowNum) -> new GetCommentRes(
                        rs.getLong("c_id"),
                        rs.getLong("u_id"),
                        rs.getString("text")),
                getCommentsParams);
    }

    public int getCommentsCount(long postId) {
        String getCommentsCountQuery = "select count(c_id) from Comment where status = 'active' and target_id = ?";
        long getCommentsCountParams = postId;
        return this.jdbcTemplate.queryForObject(getCommentsCountQuery, int.class, getCommentsCountParams);
    }

    public int deleteComment(long userId, long commentId) {
        String deleteCommentQuery = "update Comment set status = 'deleted' where u_id = ? AND c_id = ? AND status = 'active'";
        Object[] deleteCommentParams = new Object[]{userId, commentId};
        return this.jdbcTemplate.update(deleteCommentQuery, deleteCommentParams);
    }

}
