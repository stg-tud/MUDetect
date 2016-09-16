import java.io.IOException;
import java.io.RandomAccessFile;

class Test_finally extends BufferedIndexOutput {
	RandomAccessFile file;

	public void close() throws IOException {
		try {
			super.close();
		} finally {
			if (file != null)
				file.close();
		}
	}

	protected void flushBuffer(byte[] b, int offset, int len) throws IOException {
		// stub
	}

	public long length() throws IOException {
		return 0; // stub
	}
}