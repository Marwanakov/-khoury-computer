package com.khourycomputer.application.port.storage;

public enum ImageStorageFolder {

    CATEGORIES("categories"),
    PRODUCTS("products"),
    HERO_SLIDES("hero-slides");

    private final String directoryName;

    ImageStorageFolder(String directoryName) {
        this.directoryName = directoryName;
    }

    public String directoryName() {
        return directoryName;
    }
}