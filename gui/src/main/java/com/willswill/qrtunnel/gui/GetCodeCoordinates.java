package com.willswill.qrtunnel.gui;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.detector.FinderPattern;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

public class GetCodeCoordinates {
    public static Rectangle getQrCodeCoordinates(BufferedImage image) throws ReaderException {
        QRCodeReader qrCodeReader = new QRCodeReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
        hints.put(DecodeHintType.TRY_HARDER, "true");

        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap binaryBmp = new BinaryBitmap(new HybridBinarizer(source));

        Result result = qrCodeReader.decode(binaryBmp, hints);
        int left = image.getWidth();
        int top = image.getHeight();
        int right = 0;
        int bottom = 0;

        for (ResultPoint point : result.getResultPoints()) {
            if (point instanceof FinderPattern) {
                FinderPattern fp = (FinderPattern) point;
                left = Math.min(left, (int) Math.round(point.getX() - 3.5 * fp.getEstimatedModuleSize()));
                top = Math.min(top, (int) Math.round(point.getY() - 3.5 * fp.getEstimatedModuleSize()));
                right = Math.max(right, (int) Math.round(point.getX() + 3.5 * fp.getEstimatedModuleSize()));
                bottom = Math.max(bottom, (int) Math.round(point.getY() + 3.5 * fp.getEstimatedModuleSize()));
            }
        }

        int width = right - left;
        int height = bottom - top;

        String[] split = result.getText().split("\\*");
        int targetWidth = Integer.parseInt(split[0]);
        int targetHeight = Integer.parseInt(split[1]);

        if (width != targetWidth) {
            int paddingLeft = (targetWidth - width) / 2;
            int paddingTop = (targetHeight - height) / 2;
            left -= paddingLeft;
            top -= paddingTop;
        }

        return new Rectangle(left, top, targetWidth, targetHeight);
    }
}
