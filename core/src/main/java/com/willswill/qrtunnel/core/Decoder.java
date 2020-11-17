package com.willswill.qrtunnel.core;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * @author Will
 */
@Slf4j
public class Decoder {
    private final QRCodeReader qrCodeReader;
    private final Map<DecodeHintType, Object> hints;

    private final AppConfigs appConfigs;
    private final DecoderCallback callback;

    private FileInfo fileInfo;
    private RandomAccessFile dataFile;
    private RandomAccessFile configFile;
    private File configFileFile;
    private byte[] receivedFlags;

    public Decoder(AppConfigs appConfigs, DecoderCallback callback) {
        this.appConfigs = appConfigs;
        this.callback = callback;

        qrCodeReader = new QRCodeReader();
        hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
        hints.put(DecodeHintType.PURE_BARCODE, "true");
    }

    public int decode(BufferedImage image, int lastNonce) throws ReaderException, IOException {
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap binaryBmp = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = qrCodeReader.decode(binaryBmp, hints);
            byte[] buf = result.getText().getBytes(StandardCharsets.ISO_8859_1);
            return decode(buf, lastNonce);
        } finally {
            qrCodeReader.reset();
        }
    }

    public int decode(byte[] buf, int lastNonce) throws IOException {
        if (buf.length < 20) {
            return 0;
        }

        long crc = Util.bytesToLong(Arrays.copyOfRange(buf, 0, 8));
        int nonce = Util.bytesToInt(Arrays.copyOfRange(buf, 8, 12));
        int type = Util.bytesToInt(Arrays.copyOfRange(buf, 12, 16));

        if (nonce == lastNonce) {
            return nonce;
        }

        CRC32 crc32 = new CRC32();
        crc32.update(buf, 8, buf.length - 8);
        if (crc != crc32.getValue()) {
            throw new DecodeException("Chunk CRC32 is not match!");
        }

        int version = type & 0xffff0000;
        type = type & 0xffff;
        if (version == Const.VERSION_1) {
            int num = Util.bytesToInt(Arrays.copyOfRange(buf, 16, 20));
            int len = buf.length - 20;

            log.info("Received chunk " + num);

            if (type == Const.TYPE_FILE) {
                byte[] bFileInfo = Arrays.copyOfRange(buf, 20, len + 20);
                FileInfo fileInfo = FileInfo.deserialize(bFileInfo);
                if (this.fileInfo == null || !this.fileInfo.equals(fileInfo)) {
                    reset();
                    this.fileInfo = fileInfo;
                    beginFile();
                }
            } else if (type == Const.TYPE_DATA) {
                if (fileInfo == null) {
                    throw new DecodeException("File info is missing!");
                }

                writeFilePart(num, buf, 20, len);
            } else {
                throw new DecodeException("Type " + type + " is not supported!");
            }
        } else {
            throw new DecodeException("Version " + version + " is not supported!");
        }
        return nonce;
    }

    void beginFile() throws IOException {
        log.info("Begin file " + fileInfo.getFilename());
        File dir;
        if (fileInfo.getPath() == null || fileInfo.getPath().length() == 0 || fileInfo.getPath().equals("/")) {
            dir = new File(appConfigs.getSaveDir());
        } else {
            dir = new File(appConfigs.getSaveDir(), fileInfo.getPath());
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        dataFile = new RandomAccessFile(new File(dir, fileInfo.getFilename()), "rw");
        dataFile.setLength(fileInfo.getLength());

        configFileFile = new File(dir, fileInfo.getFilename() + ".cfg");
        configFile = new RandomAccessFile(configFileFile, "rw");
        configFile.setLength(fileInfo.getChunkCount());

        receivedFlags = new byte[fileInfo.getChunkCount()];

        if (callback != null) {
            callback.fileBegin(fileInfo);
        }
    }

    synchronized void writeFilePart(int num, byte[] buf, int offset, int len) throws IOException {
        int pos = (num - 1) * fileInfo.getChunkSize();
        dataFile.seek(pos);
        dataFile.write(buf, offset, len);

        configFile.seek(num - 1);
        configFile.writeByte(1);

        receivedFlags[num - 1] = 1;

        if (callback != null) {
            callback.imageReceived(num);
        }

        // check if file is completed
        if (checkFileEnd()) {
            endFile();
        }
    }

    boolean checkFileEnd() {
        for (byte receivedFlag : receivedFlags) {
            if (receivedFlag == 0) {
                return false;
            }
        }
        return true;
    }

    void endFile() throws IOException {
        log.info("End file " + fileInfo.getFilename());

        // check crc32
        long crc32 = Util.getCrc32(dataFile);
        if (crc32 != fileInfo.getCrc32()) {
            log.error("File CRC32 is not match! Desired:" + fileInfo.getCrc32() + ", Received:" + crc32);
        }

        dataFile.close();
        configFile.close();
        configFileFile.delete();

        if (callback != null) {
            callback.fileEnd(fileInfo, crc32 == fileInfo.getCrc32());
        }

        reset();
    }

    public void reset() throws IOException {
        fileInfo = null;
        if (dataFile != null) {
            dataFile.close();
            dataFile = null;
        }
        if (configFile != null) {
            configFile.close();
            configFile = null;
        }
    }
}
