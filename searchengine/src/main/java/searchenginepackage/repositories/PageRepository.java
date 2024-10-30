package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.PageEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query("SELECT p FROM PageEntity p WHERE p.siteId = :siteId")
    List<PageEntity> findBySiteId(@Param("siteId") Long siteId);
    @Query(value = "SELECT p FROM page p WHERE p.siteId = :id", nativeQuery = true)
    List<PageEntity> findAllBySiteId(List<Integer> id);
}
