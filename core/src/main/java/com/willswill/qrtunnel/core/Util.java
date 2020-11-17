package com.willswill.qrtunnel.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * @author Will
 */
public class Util {
    public static byte[] intToBytes(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static int bytesToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip(); // need flip
        return buffer.getLong();
    }

    public static long getCrc32(File file) throws IOException {
        try (RandomAccessFile file1 = new RandomAccessFile(file, "r")) {
            return getCrc32(file1);
        }
    }

    public static long getCrc32(RandomAccessFile file) throws IOException {
        CRC32 crc32 = new CRC32();
        byte[] bytes = new byte[1024];
        file.seek(0);
        int len = file.read(bytes);
        while (len > 0) {
            crc32.update(bytes, 0, len);
            len = file.read(bytes);
        }
        return crc32.getValue();
    }
}
