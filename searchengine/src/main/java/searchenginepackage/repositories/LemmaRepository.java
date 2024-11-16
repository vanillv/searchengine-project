package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.LemmaEntity;
import searchenginepackage.entities.SiteEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemmaToSearch AND l.site.id = :siteIdToSearch")
    LemmaEntity findByLemmaAndSiteId(String lemmaToSearch, Integer siteIdToSearch);
    @Query("SELECT l FROM LemmaEntity l WHERE l.site.id = :siteIdToSearch")
    List<LemmaEntity> findAllBySiteId(Integer siteIdToSearch);
    int countBySite(SiteEntity site);

}