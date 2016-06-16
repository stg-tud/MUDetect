import java.io.File;
import java.io.FileOutputStream;

class Test {
	void m1(Object o) {
		int i = o.hashCode();
		if (i > 0)
			o.hashCode();
	}

	void m2(Object o) {
		o.hashCode();
		o.hashCode();
	}

	void m3(int i) {
		if (i < 0) {
			i = -i;
		}
		else i++;
		m(i);
		m(i);
	}
	
	void m() throws Exception {
		for (int i : a)
			a(i);
	}
	
	void testTry(Test t) {
		try {
			t.m();
			new FileOutputStream(new File(new String(new char[]{})));
		} catch (Exception | Error e){
			t.n();
			e.printStackTrace();
		} finally {
			t.z();
		}
	}
}