import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class Test_try_resources {
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
}