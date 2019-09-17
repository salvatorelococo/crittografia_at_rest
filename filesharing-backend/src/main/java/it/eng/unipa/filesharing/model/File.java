package it.eng.unipa.filesharing.model;

import javax.persistence.*;

@Entity
public class File {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uuid")
    private Team team;

    private String name;
    private String description;

    // TODO: Converter

    public File () {}
}
