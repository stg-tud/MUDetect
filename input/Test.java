import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import junit.framework.TestCase;

class Test extends TestCase {
	void m1(Object o1) {
		Object o = new Object();
		int j = 0; // o.hashCode();
		if (o.j)
			o.hashCode();
	}

	void m2(ArrayList<String> l) {
		if (l.isEmpty())
			l.add("");
	}

	void m22(ArrayList<String> l) {
		int len = l.size(i);
		if (len > 0)
			l.add(len-1, "");
	}

	void m23(ArrayList<String> l) {
		if (l.isEmpty())
			l.add("");
	}

	void m3(int i) {
		if (i < 0) {
			i = -i;
			m(i);
			return;
		} else
			m(i++);
		if (true) {
			i = -i;
			m(i);
			return;
		} else
			i++;
		m(i);
	}
	
	void m() throws Exception {
		
	}

	void testTry(Test t) {
		try {
			t.m();
			t.n();
		} catch (Exception | Error e) {
			t.e();
		} finally {
			t.finalize();
		}
	}

	void aspectJTask(Path toolsJar) {        
		String url = null;
		try {
			url = "file:";
		} catch (Throwable t) {
			StringBuffer sb = new StringBuffer(new A());
			String s = url + "";
			sb.append(url);
			sb.append(s);
		}
	}
	
	public void testGetDeclaredAdvice() {
		Advice[] advice = sa.getDeclaredAdvice();
		assertEquals(10,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.BEFORE);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER_RETURNING);
		assertEquals(2,advice.length);
	}
	
	private String readFile(String filePath, int lineNumber) {
        try {
//            URL url = ClassLoader.getSystemResource(filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                return "ERROR: file " + filePath + " does not exist.";
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer contents = new StringBuffer();
            String line = reader.readLine();
            int numLines = 0;
            while (line != null) {
                numLines++;
                if (numLines < lineNumber) {
                    currHighlightStart += line.length()+1;
                }
                if (numLines == lineNumber) {
                    currHighlightEnd = currHighlightStart + line.length();
                }
                contents.append(line);
                contents.append('\n');
                line = reader.readLine();
            }
            reader.close();
            return contents.toString();
        } catch (IOException ioe) {
            return "ERROR: could not read file " + filePath + ", make sure that you have mounted /project/aop on X:\\";
        }
    }
	
	public static String decrypt(PublicKey publicKey, String cipherText) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA"); 
		try {
			cipher.init(Cipher.DECRYPT_MODE, publicKey); 
		} catch (InvalidKeyException e) { 
			// for ibm jdk
			RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
			RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent()); 
			Key fakePublicKey = KeyFactory.getInstance("RSA").generatePrivate(spec); 
			cipher.init(Cipher.DECRYPT_MODE, fakePublicKey);
		} 
		if (cipherText == null || cipherText.length() == 0) { 
			return cipherText;
		} 
		byte[] cipherBytes = Base64.base64ToByteArray(cipherText); 
		byte[] plainBytes = cipher.doFinal(cipherBytes); 
		return new String(plainBytes); 
	}
	
	Cipher pattern(PublicKey publicKey, String text) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidKeySpecException { 
		Cipher cipher = Cipher.getInstance("RSA"); 
		try {
			cipher.init(Cipher.DECRYPT_MODE, publicKey); 
		} catch (InvalidKeyException e) {
			RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
			RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent()); 
			Key fakePrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec); 
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, fakePrivateKey); 
		}
		return cipher; 
	}
	
	protected void readFDSelect(int Font, Font[] fonts)
	{
		int NumOfGlyphs = fonts[Font].nglyphs;
		int[] FDSelect = new int[NumOfGlyphs];

		switch(fonts[Font].FDSelectFormat){
			case 0:
				break;
			case 3:
				break;
			default:
				break;
		}
	}

    protected void readImage() throws IOException {
        ix = readShort();    // (sub)image position & size
        iy = readShort();
        iw = readShort();
        ih = readShort();

        int packed = in.read();
        lctFlag = (packed & 0x80) != 0;     // 1 - local color table flag
        interlace = (packed & 0x40) != 0;   // 2 - interlace flag
        // 3 - sort flag
        // 4-5 - reserved
        lctSize = 2 << (packed & 7);        // 6-8 - local color table size
        m_bpc = newBpc(m_gbpc);
        if (lctFlag) {
            m_curr_table = readColorTable((packed & 7) + 1);   // read table
            m_bpc = newBpc((packed & 7) + 1);
        }
        else {
            m_curr_table = m_global_table;
        }
        if (transparency && transIndex >= m_curr_table.length / 3)
            transparency = false;
        if (transparency && m_bpc == 1) { // Acrobat 5.05 doesn't like this combination
            byte tp[] = new byte[12];
            System.arraycopy(m_curr_table, 0, tp, 0, 6);
            m_curr_table = tp;
            m_bpc = 2;
        }
        boolean skipZero = decodeImageData();   // decode pixel data
        if (!skipZero)
            skip();

        Image img = null;
        try {
            img = new ImgRaw(iw, ih, 1, m_bpc, m_out);
            PdfArray colorspace = new PdfArray();
            colorspace.add(PdfName.INDEXED);
            colorspace.add(PdfName.DEVICERGB);
            int len = m_curr_table.length;
            colorspace.add(new PdfNumber(len / 3 - 1));
            colorspace.add(new PdfString(m_curr_table));
            PdfDictionary ad = new PdfDictionary();
            ad.put(PdfName.COLORSPACE, colorspace);
            img.setAdditional(ad);
            if (transparency) {
                img.setTransparency(new int[]{transIndex, transIndex});
            }
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        img.setOriginalType(Image.ORIGINAL_GIF);
        img.setOriginalData(fromData);
        img.setUrl(fromUrl);
        GifFrame gf = new GifFrame();
        gf.image = img;
        gf.ix = ix;
        gf.iy = iy;
        frames.add(gf);   // add image to frame list

        //resetFrame();

    }
}