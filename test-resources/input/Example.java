package input;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

class Example {
	FileInputStream fis;

	public void pattern(String file) {
		try {
			fis = new FileInputStream(file);
			fis.read();
		} catch (IOException e) {
			if (fis != null)
				fis.close();
		}
	}
	
	private void handle() {}
	
	void check1(List l) {
		if (l.size() > 0)
			l.get(0);
	}
	
	void check2(List l, boolean a, boolean b) {
		if (a && b) m(a, b);
	}
	
	private void m() {}
}