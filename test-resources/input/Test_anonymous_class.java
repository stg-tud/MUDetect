package input;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

class Test_anonymous_class {

	public static void pattern(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame("Main Window");
				// add components
				f.pack();
				f.setVisible(true);
			}
		});
	}

	public static void misuse(String[] args) {
		JFrame f = new JFrame("Main Window");
		// add components
		f.pack();
		f.setVisible(true);
	}

	public void m() {
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