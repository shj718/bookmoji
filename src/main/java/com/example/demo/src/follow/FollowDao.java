package com.example.demo.src.follow;

import com.example.demo.src.follow.model.*;
import com.example.demo.src.user.model.GetUserRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class FollowDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public int checkFollow(long fromUserId, long toUserId) {
        String checkFollowQuery = "select exists(select following from Follow where follower = ? AND following = ? AND status = 'active')";
        Object[] checkFollowParams = new Object[] {fromUserId, toUserId};
        return this.jdbcTemplate.queryForObject(checkFollowQuery,
                int.class,
                checkFollowParams);
    }

    public int createFollow(PostFollowReq postFollowReq) {
        String createFollowQuery = "insert into Follow (follower, following) VALUES (?,?)";
        Object[] createFollowParams = new Object[]{postFollowReq.getFromUserId(), postFollowReq.getToUserId()};
        return this.jdbcTemplate.update(createFollowQuery,createFollowParams);
    }

    // 팔로워 리스트
    public List<GetFollowerRes> getFollowers(long userId) {
        String getFollowersQuery = "select userName, " +
                "       name, " +
                "       profileImgUrl, " +
                "       if(u_id in (select u_id " +
                "                   from View_Story " +
                "                            inner join (select s_id, u_id " +
                "                                        from Story " +
                "                                        where createdAt in (select max(createdAt) " +
                "                                                            from Story " +
                "                                                            where status = 'shown' " +
                "                                                            group by u_id) " +
                "                                        group by u_id) latestStoryPerUser on s_id = story_id), 'seen', " +
                "          'notSeen') as seenOrNotSeenLatestStory, " +
                "       'follow'         as followEachOther " +
                "from User " +
                "where User.u_id in (select follower from Follow where following = ? AND status = 'active') " +
                "  and User.u_id in (select following from Follow where follower = ? AND status = 'active') AND User.status = 'active'" +
                "union " +
                "select userName, " +
                "       name, " +
                "       profileImgUrl, " +
                "       if(u_id in (select u_id " +
                "                   from View_Story " +
                "                            inner join (select s_id, u_id " +
                "                                        from Story " +
                "                                        where createdAt in (select max(createdAt) " +
                "                                                            from Story " +
                "                                                            where status = 'shown' " +
                "                                                            group by u_id) " +
                "                                        group by u_id) latestStoryPerUser on s_id = story_id), 'seen', " +
                "          'notSeen') as seenOrNotSeenLatestStory, " +
                "       ''            as followEachOther " +
                "from User " +
                "where User.u_id in (select follower from Follow where following = ? AND status = 'active') " +
                "  and User.u_id not in (select following from Follow where follower = ? AND status = 'active') AND User.status = 'active'";
        Object[] getFollowersParams = new Object[]{userId, userId, userId, userId};
        return this.jdbcTemplate.query(getFollowersQuery,
                (rs, rowNum) -> new GetFollowerRes(
                        rs.getString("userName"),
                        rs.getString("name"),
                        rs.getString("profileImgUrl"),
                        rs.getString("seenOrNotSeenLatestStory"),
                        rs.getString("followEachOther")),
                getFollowersParams);
    }

    // 팔로잉 리스트
    public List<GetFollowingRes> getFollowings(long userId) {
        String getFollowingsQuery = "select userName, " +
                "       name, " +
                "       profileImgUrl, " +
                "       if(u_id in (select u_id " +
                "                   from View_Story " +
                "                            inner join (select s_id, u_id " +
                "                                        from Story " +
                "                                        where createdAt in (select max(createdAt) " +
                "                                                            from Story " +
                "                                                            where status = 'shown' " +
                "                                                            group by u_id) " +
                "                                        group by u_id) latestStoryPerUser on s_id = story_id), 'seen', " +
                "          'notSeen') as seenOrNotSeenLatestStory " +
                "from User " +
                "where User.u_id in (select following from Follow where follower = ? AND status = 'active') AND User.status = 'active'";
        long getFollowingsParams = userId;
        return this.jdbcTemplate.query(getFollowingsQuery,
                (rs, rowNum) -> new GetFollowingRes(
                        rs.getString("userName"),
                        rs.getString("name"),
                        rs.getString("profileImgUrl"),
                        rs.getString("seenOrNotSeenLatestStory")),
                getFollowingsParams);
    }

    // 언팔로우
    public int deleteFollow(PatchFollowReq patchFollowReq) {
        String deleteFollowQuery = "update Follow set status = 'deactivated' where follower = ? AND following = ? ";
        Object[] deleteFollowParams = new Object[]{patchFollowReq.getFromUserId(), patchFollowReq.getToUserId()};
        return this.jdbcTemplate.update(deleteFollowQuery,deleteFollowParams);
    }

}
