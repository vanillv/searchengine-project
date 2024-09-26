package searchenginepackage.model;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.thymeleaf.spring6.processor.SpringValueTagProcessor;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Objects;
@Entity
@Table(name = "page")
@Data
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "site_id", nullable = false)
    private Integer site_id;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "code", nullable = false)
    private Integer code;
    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    public PageEntity(Integer site_id, String path, String content, int code) {
        this.site_id = site_id;
        this.path = path;
        this.content = content;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageEntity pageEntity = (PageEntity) o;
        return id == pageEntity.id && site_id == pageEntity.site_id && code == pageEntity.code && Objects.equals(path, pageEntity.path) && Objects.equals(content, pageEntity.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, site_id, path, code, content);
    }

    @Override
    public String toString() {
        return "PageEntity{" +
                "id=" + id +
                ", site_id=" + site_id +
                ", path='" + path + '\'' +
                ", code=" + code +
                ", content='" + content + '\'' +
                '}';
    }
}
