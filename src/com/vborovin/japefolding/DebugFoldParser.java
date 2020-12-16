package com.vborovin.japefolding;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.Fold;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.List;

public class DebugFoldParser extends CurlyFoldParser {

    public DebugFoldParser() {
        super(true, false);
    }

    public List<Fold> getFolds(RSyntaxTextArea textArea) {
        List<Fold> folds = new ArrayList();
        Fold currentFold = null;
        int lineCount = textArea.getLineCount();
        int lastRightCurlyLine = -1;
        Fold prevFold = null;

        try {
            for(int line = 0; line < lineCount; ++line) {
                for(Token t = textArea.getTokenListForLine(line); t != null && t.isPaintable(); t = t.getNextToken()) {
                    Fold parentFold;
                    if (t.getLexeme().equalsIgnoreCase("{{{")) {
                        if (prevFold != null && line == lastRightCurlyLine) {
                            currentFold = prevFold;
                            prevFold = null;
                            lastRightCurlyLine = -1;
                        } else if (currentFold == null) {
                            currentFold = new Fold(0, textArea, t.getOffset());
                            folds.add(currentFold);
                        } else {
                            currentFold = currentFold.createChild(0, t.getOffset());
                        }
                    }
                    else if (t.getLexeme().equalsIgnoreCase("}}}")) {
                        if (currentFold != null) {
                            currentFold.setEndOffset(t.getOffset());
                            parentFold = currentFold.getParent();
                            if (currentFold.isOnSingleLine()) {
                                if (!currentFold.removeFromParent()) {
                                    folds.remove(folds.size() - 1);
                                }
                            } else {
                                lastRightCurlyLine = line;
                                prevFold = currentFold;
                            }

                            currentFold = parentFold;
                        }
                    }
                }
            }
        } catch (BadLocationException var16) {
            var16.printStackTrace();
        }

        return folds;
    }
}
