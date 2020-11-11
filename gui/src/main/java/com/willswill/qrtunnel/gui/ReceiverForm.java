package com.willswill.qrtunnel.gui;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.willswill.qrtunnel.core.DecodeException;
import com.willswill.qrtunnel.core.Decoder;
import com.willswill.qrtunnel.core.DecoderCallback;
import com.willswill.qrtunnel.core.FileInfo;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.Collectors;

/**
 * @author 李伟
 */
@Slf4j
public class ReceiverForm {
    private final RingBuffer<String> logBuf = new RingBuffer<>(200);
    boolean running = false;
    private JPanel panel1;
    private JButton startButton;
    private JTextArea logView;
    private JButton stopButton;
    private JButton senderButton;
    private JProgressBar fileProgress;
    private JLabel filenameLabel;
    private JFrame frame;
    private Decoder decoder;
    private Rectangle captureRect;
    private int totalImages;
    private int imageIndex;

    public static ReceiverForm create() {
        JFrame frame = new JFrame("ReceiverForm");
        ReceiverForm form = new ReceiverForm();
        frame.setContentPane(form.panel1);
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        form.initComponents();

        frame.pack();
        frame.setVisible(true);
        form.frame = frame;
        return form;
    }

    public void show() {
        frame.setVisible(true);
    }

    void initComponents() {
        startButton.addActionListener(e -> {
            try {
                detectCaptureRect();
            } catch (ReaderException ex) {
                JOptionPane.showMessageDialog(panel1, "Failed to detect capture rect!");
                return;
            } catch (Exception ex) {
                log.error("Failed to capture screenshot!", ex);
                JOptionPane.showMessageDialog(panel1, "Failed to capture screenshot!");
                return;
            }

            startCaptureAsync();
        });

        stopButton.addActionListener(e -> {
            running = false;
        });

        senderButton.addActionListener(e -> {
            Launcher.self.showSenderForm();
        });

        DefaultCaret caret = (DefaultCaret) logView.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    void detectCaptureRect() throws ReaderException, AWTException {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(new Rectangle(0, 0, d.width, d.height));
        Rectangle rectangle = GetCodeCoordinates.getQrCodeCoordinates(image);
        log.info(rectangle.toString());
        Launcher.log("Capture rect is set to " + rectangle.x + "," + rectangle.y + " " + rectangle.width + "*" + rectangle.height);
        captureRect = rectangle;
    }

    void startCaptureAsync() {
        decoder = new Decoder(Launcher.getAppConfigs(), new DecoderCallback() {
            @Override
            public void imageReceived(int num) {
                imageIndex = num;
                updateProgress();
            }

            @Override
            public void fileBegin(FileInfo fileInfo) {
                totalImages = fileInfo.getDataCount();
                filenameLabel.setText(fileInfo.getFilename());
                updateProgress();
                Launcher.log("Receiving file " + fileInfo.getFilename());
            }

            @Override
            public void fileEnd(FileInfo fileInfo) {
                resetProgress();
            }
        });

        new Thread(() -> {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            try {
                startCapture();
            } catch (Exception e) {
                log.error("Capture failed", e);
            }
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }).start();
    }

    void startCapture() throws Exception {
        Robot robot = new Robot();
        BufferedImage image;
        running = true;
        while (running) {
            image = robot.createScreenCapture(captureRect);
            try {
                decoder.decode(image);
            } catch (NotFoundException | FormatException | ChecksumException ignore) {
            } catch (DecodeException e) {
                log.error("Decode failed: " + e.getMessage());
            } catch (Exception e) {
                log.error("Decode failed!", e);
            }
        }
    }

    void updateProgress() {
        fileProgress.setMaximum(totalImages);
        fileProgress.setValue(imageIndex);
    }

    void resetProgress() {
        totalImages = 0;
        imageIndex = 0;
        filenameLabel.setText("");
        updateProgress();
    }

    public void addLog(String s) {
        logBuf.put(s);
        String text = logBuf.readFully().stream().collect(Collectors.joining("\n"));
        logView.setText(text);
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
        panel1.setPreferredSize(new Dimension(400, 200));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel2.add(panel3, BorderLayout.WEST);
        startButton = new JButton();
        startButton.setText("Start");
        panel3.add(startButton);
        stopButton = new JButton();
        stopButton.setEnabled(false);
        stopButton.setText("Stop");
        panel3.add(stopButton);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel2.add(panel4, BorderLayout.CENTER);
        fileProgress = new JProgressBar();
        fileProgress.setPreferredSize(new Dimension(146, 6));
        panel4.add(fileProgress, BorderLayout.NORTH);
        filenameLabel = new JLabel();
        filenameLabel.setHorizontalAlignment(0);
        filenameLabel.setPreferredSize(new Dimension(0, 17));
        filenameLabel.setText("");
        panel4.add(filenameLabel, BorderLayout.CENTER);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel2.add(panel5, BorderLayout.EAST);
        senderButton = new JButton();
        senderButton.setText("Sender");
        panel5.add(senderButton);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        panel1.add(scrollPane1, BorderLayout.CENTER);
        logView = new JTextArea();
        logView.setEditable(false);
        logView.setMargin(new Insets(0, 5, 0, 0));
        logView.setRows(10);
        scrollPane1.setViewportView(logView);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
