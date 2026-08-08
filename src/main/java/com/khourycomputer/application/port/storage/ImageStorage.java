package com.khourycomputer.application.port.storage;

public interface ImageStorage {

    String store(
            ImageUpload image,
            ImageStorageFolder folder
    );

    void delete(String imageUrl);
}