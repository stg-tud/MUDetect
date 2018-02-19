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
}
