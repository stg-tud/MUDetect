package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

import java.util.HashSet;

import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.someAUG;

public class TestPatternBuilder {
    public static APIUsagePattern somePattern() {
        return somePattern(someAUG());
    }

    public static APIUsagePattern somePattern(int support) {
        return somePattern(someAUG(), support);
    }

    public static APIUsagePattern somePattern(int nodeCount, int support) {
        String[] nodeNames = new String[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            nodeNames[i] = Integer.toString(i);
        }
        return somePattern(buildAUG().withActionNodes(nodeNames), support);
    }

    public static APIUsagePattern somePattern(APIUsageGraph patternAUG) {
        return somePattern(patternAUG, 1);
    }

    public static APIUsagePattern somePattern(APIUsageGraph patternAUG, int support) {
        APIUsagePattern pattern = new APIUsagePattern(support, new HashSet<>());
        patternAUG.vertexSet().forEach(pattern::addVertex);
        for (Edge edge : patternAUG.edgeSet()) {
            pattern.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        return pattern;
    }

    public static APIUsagePattern somePattern(TestAUGBuilder builder) {
        return somePattern(builder.build(APIUsagePattern.class));
    }

    public static APIUsagePattern somePattern(TestAUGBuilder builder, int support) {
        return somePattern(builder.build(APIUsagePattern.class), support);
    }
}
