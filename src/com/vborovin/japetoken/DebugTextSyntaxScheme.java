package com.vborovin.japetoken;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;

import javax.swing.text.StyleContext;
import java.awt.*;

public class DebugTextSyntaxScheme {
    public static Style[] getStyle() {
        Style[] styles = new Style[39];

        Color comment = new Color(0, 128, 0);
        Color docComment = new Color(128, 64, 64);
        Color markupComment = new Color(0, 96, 0);
        Color keyword = Color.BLUE;
        Color dataType = new Color(0, 128, 128);
        Color function = new Color(173, 128, 0);
        Color preprocessor = new Color(128, 128, 128);
        Color operator = new Color(128, 64, 64);
        Color regex = new Color(0, 128, 164);
        Color variable = new Color(255, 153, 0);
        Color literalNumber = new Color(100, 0, 200);
        Color literalString = new Color(220, 0, 156);
        Color error = new Color(148, 148, 0);

        Font baseFont = RSyntaxTextArea.getDefaultFont();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        Font keywordFont = sc.getFont(baseFont.getFamily(), 1, baseFont.getSize());
        Font commentFont = sc.getFont(baseFont.getFamily(), 2, baseFont.getSize());

        styles[1] = new Style(comment, (Color)null, commentFont);
        styles[2] = new Style(comment, (Color)null, commentFont);
        styles[3] = new Style(docComment);
        styles[4] = new Style(new Color(255, 152, 0), (Color)null, commentFont);
        styles[5] = new Style(Color.gray, (Color)null, commentFont);
        styles[6] = new Style(keyword, (Color)null, keywordFont);
        styles[7] = new Style(keyword, (Color)null, keywordFont);
        styles[8] = new Style(function);
        styles[9] = new Style(dataType);
        styles[10] = new Style(literalNumber);
        styles[11] = new Style(literalNumber);
        styles[12] = new Style(literalNumber);
        styles[13] = new Style(literalString);
        styles[14] = new Style(literalString);
        styles[15] = new Style(literalString);
        styles[16] = new Style(dataType, (Color)null, keywordFont);
        styles[17] = new Style(variable);
        styles[18] = new Style(regex);
        styles[19] = new Style(Color.gray);
        styles[20] = new Style((Color)null);
        styles[21] = new Style(Color.gray);
        styles[22] = new Style(Color.RED);
        styles[23] = new Style(operator);
        styles[24] = new Style(preprocessor);
        styles[25] = new Style(Color.RED);
        styles[26] = new Style(Color.BLUE);
        styles[27] = new Style(new Color(63, 127, 127));
        styles[28] = new Style(literalString);
        styles[29] = new Style(markupComment, (Color)null, commentFont);
        styles[30] = new Style(function);
        styles[31] = new Style(preprocessor);
        styles[33] = new Style(new Color(13395456));
        styles[32] = new Style(new Color(32896));
        styles[34] = new Style(dataType);
        styles[35] = new Style(error);
        styles[36] = new Style(error);
        styles[37] = new Style(error);
        styles[38] = new Style(error);

        for(int i = 0; i < styles.length; ++i) {
            if (styles[i] == null) {
                styles[i] = new Style();
            }
        }

        return styles;
    }
}
