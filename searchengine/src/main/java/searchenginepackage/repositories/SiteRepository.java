package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.SiteEntity;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    @Query(value = "SELECT id FROM site WHERE url=urlToSearch")
    Integer findIdByUrl(String urlToSearch);
}
