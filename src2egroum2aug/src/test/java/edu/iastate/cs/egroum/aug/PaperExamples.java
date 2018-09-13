package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PaperExamples {
    @Test
    public void paperExample() throws Exception {
        printAUG("AUG",
                "void m(String file) {\n" +
                "  if (file != null) {\n" +
                "    try {\n" +
                "      return new java.io.FileInputStream(file).read();\n" +
                "    } catch(java.io.FileNotFoundException e) {\n" +
                "      Foo.handle(e);\n" +
                "    }\n" +
                "  }\n" +
                "}\n");

        printAUG("AUG2",
                "void m(String file) {\n" +
                        "  if (file != null) {\n" +
                        "    try {\n" +
                        "      java.io.FileInputStream fis = new java.io.FileInputStream(file);\n" +
                        "      int i = 0;    \n" +
                        "      while ((i = fis.read()) != -1) {\n" +
                        "          Foo.doSomething(i);    \n" +
                        "      }\n" +
                        "    } catch(java.io.FileNotFoundException e) {" +
                        "      Foo.handle(e);" +
                        "    }" +
                        "  }" +
                        "}");

        printAUG("IteratorMisuse",
                "void m(Collection<String> files) {\n" +
                        "Iterator<String> it = files.iterator();\n" +
                        "String first = it.next();\n" +
                        "}");

        printAUG("IteratorPattern",
                "void m(Collection<String> files) {\n" +
                        "Iterator<String> it = files.iterator();\n" +
                        "if (it.hasNext()) {\n" +
                        "String first = it.next();\n" +
                        "}\n" +
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
                "public static void main(String[] args) {" +
                "  SwingUtilities.invokeLater(new Runnable() {\n" +
                "    @Override\n" +
                "    public void run() {\n" +
                "      JFrame f = new JFrame(\"Main Window\");\n" +
                "      // add components...\n" +
                "      f.setVisible(true);\n" +
                "    }\n" +
                "  });" +
                "}");
    }

    private void printAUG(String exampleName, String code) throws IOException, InterruptedException {
        APIUsageExample aug = AUGBuilderTestUtils.buildAUGForMethod(code, new AUGConfiguration() {
            {
                minStatements = 0;
                groum = false;

                collapseIsomorphicSubgraphs = true;

                collapseTemporaryDataNodes = false;
                collapseTemporaryDataNodesIncomingToControlNodes = true;

                encodeUnaryOperators = false;
                encodeConditionalOperators = false;

                buildTransitiveDataEdges = false;

                removeImplementationCode = 2;
            }
        });
        List<Node> scaffoldNodes = aug.vertexSet().stream()
                .filter(node -> node instanceof DataNode && ((DataNode) node).getType().equals("C")).collect(Collectors.toList());
        for (Node scaffoldNode : scaffoldNodes) {
            aug.removeVertex(scaffoldNode);
        }
        new DisplayAUGDotExporter().toPNGFile(aug, new File("paper-examples", exampleName));
    }

}
