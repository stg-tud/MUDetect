package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasSelectionEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.nodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGForMethod;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGsForMethods;
import static edu.iastate.cs.mudetect.mining.MinerTestUtils.mineWithMinSupport;
import static edu.iastate.cs.mudetect.mining.MinerTestUtils.mineWithMinSupport2;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class MinePreconditionsTest {
    @Test
    public void minesPreconditionWithDataDependence() {
        APIUsageExample aug = buildAUGForMethod("void m(List l) {" +
                "  if (l.isEmpty()) {" +
                "    l.add(\"arbitrary element\");" +
                "  }");

        List<APIUsagePattern> patterns = mineWithMinSupport(Collections.singletonList(aug), 1);

        assertThat(patterns, hasSize(1));
        assertThat(patterns.get(0), hasSelectionEdge(nodeWith(label("List.isEmpty()")), nodeWith(label("List.add()"))));
    }

    @Test
    public void excludesPreconditionWithoutDataDependence() {
        // SMELL the control dependency in this code isn't even encoded in the AUG, I think it should be.
        APIUsageExample aug = buildAUGForMethod("void m(Collection c, Object o) {" +
                "  if (c.isEmpty()) {" +
                "    o.hashCode();" +
                "  }");

        List<APIUsagePattern> patterns = mineWithMinSupport(Collections.singletonList(aug), 1);

        assertThat(patterns, hasSize(2));
        for (APIUsagePattern pattern : patterns) {
            assertThat(pattern, not(hasEdge(nodeWith(label("List.isEmpty()")), nodeWith(label("Object.hashCode()")))));
        }
    }

    @Test
    public void minesPreconditionWithIndirectDataDependence() {
        APIUsageExample aug = buildAUGForMethod(
                "void m(List l, Object o1) {" +
                        "  if (!l.isEmpty()) {" +
                        "    Object o2 = l.get(0);" +
                        "    o1.equals(o2);" +
                        "  }");

        List<APIUsagePattern> patterns = mineWithMinSupport(Collections.singleton(aug), 1);

        assertThat(patterns, hasSize(1));
        assertThat(patterns.get(0), hasEdge(nodeWith(label("List.isEmpty()")), nodeWith(label("Object.equals()"))));
    }

    @Test
    public void excludesPreconditionIfFlowIsExcluded() {
        List<APIUsageExample> augs = buildAUGsForMethods(
                "void m(List l, Object o1) {" +
                        "  if (!l.isEmpty()) {" +
                        "    Object o2 = l.get(0);" +
                        "    o1.equals(o2);" +
                        "  }",
                "void m(List l, Object o1) {" +
                        "  if (!l.isEmpty()) {" +
                        "    Object o2 = l.iterator().next();" +
                        "    o1.equals(o2);" +
                        "  }");

        List<APIUsagePattern> patterns = mineWithMinSupport2(augs);

        for (APIUsagePattern pattern : patterns) {
            assertThat(pattern, not(hasEdge(nodeWith(label("List.isEmpty()")), nodeWith(label("Object.equals()")))));
        }
    }


}
