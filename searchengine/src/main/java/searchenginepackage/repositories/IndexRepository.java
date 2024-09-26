package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.model.IndexEntity;
import searchenginepackage.model.PageEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer > {
    List<Integer> findAllLemmaIdByPageId(Integer pageId);
    boolean existsByLemmaId();
    @Query(value = "SELECT")
    List<Integer> findAllPagesByLemmaId(Integer lemmaId);
    @Query(value = "SELECT index FROM index WHERE pageId = page_Id, lemmaId = lemma_Id")
    IndexEntity findByPageIdAndLemmaId(Integer page_Id, Integer lemma_Id);
}
