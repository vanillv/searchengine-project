package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.model.IndexEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer > {
    @Query("SELECT lemmaId FROM index WHERE pageId = page_Id")
    List<Integer> findAllLemmaIdByPageId(Integer page_Id);
    List<Integer> findAllPageIdByLemmaId(Integer lemmaId);
    @Query(value = "SELECT index WHERE pageId = page_Id, lemmaId = lemma_Id")
    IndexEntity findByPageIdAndLemmaId(Integer page_Id, Integer lemma_Id);
}
