package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EGroumTestUtils {
	public static APIUsageExample buildGroumForMethod(String code) {
        return buildGroumForMethod(code, new AUGConfiguration());
    }

	public static APIUsageExample buildGroumForMethod(String code, AUGConfiguration configuration) {
		String classCode = "class C { " + code + "}";
		Collection<APIUsageExample> groums = buildGroumsForClass(classCode, configuration);
		assertThat(groums.size(), is(1));
		return groums.iterator().next();
	}

	public static Collection<APIUsageExample> buildGroumsForClass(String classCode) {
        return buildGroumsForClass(classCode, new AUGConfiguration());
    }

	public static Collection<APIUsageExample> buildGroumsForClass(String classCode, AUGConfiguration configuration) {
		AUGBuilder builder = new AUGBuilder(configuration);
		String projectName = "test";
		String basePath = getTestFilePath("/") + projectName;
		return builder.build(classCode, basePath, projectName, null);
	}

	public static Collection<APIUsageExample> buildGroumsFromFile(String path) {
		return new AUGBuilder(new AUGConfiguration(){{removeImplementationCode = 2;}}).build(getTestFilePath(path), null);
	}

	public static ArrayList<APIUsageExample> buildGroumsForClasses(String[] sourceCodes) {
		ArrayList<APIUsageExample> groums = new ArrayList<>();
		for (String sourceCode : sourceCodes) {
			groums.addAll(buildGroumsForClass(sourceCode));
		}
		return groums;
	}

	public static ArrayList<APIUsageExample> buildGroumsForMethods(String[] sourceCodes) {
		ArrayList<APIUsageExample> groums = new ArrayList<>();
		for (String sourceCode : sourceCodes) {
			groums.add(buildGroumForMethod(sourceCode));
		}
		return groums;
	}

	public static void buildAndPrintGroumsForFile(String inputPath, String[] classpaths, String outputPath) {
		buildAndPrintGroumsForFile(inputPath, classpaths, outputPath, new AUGConfiguration(){{removeImplementationCode = 2;}});
	}

	public static ArrayList<EGroumGraph> buildAndPrintGroumsForFile(String inputPath, String[] classpaths, String outputPath, AUGConfiguration config) {
		String srcFileName = getTestFilePath(inputPath);
		EGroumBuilder gb = new EGroumBuilder(config);
		ArrayList<EGroumGraph> gs = gb.buildBatch(srcFileName, classpaths);
		for (EGroumGraph g : gs) {
			String s = g.getName();
			s = s.replace("\n", "\\l");
			s = s.replace("\t", "    ");
			s = s.replace("\"", "\\\"");
			s += "\\l";
			s = "0 [label=\"" + s + "\"" + " shape=box style=dotted]";
//			System.out.println(s);
			g.toGraphics(s, outputPath);
		}
		return gs;
	}

	private static String getTestFilePath(String relativePath) {
		if (!relativePath.startsWith("/")) {
			relativePath = "/" + relativePath;
		}
		return EGroumTestUtils.class.getResource(relativePath).getFile();
	}
}
