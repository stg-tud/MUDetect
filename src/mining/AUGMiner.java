package mining;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import egroum.EGroumBuilder;
import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class AUGMiner {
    public static final int DEFAULT_MAX_PATTERN_SIZE = 10;
    public static final int DEFAULT_MAX_PATTERN_SUPPORT = 1000;

    private int minPatternSupport;
    private int maxPatternSupport;
    private int minPatternSize;
    private int maxPatternSize;

    private String outputPath = null;
    private PrintStream out = null;

    public AUGMiner(int minPatternSupport, int minPatternSize) {
        this.minPatternSupport = minPatternSupport;
        this.maxPatternSupport = DEFAULT_MAX_PATTERN_SUPPORT;
        this.minPatternSize = minPatternSize;
        this.maxPatternSize = DEFAULT_MAX_PATTERN_SIZE;
    }

    public void setMinPatternSize(int minPatternSize) {
        this.minPatternSize = minPatternSize;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public void disableOut() {
        this.out = new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {}
        });
    }

    public Set<AUG> mine(EGroumGraph... groums) {
        ArrayList<EGroumGraph> groumList = new ArrayList<>();
        Collections.addAll(groumList, groums);
        return mine(groumList);
    }

    public Set<AUG> mine(List<EGroumGraph> groums) {
        return mine(new ArrayList<>(groums));
    }

    public Set<AUG> mine(ArrayList<EGroumGraph> groums) {
        Pattern.minFreq = this.minPatternSupport;
        Pattern.maxFreq = this.maxPatternSupport;
        Pattern.minSize = this.minPatternSize;
        Pattern.maxSize = this.maxPatternSize;

        EGroumNode.numOfNodes = 0;
        Fragment.nextFragmentId = 0;
        Fragment.numofFragments = 0;

        PrintStream originalOut = System.out;
        try {
            if (out != null) {
                System.setOut(out);
            }
            mining.Miner miner = new mining.Miner(":irrelevant:", "-subgraph-finder-");
            miner.output_path = this.outputPath;
            miner.maxSingleNodePrevalence = 100;
            return toAUGs(miner.mine(groums));
        } finally {
            System.setOut(originalOut);
        }
    }

    private static Set<AUG> toAUGs(Set<Pattern> patterns) {
        return patterns.stream().map(AUGMiner::toAUG).collect(Collectors.toSet());
    }

    private static AUG toAUG(Pattern pattern) {
        Fragment f = pattern.getRepresentative();
        EGroumGraph graph = f.getGraph();

        AUG aug = new AUG(graph.getName(), graph.getFilePath());
        for (EGroumNode node : f.getNodes()) {
            aug.addVertex(node);
        }
        for (EGroumNode node : f.getNodes()) {
            for (EGroumEdge e : node.getInEdges()) {
                aug.addEdge(e.getSource(), e.getTarget(), e);
            }
        }

        return aug;
    }
}
