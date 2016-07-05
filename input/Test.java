import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import junit.framework.TestCase;

class Test extends TestCase {
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
			l.add(len-1, "");
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
	
	public void testGetDeclaredAdvice() {
		Advice[] advice = sa.getDeclaredAdvice();
		assertEquals(10,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.BEFORE);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER_RETURNING);
		assertEquals(2,advice.length);
	}
	
	private String readFile(String filePath, int lineNumber) {
        try {
//            URL url = ClassLoader.getSystemResource(filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                return "ERROR: file " + filePath + " does not exist.";
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer contents = new StringBuffer();
            String line = reader.readLine();
            int numLines = 0;
            while (line != null) {
                numLines++;
                if (numLines < lineNumber) {
                    currHighlightStart += line.length()+1;
                }
                if (numLines == lineNumber) {
                    currHighlightEnd = currHighlightStart + line.length();
                }
                contents.append(line);
                contents.append('\n');
                line = reader.readLine();
            }
            reader.close();
            return contents.toString();
        } catch (IOException ioe) {
            return "ERROR: could not read file " + filePath + ", make sure that you have mounted /project/aop on X:\\";
        }
    }
}