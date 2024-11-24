package newREALs.backend.repository;

import newREALs.backend.domain.ThinkComment;
import newREALs.backend.dto.InsightDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InsightRepository extends JpaRepository<ThinkComment,Long> {



    @Query("SELECT new newREALs.backend.dto.InsightDTO(tc.topic, tc.basenews.category.name, tc.basenews.id) " +
            "FROM ThinkComment tc " +
            "WHERE tc.basenews.isDailyNews = true")
    List<InsightDTO> findAllBy();


}
