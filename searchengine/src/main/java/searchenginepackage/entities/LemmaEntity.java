package searchenginepackage.entities;

import lombok.Data;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lemma")
@Data
public class LemmaEntity {
    public LemmaEntity(){}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;
    @Column(name = "frequency", nullable = false)
    private Integer frequency;
    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexEntity> indices = new ArrayList<>();
    public LemmaEntity(String lemma, SiteEntity site, Integer frequency) {
        this.lemma = lemma;
        this.site = site;
        this.frequency = frequency;
    }
}

