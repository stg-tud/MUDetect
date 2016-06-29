import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

class Test {
	void m1(Object o) {
		int j = 0; // o.hashCode();
		if (j > 0)
			o.hashCode();
	}

	void m2(ArrayList<String> l) {
		if (l.isEmpty())
			l.add("");
	}

	void m22(ArrayList<String> l) {
		if (l.isEmpty())
			l.add("");
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

	void testTry(Test t) {
		try {
			t.m();
			t.finalize();
		} catch (Exception | Error e) {
			t.n();
		} finally {
			t.z();
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