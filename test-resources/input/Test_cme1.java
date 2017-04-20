package input;

import com.google.javascript.jscomp.newtypes.JSType;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.UnionTypeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

class Test_cme1 {

	public static String shortToHexString(short s) {
		int n = s & 0x0000FFFF;
		String result = ((n < 0x00001000) ? "0" : "")
				+ ((n < 0x00000100) ? "0" : "")
				+ ((n < 0x00000010) ? "0" : "")
				+ Integer.toHexString(s);
		return result.toUpperCase();
	}
	
	void addRange(PdfString from, PdfString to, PdfObject code) {
        byte[] a1 = decodeStringToByte(from);
        byte[] a2 = decodeStringToByte(to);
        if (a1.length != a2.length || a1.length == 0)
            throw new IllegalArgumentException("Invalid map.");
        byte[] sout = null;
        if (code instanceof PdfString)
            sout = decodeStringToByte((PdfString)code);
        int start = a1[a1.length - 1] & 0xff;
        int end = a2[a2.length - 1] & 0xff;
        for (int k = start; k <= end; ++k) {
            a1[a1.length - 1] = (byte)k;
            PdfString s = new PdfString(a1);
            s.setHexWriting(true);
            if (code instanceof PdfArray) {
                addChar(s, ((PdfArray)code).getPdfObject(k - start));
            }
            else if (code instanceof PdfNumber) {
                int nn = ((PdfNumber)code).intValue() + k - start;
                addChar(s, new PdfNumber(nn));
            }
            else if (code instanceof PdfString) {
                PdfString s1 = new PdfString(sout);
                s1.setHexWriting(true);
                ++sout[sout.length - 1];
                addChar(s, s1);
            }
        }
    }
}