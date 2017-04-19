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
}