package searchenginepackage.model;

import lombok.Data;

import javax.persistence.*;

@Table(name = "index")
@Data
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "page_id", nullable = false)
    private int pageId;
    @Column(name = "lemma", nullable = false)
    private int lemmaId;
    @Column(name = "rank", nullable = false)
    private float rank;
    public IndexEntity(Integer pageId, Integer lemmaId, float rank) {
        this.pageId = pageId;
        this.lemmaId = lemmaId;
    }
}
