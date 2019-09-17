package it.eng.unipa.filesharing.model;

import javax.persistence.*;

@Entity
@Table (uniqueConstraints=@UniqueConstraint(columnNames={"parentUniqueId", "name"}))
public class File {
    @Lob
    @Id
    private byte[] hash;

    @ManyToOne
    @JoinColumn(name = "bucketId")
    private Bucket bucket;

    private String parentUniqueId;
    private String name;
    private boolean crypted;
    private String password;

    public File () {}

    // Costruttore File senza password
    public File (byte[] hash, Bucket bucket, String parentUniqueId, String name) {
        this(hash, bucket, parentUniqueId, name, false, null);
    }

    // Costruttore File con password
    public File (byte[] hash, Bucket bucket, String parentUniqueId, String name, String password) {
        this(hash, bucket, parentUniqueId, name, true, password);
    }

    // Costruttore generale
    private File(byte[] hash, Bucket bucket, String parentUniqueId, String name, boolean crypted, String password) {
        this.setHash(hash);
        this.setBucket(bucket);
        this.setParentUniqueId(parentUniqueId);
        this.setName(name);
        this.setCrypted(crypted);
        this.setPassword(password);
    }

    public byte[] getHash() {
        return hash;
    }

    private void setHash(byte[] hash) {
        this.hash = hash;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCrypted() {
        return crypted;
    }

    private void setCrypted(boolean crypted) {
        this.crypted = crypted;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public String getParentUniqueId() {
        return parentUniqueId;
    }

    private void setParentUniqueId(String parentUniqueId) {
        this.parentUniqueId = parentUniqueId;
    }

}