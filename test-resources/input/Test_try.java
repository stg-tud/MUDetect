package input;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

class Test_try {
	
	public void pattern1(OutputStream out, String value) throws IOException {
		Writer writer = null;
		try {
			writer = new PrintWriter(out);
			writer.write(value + "");
//			System.out.println(value);
		} catch (IOException e) {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	public void pattern2(OutputStream out, String value) throws IOException {
		Writer writer = null;
		try {
			writer = new PrintWriter(out);
			writer.equals(null);
			writer.write(value);
			writer.close();
//			System.out.println(value);
		} catch (IOException e) {
			writer.equals(null);
			if (writer != null && writer.equals(null)) {
				writer.close();
			}
		}
	}
}