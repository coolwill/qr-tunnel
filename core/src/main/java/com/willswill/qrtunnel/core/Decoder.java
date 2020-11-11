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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.zip.CRC32;

@Slf4j
public class Decoder {
    private final QRCodeReader qrCodeReader;
    private final Map<DecodeHintType, Object> hints;

    private final AppConfigs appConfigs;
    private final DecoderCallback callback;

    private FileInfo fileInfo;
    private ByteArrayOutputStream outputStream;
    private int lastNum = -1;
    private int lastNonce = -1;

    public Decoder(AppConfigs appConfigs, DecoderCallback callback) {
        this.appConfigs = appConfigs;
        this.callback = callback;

        qrCodeReader = new QRCodeReader();
        hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
        hints.put(DecodeHintType.PURE_BARCODE, "true");
    }

    // crc/nonce/type/num
    // 8  /4    /4   /4  /4
    public void decode(BufferedImage image) throws ReaderException, IOException {
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap binaryBmp = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = qrCodeReader.decode(binaryBmp, hints);
            byte[] buf = result.getText().getBytes(StandardCharsets.ISO_8859_1);
            decode(buf);
        } finally {
            qrCodeReader.reset();
        }
    }

    public void decode(byte[] buf) throws IOException {
        if (buf.length < 20) {
            return;
        }
        long crc = Util.bytesToLong(Arrays.copyOfRange(buf, 0, 8));
        int nonce = Util.bytesToInt(Arrays.copyOfRange(buf, 8, 12));
        int type = Util.bytesToInt(Arrays.copyOfRange(buf, 12, 16));

        CRC32 crc32 = new CRC32();
        crc32.update(buf, 8, buf.length - 8);
        if (crc != crc32.getValue()) {
            throw new DecodeException("CRC mismatch!");
        }

        if (nonce == lastNonce) {
            return;
        }
        lastNonce = nonce;

        int version = type & 0xffff0000;
        type = type & 0xffff;
        if (version == Const.VERSION_1) {
            int num = Util.bytesToInt(Arrays.copyOfRange(buf, 16, 20));
            int len = buf.length - 20;

            log.info("Received picture " + num);

            if (type == Const.TYPE_FILE) {
                byte[] bFileInfo = Arrays.copyOfRange(buf, 20, len + 20);
                fileInfo = FileInfo.deserialize(bFileInfo);
                lastNum = 0;
                outputStream = new ByteArrayOutputStream();
                log.info("Begin file " + fileInfo.getFilename());

                if (callback != null) {
                    callback.fileBegin(fileInfo);
                }
            } else if (type == Const.TYPE_DATA) {
                if (fileInfo == null) {
                    throw new DecodeException("File info is missing!");
                }

                if (num != lastNum + 1) {
                    throw new DecodeException("Num mismatch! Desired: " + (lastNum + 1) + ", Received: " + num);
                }
                lastNum = num;

                outputStream.write(buf, 20, len);
                if (num == fileInfo.getDataCount()) {
                    if (callback != null) {
                        callback.fileEnd(fileInfo);
                    }
                    log.info("Save file " + fileInfo.getFilename());
                    saveFile();
                } else {
                    if (callback != null) {
                        callback.imageReceived(num);
                    }
                }
            } else {
                throw new DecodeException("Type " + type + " is not supported!");
            }
        } else {
            throw new DecodeException("Version " + version + " is not supported!");
        }
    }

    void saveFile() throws IOException {
        File dir;
        if (fileInfo.getPath() == null || fileInfo.getPath().length() == 0 || fileInfo.getPath().equals("/")) {
            dir = new File(appConfigs.getSaveDir());
        } else {
            dir = new File(appConfigs.getSaveDir(), fileInfo.getPath());
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileInfo.getFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(outputStream.toByteArray());
        }
    }

    public void reset() {
        lastNum = -1;
        lastNonce = -1;
    }
}
