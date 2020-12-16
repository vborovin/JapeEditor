package com.vborovin.japeeditor;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import java.awt.*;
import java.awt.event.KeyEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import com.vborovin.japecompletion.JapeLanguageSupport;
import com.vborovin.japeparser.DebugBatch;
import com.vborovin.japeparser.debuglog.DebugLog;
import com.vborovin.japeparser.debuglog.DebugLogEntry;
import com.vborovin.japeparser.debuglog.DebugLogSyntaxScheme;
import gate.*;
import gate.corpora.CorpusImpl;
import gate.corpora.DocumentContentImpl;
import gate.corpora.DocumentImpl;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.Transducer;
import org.apache.commons.lang.StringUtils;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.folding.*;

import org.fife.rsta.ac.LanguageSupportFactory;

import com.vborovin.japetoken.*;
import com.vborovin.japefolding.*;

public class JapeEditorForm extends JFrame {

    private ArrayList<JPanelTab> tabs;
    private final JFileChooser fileChooser;
    private boolean isDebuggerOpened = false;
    private DebugLog log;

    private ArrayList<Object> highlights;

    private JMenuItem createTransducerItem;

    public JapeEditorForm() {
        fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JAPE Source file", "jape");
        fileChooser.setFileFilter(filter);
        tabs = new ArrayList<>();
        highlights = new ArrayList<>();

        tabbedPane1 = new JTabbedPane();
        rootPanel.add(tabbedPane1);

        initToolbar();

        initSyntax();

        textSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        textSplitPane.setContinuousLayout(true);
        textSplitPane.setOneTouchExpandable(true);
        textSplitPane.setResizeWeight(1);

        outputSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        outputSplitPane.setContinuousLayout(true);
        outputSplitPane.setOneTouchExpandable(true);
        outputSplitPane.setResizeWeight(1);

        outputSplitPane.add(textSplitPane, 0);

        initDebugTextBlock();

        createNewTab(null, null);
        updateFrameTitle();

        Dimension size = new Dimension(800, 600);
        setPreferredSize(size);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setContentPane(rootPanel);
        setVisible(true);
    }

    private void initSyntax() {
        JapeLanguageSupport.addJapeLanguageSupport();

        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/JAPE", "com.naradius.japetoken.JapeTokenMaker");
        atmf.putMapping("text/JAPEDebugger", "com.naradius.japetoken.DebuggerTokenMaker");

        FoldParserManager.get().addFoldParserMapping("text/JAPE", new JapeFoldParser());
        FoldParserManager.get().addFoldParserMapping("text/JAPEDebugger", new DebugFoldParser());
    }

    private void initToolbar() {
        toolbar = new JMenuBar();
        //FILE MENU
        JMenu fileMenu = new JMenu("File");

        JMenuItem create = new JMenuItem("New");
        create.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        create.addActionListener(e -> createNewTabHandler());
        fileMenu.add(create);

        JMenuItem open = new JMenuItem("Open");
        open.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        open.addActionListener(e -> openFileHandler(null));
        fileMenu.add(open);

        JMenuItem save = new JMenuItem("Save");
        save.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        save.addActionListener(e -> saveFileHandler());
        fileMenu.add(save);

        JMenuItem saveAs = new JMenuItem("Save As");
        saveAs.setAccelerator(KeyStroke.getKeyStroke('S', KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK));
        saveAs.addActionListener(e -> saveAsFileHandler());
        fileMenu.add(saveAs);

        JMenuItem close = new JMenuItem("Close");
        close.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        close.addActionListener(e -> closeTabHandler());
        fileMenu.add(close);

        //VIEW MENU
        JMenu viewMenu = new JMenu("View");

        JMenuItem debugger = new JMenuItem("Switch debugger");
        debugger.setAccelerator(KeyStroke.getKeyStroke('T', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        debugger.addActionListener(e -> switchDebugger());
        viewMenu.add(debugger);

        //TOOLS MENU
        JMenu toolsMenu = new JMenu("Tools");

        JMenuItem doDebug = new JMenuItem("Debug");
        doDebug.setAccelerator(KeyStroke.getKeyStroke('Y', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        doDebug.addActionListener(e -> {
            try {
                debug();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        toolsMenu.add(doDebug);

        createTransducerItem = new JMenuItem("Create transducer");
        createTransducerItem.addActionListener(e -> {
            createTransducer();
        });
        toolsMenu.add(createTransducerItem);

        toolbar.add(fileMenu);
        toolbar.add(viewMenu);
        toolbar.add(toolsMenu);
        rootPanel.add(toolbar, BorderLayout.PAGE_START);
    }

    private void createTransducer() {
        try {
            JPanelTab current = (JPanelTab) tabbedPane1.getSelectedComponent();

            FeatureMap featureMap = Factory.newFeatureMap();
            featureMap.put("grammarURL", current.getFile().toURI().toURL());
            Gate.setHiddenAttribute(featureMap, false);
            Transducer transducer = (Transducer) Factory.createResource("gate.creole.Transducer", featureMap);
            transducer.setName("Transducer " + System.currentTimeMillis());
            System.out.println(Gate.getUserSessionFile());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkCreateTransducerItem() {
        JPanelTab current = (JPanelTab) tabbedPane1.getSelectedComponent();
        createTransducerItem.setEnabled(!(current.isHasErrors() || current.isChanged() || current.getFile() == null));
    }

    private void debug() throws Exception {
        log.clear();
        DocumentImpl text = new DocumentImpl();
        javax.swing.text.Document debugTextAreaDocument = debugText.getDocument();
        String debugString = debugTextAreaDocument.getText(0, debugTextAreaDocument.getLength());
        DocumentContentImpl content = new DocumentContentImpl(debugString);
        text.setContent(content);

        Corpus previousCorpus = null;
        Controller currentApplication = null;

        Corpus corpus = new CorpusImpl();
        corpus.add(text);

        JPanelTab current = (JPanelTab)tabbedPane1.getSelectedComponent();
        if (!current.isHasErrors()) {
            ArrayList<Controller> applications = JapeEditor.getApplications();
            if (applications.size() > 0) {
                Controller resource = applications.get(0);
                currentApplication = resource;

                if (resource instanceof ConditionalSerialAnalyserController) {
                    ConditionalSerialAnalyserController csac = (ConditionalSerialAnalyserController) resource;
                    previousCorpus = csac.getCorpus();
                    csac.setCorpus(corpus);
                    csac.execute();
                }

                if (resource instanceof SerialAnalyserController) {
                    SerialAnalyserController sac = (SerialAnalyserController) resource;
                    previousCorpus = sac.getCorpus();
                    sac.setCorpus(corpus);
                    sac.execute();
                }
            }

            text.getFeatures();
            text.getAnnotations();

            javax.swing.text.Document grammarDocument = current.getTextArea().getDocument();
            String grammar = grammarDocument.getText(0, grammarDocument.getLength());

            DebugBatch batch = new DebugBatch("UTF-8", log);
            batch.transduce(grammar, text);
            RSyntaxDocument doc = (RSyntaxDocument) debugOutput.getDocument();
            if (doc != null) {
                doc.remove(0, doc.getLength());
                doc.insertString(0, log.toString(), null);
            }

            if (currentApplication instanceof ConditionalSerialAnalyserController) {
                ConditionalSerialAnalyserController csac = (ConditionalSerialAnalyserController) currentApplication;
                csac.setCorpus(previousCorpus);
            }

            if (currentApplication instanceof SerialAnalyserController) {
                SerialAnalyserController sac = (SerialAnalyserController) currentApplication;
                sac.setCorpus(previousCorpus);
            }
        }
    }

    private void initDebugTextBlock() {
        log = new DebugLog();

        debugText = new RSyntaxTextArea();
        debugText.getSyntaxScheme().setStyles(DebugTextSyntaxScheme.getStyle());
        debugText.setCurrentLineHighlightColor(Color.white);
        debugText.setSyntaxEditingStyle("text/JAPEDebugger");
        debugText.getSyntaxScheme().setStyles(DebugLogSyntaxScheme.getStyle());

        RTextScrollPane sp = new RTextScrollPane(debugText);
        sp.setLineNumbersEnabled(false);

        textSplitPane.setRightComponent(sp);

        debugOutput = new RSyntaxTextArea();
        debugOutput.setEditable(false);
        debugOutput.getSyntaxScheme().setStyles(DebugTextSyntaxScheme.getStyle());
        //debugOutput.setCurrentLineHighlightColor(Color.orange);
        debugOutput.setSyntaxEditingStyle("text/JAPEDebugger");
        debugOutput.setCodeFoldingEnabled(true);
        debugOutput.getSyntaxScheme().setStyles(DebugLogSyntaxScheme.getStyle());

        debugOutput.addCaretListener((CaretEvent e) -> {
            int offset = e.getDot();
            highlights.forEach(obj -> {
                debugText.getHighlighter().removeHighlight(obj);
            });
            highlights.clear();
            DebugLogEntry entry = log.getEntryAtOffset(offset);
            if (entry != null) {
                if (entry.getOffset() != -1) {
                    try {
                        highlights.add(debugText.getHighlighter().addHighlight(entry.getOffset(), entry.getOffset() + entry.getLength(), new DefaultHighlighter.DefaultHighlightPainter(Color.orange)));
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        RTextScrollPane outputSp = new RTextScrollPane(debugOutput);
        outputSp.setMinimumSize(new Dimension());
        outputSp.setLineNumbersEnabled(false);

        outputSplitPane.add(outputSp, 1);
    }

    private void switchDebugger() {
        if (!isDebuggerOpened) {
            rootPanel.remove(tabbedPane1);
            textSplitPane.setLeftComponent(tabbedPane1);
            rootPanel.add(outputSplitPane);
        } else {
            textSplitPane.remove(tabbedPane1);
            rootPanel.remove(outputSplitPane);
            rootPanel.add(tabbedPane1);
        }

        isDebuggerOpened = !isDebuggerOpened;
        rootPanel.revalidate();
        rootPanel.repaint();
    }

    private void saveFile(File file, String text) {
        if (file != null && text != null) {
            try {
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.print(text);
                writer.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void createNewTab(File file, String text) {

        JPanelTab tab = new JPanelTab();

        if (file != null) {
            tab.setFile(file);
        }

        tab.setLayout(new GridLayout(0, 1));
        RSyntaxTextArea textArea = new RSyntaxTextArea(text);
        textArea.setPaintMatchedBracketPair(false);
        textArea.setAnimateBracketMatching(false);
        textArea.setShowMatchedBracketPopup(false);
        textArea.setMatchedBracketBGColor(Color.white);
        textArea.setMatchedBracketBorderColor(Color.white);
        LanguageSupportFactory.get().register(textArea);
        textArea.requestFocusInWindow();
        textArea.getSyntaxScheme().setStyles(JapeSyntaxScheme.getStyle());
        ToolTipManager.sharedInstance().registerComponent(textArea);

        textArea.setSyntaxEditingStyle("text/JAPE");

        textArea.setCodeFoldingEnabled(true);
        tab.setTextArea(textArea);

        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setIconRowHeaderEnabled(true);
        sp.getGutter().setBookmarkingEnabled(true);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            private void updateTabName() {
                tab.setChanged(true);
                updateFrameTitle();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTabName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTabName();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTabName();
            }
        });

        tab.add(sp);
        tabbedPane1.add(tab, 0);
        tabbedPane1.setSelectedIndex(0);

        textArea.discardAllEdits();
        textArea.requestFocusInWindow();

        int tabCount = tabbedPane1.getTabCount();
        String tabName = "New tab " + tabCount;
        if (file != null && StringUtils.isNotBlank(file.getName())) {
            tabName = file.getName();
        }
        tab.setName(tabName);
    }

    private void updateFrameTitle() {
        Component selected = tabbedPane1.getSelectedComponent();
        if (selected != null) {
            updateFrameTitle(selected);
        } else {
            setTitle("Jape Editor");
        }
    }

    private void updateFrameTitle(Component tab) {
        String tabName = tab.getName();
        tabbedPane1.setTitleAt(tabbedPane1.indexOfComponent(tab), tabName);
        setTitle(tabName + " - Jape Editor");
        checkCreateTransducerItem();
    }

    protected void createNewTabHandler() {
        createNewTab(null, null);
        updateFrameTitle();
    }

    protected void closeTabHandler() {
        if (tabbedPane1.getTabCount() > 0) {
            int index = tabbedPane1.getSelectedIndex();
            tabbedPane1.removeTabAt(index);
            updateFrameTitle();
        }
    }

    protected void openFileHandler(File file) {
        int returnVal = 0;
        if (file == null) {
            returnVal = fileChooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            }
        }

        try {
            int count = tabbedPane1.getTabCount();
            int matchIndex = -1;
            for (int i = 0; i < count; i++) {
                JPanelTab tab = (JPanelTab) tabbedPane1.getComponentAt(i);
                File tabFile = tab.getFile();

                if (tabFile != null && tabFile.equals(file)) {
                    matchIndex = i;
                    break;
                }
            }

            if (matchIndex != -1) {
                tabbedPane1.setSelectedIndex(matchIndex);
            } else {
                String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                text = text.replaceAll("(\\r)", "");
                createNewTab(file, text);
            }

            updateFrameTitle();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    protected void saveFileHandler() {
        JPanelTab tab = (JPanelTab) tabbedPane1.getSelectedComponent();
        String text = tab.getTextArea().getText();
        File file = tab.getFile();
        if (file != null) {
            saveFile(file, text);
            tab.setChanged(false);
            updateFrameTitle();
        } else {
            int returnVal = fileChooser.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                saveFile(file, text);
                tab.setFile(file);
                tab.setName(StringUtils.isNotBlank(file.getName()) ? file.getName() : "New tab " + tabbedPane1.getTabCount());
                tab.setChanged(false);
                updateFrameTitle();
            }
        }
    }

    protected void saveAsFileHandler() {
        JPanelTab tab = (JPanelTab) tabbedPane1.getSelectedComponent();
        String text = tab.getTextArea().getText();
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveFile(file, text);
            tab.setFile(file);
            tab.setChanged(false);
            tab.setName(StringUtils.isNotBlank(file.getName()) ? file.getName() : "New tab " + tabbedPane1.getTabCount());
            updateFrameTitle();
        }
    }

    public void addTabWithTransducer(Transducer transducer) {
        int index = -1;
        Component[] tabs = tabbedPane1.getComponents();
        for (Component component : tabs) {
            JPanelTab tab = (JPanelTab) component;
            if (tab.getTransducer() == transducer) {
                index = tabbedPane1.indexOfComponent(tab);
                break;
            }
        }

        if (index == -1) {
            try {
                File file = new File(transducer.getGrammarURL().toURI());
                openFileHandler(file);
                JPanelTab current = (JPanelTab)tabbedPane1.getSelectedComponent();
                current.setTransducer(transducer);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tabbedPane1.setSelectedIndex(index);
        }
    }

    @Override
    public void dispose() {
        JapeEditor.setEditorForm(null);
        super.dispose();
    }

    private RSyntaxTextArea debugText;
    private RSyntaxTextArea debugOutput;
    private JSplitPane textSplitPane;
    private JSplitPane outputSplitPane;

    private JPanel rootPanel;
    private JTabbedPane tabbedPane1;
    private JMenuBar toolbar;

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
        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout(0, 0));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}