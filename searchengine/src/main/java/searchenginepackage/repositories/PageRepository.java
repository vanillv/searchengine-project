package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import searchenginepackage.model.LemmaEntity;
import searchenginepackage.model.PageEntity;
import searchenginepackage.model.SiteEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query(value = "SELECT p FROM page WHERE siteId = SITE_ID")
    List<PageEntity> findAllBySite(Integer SITE_ID);
    List<PageEntity> getByListOfIds(List<Integer> id);
}
