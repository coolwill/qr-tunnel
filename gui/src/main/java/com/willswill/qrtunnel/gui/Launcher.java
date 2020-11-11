package com.willswill.qrtunnel.gui;

import com.willswill.qrtunnel.core.AppConfigs;
import lombok.Data;

@Data
public class Launcher {
    public static final Launcher self = new Launcher();

    private AppConfigs appConfigs;
    private ReceiverForm receiverForm;
    private SenderForm senderForm;

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
        appConfigs.setSaveDir("Received");
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
}
