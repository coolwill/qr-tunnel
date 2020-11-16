package com.willswill.qrtunnel.gui;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.detector.FinderPattern;
import com.willswill.qrtunnel.core.DecodeException;
import lombok.AllArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

public class GetCodeCoordinates {
    public static Layout detect(BufferedImage image) throws ReaderException, DecodeException {
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

        String[] split = result.getText().split("/");
        int num = Integer.parseInt(split[0]);
        String[] split1 = split[1].split("\\*");
        String[] split2 = split[2].split("\\*");
        int targetWidth = Integer.parseInt(split2[0]);
        int targetHeight = Integer.parseInt(split2[1]);

        if (width != targetWidth) {
            int paddingLeft = (targetWidth - width) / 2;
            int paddingTop = (targetHeight - height) / 2;
            left -= paddingLeft;
            top -= paddingTop;
        }

        int rows = Integer.parseInt(split1[0]);
        int cols = Integer.parseInt(split1[1]);
        int index = num - 1;
        int rowIndex = index / cols;
        int colIndex = index % cols;

        int rect0Left = left - targetWidth * colIndex;
        int rect0Top = top - targetHeight * rowIndex;

        // check
        if (rect0Left + targetWidth * cols > image.getWidth() || rect0Top + targetHeight * rows > image.getHeight()) {
            throw new DecodeException("Capture rect is out of screen");
        }

        return new Layout(rect0Left, rect0Top, targetWidth, targetHeight, rows, cols);
    }

    @AllArgsConstructor
    public static class Layout {
        public int left;
        public int top;
        public int width;
        public int height;
        public int rows;
        public int cols;
    }
}
