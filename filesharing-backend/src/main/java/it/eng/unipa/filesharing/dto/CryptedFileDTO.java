package it.eng.unipa.filesharing.dto;

public class CryptedFileDTO {

    private byte[] content;
    private byte[] hash;
    private byte[] salt;
    private byte[] iv;

    public CryptedFileDTO(byte[] content, byte[] hash, byte[] salt, byte[] iv) {
        this.content = content;
        this.hash = hash;
        this.salt = salt;
        this.iv = iv;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
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
