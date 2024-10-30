package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.IndexEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer > {
    @Query(value = "SELECT i.lemmaId FROM `page_index` i WHERE i.pageId = :page_Id", nativeQuery = true)
    List<Integer> findAllLemmaIdByPageId(Integer page_Id);
    List<Integer> findAllPageIdByLemmaId(Integer lemmaId);
    @Query(value = "SELECT * FROM `page_index` WHERE pageId = :page_Id AND lemmaId = :lemma_Id", nativeQuery = true)
    IndexEntity findByPageIdAndLemmaId(Integer page_Id, Integer lemma_Id);
}
