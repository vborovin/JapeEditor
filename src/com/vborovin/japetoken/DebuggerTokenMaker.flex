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
 * Scanner for debug output text.
 *
 * @author Vladislav Borovin
 * @version 1.0
 *
 */
%%

%public
%class DebuggerTokenMaker
%extends AbstractJFlexTokenMaker
%unicode
%type org.fife.ui.rsyntaxtextarea.Token


%{


	/**
	 * Constructor.  This must be here because JFlex does not generate a
	 * no-parameter constructor.
	 */
	public DebuggerTokenMaker() {
	}

	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		int so = zzStartRead + offsetShift;
		super.addToken(zzBuffer, zzStartRead,zzMarkedPos-1, tokenType, so, false);
		zzStartRead = zzMarkedPos;
	}

	/**
	 * Returns the text to place at the beginning and end of a
	 * line to "comment" it in a this programming language.
	 *
	 * @return <code>null</code>, as there are no comments in plain text.
	 */
	@Override
	public String[] getLineCommentStartAndEnd(int languageIndex) {
		return null;
	}


	/**
	 * Always returns <tt>false</tt>, as you never want "mark occurrences"
	 * working in plain text files.
	 *
	 * @param type The token type.
	 * @return Whether tokens of this type should have "mark occurrences"
	 *         enabled.
	 */
	public boolean getMarkOccurrencesOfTokenType(int type) {
		return false;
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

		resetTokenList();
		this.offsetShift = -text.offset + startOffset;

		// Start off in the proper state.
		s = text;
		try {
			yyreset(zzReader);
			yybegin(YYINITIAL);
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
	 * @exception   IOException  if any I/O-Error occurs.
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
	public final void yyreset(java.io.Reader reader) {
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


%}

LetterOrDigit	= ([a-zA-Z0-9])
Identifier		= ({LetterOrDigit}+)
Separator		= ([^a-zA-Z0-9 \t\n])
WhiteSpace		= ([ \t]+)
LineTerminator	= (\n+)
CarriageReturn  = (\r+)
BlockBegin      = "{{{"
BlockEnd        = "}}}"
NotFoundString  = "Not found"
FoundKeyword    = ({BlockBegin}Found{BlockEnd})
NotFoundKeyword = ({BlockBegin}{NotFoundString}{BlockEnd})

%%

<YYINITIAL> {
    {FoundKeyword}      { addToken(Token.OPERATOR); }
    {NotFoundKeyword}   { addToken(Token.PREPROCESSOR);}
    {BlockBegin}|
    {BlockEnd}          { addToken(Token.IDENTIFIER); }
	{Identifier}		{ addToken(Token.IDENTIFIER); }
	{Separator}			{ addToken(Token.IDENTIFIER); }

	{LineTerminator}	{ addToken(Token.NULL); }
	{CarriageReturn}    {}
	{WhiteSpace}		{ addToken(Token.WHITESPACE); }

	<<EOF>>				{ addToken(Token.NULL); return firstToken; }
	.					{ /* Never happens */ addToken(Token.IDENTIFIER); }
}