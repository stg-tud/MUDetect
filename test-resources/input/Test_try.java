package input;

import java.io.PrintWriter;
import java.io.Writer;

class Test_try {
	public void pattern(OutputStream out, String value) throws IOException {
		Writer writer = null;
		try {
			writer = new PrintWriter(out);
			writer.write(value);
//			System.out.println(value);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}