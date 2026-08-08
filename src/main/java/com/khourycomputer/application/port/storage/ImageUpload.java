package com.khourycomputer.application.port.storage;

import java.util.Arrays;

public final class ImageUpload {

    private static final ImageUpload EMPTY =
            new ImageUpload(new byte[0]);

    private final byte[] content;

    public ImageUpload(byte[] content) {
        this.content = content == null
                ? new byte[0]
                : Arrays.copyOf(content, content.length);
    }

    public static ImageUpload empty() {
        return EMPTY;
    }

    public boolean isPresent() {
        return content.length > 0;
    }

    public long size() {
        return content.length;
    }

    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}