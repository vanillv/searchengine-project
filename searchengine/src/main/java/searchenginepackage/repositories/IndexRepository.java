package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.IndexEntity;
import searchenginepackage.entities.LemmaEntity;
import searchenginepackage.entities.PageEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer > {
    @Query("SELECT i.lemma FROM IndexEntity i WHERE i.page = :page")
    List<LemmaEntity> findAllLemmasByPageId(@Param("page") PageEntity page);
    @Query("SELECT i.page FROM IndexEntity i WHERE i.lemma = :lemma")
    List<PageEntity> findAllPagesByLemma(@Param("lemma") LemmaEntity lemma);
    @Query("SELECT i FROM IndexEntity i WHERE i.lemma IN :lemmas")
    List<IndexEntity> findAllByLemmas(@Param("lemmas") List<LemmaEntity> lemmas);
    @Query("SELECT i FROM IndexEntity i WHERE i.page = :page AND i.lemma = :lemma")
    IndexEntity findByPageAndLemma(@Param("page") PageEntity page, @Param("lemma") LemmaEntity lemma);
}
