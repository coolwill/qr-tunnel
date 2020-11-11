package com.willswill.qrtunnel.gui;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author 李伟
 */
public class ImageView extends JPanel {
    @Getter
    private final int margin;
    @Getter
    private final int imageWidth;
    @Getter
    private final int imageHeight;
    private BufferedImage image;

    public ImageView() {
        this(null, 500, 500, 0);
    }

    public ImageView(BufferedImage image, int imageWidth, int imageHeight, int margin) {
        this.image = image;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.margin = margin;
        setLayout(null);
        setPreferredSize(new Dimension(imageWidth + margin * 2, imageHeight + margin * 2));
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g1) {
        if (this.image == null) {
            return;
        }

        g1.drawImage(image, margin, margin, image.getWidth(), image.getHeight(), null);
    }
}