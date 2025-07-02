package newREALs.backend.accounts.repository;

import newREALs.backend.accounts.domain.UserThoughtComment;
import newREALs.backend.accounts.dto.userKeywordDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserThoughtCommentRepository extends JpaRepository<UserThoughtComment,Long> {

   // @Query("select tc.UserThoughtComment from UserThoughtComment tc where tc.ThoughtComment.id = :id and tc.user.id = :userId")
    String findByThoughtComment_IdAndUser_Id(@Param("id") Long id, @Param("userId") Long userId);


   // @Query("select tc from UserThoughtComment tc where tc.ThoughtComment.basenews.id = :newsId and tc.user.id = :userId")
    UserThoughtComment findUserCommentByThoughtComment_IdAndUser_Id(@Param("newsId") Long newsId, @Param("userId") Long userId);

    Optional<UserThoughtComment> findByUser_Id(Long userid);
//
//    @Query("select new newREALs.backend.accounts.dto.userKeywordDto.UserCommentListDTO(uc.ThoughtComment.topic, uc.UserThoughtComment, uc.ThoughtComment.basenews.id) " +
//            "from UserThoughtComment uc " +
//            "WHERE uc.user.id = :userid")
    Slice<userKeywordDto.UserCommentListDTO> findAllByUserId(@Param("userid")Long userid, Pageable pageable);

   // @Query("select tc.UserThoughtComment from UserThoughtComment tc where tc.ThoughtComment.id = :id")
    List<String> findByThoughtComment_Id(Long id);
}
