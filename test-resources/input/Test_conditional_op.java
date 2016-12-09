package input;

class Test_conditional_op extends BufferedIndexOutput {

	void m(int a, int b) {
		if (!(a > 1) && b > 2) {
			m(a, b);
		}
	}
}