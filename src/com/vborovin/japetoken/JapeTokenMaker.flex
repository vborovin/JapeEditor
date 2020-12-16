/*
 * JapeTokenMaker. Based on default JavaTokenMaker.
 */
package com.vborovin.japetoken;

import java.io.*;
import javax.swing.text.Segment;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rsyntaxtextarea.*;


/**
 * Scanner for JAPE and Java programming languages.<p>
 * @author Vladislav Borovin (JAPE), Robert Futrell (Java)
 * @version 1.0
 */
%%

%public
%class JapeTokenMaker
%extends AbstractJFlexCTokenMaker
%unicode
%type org.fife.ui.rsyntaxtextarea.Token


%{
    private int previousState;
    private int zzPushbackPos;
    private static final Pattern MLC_PATTERN = Pattern.compile("([ \\t]*)(/?[\\*])([ \\t]*)");

	/**
	 * Constructor.  This must be here because JFlex does not generate a
	 * no-parameter constructor.
	 */
	public JapeTokenMaker() {
	    //setTokenFactory(new ExtendedTokenFactory());
	}

	private boolean isInternalEolTokenForMLCs(Token t) {
        int type = t.getType();
        if (type >= 0) {
            return false;
        } else {
            type = this.getClosestStandardTokenTypeForInternalType(type);
            return type == 2 || type == 3;
        }
    }

    @Override
	protected Action createInsertBreakAction() {
            return new JapeInsertBreakAction();
    }

    protected class JapeInsertBreakAction extends RSyntaxTextAreaEditorKit.InsertBreakAction {
        protected JapeInsertBreakAction() {
        }

        public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
            if (textArea.isEditable() && textArea.isEnabled()) {
                RSyntaxTextArea rsta = (RSyntaxTextArea)this.getTextComponent(e);
                RSyntaxDocument doc = (RSyntaxDocument)rsta.getDocument();
                int line = textArea.getCaretLineNumber();
                int type = doc.getLastTokenTypeOnLine(line);
                if (type < 0) {
                    type = doc.getClosestStandardTokenTypeForInternalType(type);
                }

                if (type != 2) {
                    this.handleInsertBreak(rsta, true);
                } else {
                    this.insertBreakInMLC(e, rsta, line);
                }

            } else {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }

        private boolean appearsNested(RSyntaxTextArea textArea, int line, int offs) {
            int firstLine = line;

            Token t;
            do {
                int ix;
                while(true) {
                    if (line >= textArea.getLineCount()) {
                        return true;
                    }

                    t = textArea.getTokenListForLine(line);
                    boolean i = false;
                    if (line++ == firstLine) {
                        t = RSyntaxUtilities.getTokenAtOffset(t, offs);
                        if (t == null) {
                            continue;
                        }

                        ix = t.documentToToken(offs);
                        break;
                    }

                    ix = t.getTextOffset();
                    break;
                }

                for(int textOffset = t.getTextOffset(); ix < textOffset + t.length() - 1; ++ix) {
                    if (t.charAt(ix - textOffset) == '/' && t.charAt(ix - textOffset + 1) == '*') {
                        return true;
                    }
                }
            } while((t = t.getNextToken()) == null || JapeTokenMaker.this.isInternalEolTokenForMLCs(t));

            return false;
        }

        private void insertBreakInMLC(ActionEvent e, RSyntaxTextArea textArea, int line) {
            Matcher m = null;
            boolean startx = true;
            boolean endx = true;
            String text = null;

            int start;
            int end;
            try {
                start = textArea.getLineStartOffset(line);
                end = textArea.getLineEndOffset(line);
                text = textArea.getText(start, end - start);
                m = MLC_PATTERN.matcher(text);
            } catch (BadLocationException var14) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                var14.printStackTrace();
                return;
            }

            if (m.lookingAt()) {
                String leadingWS = m.group(1);
                String mlcMarker = m.group(2);
                int dot = textArea.getCaretPosition();
                boolean moved;
                if (dot >= start && dot < start + leadingWS.length() + mlcMarker.length()) {
                    if (mlcMarker.charAt(0) == '/') {
                        this.handleInsertBreak(textArea, true);
                        return;
                    }

                    textArea.setCaretPosition(end - 1);
                } else {
                    for(moved = false; dot < end - 1 && Character.isWhitespace(text.charAt(dot - start)); ++dot) {
                        moved = true;
                    }

                    if (moved) {
                        textArea.setCaretPosition(dot);
                    }
                }

                moved = mlcMarker.charAt(0) == '/';
                boolean nested = this.appearsNested(textArea, line, start + leadingWS.length() + 2);
                String header = leadingWS + (moved ? " * " : "*") + m.group(3);
                textArea.replaceSelection("\n" + header);
                if (nested) {
                    dot = textArea.getCaretPosition();
                    textArea.insert("\n" + leadingWS + " */", dot);
                    textArea.setCaretPosition(dot);
                }
            } else {
                this.handleInsertBreak(textArea, true);
            }

        }
    }


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addToken(int, int, int)
	 */
	private void addHyperlinkToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, true);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addHyperlinkToken(int, int, int)
	 */
	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, false);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param array The character array.
	 * @param start The starting offset in the array.
	 * @param end The ending offset in the array.
	 * @param tokenType The token's type.
	 * @param startOffset The offset in the document at which this token
	 *                    occurs.
	 * @param hyperlink Whether this token is a hyperlink.
	 */
	@Override
	public void addToken(char[] array, int start, int end, int tokenType,
						int startOffset, boolean hyperlink) {
		super.addToken(array, start,end, tokenType, startOffset, hyperlink);
		zzStartRead = zzMarkedPos;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getLineCommentStartAndEnd(int languageIndex) {
		return new String[] { "//", null };
	}


	/**
	 * Returns the first token in the linked list of tokens generated
	 * from <code>text</code>.  This method must be implemented by
	 * subclasses so they can correctly implement syntax highlighting.
	 *
	 * @param text The text from which to get tokens.
	 * @param initialTokenType The token type we should start with.
	 * @param startOffset The offset into the document at which
	 *        <code>text</code> starts.
	 * @return The first <code>Token</code> in a linked list representing
	 *         the syntax highlighted text.
	 */
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

		states.clear();
        resetTokenList();
        this.offsetShift = -text.offset + startOffset;

        // Start off in the proper state.
        int state = YYINITIAL;

        s = text;
        try {
            yyreset(zzReader);
            yypushstate(state);
            return yylex();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return new TokenImpl();
        }
	}


	/**
	 * Refills the input buffer.
	 *
	 * @return      <code>true</code> if EOF was reached, otherwise
	 *              <code>false</code>.
	 */
	private boolean zzRefill() {
		return zzCurrentPos>=s.offset+s.count;
	}


	/**
	 * Resets the scanner to read from a new input stream.
	 * Does not close the old reader.
	 *
	 * All internal variables are reset, the old input stream
	 * <b>cannot</b> be reused (internal buffer is discarded and lost).
	 * Lexical state is set to <tt>YY_INITIAL</tt>.
	 *
	 * @param reader   the new input stream
	 */
	public final void yyreset(Reader reader) {
		// 's' has been updated.
		zzBuffer = s.array;
		/*
		 * We replaced the line below with the two below it because zzRefill
		 * no longer "refills" the buffer (since the way we do it, it's always
		 * "full" the first time through, since it points to the segment's
		 * array).  So, we assign zzEndRead here.
		 */
		//zzStartRead = zzEndRead = s.offset;
		zzStartRead = s.offset;
		zzEndRead = zzStartRead + s.count - 1;
		zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
		zzLexicalState = YYINITIAL;
		zzReader = reader;
		zzAtBOL  = true;
		zzAtEOF  = false;
	}

	private final LinkedList<Integer> states = new LinkedList();

	public void yypushstate(int newState) {
	    states.addFirst(zzLexicalState);
	    yybegin(newState);
	}

	private void yypopstate() {
        final int state = states.removeFirst();
        yybegin(state);
    }

%}

Letter						= ([A-Za-z])
LetterOrUnderscore			= ({Letter}|"_")
Underscores					= ([_]+)
NonzeroDigit				= ([1-9])
BinaryDigit					= ([0-1])
Digit						= ("0"|{NonzeroDigit})
HexDigit					= ({Digit}|[A-Fa-f])
OctalDigit					= ([0-7])
AnyCharacterButApostropheOrBackSlash	= ([^\\'])
AnyCharacterButDoubleQuoteOrBackSlash	= ([^\\\"\n])
StringSymbol                = ([\"])
EscapedSourceCharacter		= ("u"{HexDigit}{HexDigit}{HexDigit}{HexDigit})
Escape						= ("\\"(([btnfr\"'\\])|([0123]{OctalDigit}?{OctalDigit}?)|({OctalDigit}{OctalDigit}?)|{EscapedSourceCharacter}))
NonSeparator				= ([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\']|"#"|"\\")
IdentifierStart				= ({LetterOrUnderscore}|"$")
IdentifierPart				= ({IdentifierStart}|{Digit}|("\\"{EscapedSourceCharacter}))

LineTerminator				= (\n+)
CarriageReturn              = (\r+)
WhiteSpace				    = ([ \t\f])
EmptySpace                  = ({LineTerminator}|{WhiteSpace})

CharLiteral				    = ([\']({AnyCharacterButApostropheOrBackSlash}|{Escape})[\'])
UnclosedCharLiteral			= ([\'][^\'\n]*)
ErrorCharLiteral			= ({UnclosedCharLiteral}[\'])
StringLiteral				= ([\"]({AnyCharacterButDoubleQuoteOrBackSlash}|{Escape})*[\"])
UnclosedStringLiteral		= ([\"]([\\].|[^\\\"])*[^\"]?)
ErrorStringLiteral			= ({UnclosedStringLiteral}[\"])

MLCBegin					= "/*"
MLCEnd					    = "*/"
LineCommentBegin			= "//"

DigitOrUnderscore			= ({Digit}|[_])
DigitsAndUnderscoresEnd		= ({DigitOrUnderscore}*{Digit})
IntegerHelper				= (({NonzeroDigit}{DigitsAndUnderscoresEnd}?)|"0")
IntegerLiteral				= ({IntegerHelper}[lL]?)

BinaryDigitOrUnderscore		= ({BinaryDigit}|[_])
BinaryDigitsAndUnderscores	= ({BinaryDigit}({BinaryDigitOrUnderscore}*{BinaryDigit})?)
BinaryLiteral				= ("0"[bB]{BinaryDigitsAndUnderscores})

HexDigitOrUnderscore		= ({HexDigit}|[_])
HexDigitsAndUnderscores		= ({HexDigit}({HexDigitOrUnderscore}*{HexDigit})?)
OctalDigitOrUnderscore		= ({OctalDigit}|[_])
OctalDigitsAndUnderscoresEnd= ({OctalDigitOrUnderscore}*{OctalDigit})
HexHelper					= ("0"(([xX]{HexDigitsAndUnderscores})|({OctalDigitsAndUnderscoresEnd})))
HexLiteral					= ({HexHelper}[lL]?)

FloatHelper1				= ([fFdD]?)
FloatHelper2				= ([eE][+-]?{Digit}+{FloatHelper1})
FloatLiteral1				= ({Digit}+"."({FloatHelper1}|{FloatHelper2}|{Digit}+({FloatHelper1}|{FloatHelper2})))
FloatLiteral2				= ("."{Digit}+({FloatHelper1}|{FloatHelper2}))
FloatLiteral3				= ({Digit}+{FloatHelper2})
FloatLiteral				= ({FloatLiteral1}|{FloatLiteral2}|{FloatLiteral3}|({Digit}+[fFdD]))

ErrorNumberFormat			= (({IntegerLiteral}|{HexLiteral}|{FloatLiteral}){NonSeparator}+)
BooleanLiteral				= ("true"|"false")

BraceLeft                   = "{"
BraceRight                  = "}"
ParenthesisLeft             = "("
ParenthesisRight            = ")"
SquareBracketLeft           = "["
SquareBracketRight          = "]"

SeparatorWithoutBraces      = ({ParenthesisLeft}|{ParenthesisRight}|{SquareBracketLeft}|{SquareBracketRight})
Separator					= ({BraceLeft}|{BraceRight}|{SeparatorWithoutBraces})
Separator2				    = ([\;,.])
Colon                       = ":"

NonAssignmentOperator		= ("+"|"-"|"<="|"^"|"++"|"<"|"*"|">="|"%"|"--"|">"|"/"|"!="|"?"|">>"|"!"|"&"|"=="|":"|">>"|"~"|"|"|"&&"|">>>"|"=~"|"!~"|"==~"|"!=~"|"contains"|"notContains"|"within"|"notWithin")
AssignmentOperator			= ("="|"-="|"*="|"/="|"|="|"&="|"^="|"+="|"%="|"<<="|">>="|">>>=")
Operator					= ({NonAssignmentOperator}|{AssignmentOperator})

CurrentBlockTag				= ("author"|"deprecated"|"exception"|"param"|"return"|"see"|"serial"|"serialData"|"serialField"|"since"|"throws"|"version")
ProposedBlockTag			= ("category"|"example"|"tutorial"|"index"|"exclude"|"todo"|"internal"|"obsolete"|"threadsafety")
BlockTag					= ({CurrentBlockTag}|{ProposedBlockTag})
InlineTag					= ("code"|"docRoot"|"inheritDoc"|"link"|"linkplain"|"literal"|"value")

Identifier				    = ({IdentifierStart}{IdentifierPart}*)
ErrorIdentifier			    = ({NonSeparator}+)

Annotation				    = ("@"{Identifier}?)

JavaDataType                = ("boolean"|"byte"|"char"|"double"|"float"|"int"|"long"|"short")

RHSArrow                    = "-->"
JapeLHSLabel                = ({Colon}{Identifier})
JapeRHSLabel                = ({JapeLHSLabel}(\.{Identifier})?)
JapeRHSAssignment           = (\={EmptySpace}*\{)

JapeKeyword                 = (("Rule"|"Macro"|"Phase"|"Input"|"Options"|"Priority"|"MultiPhase"|"Phases"|"Template"|"Imports"){Colon})
JapeReservedWord            = ({RHSArrow}|{JapeKeyword})

JapeBasicAnnotation         = ("Token"|"Lookup"|"Person"|"SpaceToken"|"Organization"|"Location")
JapeTemplateVariable        = ("$"{BraceLeft}{Identifier}{BraceRight})

%state MLC
%state JAVA_BLOCK
%state EOL_COMMENT
%state RHS_ASSIGNMENT
%state ANNOTATION_REGION
%state STRING_STATE

%%

<YYINITIAL> {

    //Проверить
    {JapeRHSAssignment}     { yypushback(yylength()); yypushstate(RHS_ASSIGNMENT); }

    {JapeReservedWord}      { addToken(Token.RESERVED_WORD); }

    {BooleanLiteral}        { addToken(Token.LITERAL_BOOLEAN); }

    //{LHSLABEL}            {ADDTO4T0NIBYD' SPISOK LABELOV + PUSHBACK}

    {Colon}|
    {Identifier}		    { addToken(Token.IDENTIFIER); }

    {LineTerminator}	    { addToken(Token.NULL); }
    {CarriageReturn}        {}
    {WhiteSpace}+			{ addToken(Token.WHITESPACE); }

    /* Comment literals. */
    "/**/"					{ addToken(Token.COMMENT_MULTILINE); }
    {MLCBegin}				{ addToken(Token.COMMENT_MULTILINE); yypushstate(MLC); }
    {LineCommentBegin}		{ addToken(Token.COMMENT_EOL); yypushstate(EOL_COMMENT); }

    {BraceLeft}             { addToken(Token.SEPARATOR); yypushstate(JAVA_BLOCK); }
    {ParenthesisLeft}       { addToken(Token.SEPARATOR); yypushstate(ANNOTATION_REGION); }

    /* Numbers */
    {IntegerLiteral}	    { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
    {BinaryLiteral}		    { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
    {HexLiteral}			{ addToken(Token.LITERAL_NUMBER_HEXADECIMAL); }
    {FloatLiteral}			{ addToken(Token.LITERAL_NUMBER_FLOAT); }

    <<EOF>>					{ addToken(Token.NULL); return firstToken; }

    /* Unhandled */
    .						{ addToken(Token.ERROR_IDENTIFIER); }
}

<ANNOTATION_REGION> {

    {ParenthesisLeft}       { addToken(Token.SEPARATOR); yypushstate(ANNOTATION_REGION); }
    {ParenthesisRight}      { addToken(Token.SEPARATOR); yypopstate(); }
    {BraceLeft}|
    {BraceRight}            { addToken(Token.SEPARATOR); }

    {BooleanLiteral}        { addToken(Token.LITERAL_BOOLEAN); }

    {JapeBasicAnnotation}   { addToken(Token.FUNCTION); }
    {Identifier}            { addToken(Token.IDENTIFIER); }

    {Separator2}            { addToken(Token.IDENTIFIER); }
    {LineTerminator}	    { addToken(Token.NULL); }
    {CarriageReturn}        {}
    {WhiteSpace}+			{ addToken(Token.WHITESPACE); }

    /* Comment literals. */
    "/**/"					{ addToken(Token.COMMENT_MULTILINE); }
    {MLCBegin}				{ addToken(Token.COMMENT_MULTILINE); yypushstate(MLC); }
    {LineCommentBegin}		{ addToken(Token.COMMENT_EOL); yypushstate(EOL_COMMENT); }

    /* String/Character literals. */
    {CharLiteral}			{ addToken(Token.LITERAL_CHAR); }
    {StringSymbol}			{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); yypushstate(STRING_STATE); }

    {Separator2}            { addToken(Token.IDENTIFIER); }

    /* Operators */
    {Operator}              { addToken(Token.OPERATOR); }

    /* Numbers */
    {IntegerLiteral}	    { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
    {BinaryLiteral}		    { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
    {HexLiteral}			{ addToken(Token.LITERAL_NUMBER_HEXADECIMAL); }
    {FloatLiteral}			{ addToken(Token.LITERAL_NUMBER_FLOAT); }

    {ErrorIdentifier}		{ addToken(Token.ERROR_IDENTIFIER); }

    <<EOF>>					{ addToken(Token.NULL); return firstToken; }

    /* Unhandled */
    .						{ addToken(Token.ERROR_IDENTIFIER); }

}

<RHS_ASSIGNMENT> {

    {JapeRHSLabel}          { addToken(Token.IDENTIFIER); }
    {JapeBasicAnnotation}   { addToken(Token.FUNCTION); }
    {Identifier}            { addToken(Token.IDENTIFIER); }

    {BraceLeft}             { addToken(Token.SEPARATOR); }
    {BraceRight}            { addToken(Token.SEPARATOR); yypopstate(); }

    {LineTerminator}        { addToken(Token.NULL); }
    {CarriageReturn}        {}
    {WhiteSpace}+           { addToken(Token.WHITESPACE); }

    /* Comment literals. */
    "/**/"					{ addToken(Token.COMMENT_MULTILINE); }
    {MLCBegin}				{ addToken(Token.COMMENT_MULTILINE); yypushstate(MLC); }
    {LineCommentBegin}		{ addToken(Token.COMMENT_EOL); yypushstate(EOL_COMMENT); }

    /* String/Character literals. */
    {CharLiteral}			{ addToken(Token.LITERAL_CHAR); }
    {StringSymbol}			{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); yypushstate(STRING_STATE); }

    {Separator2}            { addToken(Token.IDENTIFIER); }

    /* Operators */
    {Operator}              { addToken(Token.OPERATOR); }

    /* Numbers */
    {IntegerLiteral}		{ addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
    {BinaryLiteral}			{ addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
    {HexLiteral}			{ addToken(Token.LITERAL_NUMBER_HEXADECIMAL); }
    {FloatLiteral}			{ addToken(Token.LITERAL_NUMBER_FLOAT); }
    {ErrorNumberFormat}		{ addToken(Token.ERROR_NUMBER_FORMAT); }

    {ErrorIdentifier}		{ addToken(Token.ERROR_IDENTIFIER); }

    <<EOF>>					{ addToken(Token.NULL); return firstToken; }

    /* Unhandled */
    .						{ addToken(Token.ERROR_IDENTIFIER); }
}

//TODO:Добавить стандартные классы GATE
<JAVA_BLOCK> {

    /* Keywords */
    "abstract"|
    "assert" |
    "break"	 |
    "case"	 |
    "catch"	 |
    "class"	 |
    "const"	 |
    "continue" |
    "default" |
    "do"	 |
    "else"	 |
    "enum"	 |
    "extends" |
    "final"	 |
    "finally" |
    "for"	 |
    "goto"	 |
    "if"	 |
    "implements" |
    "import" |
    "instanceof" |
    "interface" |
    "native" |
    "new"	 |
    "null"	 |
    "package" |
    "private" |
    "protected" |
    "public" |
    "static" |
    "strictfp" |
    "super"	 |
    "switch" |
    "synchronized" |
    "this"	 |
    "throw"	 |
    "throws" |
    "transient" |
    "try"	 |
    "void"	 |
    "volatile" |
    "while"					    { addToken(Token.RESERVED_WORD); }
    "return"				    { addToken(Token.RESERVED_WORD); }

    /* Data types. */
    "boolean" |
    "byte" |
    "char" |
    "double" |
    "float" |
    "int" |
    "long" |
    "short"					    { addToken(Token.DATA_TYPE); }

    /* Booleans. */
    {BooleanLiteral}		    { addToken(Token.LITERAL_BOOLEAN); }

    /* java.lang classes */
    "Appendable" |
    "AutoCloseable" |
    "CharSequence" |
    "Cloneable" |
    "Comparable" |
    "Iterable" |
    "Readable" |
    "Runnable" |
    "Thread.UncaughtExceptionHandler" |
    "Boolean" |
    "Byte" |
    "Character" |
    "Character.Subset" |
    "Character.UnicodeBlock" |
    "Class" |
    "ClassLoader" |
    "ClassValue" |
    "Compiler" |
    "Double" |
    "Enum" |
    "Float" |
    "InheritableThreadLocal" |
    "Integer" |
    "Long" |
    "Math" |
    "Number" |
    "Object" |
    "Package" |
    "Process" |
    "ProcessBuilder" |
    "ProcessBuilder.Redirect" |
    "Runtime" |
    "RuntimePermission" |
    "SecurityManager" |
    "Short" |
    "StackTraceElement" |
    "StrictMath" |
    "String" |
    "StringBuffer" |
    "StringBuilder" |
    "System" |
    "Thread" |
    "ThreadGroup" |
    "ThreadLocal" |
    "Throwable" |
    "Void" |
    "Character.UnicodeScript" |
    "ProcessBuilder.Redirect.Type" |
    "Thread.State" |
    "ArithmeticException" |
    "ArrayIndexOutOfBoundsException" |
    "ArrayStoreException" |
    "ClassCastException" |
    "ClassNotFoundException" |
    "CloneNotSupportedException" |
    "EnumConstantNotPresentException" |
    "Exception" |
    "IllegalAccessException" |
    "IllegalArgumentException" |
    "IllegalMonitorStateException" |
    "IllegalStateException" |
    "IllegalThreadStateException" |
    "IndexOutOfBoundsException" |
    "InstantiationException" |
    "InterruptedException" |
    "NegativeArraySizeException" |
    "NoSuchFieldException" |
    "NoSuchMethodException" |
    "NullPointerException" |
    "NumberFormatException" |
    "RuntimeException" |
    "SecurityException" |
    "StringIndexOutOfBoundsException" |
    "TypeNotPresentException" |
    "UnsupportedOperationException" |
    "AbstractMethodError" |
    "AssertionError" |
    "BootstrapMethodError" |
    "ClassCircularityError" |
    "ClassFormatError" |
    "Error" |
    "ExceptionInInitializerError" |
    "IllegalAccessError" |
    "IncompatibleClassChangeError" |
    "InstantiationError" |
    "InternalError" |
    "LinkageError" |
    "NoClassDefFoundError" |
    "NoSuchFieldError" |
    "NoSuchMethodError" |
    "OutOfMemoryError" |
    "StackOverflowError" |
    "ThreadDeath" |
    "UnknownError" |
    "UnsatisfiedLinkError" |
    "UnsupportedClassVersionError" |
    "VerifyError" |
    "VirtualMachineError" |

    /* java.io classes*/
    "Closeable" |
    "DataInput" |
    "DataOutput" |
    "Externalizable" |
    "FileFilter" |
    "FilenameFilter" |
    "Flushable" |
    "ObjectInput" |
    "ObjectInputValidation" |
    "ObjectOutput" |
    "ObjectStreamConstants" |
    "Serializable" |

    "BufferedInputStream" |
    "BufferedOutputStream" |
    "BufferedReader" |
    "BufferedWriter" |
    "ByteArrayInputStream" |
    "ByteArrayOutputStream" |
    "CharArrayReader" |
    "CharArrayWriter" |
    "Console" |
    "DataInputStream" |
    "DataOutputStream" |
    "File" |
    "FileDescriptor" |
    "FileInputStream" |
    "FileOutputStream" |
    "FilePermission" |
    "FileReader" |
    "FileWriter" |
    "FilterInputStream" |
    "FilterOutputStream" |
    "FilterReader" |
    "FilterWriter" |
    "InputStream" |
    "InputStreamReader" |
    "LineNumberInputStream" |
    "LineNumberReader" |
    "ObjectInputStream" |
    "ObjectInputStream.GetField" |
    "ObjectOutputStream" |
    "ObjectOutputStream.PutField" |
    "ObjectStreamClass" |
    "ObjectStreamField" |
    "OutputStream" |
    "OutputStreamWriter" |
    "PipedInputStream" |
    "PipedOutputStream" |
    "PipedReader" |
    "PipedWriter" |
    "PrintStream" |
    "PrintWriter" |
    "PushbackInputStream" |
    "PushbackReader" |
    "RandomAccessFile" |
    "Reader" |
    "SequenceInputStream" |
    "SerializablePermission" |
    "StreamTokenizer" |
    "StringBufferInputStream" |
    "StringReader" |
    "StringWriter" |
    "Writer" |

    "CharConversionException" |
    "EOFException" |
    "FileNotFoundException" |
    "InterruptedIOException" |
    "InvalidClassException" |
    "InvalidObjectException" |
    "IOException" |
    "NotActiveException" |
    "NotSerializableException" |
    "ObjectStreamException" |
    "OptionalDataException" |
    "StreamCorruptedException" |
    "SyncFailedException" |
    "UncheckedIOException" |
    "UnsupportedEncodingException" |
    "UTFDataFormatException" |
    "WriteAbortedException" |

    "IOError" |

    /* java.util classes */
    "Collection" |
    "Comparator" |
    "Deque" |
    "Enumeration" |
    "EventListener" |
    "Formattable" |
    "Iterator" |
    "List" |
    "ListIterator" |
    "Map" |
    "Map.Entry" |
    "NavigableMap" |
    "NavigableSet" |
    "Observer" |
    "PrimitiveIterator" |
    "PrimitiveIterator.OfDouble" |
    "PrimitiveIterator.OfInt" |
    "PrimitiveIterator.OfLong" |
    "Queue" |
    "RandomAccess" |
    "Set" |
    "SortedMap" |
    "SortedSet" |
    "Spliterator" |
    "Spliterator.OfDouble" |
    "Spliterator.OfInt" |
    "Spliterator.OfLong" |
    "Spliterator.OfPrimitive" |

    "AbstractCollection" |
    "AbstractList" |
    "AbstractMap" |
    "AbstractMap.SimpleEntry" |
    "AbstractMap.SimpleImmutableEntry" |
    "AbstractQueue" |
    "AbstractSequentialList" |
    "AbstractSet" |
    "ArrayDeque" |
    "ArrayList" |
    "Arrays" |
    "Base64" |
    "Base64.Decoder" |
    "Base64.Encoder" |
    "BitSet" |
    "Calendar" |
    "Calendar.Builder" |
    "Collections" |
    "Currency" |
    "Date" |
    "Dictionary" |
    "DoubleSummaryStatistics" |
    "EnumMap" |
    "EnumSet" |
    "EventListenerProxy" |
    "EventObject" |
    "FormattableFlags" |
    "Formatter" |
    "GregorianCalendar" |
    "HashMap" |
    "HashSet" |
    "Hashtable" |
    "IdentityHashMap" |
    "IntSummaryStatistics" |
    "LinkedHashMap" |
    "LinkedHashSet" |
    "LinkedList" |
    "ListResourceBundle" |
    "Locale" |
    "Locale.Builder" |
    "Locale.LanguageRange" |
    "LongSummaryStatistics" |
    "Objects" |
    "Observable" |
    "Optional" |
    "OptionalDouble" |
    "OptionalInt" |
    "OptionalLong" |
    "PriorityQueue" |
    "Properties" |
    "PropertyPermission" |
    "PropertyResourceBundle" |
    "Random" |
    "ResourceBundle" |
    "ResourceBundle.Control" |
    "Scanner" |
    "ServiceLoader" |
    "SimpleTimeZone" |
    "Spliterators" |
    "Spliterators.AbstractDoubleSpliterator" |
    "Spliterators.AbstractIntSpliterator" |
    "Spliterators.AbstractLongSpliterator" |
    "Spliterators.AbstractSpliterator" |
    "SpliteratorRandom" |
    "Stack" |
    "StringJoiner" |
    "StringTokenizer" |
    "Timer" |
    "TimerTask" |
    "TimeZone" |
    "TreeMap" |
    "TreeSet" |
    "UUID" |
    "Vector" |
    "WeakHashMap" |

    "Formatter.BigDecimalLayoutForm" |
    "Locale.Category" |
    "Locale.FilteringMode" |

    "ConcurrentModificationException" |
    "DuplicateFormatFlagsException" |
    "EmptyStackException" |
    "FormatFlagsConversionMismatchException" |
    "FormatterClosedException" |
    "IllegalFormatCodePointException" |
    "IllegalFormatConversionException" |
    "IllegalFormatException" |
    "IllegalFormatFlagsException" |
    "IllegalFormatPrecisionException" |
    "IllegalFormatWidthException" |
    "IllformedLocaleException" |
    "InputMismatchException" |
    "InvalidPropertiesFormatException" |
    "MissingFormatArgumentException" |
    "MissingFormatWidthException" |
    "MissingResourceException" |
    "NoSuchElementException" |
    "TooManyListenersException" |
    "UnknownFormatConversionException" |
    "UnknownFormatFlagsException" |

    "ServiceConfigurationError" { addToken(Token.FUNCTION); }

    {Identifier}                { addToken(Token.IDENTIFIER); }

    {LineTerminator}		    { addToken(Token.NULL); }
    {CarriageReturn}            {}
    {WhiteSpace}+			    { addToken(Token.WHITESPACE); }

    /* String/Character literals. */
    {CharLiteral}			    { addToken(Token.LITERAL_CHAR); }
    {StringSymbol}			    { addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); yypushstate(STRING_STATE); }

    /* Comment literals. */
    "/**/"					    { addToken(Token.COMMENT_MULTILINE); }
    {MLCBegin}				    { addToken(Token.COMMENT_MULTILINE); yypushstate(MLC); }
    {LineCommentBegin}		    { addToken(Token.COMMENT_EOL); yypushstate(EOL_COMMENT); }

    /* Annotations. */
    {Annotation}			    { addToken(Token.ANNOTATION); }

    {BraceLeft}                 { addToken(Token.SEPARATOR); yypushstate(JAVA_BLOCK); }
    {BraceRight}                { addToken(Token.SEPARATOR); yypopstate(); }
    {SeparatorWithoutBraces}    { addToken(Token.SEPARATOR); }
    {Separator2}			    { addToken(Token.IDENTIFIER); }

    /* Operators. */
    {Operator}			        { addToken(Token.OPERATOR); }

    /* Numbers */
    {IntegerLiteral}		    { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
    {BinaryLiteral}			    { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
    {HexLiteral}			    { addToken(Token.LITERAL_NUMBER_HEXADECIMAL); }
    {FloatLiteral}			    { addToken(Token.LITERAL_NUMBER_FLOAT); }
    {ErrorNumberFormat}		    { addToken(Token.ERROR_NUMBER_FORMAT); }

    {ErrorIdentifier}		    { addToken(Token.ERROR_IDENTIFIER); }

    /* Ended with a line not in a string or comment. */
    <<EOF>>					    { addToken(Token.NULL); return firstToken; }

    /* Catch any other (unhandled) characters and flag them as identifiers. */
    .							{ addToken(Token.ERROR_IDENTIFIER); }

}

<MLC> {

    {MLCBegin}              { addToken(Token.COMMENT_MULTILINE); yypushstate(MLC); }
    [^\/\*\r\n]+			{ addToken(Token.COMMENT_MULTILINE); }
    {MLCEnd}				{ addToken(Token.COMMENT_MULTILINE); yypopstate(); }
    [\*\/\/]                { addToken(Token.COMMENT_MULTILINE); }

	{LineTerminator}		{ addToken(Token.NULL); }

	<<EOF>>					{ addToken(Token.COMMENT_MULTILINE); return firstToken; }
	.                       { addToken(Token.COMMENT_MULTILINE); }

}

<EOL_COMMENT> {

	[^\n]+  				{ addToken(Token.COMMENT_EOL); }

	{LineTerminator}	    { addToken(Token.NULL); yypopstate(); }

	<<EOF>>					{ addToken(Token.COMMENT_EOL); return firstToken; }
	.                       { addToken(Token.COMMENT_EOL); }
}

<STRING_STATE> {

    {StringSymbol}          { addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); yypopstate(); }
    [^\n\"]+  				{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }

    {LineTerminator}	    { addToken(Token.NULL); }

    <<EOF>>					{ addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); return firstToken; }
    .                       { addToken(Token.LITERAL_STRING_DOUBLE_QUOTE); }
}