package input;

import java.util.ArrayList;

public static class Test_switch {
	void n(C c, String s) { c.foo(s); }
	
	void m(int i) {
		String s = null;
		switch (i) {
		case 1:
			s = new String(i);
			break;
		default:
			throw new IllegalArgumentException(s);
		}
		System.out.println(s);
	}

	void m2(int i) {
		String s = null;
		if (i == 1)
			s = new String(i);
		else
			return; //throw new IllegalArgumentException("Illegal type of node.");
		System.out.println(s);
	}

	void m3(int i) {
		String s = null;
		s = new String(i);
		System.out.println(s);
	}
}