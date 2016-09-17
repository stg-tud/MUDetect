package input;

class Test_order {
	public void misuse(JFrame f, Dimension d) {
		f.pack();
		f.setPreferredSize(d);
	}

	public void pattern(JFrame f, Dimension d) {
		f.setPreferredSize(d);
		f.pack();
	}
}