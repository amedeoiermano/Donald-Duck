package com.xayris.donalduck.data.entities;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Story extends RealmObject {

    @PrimaryKey
    private String code;
    private String title;
    private boolean read;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getIsRead() {
        return read;
    }

    public void setIsRead(boolean read) {
        this.read = read;
    }

}
