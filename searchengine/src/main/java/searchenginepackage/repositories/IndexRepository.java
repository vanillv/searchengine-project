package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.model.IndexEntity;
import searchenginepackage.model.PageEntity;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer > {
    List<Integer> findAllLemmasByPageId(Integer pageId);
    List<PageEntity> findAllPagesByLemmaId(Integer lemmaId);
    IndexEntity findByPageIdAndLemmaId(Integer pageId, Integer lemmaId);
}
