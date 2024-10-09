package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.model.PageEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query(value = "SELECT p FROM page WHERE siteId = SITE_ID")
    List<PageEntity> findAllPageBySiteId(Integer SITE_ID);
    List<PageEntity> findAllPageByListOfIds(List<Integer> id);
}
