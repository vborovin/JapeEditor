/*
 *  JapeEditor.java
 *
 * Copyright (c) 2000-2012, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 3, 29 June 2007.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  baton, 24/1/2018
 *
 * For details on the configuration options, see the user guide:
 * http://gate.ac.uk/cgi-bin/userguide/sec:creole-model:config
 */

package com.vborovin.japeeditor;

import gate.*;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.event.ProgressListener;
import gate.gui.MainFrame;
import gate.gui.jape.JapeViewer;
import gate.jape.parser.ParseCpslConstants;
import gate.jape.parser.ParseCpslTokenManager;
import gate.jape.parser.SimpleCharStream;
import gate.jape.parser.Token;
import gate.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static gate.gui.MainFrame.getIcon;


/** 
 * This class is the implementation of the resource JAPEEDITOR.
 */
@CreoleResource(name="Jape Editor", helpURL="http://gate.ac.uk/userguide/chap:jape", guiType=GuiType.LARGE, tool=true, resourceDisplayed="gate.creole.Transducer")
public class JapeEditor extends AbstractVisualResource implements
        ActionsPublisher, CreoleListener {

    private static ArrayList<Controller> applications;

    private static JapeEditorForm editorForm;

    public static JapeEditorForm getEditorForm() {
        return editorForm;
    }

    public static void setEditorForm(JapeEditorForm editorForm) {
        JapeEditor.editorForm = editorForm;
    }

    public static ArrayList<Controller> getApplications() {
        return applications;
    }

    public static void setApplications(ArrayList<Controller> applications) {
        JapeEditor.applications = applications;
    }

    @Override
    public Resource init() {
        setApplications(new ArrayList());
        Gate.addCreoleListener(this);
        return this;
    }

    @Override
    public void setTarget(final Object target) {
        setLayout(new BorderLayout());
        JButton openInEditor = new JButton();
        openInEditor.setText("Open in editor");
        openInEditor.setAction(new OpenJapeEditorAction((Transducer) target));

        this.add(openInEditor);
    }

    public List<Action> getActions() {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new OpenJapeEditorAction(null));
        return actions;
    }

    @Override
    public void resourceLoaded(CreoleEvent creoleEvent) {
        final Resource resource = creoleEvent.getResource();
        if (resource instanceof ConditionalSerialAnalyserController || resource instanceof  SerialAnalyserController) {
            getApplications().add((Controller) resource);
        }
    }

    @Override
    public void resourceUnloaded(CreoleEvent creoleEvent) {
        final Resource resource = creoleEvent.getResource();
        if (resource instanceof Controller) {
            getApplications().remove((Controller) resource);
        }
    }

    @Override
    public void datastoreOpened(CreoleEvent creoleEvent) {

    }

    @Override
    public void datastoreCreated(CreoleEvent creoleEvent) {

    }

    @Override
    public void datastoreClosed(CreoleEvent creoleEvent) {

    }

    @Override
    public void resourceRenamed(Resource resource, String s, String s1) {

    }
}