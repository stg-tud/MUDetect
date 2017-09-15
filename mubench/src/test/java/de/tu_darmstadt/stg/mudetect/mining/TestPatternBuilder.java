package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import egroum.EGroumEdge;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;

public class TestPatternBuilder {
    public static Pattern somePattern() {
        return somePattern(someAUG());
    }

    public static Pattern somePattern(int support) {
        return somePattern(someAUG(), support);
    }

    public static Pattern somePattern(int nodeCount, int support) {
        String[] nodeNames = new String[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            nodeNames[i] = Integer.toString(i);
        }
        return TestPatternBuilder.somePattern(buildAUG().withActionNodes(nodeNames), support);
    }

    public static Pattern somePattern(AUG patternAUG) {
        return somePattern(patternAUG, 1);
    }

    public static Pattern somePattern(AUG patternAUG, int support) {
        Pattern pattern = new Pattern(support);
        patternAUG.vertexSet().forEach(pattern::addVertex);
        for (EGroumEdge edge : patternAUG.edgeSet()) {
            pattern.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        return pattern;
    }

    public static Pattern somePattern(TestAUGBuilder builder) {
        return somePattern(builder.build());
    }

    public static Pattern somePattern(TestAUGBuilder builder, int support) {
        return somePattern(builder.build(), support);
    }
}
