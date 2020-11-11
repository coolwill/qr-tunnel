package com.willswill.qrtunnel.core;

import java.awt.image.BufferedImage;

/**
 * @author 李伟
 */
public interface EncoderCallback {
    default void imageCreated(int num, BufferedImage image) {
    }

    default void fileBegin(FileInfo fileInfo) {
    }

    default void fileEnd(FileInfo fileInfo) {
    }
}
