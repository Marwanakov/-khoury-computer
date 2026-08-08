package com.khourycomputer.application.port.storage;

public enum ImageStorageFolder {

    CATEGORIES("categories"),
    PRODUCTS("products");

    private final String directoryName;

    ImageStorageFolder(String directoryName) {
        this.directoryName = directoryName;
    }

    public String directoryName() {
        return directoryName;
    }
}