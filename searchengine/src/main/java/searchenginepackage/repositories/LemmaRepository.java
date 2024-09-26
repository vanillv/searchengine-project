package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import searchenginepackage.model.LemmaEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    @Query(value = "SELECT id FROM lemma WHERE lemma = lemmaToSearch, siteId = siteIdToSearch")
    Integer findByLemmaAndSiteId(String lemmaToSearch, Integer siteIdToSearch);
    List<LemmaEntity> getByListOfIds(List<Integer> id);
}