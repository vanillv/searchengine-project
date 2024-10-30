package searchenginepackage.entities;

import lombok.Data;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "page")
@Data
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "site_id", nullable = false)  // Consistency with naming
    private Integer siteId;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "code", nullable = false)
    private Integer code;
    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
    public PageEntity(Integer siteId, String path, String content, int code) {
        this.siteId = siteId;
        this.path = path;
        this.content = content;
        this.code = code;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageEntity pageEntity = (PageEntity) o;
        return id.equals(pageEntity.id) && siteId.equals(pageEntity.siteId) &&
                code.equals(pageEntity.code) && Objects.equals(path, pageEntity.path) &&
                Objects.equals(content, pageEntity.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, siteId, path, code, content);
    }

    @Override
    public String toString() {
        return "PageEntity{" +
                "id=" + id +
                ", siteId=" + siteId +
                ", path='" + path + '\'' +
                ", code=" + code +
                ", content='" + content + '\'' +
                '}';
    }
}

