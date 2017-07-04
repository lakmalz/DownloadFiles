package lk.lakmalz.downloadfiles.models;

import java.io.Serializable;

/**
 * Created by A Lakmal Weerasekara (Lakmalz) on 28/6/17.
 * alrweerasekara@gmail.com
 */

public class DownloadFile implements Serializable {

    String fileType;
    String url;
    String savedPath;
    int id;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSavedPath() {
        return savedPath;
    }

    public void setSavedPath(String savedPath) {
        this.savedPath = savedPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
