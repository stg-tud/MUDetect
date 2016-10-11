package input;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

class Test_anonymous_class {

	public void target() {
		// create a lock file
		final File lockFile = new File("");
		new Object() {
			public boolean obtain() throws IOException {
				if (DISABLE_LOCKS) {
					return true;
				}
				return lockFile.createNewFile();
			}
		};
	}
}