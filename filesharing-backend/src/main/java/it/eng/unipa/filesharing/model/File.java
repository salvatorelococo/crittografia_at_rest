package it.eng.unipa.filesharing.model;

import javax.persistence.*;

@Entity
@Table (uniqueConstraints=@UniqueConstraint(columnNames={"parentUniqueId", "name"}))
public class File {
    @Id
    @SequenceGenerator(name="file_seq", initialValue=1, allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="file_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bucketId")
    private Bucket bucket;

    private String parentUniqueId;
    private String name;
    private byte[] hash;
    private byte[] salt;
    private byte[] iv;

    public File () {}

    public File(Bucket bucket, String parentUniqueId, String name, byte[] hash, byte[] salt, byte[] iv) {
        this.setBucket(bucket);
        this.setParentUniqueId(parentUniqueId);
        this.setName(name);
        this.setHash(hash);
        this.setSalt(salt);
        this.setIv(iv);
    }


    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public String getParentUniqueId() {
        return parentUniqueId;
    }

    public void setParentUniqueId(String parentUniqueId) {
        this.parentUniqueId = parentUniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
}