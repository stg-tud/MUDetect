package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import de.tu_darmstadt.stg.mudetect.aug.dot.DisplayAUGDotExporter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PaperExamples {
    @Test
    public void paperExample() throws Exception {
        printAUG("AUG",
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
        printAUG("condition-relational-operators",
                "void m(List l) {" +
                "  if (l.size() > 0) l.get(0);" +
                "  else {}" +
                "}");
        printAUG("condition-relational-operators",
                "void m() {" +
                "  boolean a = a();" +
                "  boolean b = b();" +
                "  if (a && b) m(a, b);" +
                "}");
    }

    @Test
    public void anonymousClassInstances() throws Exception {
        printAUG("anonymous-class-instance",
                "void m() {" +
                "  new Thread(new Runnable() {\n" +
                "    @Override\n" +
                "    public void run() {\n" +
                "      new Object();\n" +
                "    }\n" +
                "  }).start();" +
                "}");
    }

    private void printAUG(String exampleName, String code) throws IOException, InterruptedException {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUGForMethod(code);
        List<Node> scaffoldNodes = aug.vertexSet().stream()
                .filter(node -> node.getLabel().equals("C")).collect(Collectors.toList());
        for (Node scaffoldNode : scaffoldNodes) {
            aug.removeVertex(scaffoldNode);
        }
        new DisplayAUGDotExporter().toPNGFile(aug, new File("paper-examples", exampleName));
    }

}
