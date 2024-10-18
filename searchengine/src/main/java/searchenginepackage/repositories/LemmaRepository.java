package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.LemmaEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    @Query(value = "SELECT id FROM lemma WHERE lemma = lemmaToSearch, siteId = siteIdToSearch")
    Integer findIdByLemmaAndSiteId(String lemmaToSearch, Integer siteIdToSearch);
    List<LemmaEntity> getLemmaEntityListByListOfIds(List<Integer> id);
}