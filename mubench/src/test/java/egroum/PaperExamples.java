package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import de.tu_darmstadt.stg.mudetect.aug.dot.DisplayAUGDotExporter;
import graphics.DotGraph;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static egroum.EGroumTestUtils.buildGroumForMethod;

public class PaperExamples {
    @Test
    public void paperExample() throws Exception {
        printGroum("AUG",
                "void m(String file) {" +
                "  if (file != null) {" +
                "    try {" +
                "      java.io.FileInputStream fis = new java.io.FileInputStream(file);" +
                "      return fis.read();" +
                "    } catch(java.io.FileNotFoundException e) {" +
                "      handle(e);" +
                "    }" +
                "  }" +
                "}");
    }

    @Test
    public void conditionVariations() throws Exception {
        printGroum("condition-relational-operators",
                "void m(List l) {" +
                "  if (l.size() > 0) l.get(0);" +
                "  else {}" +
                "}");
        printGroum("condition-relational-operators",
                "void m() {" +
                "  boolean a = a();" +
                "  boolean b = b();" +
                "  if (a && b) m(a, b);" +
                "}");
    }

    @Test
    public void anonymousClassInstances() throws Exception {
        printGroum("anonymous-class-instance",
                "void m() {" +
                "  new Thread(new Runnable() {\n" +
                "    @Override\n" +
                "    public void run() {\n" +
                "      new Object();\n" +
                "    }\n" +
                "  }).start();" +
                "}");
    }

    private void printGroum(String exampleName, String code) throws IOException, InterruptedException {
        APIUsageExample aug = buildGroumForMethod(code);
        List<Node> scaffoldNodes = aug.vertexSet().stream()
                .filter(node -> node.getLabel().equals("C")).collect(Collectors.toList());
        for (Node scaffoldNode : scaffoldNodes) {
            aug.removeVertex(scaffoldNode);
        }
        outputExample(exampleName, aug);
    }

    private void outputExample(String exampleName, APIUsageExample aug) throws IOException, InterruptedException {
        new DisplayAUGDotExporter().toPNGFile(aug, new File("paper-examples", exampleName));
    }
}
