package newREALs.backend.repository;

import newREALs.backend.domain.UserComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCommentRepository extends JpaRepository<UserComment,Long> {

    @Query("select tc.userComment from UserComment tc where tc.thinkComment.id = :id and tc.user.id = :userId")
    String findByThinkComment_IdAndUser_Id(@Param("id") Long id, @Param("userId") Long userId);


    @Query("select tc from UserComment tc where tc.thinkComment.basenews.id = :newsId and tc.user.id = :userId")
    UserComment findUserCommentByThinkComment_IdAndUser_Id(@Param("newsId") Long newsId, @Param("userId") Long userId);

    Optional<UserComment> findByUser_Id(Long userid);
}
