package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.PageEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query("SELECT p FROM PageEntity p WHERE p.site.id = :siteId")
    List<PageEntity> findBySiteId(@Param("siteId") Integer siteId);

    @Query(value = "SELECT * FROM page p WHERE p.site.id IN (:ids)", nativeQuery = true)
    List<PageEntity> findAllBySiteId(@Param("ids") List<Integer> ids);
}
