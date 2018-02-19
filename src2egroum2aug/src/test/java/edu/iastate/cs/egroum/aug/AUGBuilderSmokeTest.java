package edu.iastate.cs.egroum.aug;

import org.junit.Test;

import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGForMethod;

public class AUGBuilderSmokeTest {
    /**
     * When adjusting control edges from the catch to if within it, we accidentally generated a cycle, because there was
     * a superfluous dependency edge from the catch to the condition.
     */
    @Test
    public void bug_cycleGeneratedInAdjustControlsDueToAdditionalDependencyFromCatchToFirstActionInCatch() {
        buildAUGForMethod("void m(A a) {\n" +
                "  try {\n" +
                "    a.m();\n" +
                "  } catch (Exception e) {\n" +
                "    if (e instanceof OpenException) {\n" +
                "      throw (OpenException) e;\n" +
                "    }\n" +
                "    throw new OpenException(e);\n" +
                "  }" +
                "}");
    }

    /**
     * When adjusting control edges from the outer finally, through the try block, to the if, we accidentally generated
     * a cycle, because there was a superfluous edge from the bock to the condition (null check).
     */
    @Test
    public void bug_cycleGeneratedInAdjustControlsDueToAdditionalDependencyFromFinallyBlockToFirstActionInFinally() {
        buildAUGForMethod("void renameFile(java.io.InputStream in, java.io.OutputStream out) throws IOException {\n" +
                "    try {\n" +
                "      old.delete();\n" +
                "    }\n" +
                "    finally {\n" +
                "      try {\n" +
                "        if (in != null) {\n" +
                "          try {\n" +
                "            in.close();\n" +
                "          } catch (IOException e) {\n" +
                "            throw new RuntimeException();\n" +
                "          }\n" +
                "        }\n" +
                "      } finally {}\n" +
                "    }\n" +
                "  }");
    }
}
