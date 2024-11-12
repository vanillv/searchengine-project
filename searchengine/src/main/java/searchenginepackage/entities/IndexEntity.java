package searchenginepackage.entities;
import lombok.Data;
import jakarta.persistence.*;
@Entity
@Table(name = "page_index")
@Data
public class IndexEntity {
    public IndexEntity(){}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity page;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", nullable = false)
    private LemmaEntity lemma;
    @Column(name = "rank_score", nullable = false)
    private float rankScore;
    public IndexEntity(PageEntity page, LemmaEntity lemma, float rankScore) {
        this.page = page;
        this.lemma = lemma;
        this.rankScore = rankScore; // Ensure to assign rank as well
    }
}

