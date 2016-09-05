package egroum;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EGroumTestUtils {
	public static EGroumGraph buildGroumForMethod(String code) {
		String classCode = "class C { " + code + "}";
		ArrayList<EGroumGraph> groums = buildGroumsForClass(classCode);
		assertThat(groums.size(), is(1));
		return groums.iterator().next();
	}

	private static ArrayList<EGroumGraph> buildGroumsForClass(String classCode) {
		EGroumBuilder builder = new EGroumBuilder();
		return builder.buildGroums(classCode, "test", "test");
	}
}
