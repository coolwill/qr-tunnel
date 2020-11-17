package com.willswill.qrtunnel.core;

/**
 * @author Will
 */
public interface DecoderCallback {
    default void imageReceived(int num) {
    }

    default void fileBegin(FileInfo fileInfo) {
    }

    default void fileEnd(FileInfo fileInfo, boolean crc32Matches) {
    }
}
