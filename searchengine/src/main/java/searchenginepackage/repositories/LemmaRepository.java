package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.LemmaEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    @Query(value = "SELECT l.id FROM lemma l WHERE l.lemma = :lemmaToSearch AND l.site_id = :siteIdToSearch", nativeQuery = true)
    Integer findIdByLemmaAndSiteId(String lemmaToSearch, Integer siteIdToSearch);
}