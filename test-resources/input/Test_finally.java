package input;

import java.io.IOException;
import java.io.RandomAccessFile;

class Test_finally extends BufferedIndexOutput {
	RandomAccessFile file;

//	public void close() throws IOException {
//		try {
//			file.length();
//			return;
//		} finally {
//			file.close();
//		}
//	}
	
	protected void getFullFont() throws IOException {
        RandomAccessFileOrArray rf2 = null;
        try {
//            rf2 = new RandomAccessFileOrArray();
            rf2.reOpen();
//            byte b[] = new byte[(int)rf2.length()];
            rf2.readFully(b);
//            return;
        }
        finally {
            try {if (rf2 != null) {rf2.close();}} catch (Exception e) {}
        }
    }


    private void updateByteRange(PdfPKCS7 pkcs7, PdfDictionary v) {
        PdfArray b = v.getAsArray(PdfName.BYTERANGE);
        RandomAccessFileOrArray rf = reader.getSafeFile();
        try {
            rf.reOpen();
            byte buf[] = new byte[8192];
            for (int k = 0; k < b.size(); ++k) {
                int start = b.getAsNumber(k).intValue();
                int length = b.getAsNumber(++k).intValue();
                rf.seek(start);
                while (length > 0) {
                    int rd = rf.read(buf, 0, Math.min(length, buf.length));
                    if (rd <= 0)
                        break;
                    length -= rd;
                    pkcs7.update(buf, 0, rd);
                }
            }
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        finally {
            try{rf.close();}catch(Exception e){}
        }
    }
}