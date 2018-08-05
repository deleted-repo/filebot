package net.filebot.ui.rename;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenImpl;
import org.fife.ui.rsyntaxtextarea.TokenMakerBase;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rsyntaxtextarea.modes.GroovyTokenMaker;

public class FormatExpressionTokenMaker extends TokenMakerBase {

	private static final GroovyExpressionTokenMaker groovyTokenMaker = new GroovyExpressionTokenMaker();

	public static final int LANGUAGE_LITERAL = 0;
	public static final int LANGUAGE_GROOVY = 10;

	@Override
	public Token getTokenList(Segment segment, int initialTokenType, int startOffset) {
		resetTokenList();
		groovyTokenMaker.reset();

		int level = getInitialLevel(segment, initialTokenType, startOffset);
		setLanguageIndex(level > 0 ? LANGUAGE_GROOVY : LANGUAGE_LITERAL);

		int start = 0;
		int end = 0;

		// parse expressions and literals
		for (int i = 0; i < segment.length(); i++) {
			switch (segment.charAt(i)) {
			case '{':
				if (level <= 0) {
					if (start != end) {
						addToken(segment, segment.getBeginIndex() + start, segment.getBeginIndex() + end - 1, Token.MARKUP_CDATA, startOffset + start);
					}
					setLanguageIndex(LANGUAGE_GROOVY);
					addToken(segment, segment.getBeginIndex() + i, segment.getBeginIndex() + i, Token.MARKUP_CDATA_DELIMITER, startOffset + i);
					start = end = i + 1;
					level = 0;
				} else {
					end++;
				}
				level++;
				break;
			case '}':
				if (level == 1) {
					if (start != end) {
						Segment groovySegment = new Segment(segment.array, segment.getBeginIndex() + start, end - start);
						addToken(groovyTokenMaker.getTokenList(groovySegment, initialTokenType, startOffset + start));
					}
					addToken(segment, segment.getBeginIndex() + i, segment.getBeginIndex() + i, Token.MARKUP_CDATA_DELIMITER, startOffset + i);
					setLanguageIndex(LANGUAGE_LITERAL);
					start = end = i + 1;
				} else {
					end++;
				}
				level--;
				break;
			default:
				end++;
				break;
			}

		}

		switch (getLanguageIndex()) {
		case LANGUAGE_GROOVY:
			if (start != end) {
				Segment groovySegment = new Segment(segment.array, segment.getBeginIndex() + start, end - start);
				addToken(groovyTokenMaker.getTokenList(groovySegment, initialTokenType, startOffset + start));
			}
			if (firstToken == null) {
				addToken(segment, segment.getBeginIndex() + start, segment.getBeginIndex() + end - 1, initialTokenType, startOffset + start);
			}
			break;
		default:
			if (start != end) {
				addToken(segment, segment.getBeginIndex() + start, segment.getBeginIndex() + end - 1, Token.MARKUP_CDATA, startOffset + start);
			}
			addNullToken();
			break;
		}

		return firstToken;
	}

	protected int getInitialLevel(Segment segment, int initialTokenType, int startOffset) {
		return initialTokenType == TokenTypes.NULL ? 0 : 1;
	}

	protected void addToken(Token token) {
		Token tail = token.getNextToken();

		if (firstToken == null) {
			firstToken = (TokenImpl) token;
			currentToken = firstToken;
		} else {
			TokenImpl next = (TokenImpl) token;
			currentToken.setNextToken(next);
			previousToken = currentToken;
			currentToken = next;
		}

		currentToken.setLanguageIndex(getLanguageIndex());
		currentToken.setHyperlink(false);

		// new current token must be the tail
		currentToken.setNextToken(null);

		if (tail != null && tail.getType() != TokenTypes.NULL) {
			addToken(tail);
		}
	}

	@Override
	public boolean getCurlyBracesDenoteCodeBlocks(int languageIndex) {
		return true;
	}

	@Override
	public String[] getLineCommentStartAndEnd(int languageIndex) {
		switch (languageIndex) {
		case LANGUAGE_GROOVY:
			return groovyTokenMaker.getLineCommentStartAndEnd(0);
		default:
			return null;
		}
	}

	@Override
	public boolean getShouldIndentNextLineAfter(Token token) {
		return groovyTokenMaker.getShouldIndentNextLineAfter(token);
	}

	private static class GroovyExpressionTokenMaker extends GroovyTokenMaker {

		@Override
		protected void resetTokenList() {
			// reset list structure, but do not reset shared token objects
			firstToken = currentToken = previousToken = null;
		}

		protected void reset() {
			// reset list structure and reset shared token objects
			super.resetTokenList();
		}
	}

}
