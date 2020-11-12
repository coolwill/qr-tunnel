package com.willswill.qrtunnel.gui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.willswill.qrtunnel.core.Encoder;
import com.willswill.qrtunnel.core.EncoderCallback;
import com.willswill.qrtunnel.core.FileInfo;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 * @author 李伟
 */
@Slf4j
public class SenderForm {
    private JPanel panel1;
    private JButton startButton;
    private JButton chooseButton;
    private JPanel imagePanel;
    private JButton stopButton;
    private JProgressBar totalProgress;
    private JProgressBar fileProgress;
    private JLabel filenameLabel;
    private ImageView imageView;

    private JFrame frame;
    private Encoder encoder;
    private File selectedFile = null;
    private boolean running = false;

    private int totalFiles;
    private int fileIndex;
    private int totalImages;
    private int imageIndex;

    public static SenderForm create() {
        JFrame frame = new JFrame("SenderForm");
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);  // 窗体居中显示
        SenderForm form = new SenderForm();
        frame.setContentPane(form.panel1);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        form.initForm();

        frame.pack();
        frame.setVisible(true);
        form.frame = frame;
        return form;
    }

    public void show() {
        frame.setVisible(true);
    }

    public void initForm() {
        initEncoder();
        initFileChooser();
        initOtherComponents();
    }

    void initEncoder() {
        encoder = new Encoder(Launcher.getAppConfigs(), new EncoderCallback() {
            @Override
            public void imageCreated(int num, BufferedImage image) {
                try {
                    imageIndex = num;
                    updateProgress();
                    imageView.setImage(image);
                    Thread.sleep(Launcher.getAppConfigs().getSendInterval());
                } catch (Exception e) {
                    log.error("Error displaying image", e);
                }
            }

            @Override
            public void fileBegin(FileInfo fileInfo) {
                totalImages = fileInfo.getChunkCount();
                updateProgress();
                filenameLabel.setText(fileInfo.getFilename());
                Launcher.log("Sending file " + fileInfo.getFilename());
            }
        });
    }

    void initFileChooser() {
        chooseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(selectedFile == null ? new File("").getAbsoluteFile() : selectedFile.getParentFile());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setDialogTitle("选择文件");
            if (selectedFile != null) {
                fileChooser.setCurrentDirectory(selectedFile.getParentFile());
            }
            fileChooser.showDialog(new JLabel(), "选择");
            File file = fileChooser.getSelectedFile();
            if (file != null && file.exists()) {
                selectedFile = file;
                Launcher.getAppConfigs().setRootPath(selectedFile.getParent());
            }
        });
    }

    void initOtherComponents() {
        imageView = new ImageView(null, Launcher.getAppConfigs().getImageWidth(), Launcher.getAppConfigs().getImageWidth(), 0);
        imagePanel.add(imageView, BorderLayout.CENTER);

        startButton.addActionListener(e -> {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(panel1, "No file selected!");
                return;
            }
            processFileAsync(selectedFile);
        });

        stopButton.addActionListener(e -> {
            running = false;
            encoder.interrupt();
        });

        showRectImage();
    }

    void showRectImage() {
        try {
            String content = imageView.getImageWidth() + "*" + imageView.getImageHeight();
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, Launcher.getAppConfigs().getImageWidth(), Launcher.getAppConfigs().getImageWidth());
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            imageView.setImage(image);
        } catch (Exception e) {
            log.error("Can't create QR code", e);
        }
    }

    void processFileAsync(File selectedFile) {
        new Thread(() -> {
            chooseButton.setEnabled(false);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            frame.setAlwaysOnTop(true);
            try {
                processFile(selectedFile);
            } catch (Exception ex) {
                log.error("Error processing file", ex);
            }
            frame.setAlwaysOnTop(false);
            chooseButton.setEnabled(true);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }).start();
    }

    void processFile(File selectedFile) {
        if (!selectedFile.exists()) {
            log.error("selected file is not exist: " + selectedFile.getAbsolutePath());
            return;
        }

        if (selectedFile.isFile()) {
            totalFiles = 1;
            fileIndex = 1;
            updateProgress();
            try {
                encoder.encode(selectedFile);
            } catch (Exception e) {
                log.error("Error processing file", e);
            }
        } else if (selectedFile.isDirectory()) {
            java.util.List<File> files = listFiles(selectedFile);
            totalFiles = files.size();
            running = true;
            for (int i = 0; i < files.size(); i++) {
                if (!running) {
                    break;
                }
                fileIndex = i + 1;
                updateProgress();
                File file = files.get(i);
                try {
                    encoder.encode(file);
                } catch (Exception e) {
                    log.error("Error processing file", e);
                }
            }
        }
        resetProgress();
        showRectImage();
    }

    java.util.List<File> listFiles(File dir) {
        java.util.List<File> fileList = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                fileList.add(file);
            } else if (file.isDirectory()) {
                fileList.addAll(listFiles(file));
            }
        }
        return fileList;
    }

    void updateProgress() {
        totalProgress.setMaximum(totalFiles);
        totalProgress.setValue(fileIndex);
        fileProgress.setMaximum(totalImages);
        fileProgress.setValue(imageIndex);
    }

    void resetProgress() {
        totalFiles = 0;
        fileIndex = 0;
        totalImages = 0;
        imageIndex = 0;
        filenameLabel.setText("");
        updateProgress();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        panel1.setMinimumSize(new Dimension(444, 400));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panel2.add(panel3, BorderLayout.WEST);
        chooseButton = new JButton();
        chooseButton.setText("Choose File");
        panel3.add(chooseButton);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel2.add(panel4, BorderLayout.CENTER);
        totalProgress = new JProgressBar();
        totalProgress.setForeground(new Color(-11768192));
        totalProgress.setPreferredSize(new Dimension(146, 6));
        panel4.add(totalProgress, BorderLayout.NORTH);
        fileProgress = new JProgressBar();
        fileProgress.setForeground(new Color(-11768192));
        fileProgress.setPreferredSize(new Dimension(146, 6));
        panel4.add(fileProgress, BorderLayout.CENTER);
        filenameLabel = new JLabel();
        filenameLabel.setHorizontalAlignment(0);
        filenameLabel.setPreferredSize(new Dimension(0, 17));
        panel4.add(filenameLabel, BorderLayout.SOUTH);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panel2.add(panel5, BorderLayout.EAST);
        startButton = new JButton();
        startButton.setText("Start");
        panel5.add(startButton);
        stopButton = new JButton();
        stopButton.setText("Stop");
        panel5.add(stopButton);
        imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout(0, 0));
        panel1.add(imagePanel, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
