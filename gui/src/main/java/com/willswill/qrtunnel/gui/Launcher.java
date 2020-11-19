package com.willswill.qrtunnel.gui;

import com.willswill.qrtunnel.core.AppConfigs;
import lombok.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Will
 */
@Data
public class Launcher {
    public static final Launcher self = new Launcher();

    private AppConfigs appConfigs;
    private ReceiverForm receiverForm;
    private SenderForm senderForm;
    private ConfigsForm configsForm;

    public static void main(String[] args) {
        self.loadConfigs();
        self.showReceiverForm();
    }

    private Launcher() {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdir();
        }
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File file = new File(logDir, date + ".log");

        try {
            PrintStream printStream = new PrintStream(new FileOutputStream(file, true));
            System.setOut(printStream);
            System.setErr(printStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static AppConfigs getAppConfigs() {
        return self.appConfigs;
    }

    public static void log(String s) {
        self.receiverForm.addLog(s);
    }

    private void loadConfigs() {
        self.appConfigs = new AppConfigs();
        appConfigs.setImageWidth(330);
        appConfigs.setImageHeight(330);
        appConfigs.setSenderLayout("2*3");
        appConfigs.setSaveDir(new File("Received").getAbsolutePath());
    }

    public void showReceiverForm() {
        if (receiverForm == null) {
            receiverForm = ReceiverForm.create();
        }
        receiverForm.show();
    }

    public void showSenderForm() {
        if (senderForm == null) {
            senderForm = SenderForm.create();
        }
        senderForm.show();
    }

    public void showConfigsForm() {
        if (configsForm == null) {
            configsForm = ConfigsForm.create();
        }
        configsForm.show();
    }
}
