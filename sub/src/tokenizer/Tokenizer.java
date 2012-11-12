package tokenizer;

import nfa.NFA;
import nfa.State;
import nfa.Transition;

import java.io.*;
import java.util.Iterator;

/**
 * Tokenizes an input stream. The token type is the name of the final state.
 * 
 * @author Kefu Zhou
 *
 */
public class Tokenizer implements Iterable<Token> {
	private final BufferedReader reader;
	private final NFA dfa;
	private String curLine;
	private Token nextToken = null;

	public Tokenizer(final NFA dfa, final InputStream input) {
		if (!dfa.isDFA())
			throw new RuntimeException("Must be DFA");

		this.dfa = dfa;
		this.reader = new BufferedReader(new InputStreamReader(input));
	}

	private class TokenIterator implements Iterator<Token> {
		public boolean hasNext() {
			if (nextToken == null) {
				nextToken = getNextToken();
			}

			return nextToken != null;
		}

		public Token next() {
			return getNextToken();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterator<Token> iterator() {
		return new TokenIterator();
	}

	public Token getNextToken() {
		if (nextToken != null) {
			Token token = nextToken;
			nextToken = null;
			return token;
		}

		if (curLine == null || curLine.length() == 0) {
			try {
				curLine = reader.readLine();
			} catch (IOException ex) {
				return null;
			}

			// If curLine is still null, no more input
			if (curLine == null) {
				return null;
			}
		}

		Token t = null;

		for (int max = curLine.length(); max > 0; max--) {
			t = getNextToken(dfa.getStartState(), 0, max);
			if (t != null) {
				curLine = curLine.substring(t.value.length());
				break;
			}
		}

		if (t == null && curLine.length() > 0) {
			curLine = curLine.substring(1);
			return getNextToken();
		}

		return t;
	}

	private Token getNextToken(State state, int min, int max) {
		return getNextToken(state, min, max, new StringBuffer());
	}

	private Token getNextToken(State state, int min, int max, StringBuffer tokenBuffer) {
		Token t = null;

		for (Transition tr : state.getTransitions()) {
			if (min == max) {
				break;
			}
			String c = String.valueOf(curLine.charAt(min));
			if (tr.isValid(c)) {
				tokenBuffer.append(c);
				return getNextToken(tr.getDestinationState(), min + 1, max, tokenBuffer);
			}
		}

		if (state.isFinal()) {
			t = new Token(state.getName(), tokenBuffer.toString());
		}
		
		return t;
	}
}
