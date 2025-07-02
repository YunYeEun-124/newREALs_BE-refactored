package newREALs.backend.news.repository;

import newREALs.backend.news.domain.Basenews;
import newREALs.backend.news.domain.ThoughtComment;
import newREALs.backend.news.dto.ThoughtCommentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ThoughtCommentRepository extends JpaRepository<ThoughtComment,Long> {



    @Query("SELECT new newREALs.backend.news.dto.ThoughtCommentDto(tc.topic, tc.basenews.category.name, tc.basenews.id) " +
            "FROM ThoughtComment tc " +
            "WHERE tc.basenews.isDailyNews = true")
    List<ThoughtCommentDto> findAllBy();

    @Query("select tc from ThoughtComment tc where tc.basenews.id = :newsId")
    Optional<ThoughtComment> findByBasenews_Id(@Param("newsId") Long newsId);


    boolean existsByBasenews(Basenews news);
}

