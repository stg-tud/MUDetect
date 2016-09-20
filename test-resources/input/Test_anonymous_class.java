package input;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

class Test_anonymous_class {

	public static void pattern(String[] args) {
		final JFrame f = new JFrame("Main Window");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
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
}