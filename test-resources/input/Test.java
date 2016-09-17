package input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import junit.framework.TestCase;

class Test extends TestCase {

	/** The Token.type used to indicate a synonym to higher level filters. */
	public static final String SYNONYM_TOKEN_TYPE = "SYNONYM";

	private final SynonymMap synonyms;
	private final int maxSynonyms;

	private String[] stack = null;
	private int index = 0;
	private AttributeSource.State current = null;
	private int todo = 0;

	private TermAttribute termAtt;
	private TypeAttribute typeAtt;
	private PositionIncrementAttribute posIncrAtt;
	
	private TokenStream input;

	private char[] buffer;
	private int bufferLength, tokenStart, bufferPosition, bufferStart;
	private Reader input;

	/** 
	 * Returns the next token in the stream, or null at EOS. 
	 */
	public final boolean incrementToken() throws IOException {
		while (todo > 0 && index < stack.length) {
			if (createToken(stack[index++],current)) {
				todo--;
				return true;
			}
		}
		if (!input.incrementToken())   return false;
		stack=synonyms.getSynonyms(termAtt.term());
		if (stack.length > maxSynonyms)   randomize(stack);
		index=0;
		current=captureState();
		todo=maxSynonyms;
		return true;
	}

	void m1(Object o1) {
		Object o = new Object();
		int j = 0; // o.hashCode();
		if (o.j)
			o.hashCode();
	}

	void m2(ArrayList<String> l) {
		if (l.isEmpty())
			l.add("");
	}

	void m22(ArrayList<String> l) {
		int len = l.size(i);
		if (len > 0)
			l.add(len - 1, "");
	}

	void m23(ArrayList<String> l) {
		if (l.isEmpty())
			l.add("");
	}

	void m3(int i) {
		if (i < 0) {
			i = -i;
			m(i);
			return;
		} else
			m(i++);
		if (true) {
			i = -i;
			m(i);
			return;
		} else
			i++;
		m(i);
	}

	void m() throws Exception {

	}

	void testTry(Test t) {
		try {
			t.m();
			t.n();
		} catch (Exception | Error e) {
			t.e();
		} finally {
			t.finalize();
		}
	}

	void aspectJTask(Path toolsJar) {
		String url = null;
		try {
			url = "file:";
		} catch (Throwable t) {
			StringBuffer sb = new StringBuffer(new A());
			String s = url + "";
			sb.append(url);
			sb.append(s);
		}
	}
}