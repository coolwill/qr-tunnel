package com.willswill.qrtunnel;

import com.willswill.qrtunnel.core.AppConfigs;
import com.willswill.qrtunnel.core.Decoder;
import com.willswill.qrtunnel.core.Encoder;
import com.willswill.qrtunnel.core.EncoderCallback;

import java.awt.image.BufferedImage;
import java.io.File;

public class Test {
    public static void main(String[] args) {
        test2();
    }

    public static void test2() {
        AppConfigs appConfigs = new AppConfigs();
        Decoder decoder = new Decoder(appConfigs, null);

        Encoder encoder = new Encoder(appConfigs, new EncoderCallback() {
            @Override
            public void imageCreated(int num, BufferedImage image) {
                try {
                    decoder.decode(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        File file = new File("D:\\Temp\\1.jpg");
        try {
            encoder.encode(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
