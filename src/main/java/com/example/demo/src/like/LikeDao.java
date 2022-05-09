package com.example.demo.src.like;

// import com.example.demo.src.like.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class LikeDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public int checkReview(long reviewIdx) {
        String checkReviewQuery = "select exists(select id from Review where id = ? and status = 'A')";
        long checkReviewParams = reviewIdx;

        return this.jdbcTemplate.queryForObject(checkReviewQuery,
                int.class,
                checkReviewParams);
    }

    public int checkLike(long userIdx, long reviewIdx) {
        String checkLikeQuery = "select exists(select id from Likes where userId = ? and reviewId = ? and status = 'A')";
        Object[] checkLikeParams = new Object[]{userIdx, reviewIdx};

        return this.jdbcTemplate.queryForObject(checkLikeQuery,
                int.class,
                checkLikeParams);
    }

    public long createLike(long userIdx, long reviewIdx) {
        String createLikeQuery = "insert into Likes (userId, reviewId) VALUES (?,?)";
        Object[] createLikeParams = new Object[]{userIdx, reviewIdx};

        this.jdbcTemplate.update(createLikeQuery, createLikeParams);
        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,long.class);
    }

    public int deleteLike(long userIdx, long reviewIdx) {
        String deleteLikeQuery = "update Likes set status = 'D' where userId = ? and reviewId = ? and status = 'A'";
        Object[] deleteLikeParams = new Object[]{userIdx, reviewIdx};

        return this.jdbcTemplate.update(deleteLikeQuery, deleteLikeParams);
    }

    public int checkUserReview(long userIdx, long reviewIdx) {
        String checkUserReviewQuery = "select exists(select id from Review where userId = ? and id = ? and status = 'A')";
        Object[] checkUserReviewParams = new Object[]{userIdx, reviewIdx};

        return this.jdbcTemplate.queryForObject(checkUserReviewQuery,
                int.class,
                checkUserReviewParams);
    }

    public int getLikeCount(long reviewIdx) {
        String getLikeCountQuery = "select count(id) as likeCount from Likes where reviewId = ? and status = 'A'";
        long getLikeCountParams = reviewIdx;

        return this.jdbcTemplate.queryForObject(getLikeCountQuery,
                int.class,
                getLikeCountParams);
    }
}
