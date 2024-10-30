package searchenginepackage.entities;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "lemma")
@Data
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;
    @Column(name = "site_id", nullable = false)
    private Integer siteId;
    @Column(name = "frequency", nullable = false)
    private Integer frequency;
    public LemmaEntity(String lemma, Integer siteId, Integer frequency) {
        this.lemma = lemma;
        this.siteId = siteId;
        this.frequency = frequency;
    }
}

