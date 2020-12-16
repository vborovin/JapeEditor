package com.vborovin.japefolding;

import com.vborovin.japetoken.ExtendedToken;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;

import javax.swing.text.BadLocationException;

import java.util.List;
import java.util.ArrayList;

public class JapeFoldParser extends CurlyFoldParser {

    public JapeFoldParser() {
        super(true, false);
    }

    public List<Fold> getFolds(RSyntaxTextArea textArea) {
        List<Fold> folds = new ArrayList();
        Fold currentFold = null;
        int lineCount = textArea.getLineCount();
        boolean inMLC = false;
        int mlcStart = 0;
        int lastRightCurlyLine = -1;
        int lastRightParenthesisLine = -1;
        Fold prevFold = null;

        try {
            for(int line = 0; line < lineCount; ++line) {
                for(Token t = textArea.getTokenListForLine(line); t != null && t.isPaintable(); t = t.getNextToken()) {
                    ExtendedToken ext = (ExtendedToken)t;
                    Fold parentFold;
                    if (getFoldableMultiLineComments() && t.isComment()) {
                        if (inMLC) {
                            if (t.endsWith(C_MLC_END)) {
                                int mlcEnd = t.getEndOffset() - 1;
                                if (currentFold == null) {
                                    currentFold = new Fold(1, textArea, mlcStart);
                                    currentFold.setEndOffset(mlcEnd);
                                    folds.add(currentFold);
                                    currentFold = null;
                                } else {
                                    currentFold = currentFold.createChild(1, mlcStart);
                                    currentFold.setEndOffset(mlcEnd);
                                    currentFold = currentFold.getParent();
                                }

                                inMLC = false;
                                mlcStart = 0;
                            }
                        } else if (t.getType() != 1 && !t.endsWith(C_MLC_END)) {
                            inMLC = true;
                            mlcStart = t.getOffset();
                        }
                    } else if (this.isLeftCurly(t)) {
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
                    else if (this.isRightCurly(t)) {
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
                    } else if (ext.isLeftParenthesis()) {
                        if (prevFold != null && line == lastRightParenthesisLine) {
                            currentFold = prevFold;
                            prevFold = null;
                            lastRightParenthesisLine = -1;
                        } else if (currentFold == null) {
                            currentFold = new Fold(0, textArea, t.getOffset());
                            folds.add(currentFold);
                        } else {
                            currentFold = currentFold.createChild(0, t.getOffset());
                        }
                    } else if (ext.isRightParenthesis()) {
                        if (currentFold != null) {
                            currentFold.setEndOffset(t.getOffset());
                            parentFold = currentFold.getParent();
                            if (currentFold.isOnSingleLine()) {
                                if (!currentFold.removeFromParent()) {
                                    folds.remove(folds.size() - 1);
                                }
                            } else {
                                lastRightParenthesisLine = line;
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
