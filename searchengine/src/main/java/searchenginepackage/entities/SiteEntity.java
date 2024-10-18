package searchenginepackage.entities;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity
@Data
@Table(name = "site")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private IndexStatus status;
    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime statusTime;
    @Column(name = "last_error")
    private String lastError;
    @Column(name = "url", nullable = false)
    private String url;
    @Column(name = "name", nullable = false)
    private String name;

    public SiteEntity(String url, String name, IndexStatus status) {
        this.url = url;
        this.name = name;
        this.status = status;
        statusTime = LocalDateTime.now();
    }
}
