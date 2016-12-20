package input;

import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

class Test_keep_data {
	
	String s;
	
	public void before() {
		StringBuffer sb = new StringBuffer();
		sb.append("");
		sb.append(s.length());
		sb.append("");
		sb.toString();
	}

	public void after(String s) {
		StringBuffer sb = new StringBuffer();
		sb.append(s.length());
		sb.toString();
	}
	
	void m() {
		a[0] = a[0] + 1;
	}
}