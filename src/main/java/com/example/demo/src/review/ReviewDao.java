package com.example.demo.src.review;

import com.example.demo.config.BaseException;
import com.example.demo.src.review.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class ReviewDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public long createReview(PostReviewReq postReviewReq, long bookIdx) {
        String createReviewQuery = "insert into Review (emoji, text, bookId, userId) VALUES (?,?,?,?)";
        Object[] createReviewParams = new Object[]{postReviewReq.getEmoji(), postReviewReq.getText(), bookIdx, postReviewReq.getUserIdx()};

        this.jdbcTemplate.update(createReviewQuery, createReviewParams);
        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,long.class);
    }

    public int checkBook(String isbn) {
        String checkBookQuery = "select exists(select isbn from Book where isbn = ? and status = 'A')";
        String checkBookParams = isbn;

        return this.jdbcTemplate.queryForObject(checkBookQuery,
                int.class,
                checkBookParams);
    }

    public long createBook(PostReviewReq postReviewReq) {
        String createBookQuery = "insert into Book (isbn, title, thumbnailUrl, author, publisher, introduction, releaseYear) VALUES (?,?,?,?,?,?,?)";
        Object[] createBookParams = new Object[]{postReviewReq.getIsbn(), postReviewReq.getTitle(), postReviewReq.getThumbnailUrl(),
        postReviewReq.getAuthor(), postReviewReq.getPublisher(), postReviewReq.getIntroduction(), postReviewReq.getReleaseYear()};

        this.jdbcTemplate.update(createBookQuery, createBookParams);
        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,long.class);
    }

    public long getBookIdx(String isbn) {
        String getBookIdxQuery = "select id from Book where isbn = ? and status = 'A'";
        String getBookIdxParams = isbn;

        return this.jdbcTemplate.queryForObject(getBookIdxQuery,
                long.class,
                getBookIdxParams);
    }

    public int checkReview(long userIdx) {
        String checkReviewQuery = "select exists(select id from Review where userId = ? and status = 'A')";
        long checkReviewParams = userIdx;

        return this.jdbcTemplate.queryForObject(checkReviewQuery,
                int.class,
                checkReviewParams);
    }

    public List<GetReviewRes> getReviews(long userIdx) {
        String getReviewsQuery = "select Reviews.id as reviewIdx, title, thumbnailUrl, emoji " +
                "from (select id, emoji, bookId from Review where userId = ? and status = 'A') Reviews " +
                "         inner join (select id, title, thumbnailUrl from Book where status = 'A') Books " +
                "                    on Reviews.bookId = Books.id";
        long getReviewsParams = userIdx;

        return this.jdbcTemplate.query(getReviewsQuery,
                (rs,rowNum) -> new GetReviewRes(
                        rs.getLong("reviewIdx"),
                        rs.getString("title"),
                        rs.getString("thumbnailUrl"),
                        rs.getString("emoji")),
                getReviewsParams);
    }

    public int checkUserReview(long userIdx, long reviewIdx) {
        String checkUserReviewQuery = "select exists(select id from Review where userId = ? and id = ? and status = 'A')";
        Object[] checkUserReviewParams = new Object[]{userIdx, reviewIdx};

        return this.jdbcTemplate.queryForObject(checkUserReviewQuery,
                int.class,
                checkUserReviewParams);
    }

    public GetReviewDetailRes getReviewDetail(long reviewIdx) {
        String getReviewDetailQuery = "select reviewIdx, emoji, text, " +
                "       case " +
                "           when timestampdiff(minute, createdAt, current_timestamp) < 60 " +
                "               then concat(timestampdiff(minute, createdAt, current_timestamp), '분 전') " +
                "           when timestampdiff(hour, createdAt, current_timestamp) < 24 " +
                "               then concat(timestampdiff(hour, createdAt, current_timestamp), '시간 전') " +
                "           when timestampdiff(day, createdAt, current_timestamp) < 7 " +
                "               then concat(timestampdiff(day, createdAt, current_timestamp), '일 전') " +
                "           when timestampdiff(year, createdAt, current_timestamp) < 1 " +
                "               then date_format(createdAt, '%c월 %e일') " +
                "           else date_format(createdAt, '%Y년 %c월 %e일') " +
                "           end as reviewTime, " +
                "       isbn, title, thumbnailUrl, author, publisher, introduction, releaseYear " +
                "from (select id as reviewIdx, emoji, text, bookId, createdAt " +
                "      from Review " +
                "      where id = ? " +
                "        and status = 'A') ReviewDetail " +
                "         inner join (select id, isbn, title, thumbnailUrl, author, publisher, introduction, releaseYear " +
                "                     from Book " +
                "                     where status = 'A') BookDetail " +
                "                    on ReviewDetail.bookId = BookDetail.id";
        long getReviewDetailParams = reviewIdx;
        return this.jdbcTemplate.queryForObject(getReviewDetailQuery,
                (rs,rowNum) -> new GetReviewDetailRes(
                        rs.getLong("reviewIdx"),
                        rs.getString("emoji"),
                        rs.getString("text"),
                        rs.getString("reviewTime"),
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("thumbnailUrl"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getString("introduction"),
                        rs.getString("releaseYear")),
                getReviewDetailParams);
    }

    public int modifyReview(PatchReviewReq patchReviewReq) {
        String modifyReviewQuery = "update Review set emoji = ?, text = ? where id = ?";
        Object[] modifyReviewParams = new Object[]{patchReviewReq.getEmoji(), patchReviewReq.getText(), patchReviewReq.getReviewIdx()};

        return this.jdbcTemplate.update(modifyReviewQuery, modifyReviewParams);
    }

    public int deleteReview(long reviewIdx) {
        String deleteReviewQuery = "update Review set status = 'D' where id = ?";
        long deleteReviewParams = reviewIdx;

        return this.jdbcTemplate.update(deleteReviewQuery, deleteReviewParams);
    }

    public int checkOtherReview(long userIdx) {
        String checkOtherReviewQuery = "select exists(select id from Review where status = 'A' and userId != ?)";
        long checkOtherReviewParams = userIdx;

        return this.jdbcTemplate.queryForObject(checkOtherReviewQuery,
                int.class,
                checkOtherReviewParams);
    }

    public List<OtherReview> getOtherReviewsTmp(long userIdx) { // 일단은 최신순 6개
        String getOtherReviewsTmpQuery = "select reviewIdx, emoji, text, isbn, title, thumbnailUrl, author " +
                "from (select id as reviewIdx, emoji, text, bookId " +
                "      from Review " +
                "      where userId != ? " +
                "        and status = 'A') Reviews " +
                "         inner join (select id, isbn, title, thumbnailUrl, author " +
                "                     from Book " +
                "                     where status = 'A') Books " +
                "                    on Reviews.bookId = Books.id " +
                "order by reviewIdx desc limit 6";
        long getOtherReviewsTmpParams = userIdx;

        return this.jdbcTemplate.query(getOtherReviewsTmpQuery,
                (rs,rowNum) -> new OtherReview(
                        rs.getLong("reviewIdx"),
                        rs.getString("emoji"),
                        rs.getString("text"),
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("thumbnailUrl"),
                        rs.getString("author")),
                getOtherReviewsTmpParams);
    }

    public int getLikedOrNot(long userIdx, long reviewIdx) {
        String getLikedOrNotQuery = "select exists(select id from Likes where userId = ? and reviewId = ? and status = 'A')";
        Object[] getLikedOrNotParams = new Object[]{userIdx, reviewIdx};

        return this.jdbcTemplate.queryForObject(getLikedOrNotQuery,
                int.class,
                getLikedOrNotParams);
    }
}
