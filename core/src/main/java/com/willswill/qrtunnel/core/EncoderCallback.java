package com.willswill.qrtunnel.core;

import java.awt.image.BufferedImage;

/**
 * @author Will
 */
public interface EncoderCallback {
    default void imageCreated(int num, BufferedImage image) {
    }

    default void fileBegin(FileInfo fileInfo) {
    }

    default void fileEnd(FileInfo fileInfo) {
    }
}
