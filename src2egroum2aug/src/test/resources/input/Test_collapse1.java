package input;

import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

class Test_collapse1 {
	
	String s;
	
	public void before() {
		StringBuffer sb = new StringBuffer();
		sb.append("");
		sb.append(s);
		sb.append("");
		sb.toString();
	}

	public void after() {
		StringBuffer sb = new StringBuffer();
		sb.append("");
		sb.toString();
	}
}