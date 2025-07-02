package newREALs.backend.news.repository;

import newREALs.backend.news.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

     Optional<Category> findByName(String cate);
}