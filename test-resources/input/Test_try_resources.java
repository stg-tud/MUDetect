package input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class Test_try_resources {
	public void misuse(File file) throws IOException {
		try (FileInputStream fis1 = new FileInputStream(file); FileInputStream fis2 = new FileInputStream(file)) {
			fis1.read();
			fis2.read();
		}
	}

	public void pattern(File file) throws IOException {
		if (file.exists()) {
			public void misuse(File file) throws IOException {
				try (FileInputStream fis1 = new FileInputStream(file); FileInputStream fis2 = new FileInputStream(file)) {
					fis1.read();
					fis2.read();
				}
		}
	}
}