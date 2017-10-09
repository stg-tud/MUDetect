package input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

class Example_09_14 {
	public void misuse(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			fis.read();
		}
	}

	public void pattern(File file) throws IOException {
		if (file.exists()) {
			try (FileInputStream fis = new FileInputStream(file)) {
				fis.read();
			}
		}
	}
	
	void check(List l) {
		if (0 < l.size())
			l.get(0);
	}
	
	void iterator(Iterator i) {
		while (i.hasNext()) i.next();
	}
}