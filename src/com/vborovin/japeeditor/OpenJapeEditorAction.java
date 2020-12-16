package com.vborovin.japeeditor;

import gate.creole.Transducer;
import gate.util.Err;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static gate.gui.MainFrame.getIcon;

public class OpenJapeEditorAction extends AbstractAction {

    Transducer openedTransducer;

    public OpenJapeEditorAction(Transducer transducer) {
        super("Jape Editor", getIcon("application"));
        this.putValue("ShortDescription", "Open Jape Editor");
        this.putValue("AcceleratorKey", KeyStroke.getKeyStroke('J', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        openedTransducer = transducer;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            JapeEditorForm editor = JapeEditor.getEditorForm();
            if (editor == null) {
                editor = new JapeEditorForm();
                JapeEditor.setEditorForm(editor);
            } else {
                editor.toFront();
                editor.repaint();
            }

            if (openedTransducer != null) {
                editor.addTabWithTransducer(openedTransducer);
            }
        }
        catch (Exception ex) {
            Err.prln("Unable to open Jape Editor.\n");
            ex.printStackTrace();
        }
    }
}
