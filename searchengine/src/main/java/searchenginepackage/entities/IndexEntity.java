package searchenginepackage.entities;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "page_index")
@Data
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "page_id", nullable = false)
    private int pageId;
    @Column(name = "lemma_id", nullable = false)
    private int lemmaId;
    @Column(name = "rank", nullable = false)
    private float rank;
    public IndexEntity(int pageId, int lemmaId, float rank) {
        this.pageId = pageId;
        this.lemmaId = lemmaId;
        this.rank = rank; // Ensure to assign rank as well
    }
}

