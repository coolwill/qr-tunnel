package com.willswill.qrtunnel.gui;

import com.willswill.qrtunnel.core.AppConfigs;
import lombok.Data;

import java.io.File;

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

    public static AppConfigs getAppConfigs() {
        return self.appConfigs;
    }

    public static void log(String s) {
        self.receiverForm.addLog(s);
    }

    private void loadConfigs() {
        self.appConfigs = new AppConfigs();
        appConfigs.setImageWidth(480);
        appConfigs.setImageHeight(480);
        appConfigs.setSenderLayout("1*1");
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
