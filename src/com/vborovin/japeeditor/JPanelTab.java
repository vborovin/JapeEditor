package com.vborovin.japeeditor;

import gate.creole.Transducer;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.io.File;

public class JPanelTab extends JPanel {

    private RSyntaxTextArea textArea;
    private File file;
    private boolean changed;
    private boolean hasErrors;
    private String tabName;
    private Transducer transducer;

    public JPanelTab() {
        changed = false;
    }

    @Override
    public void setName(String name) {
        tabName = name;
        super.setName(name);
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        if (changed) {
            super.setName(tabName + " *");
        } else {
            super.setName(tabName);
        }

        this.changed = changed;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(RSyntaxTextArea textArea) {
        this.textArea = textArea;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Transducer getTransducer() {
        return transducer;
    }

    public void setTransducer(Transducer transducer) {
        this.transducer = transducer;
    }
}