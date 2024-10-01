package com.plantpal.model;

import java.time.LocalDateTime;

public class PhotoLog_Model {
    private final String photoPath;
    private final LocalDateTime dateTaken;

    public PhotoLog_Model(String photoPath, LocalDateTime dateTaken) {
        this.photoPath = photoPath;
        this.dateTaken = dateTaken;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }
}
