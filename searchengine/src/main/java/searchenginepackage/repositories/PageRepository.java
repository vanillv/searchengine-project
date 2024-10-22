package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.PageEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query(value = "SELECT p FROM page p WHERE p.site_id = SITE_ID")
    List<PageEntity> findAllBySiteId(Integer SITE_ID);

    List<PageEntity> findAllBySiteId(List<Integer> id);
}
