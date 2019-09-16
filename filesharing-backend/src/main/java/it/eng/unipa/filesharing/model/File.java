package it.eng.unipa.filesharing.model;

import javax.persistence.*;

@Entity
public class File {
    @Id
    @SequenceGenerator(name="file_seq", initialValue=1, allocationSize=1)
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="file_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uuid")
    private Team team;

    private String name;
    private String description;

    // TODO: Converter

    public File () {}
}
