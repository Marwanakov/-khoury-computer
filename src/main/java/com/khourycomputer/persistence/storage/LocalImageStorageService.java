package com.khourycomputer.persistence.storage;

import com.khourycomputer.application.exception.InvalidImageException;
import com.khourycomputer.application.port.storage.ImageStorage;
import com.khourycomputer.application.port.storage.ImageStorageFolder;
import com.khourycomputer.application.port.storage.ImageUpload;
import com.khourycomputer.config.storage.ImageStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Component
public class LocalImageStorageService
        implements ImageStorage {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    LocalImageStorageService.class
            );

    private static final String PUBLIC_PREFIX =
            "/uploads/";

    private final ImageStorageProperties properties;
    private final Path uploadRoot;

    public LocalImageStorageService(
            ImageStorageProperties properties
    ) {
        this.properties = properties;

        this.uploadRoot = Path.of(
                properties.getDirectory()
        )
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String store(
            ImageUpload image,
            ImageStorageFolder folder
    ) {
        if (image == null || !image.isPresent()) {
            throw new InvalidImageException(
                    "Please select an image to upload."
            );
        }

        if (folder == null) {
            throw new InvalidImageException(
                    "Image storage folder is required."
            );
        }

        if (image.size() > properties.getMaxImageSize()) {
            throw new InvalidImageException(
                    "Image size cannot exceed 5 MB."
            );
        }

        byte[] content = image.content();

        ImageType imageType =
                detectImageType(content);

        Path targetDirectory = uploadRoot
                .resolve(folder.directoryName())
                .normalize();

        if (!targetDirectory.startsWith(uploadRoot)) {
            throw new InvalidImageException(
                    "Invalid image storage location."
            );
        }

        String generatedFilename =
                UUID.randomUUID()
                        + imageType.extension();

        Path targetFile = targetDirectory
                .resolve(generatedFilename)
                .normalize();

        if (!targetFile.startsWith(targetDirectory)) {
            throw new InvalidImageException(
                    "Invalid image filename."
            );
        }

        try {
            Files.createDirectories(targetDirectory);

            Files.write(
                    targetFile,
                    content,
                    StandardOpenOption.CREATE_NEW
            );

        } catch (IOException exception) {
            throw new InvalidImageException(
                    "The image could not be saved. Please try again.",
                    exception
            );
        }

        return PUBLIC_PREFIX
                + folder.directoryName()
                + "/"
                + generatedFilename;
    }

    /*
     * Deleting an unused image is best-effort.
     *
     * The database operation should not appear to fail merely because
     * an old file could not be removed. Cleanup failures are logged.
     */
    @Override
    public void delete(String imageUrl) {
        if (imageUrl == null
                || imageUrl.isBlank()
                || !imageUrl.startsWith(PUBLIC_PREFIX)) {
            return;
        }

        String relativePath = imageUrl.substring(
                PUBLIC_PREFIX.length()
        );

        Path targetFile = uploadRoot
                .resolve(relativePath)
                .normalize();

        if (!targetFile.startsWith(uploadRoot)) {
            LOGGER.warn(
                    "Refused to delete image outside upload root: {}",
                    imageUrl
            );
            return;
        }

        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException exception) {
            LOGGER.warn(
                    "Could not delete unused image: {}",
                    targetFile,
                    exception
            );
        }
    }

    private ImageType detectImageType(byte[] content) {
        if (isJpeg(content)) {
            return ImageType.JPEG;
        }

        if (isPng(content)) {
            return ImageType.PNG;
        }

        if (isWebp(content)) {
            return ImageType.WEBP;
        }

        throw new InvalidImageException(
                "Only JPG, PNG, and WebP images are allowed."
        );
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && unsigned(bytes[0]) == 0xFF
                && unsigned(bytes[1]) == 0xD8
                && unsigned(bytes[2]) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length >= 8
                && unsigned(bytes[0]) == 0x89
                && unsigned(bytes[1]) == 0x50
                && unsigned(bytes[2]) == 0x4E
                && unsigned(bytes[3]) == 0x47
                && unsigned(bytes[4]) == 0x0D
                && unsigned(bytes[5]) == 0x0A
                && unsigned(bytes[6]) == 0x1A
                && unsigned(bytes[7]) == 0x0A;
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P';
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private enum ImageType {

        JPEG(".jpg"),
        PNG(".png"),
        WEBP(".webp");

        private final String extension;

        ImageType(String extension) {
            this.extension =
                    extension.toLowerCase(Locale.ROOT);
        }

        public String extension() {
            return extension;
        }
    }
}