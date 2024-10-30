package searchenginepackage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchenginepackage.entities.SiteEntity;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    @Query(value = "SELECT s.id FROM site s WHERE s.url = :urlToSearch", nativeQuery = true)
    Integer findIdByUrl(String urlToSearch);
}
