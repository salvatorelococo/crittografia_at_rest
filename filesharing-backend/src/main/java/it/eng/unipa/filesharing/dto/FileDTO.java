package it.eng.unipa.filesharing.dto;

public class FileDTO {

    private String name;
    private String folderUniqueId;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getFolderUniqueId() {
        return folderUniqueId;
    }
    public void setFolderUniqueId(String folderUniqueId) {
        this.folderUniqueId = folderUniqueId;
    }
}
