package com.vborovin.japecompletion;

import com.vborovin.japetoken.ExtendedToken;
import com.vborovin.japetoken.JapeTokenMaker;
import gate.util.Err;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;

import javax.swing.text.JTextComponent;

public class JapeCompletionProvider extends LanguageAwareCompletionProvider {

    private CompletionProvider annotationRegionCompetionProvider;
    private CompletionProvider rhsCompletionProvider;
    private CompletionProvider javaCompletionProvider;

    public JapeCompletionProvider() {
        setDefaultCompletionProvider(new DefaultJapeCompletionProvider());
        setAnnotationRegionCompetionProvider(new AnnotationJapeCompletionProvider());
        setRhsCompletionProvider(new RhsJapeCompletionProvider());
        //setJavaCompletionProvider(scp);
    }

    @Override
    protected CompletionProvider getProviderFor(JTextComponent comp) {
        RSyntaxTextArea rsta = (RSyntaxTextArea)comp;
        RSyntaxDocument doc = (RSyntaxDocument)rsta.getDocument();
        int line = rsta.getCaretLineNumber();
        Token t = doc.getTokenListForLine(line);
        if (t == null) {
            return this.getDefaultCompletionProvider();
        } else {
            int dot = rsta.getCaretPosition();
            Token curToken = RSyntaxUtilities.getTokenAtOffset(t, dot);
            if (curToken == null) {
                Token temp = t.getLastPaintableToken();

                if (temp == null) {
                    return this.getDefaultCompletionProvider();
                }
                int type = temp.getType();

                if (type < 0) {
                    type = doc.getClosestStandardTokenTypeForInternalType(type);
                }

                //TODO:Проверка на state токена?
                switch(type) {
                    case Token.COMMENT_EOL:
                    case Token.COMMENT_MULTILINE:
                        return this.getCommentCompletionProvider();
                    case Token.ERROR_STRING_DOUBLE:
                        return this.getStringCompletionProvider();
                    default:
                        return this.getDefaultCompletionProvider();
                }
            } else if (dot == curToken.getOffset()) { //TODO:??????????
                return this.getDefaultCompletionProvider();
            } else if (((ExtendedToken)curToken) != null) {
                ExtendedToken extToken = (ExtendedToken)curToken;
                switch(curToken.getType()) {
                    case 0:
                    case 8:
                    case 16:
                    case 17:
                    case 20:
                    case 21:
                    case 23:
                    case 24:
                        if (extToken.getState() == JapeTokenMaker.ANNOTATION_REGION) {
                            return this.getAnnotationRegionCompetionProvider();
                        } else if (extToken.getState() == JapeTokenMaker.RHS_ASSIGNMENT) {
                            return this.getRhsCompletionProvider();
                        } else if (extToken.getState() == JapeTokenMaker.JAVA_BLOCK) {
                            return this.getJavaCompletionProvider();
                        }
                        return this.getDefaultCompletionProvider();
                    case 1:
                    case 2:
                        return this.getCommentCompletionProvider();
                    case 3:
                        return this.getDocCommentCompletionProvider();
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 14:
                    case 15:
                    case 18:
                    case 19:
                    case 22:
                    case 25:
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                    case 32:
                    case 33:
                    case 34:
                    case 35:
                    case 36:
                    default:
                        return null;
                    case 13:
                    case 37:
                        return this.getStringCompletionProvider();
                }
            } else {
                return this.getDefaultCompletionProvider();
            }
        }
    }

    public CompletionProvider getAnnotationRegionCompetionProvider() {
        return annotationRegionCompetionProvider;
    }

    public void setAnnotationRegionCompetionProvider(CompletionProvider annotationRegionCompetionProvider) {
        this.annotationRegionCompetionProvider = annotationRegionCompetionProvider;
    }

    public CompletionProvider getRhsCompletionProvider() {
        return rhsCompletionProvider;
    }

    public void setRhsCompletionProvider(CompletionProvider rhsCompletionProvider) {
        this.rhsCompletionProvider = rhsCompletionProvider;
    }

    public CompletionProvider getJavaCompletionProvider() {
        return javaCompletionProvider;
    }

    public void setJavaCompletionProvider(CompletionProvider javaCompletionProvider) {
        this.javaCompletionProvider = javaCompletionProvider;
    }

    public class DefaultJapeCompletionProvider extends DefaultCompletionProvider {
        public DefaultJapeCompletionProvider() {
            addCompletion(new BasicCompletion(this, "Rule:"));
            addCompletion(new BasicCompletion(this, "Macro:"));
            addCompletion(new BasicCompletion(this, "Phase:"));
            addCompletion(new BasicCompletion(this, "Input:"));
            addCompletion(new BasicCompletion(this, "Imports:"));
            addCompletion(new BasicCompletion(this, "Options:"));
            addCompletion(new BasicCompletion(this, "Priority:"));
            addCompletion(new BasicCompletion(this, "MultiPhase:"));
            addCompletion(new BasicCompletion(this, "Phases:"));
            addCompletion(new BasicCompletion(this, "Template:"));
            addCompletion(new BasicCompletion(this, "control = "));
            addCompletion(new BasicCompletion(this, "appelt"));
            addCompletion(new BasicCompletion(this, "brill"));
            addCompletion(new BasicCompletion(this, "all"));
            addCompletion(new BasicCompletion(this, "first"));
            addCompletion(new BasicCompletion(this, "once"));
            addCompletion(new BasicCompletion(this, "debug"));
            addCompletion(new BasicCompletion(this, "true"));
            addCompletion(new BasicCompletion(this, "false"));
            addCompletion(new BasicCompletion(this, "Token"));
            addCompletion(new BasicCompletion(this, "Lookup"));
            addCompletion(new BasicCompletion(this, "SpaceToken"));
            addCompletion(new BasicCompletion(this, "Person"));
            addCompletion(new BasicCompletion(this, "Organization"));
            addCompletion(new BasicCompletion(this, "Location"));
            addCompletion(new BasicCompletion(this, "-->"));
            //addCompletion(new ShorthandCompletion(this, "importUtils", "import static gate.Utils.*;", "Import GATE Utils"));

            addCompletion(new ShorthandCompletion(this, "header", "/**\n" +
                    " * @author \n" +
                    " */\n" +
                    "Phase: \n" +
                    "Input: \n" +
                    "Options: control = , debug = \n" +
                    "\n" +
                    "", "Grammar header"));
            addCompletion(new ShorthandCompletion(this, "rule", "Rule: \n" +
                    "Priority: \n" +
                    "(\n" +
                    "   \n" +
                    "):\n" +
                    "-->\n", "JAPE Rule"));
            addCompletion(new ShorthandCompletion(this, "lhsmacro", "Macro: \n" +
                    "(\n" +
                    "   ({})\n" +
                    ")", "LHS Macro"));
            addCompletion(new ShorthandCompletion(this, "match", "({})", "Bracket matcher"));

            addCompletion(new TemplateCompletion(this, "debug", "debug = (t, f)", "debug = ${false}${cursor}", "debug = (t, f)", "Template for debug = ()"));
            addCompletion(new TemplateCompletion(this, "control", "control = ()", "control = ${brill}${cursor}", "control = ()", "Template for control = ()"));
            addCompletion(new TemplateCompletion(this, "header", "Grammar header with parameters", "/**\n" +
                    " * @author ${authorname} \n" +
                    " */\n" +
                    "Phase: ${FirstPhase}\n" +
                    "Input: ${Token}\n" +
                    "Options: control = ${appelt}, debug = ${false}\n" +
                    "${cursor}", "Grammar header with parameters", "Template for grammar header."));
            addCompletion(new TemplateCompletion(this, "rule", "JAPE Rule without Java", "Rule: ${ruleName}\n" +
                    "Priority: ${100}\n" +
                    "(\n" +
                    "   ({${Token}})[1,2]\n" +
                    "):${bind}\n" +
                    "-->\n" +
                    ":${bind}.${annotationName} = {rule = \"${ruleName}\"}${cursor}", "JAPE Rule without Java", "Template for rule without Java in RHS"));
            addCompletion(new TemplateCompletion(this, "rule", "JAPE Rule with Java", "Rule: ${First}\n" +
                    "Priority: ${100}\n" +
                    "(\n" +
                    "   ({${Lookup.majorType == \"\"}})\n" +
                    "):${bind}\n" +
                    "-->\n" +
                    "{\n" +
                    "   AnnotationSet set = bindings.get(\"${bind}\");\n" +
                    "   Annotation setAnn = set.iterator().next();\n" +
                    "   FeatureMap features = Factory.newFeatureMap();\n" +
                    "   features.put(\"rule\", \"${First}\");\n" +
                    "   outputAS.add(set.firstNode(), set.lastNode(), \"${Annotation}\", features);\n" +
                    "}${cursor}", "JAPE Rule with Java", "Template for rule with Java in RHS"));
            addCompletion(new TemplateCompletion(this, "lhsmacro", "Jape LHS Macro", "Macro: ${EXAMPLE}\n" +
                    "(\n" +
                    "   ({${Token}})\n" +
                    ")${cursor}", "Jape LHS Macro","Template for LHS macros"));
            addCompletion(new TemplateCompletion(this, "match", "Bracketed matcher", "({${Token}})${cursor}", "Bracketed matcher", "Template for bracketed matcher"));
            addCompletion(new TemplateCompletion(this, "look", "Lookup annotation filter", "{Lookup.${majorType} == \"${city}\"}${cursor}", "Lookup annotation filter", "Template for lookup annotation with filter for features"));
            addCompletion(new TemplateCompletion(this, "tok", "Token annotation filter", "{Token.${category} == \"${NNP}\"}${cursor}", "Token annotation filter", "Template for token annotation with filter for features"));
            addCompletion(new TemplateCompletion(this, "imports", "Java imports block", "Imports: {\n" +
                    "import ${java.util.logging.Logger}; \n" +
                    "}\n" +
                    "${cursor}", "Java imports block", "Template for Java imports block"));
            addCompletion(new TemplateCompletion(this, "controllerStart", "Controller Started", "ControllerStarted: {\n" +
                    "   ${}\n" +
                    "}\n" +
                    "${cursor}", "Controller Started", "Template for controller started event handler"));
            addCompletion(new TemplateCompletion(this, "controllerFinish", "Controller Finished", "ControllerFinished: {\n" +
                    "   ${}\n" +
                    "}\n" +
                    "${cursor}", "Controller Finished", "Template for controller finished event handler"));
            addCompletion(new TemplateCompletion(this, "controllerAbort", "Controller Aborted", "ControllerAborted: {\n" +
                    "   ${}\n" +
                    "}\n" +
                    "${cursor}", "Controller Aborted", "Template for controller aborted event handler"));
        }
    }

    public class AnnotationJapeCompletionProvider extends DefaultCompletionProvider {
        public AnnotationJapeCompletionProvider() {
            addCompletion(new BasicCompletion(this, "kind"));
            addCompletion(new BasicCompletion(this, "string"));
            addCompletion(new BasicCompletion(this, "length"));
            addCompletion(new BasicCompletion(this, "contains"));
            addCompletion(new BasicCompletion(this, "notContains"));
            addCompletion(new BasicCompletion(this, "within"));
            addCompletion(new BasicCompletion(this, "notWithin"));
            addCompletion(new BasicCompletion(this, "true"));
            addCompletion(new BasicCompletion(this, "false"));
            addCompletion(new BasicCompletion(this, "Token"));
            addCompletion(new BasicCompletion(this, "Lookup"));
            addCompletion(new BasicCompletion(this, "SpaceToken"));
            addCompletion(new BasicCompletion(this, "Person"));
            addCompletion(new BasicCompletion(this, "category"));
            addCompletion(new BasicCompletion(this, "rule"));
            addCompletion(new BasicCompletion(this, "cleanString"));
            addCompletion(new BasicCompletion(this, "Location"));
            addCompletion(new BasicCompletion(this, "majorType"));
            addCompletion(new BasicCompletion(this, "minorType"));

            addCompletion(new ShorthandCompletion(this, "match", "({})", "Bracket matcher"));
            addCompletion(new TemplateCompletion(this, "match", "Bracketed matcher", "({${Token}})${cursor}", "Bracketed matcher", "Template for bracketed matcher"));
            addCompletion(new TemplateCompletion(this, "look", "Lookup annotation filter", "{Lookup.${majorType} == \"${city}\"}${cursor}", "Lookup annotation filter", "Template for lookup annotation with filter for features"));
            addCompletion(new TemplateCompletion(this, "tok", "Token annotation filter", "{Token.${category} == \"${NNP}\"}${cursor}", "Token annotation filter", "Template for token annotation with filter for features"));
        }
    }

    public class RhsJapeCompletionProvider extends DefaultCompletionProvider {
        public RhsJapeCompletionProvider() {
            addCompletion(new BasicCompletion(this, "kind"));
            addCompletion(new BasicCompletion(this, "rule"));
            addCompletion(new BasicCompletion(this, "type"));
            addCompletion(new BasicCompletion(this, "source"));

            addCompletion(new TemplateCompletion(this, "rule", "Rule feature", "rule = \"${ruleName}\"${cursor}", "Rule feature", "Template for rule feature"));
            addCompletion(new TemplateCompletion(this, "kind", "Kind feature", "kind = \"${kindName}\"${cursor}", "Kind feature", "Template for kind feature"));
            addCompletion(new TemplateCompletion(this, "type", "Type feature", "type = \"${typeName}\"${cursor}", "Type feature", "Template for type feature"));
        }
    }
}
