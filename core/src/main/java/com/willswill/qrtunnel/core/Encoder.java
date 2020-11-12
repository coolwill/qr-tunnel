package com.willswill.qrtunnel.core;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * @author 李伟
 */
@Slf4j
@Data
public class Encoder {
    private byte[] buf;
    private CRC32 crc32;
    private Random random;
    private QRCodeWriter qrCodeWriter;
    private Map<EncodeHintType, Object> hints;

    private AppConfigs appConfigs;
    private EncoderCallback callback;
    private boolean running;

    public Encoder(AppConfigs appConfigs, EncoderCallback callback) {
        this.appConfigs = appConfigs;
        this.callback = callback;

        buf = new byte[appConfigs.getChunkSize() + 20];
        crc32 = new CRC32();
        random = new Random();
        qrCodeWriter = new QRCodeWriter();
        hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
    }

    public void encode(File file) throws IOException, WriterException {
        log.info("begin file " + file.getAbsolutePath());
        FileInfo fileInfo = new FileInfo(
                appConfigs.getRootPath() == null ? "/" : file.getParentFile().getAbsolutePath().substring(appConfigs.getRootPath().length()),
                file.getName(),
                file.length(),
                appConfigs.getChunkSize(),
                (int) Math.ceil(file.length() / (double) appConfigs.getChunkSize()));

        if (callback != null) {
            callback.fileBegin(fileInfo);
        }

        // file info
        byte[] bFileInfo = FileInfo.serialize(fileInfo);
        if (bFileInfo.length > appConfigs.getChunkSize()) {
            throw new IOException("File name or path is too long!");
        }
        System.arraycopy(bFileInfo, 0, buf, 20, bFileInfo.length);
        encode(Const.VERSION_1 | Const.TYPE_FILE, 0, bFileInfo.length);

        // file content
        try (InputStream inputStream = new FileInputStream(file)) {
            running = true;
            int num = 1;
            while (running && inputStream.available() > 0) {
                int len = inputStream.read(buf, 20, appConfigs.getChunkSize());
                if (len > 0) {
                    log.info("Send picture " + (num));

                    encode(Const.VERSION_1 | Const.TYPE_DATA, num, len);
                }
                num++;
            }
            running = false;
        }

        // file end
        if (callback != null) {
            callback.fileEnd(fileInfo);
        }
    }

    // crc/nonce/type/num
    // 8  /4    /4   /4  /4
    private void encode(int type, int num, int len) throws WriterException {
        // nonce
        System.arraycopy(Util.intToBytes(random.nextInt()), 0, buf, 8, 4);
        // type
        System.arraycopy(Util.intToBytes(type), 0, buf, 12, 4);
        // num
        System.arraycopy(Util.intToBytes(num), 0, buf, 16, 4);

        // crc
        crc32.reset();
        crc32.update(buf, 8, len + 12);
        System.arraycopy(Util.longToBytes(crc32.getValue()), 0, buf, 0, 8);

        // generate image
        String content = new String(buf, 0, len + 20, StandardCharsets.ISO_8859_1);
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, appConfigs.getImageWidth(), appConfigs.getImageHeight(), hints);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // callback
        if (callback != null) {
            callback.imageCreated(num, image);
        }
    }

    public void interrupt() {
        running = false;
    }
}
