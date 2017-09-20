package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AUGBuilderTestUtils {
    public static APIUsageExample buildAUG(String code) {
        return buildAUGForMethod(code);
    }

    static APIUsageExample buildAUG(String code, AUGConfiguration configuration) {
        return buildAUGForMethod(code, configuration);
    }

    static APIUsageExample buildAUGForMethod(String code) {
        return buildAUGForMethod(code, new AUGConfiguration());
    }

    private static APIUsageExample buildAUGForMethod(String code, AUGConfiguration configuration) {
        String classCode = "class C { " + code + "}";
        Collection<APIUsageExample> groums = buildAUGsForClass(classCode, configuration);
        assertThat(groums.size(), is(1));
        return groums.iterator().next();
    }

    public static Collection<APIUsageExample> buildAUGsForClass(String classCode) {
        return buildAUGsForClass(classCode, new AUGConfiguration());
    }

    private static Collection<APIUsageExample> buildAUGsForClass(String classCode, AUGConfiguration configuration) {
        AUGBuilder builder = new AUGBuilder(configuration);
        String projectName = "test";
        String basePath = getTestFilePath("/") + projectName;
        return builder.build(classCode, basePath, projectName, null);
    }

    public static Collection<APIUsageExample> buildAUGsFromFile(String path) {
        return new AUGBuilder(new AUGConfiguration() {{
            removeImplementationCode = 2;
        }}).build(getTestFilePath(path), null);
    }

    public static ArrayList<APIUsageExample> buildAUGsForClasses(String[] sourceCodes) {
        ArrayList<APIUsageExample> groums = new ArrayList<>();
        for (String sourceCode : sourceCodes) {
            groums.addAll(buildAUGsForClass(sourceCode));
        }
        return groums;
    }

    public static ArrayList<APIUsageExample> buildAUGsForMethods(String[] sourceCodes) {
        ArrayList<APIUsageExample> groums = new ArrayList<>();
        for (String sourceCode : sourceCodes) {
            groums.add(buildAUGForMethod(sourceCode));
        }
        return groums;
    }

    static void buildAndPrintAUGsForFile(String inputPath, String[] classpaths, String outputPath) {
        buildAndPrintAUGsForFile(inputPath, classpaths, outputPath, new AUGConfiguration() {{
            removeImplementationCode = 2;
        }});
    }

    static ArrayList<EGroumGraph> buildAndPrintAUGsForFile(String inputPath, String[] classpaths, String outputPath, AUGConfiguration config) {
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
        return AUGBuilderTestUtils.class.getResource(relativePath).getFile();
    }
}
