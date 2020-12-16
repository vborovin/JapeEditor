package com.vborovin.japecompletion;

import com.vborovin.japeparser.JapeParser;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.AbstractLanguageSupport;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class JapeLanguageSupport extends AbstractLanguageSupport {

    public JapeParser getParser(RSyntaxTextArea textArea) {
        Object parser = textArea.getClientProperty(PROPERTY_LANGUAGE_PARSER);
        if (parser instanceof JapeParser) {
            return (JapeParser)parser;
        }
        return null;
    }

    public static void addJapeLanguageSupport() {
        LanguageSupportFactory lsf = LanguageSupportFactory.get();
        lsf.addLanguageSupport("text/JAPE", "com.naradius.japecompletion.JapeLanguageSupport");
    }

    @Override
    public void install(RSyntaxTextArea rSyntaxTextArea) {
        JapeCompletionProvider p = new JapeCompletionProvider();

        AutoCompletion ac = new AutoCompletion(p);
        ac.setAutoCompleteEnabled(true);
        ac.setAutoActivationEnabled(true);
        ac.setShowDescWindow(true);
        ac.setParameterAssistanceEnabled(true);
        ac.setAutoCompleteSingleChoices(false);
        ac.setAutoActivationDelay(500);
        ac.install(rSyntaxTextArea);
        this.installImpl(rSyntaxTextArea, ac);
        rSyntaxTextArea.setToolTipSupplier(p);

        JapeParser parser = new JapeParser(rSyntaxTextArea);
        parser.setTextArea(rSyntaxTextArea);
        rSyntaxTextArea.addParser(parser);
    }

    @Override
    public void uninstall(RSyntaxTextArea rSyntaxTextArea) {
        this.uninstallImpl(rSyntaxTextArea);
        rSyntaxTextArea.setToolTipSupplier(null);

        JapeParser parser = getParser(rSyntaxTextArea);
        rSyntaxTextArea.removeParser(parser);
    }
}
