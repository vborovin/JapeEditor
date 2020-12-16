package com.vborovin.japeparser;

import com.vborovin.japeeditor.JPanelTab;
import gate.*;
import gate.creole.annic.Parser;
import gate.jape.MultiPhaseTransducer;
import gate.jape.parser.ParseCpsl;
import gate.jape.parser.ParseException;
import gate.util.Err;
import org.apache.commons.lang.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JapeParser extends AbstractParser {

    private RSyntaxTextArea textArea;
    private DefaultParseResult parseResult;
    private ParseCpsl japeParser;
    private PrintWriter gatePrintWriter;

    public JapeParser(RSyntaxTextArea textArea) {
        this.textArea = textArea;
        parseResult = new DefaultParseResult(this);
        try {
            gatePrintWriter = Err.getPrintWriter();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ParseResult parse(RSyntaxDocument rSyntaxDocument, String s) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter newPrintWriter = new PrintWriter(stringWriter);

        PrintStream systemErrWriter = new PrintStream(System.err);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newErrWriter = new PrintStream(baos);
        try {
            String text = rSyntaxDocument.getText(0, rSyntaxDocument.getLength());
            StringReader reader = new StringReader(text);
            parseResult.clearNotices();

            System.setErr(newErrWriter);
            Err.setPrintWriter(newPrintWriter);

            japeParser = new ParseCpsl(reader, new HashMap<>(), new HashMap<>());
            MultiPhaseTransducer transducer = japeParser.MultiPhaseTransducer();
            transducer.finish(Gate.getClassLoader());
        } catch (ParseException e) {
            Pattern p = Pattern.compile("^(Cannot parse a phase in .*\\n*: *)*((.*\\n*.*)at line ([0-9]+), column ([0-9]+)\\.*)|((.*\\n*.*):([0-9]+):([0-9]+): *(.*\\n*.*))", Pattern.MULTILINE);
            Matcher m = p.matcher(e.getMessage());

            String message = null;
            String lineStr = null;
            String columnStr = null;
            if(m.find()) {
                message = m.group(3);

                if (message != null) {
                    lineStr = m.group(4);
                    columnStr = m.group(5);
                } else {
                    lineStr = m.group(8);
                    columnStr = m.group(9);
                    message = m.group(10);
                }
            }

            if (lineStr == null) {
                lineStr = "0";
            }
            if (columnStr == null) {
                columnStr = "0";
            }
            if (message == null) {
                message = "";
            }

            int line = Integer.parseInt(lineStr);
            if (line > 0) {
                line -= 1;
            }
            int column = Integer.parseInt(columnStr);

            Token tokenLine = rSyntaxDocument.getTokenListForLine(line);
            int offset = tokenLine.getOffset() + column;
            Token matchToken = RSyntaxUtilities.getTokenAtOffset(tokenLine, offset);
            if (matchToken != null && matchToken.getType() == Token.NULL) {
                matchToken = tokenLine.getLastPaintableToken();
            }
            if (matchToken == null) {
                matchToken = tokenLine;
            }
            DefaultParserNotice notice = new DefaultParserNotice(this, message, line, matchToken.getOffset(), matchToken.length());
            parseResult.addNotice(notice);
        }
        catch (Exception e) {

        } finally {
            Err.setPrintWriter(gatePrintWriter);
            System.setErr(systemErrWriter);
        }

        String temp = stringWriter.toString();
        newPrintWriter.close();
        if (StringUtils.isNotBlank(temp)) {
            Pattern p = Pattern.compile("^((.*\\n*.*) *at line ([0-9]+) in)+", Pattern.MULTILINE);
            Matcher m = p.matcher(temp);

            String lineStr = null;
            String message = null;

            if (m.find()) {
                message = m.group(2);
                lineStr = m.group(3);
            }

            Pattern lineP = Pattern.compile("^ *(" + lineStr + ") *(.*)");

            String lineContent = null;
            String[] lines = temp.split("[\n\r]");
            for (String curLine : lines) {
                Matcher lineM = lineP.matcher(curLine);
                if (lineM.find()) {
                    lineContent = lineM.group(2);
                    break;
                }
            }

            if (lineContent != null) {
                try {
                    String text = rSyntaxDocument.getText(0, rSyntaxDocument.getLength());
                    int offset =  text.indexOf(lineContent);
                    int line = rSyntaxDocument.getDefaultRootElement().getElementIndex(offset);
                    int lineEdit = line + 1;

                    Token token = rSyntaxDocument.getTokenListForLine(line);
                    Token tokenEnd = token.getLastPaintableToken();

                    DefaultParserNotice notice = new DefaultParserNotice(this, message, line, offset, tokenEnd.getOffset() - token.getOffset());
                    parseResult.addNotice(notice);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }

        newErrWriter.close();

        JPanelTab tab = (JPanelTab) SwingUtilities.getAncestorOfClass(JPanelTab.class, textArea);
        if (tab != null) {
            tab.setHasErrors(parseResult.getNotices().size() > 0);
        }

        return parseResult;
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(RSyntaxTextArea textArea) {
        this.textArea = textArea;
    }
}