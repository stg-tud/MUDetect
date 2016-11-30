package egroum;

import graphics.DotGraph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;

import static egroum.EGroumTestUtils.buildGroumsForClass;

public class AUGImprovement {
    @Rule
    public TestName name = new TestName();

    @Test
    public void syncEdges() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/deadlock/synthetic.deadlock/no_findings.php
        print("public class Synchronized {" +
                "  public void misuse(Object o) {" +
                "    synchronized (o) {" +
                "      o.hashCode();" +
                "      synchronized (o) {" +
                "        o.hashCode();" +
                "      }" +
                "    }" +
                "  }" +
                "}");
    }

    @Test
    public void tryWithResources() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/synthetic/fisexists/synthetic.fisexists/no_findings.php
        print("import java.io.FileInputStream;" +
                "class FISExists {" +
                "  public void misuse(File file) throws IOException {" +
                "    try (FileInputStream fis = new FileInputStream(file)) {" +
                "      // do something with fis...\n" +
                "    }" +
                "  }" +
                "" +
                "  public void pattern(File file) throws IOException {" +
                "    if (file.exists()) {" +
                "      try (FileInputStream fis = new FileInputStream(file)) {" +
                "        // do something with fis...\n" +
                "      }" +
                "    }" +
                "  }" +
                "}");
    }

    @Test
    public void linkOutMethodAndAnonymousInstanceMethod() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/grouminer-do/synthetic/callondte/synthetic.callondte/review.php
        print("public class RunOnEDT {" +
                "  public static void main(String[] args) {" +
                "    SwingUtilities.invokeLater(new Runnable() {" +
                "            public void run() {" +
                "              JFrame f = new JFrame(\"Main Window\");" +
                "              // add components\n" +
                "              f.pack(); " +
                "              f.setVisible(true); " +
                "            }" +
                "        });" +
                "  }" +
                "}");
    }

    @Test
    public void anonymousClassMethods() throws Exception {
        // http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/reviews/ex1_detect-only/mudetect-do/lucene/207/lucene.3/no_findings.php
        print("class FSDirectory {" +
                "  public void m() {" +
                "    new Object() {" +
                "      public boolean obtain() throws IOException {" +
                "        if (DISABLE_LOCKS) {" +
                "          return true;" +
                "        }" +
                "        return lockFile.createNewFile();" +
                "      }" +
                "    };" +
                "  }" +
                "}");
    }

    private void print(String code) {
        System.out.println(name.getMethodName() + "  ################################################");
        for (EGroumGraph aug : buildGroumsForClass(code)) {
            DotGraph dotGraph = new DotGraph(aug);
            System.out.println(dotGraph.getGraph());
            dotGraph.toPNG(new File("aug-improvement"), name.getMethodName() + "_" + aug.getName());
        }
    }

}
