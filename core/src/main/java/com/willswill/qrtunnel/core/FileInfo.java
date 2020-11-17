package com.willswill.qrtunnel.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.*;

/**
 * @author Will
 */
@Data
@AllArgsConstructor
public class FileInfo {
    private String path;
    private String filename;
    private long length;
    private long crc32;
    private int chunkSize;
    private int chunkCount;

    public static byte[] serialize(FileInfo fileInfo) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeUTF(fileInfo.path);
            oos.writeUTF(fileInfo.filename);
            oos.writeLong(fileInfo.length);
            oos.writeLong(fileInfo.crc32);
            oos.writeInt(fileInfo.chunkSize);
            oos.writeInt(fileInfo.chunkCount);
            oos.flush();
            return outputStream.toByteArray();
        }
    }

    public static FileInfo deserialize(byte[] bytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            return new FileInfo(ois.readUTF(), ois.readUTF(), ois.readLong(), ois.readLong(), ois.readInt(), ois.readInt());
        }
    }
}
