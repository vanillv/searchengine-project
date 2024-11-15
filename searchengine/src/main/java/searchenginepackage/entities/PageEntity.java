package searchenginepackage.entities;

import lombok.Data;

import jakarta.persistence.*;
import lombok.ToString;
import org.hibernate.query.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "page")
@Data
public class PageEntity {
    public PageEntity(){}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "code", nullable = false)
    private Integer code;
    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<IndexEntity> indices = new ArrayList<>();
    public PageEntity(SiteEntity site, String path, String content, int code) {
        this.site = site;
        this.path = path;
        this.content = content;
        this.code = code;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, site.getId(), path, code, content);
    }
    @Override
    public String toString() {
        return "PageEntity{" +
                "id=" + id +
                ", siteId=" + site.getId() +
                ", path='" + path + '\'' +
                ", code=" + code +
                ", content='" + content + '\'' +
                '}';
    }
}

