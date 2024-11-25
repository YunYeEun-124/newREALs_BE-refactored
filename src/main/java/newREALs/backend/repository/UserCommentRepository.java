package newREALs.backend.repository;

import newREALs.backend.domain.UserComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCommentRepository extends JpaRepository<UserComment,Long> {


}
