package mining;

import egroum.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class AUGMiner {
    public static final int DEFAULT_MAX_PATTERN_SIZE = Integer.MAX_VALUE;
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

    public Set<de.tu_darmstadt.stg.mudetect.model.Pattern> mine(EGroumGraph... groums) {
        ArrayList<EGroumGraph> groumList = new ArrayList<>();
        Collections.addAll(groumList, groums);
        return mine(groumList);
    }

    public Set<de.tu_darmstadt.stg.mudetect.model.Pattern> mine(Collection<EGroumGraph> groums) {
        return mine(new ArrayList<>(groums));
    }

    public Set<de.tu_darmstadt.stg.mudetect.model.Pattern> mine(ArrayList<EGroumGraph> groums) {
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
            mining.Miner miner = new mining.Miner("-subgraph-finder-");
            miner.output_path = this.outputPath;
            miner.maxSingleNodePrevalence = 100;
            return toAUGPatterns(miner.mine(groums));
        } finally {
            System.setOut(originalOut);
        }
    }

    private static Set<de.tu_darmstadt.stg.mudetect.model.Pattern> toAUGPatterns(Set<Pattern> patterns) {
        return patterns.stream().map(AUGMiner::toAUGPattern).collect(Collectors.toSet());
    }

    private static de.tu_darmstadt.stg.mudetect.model.Pattern toAUGPattern(Pattern pattern) {
        de.tu_darmstadt.stg.mudetect.model.Pattern augPattern =
                new de.tu_darmstadt.stg.mudetect.model.Pattern(pattern.getFreq());

        Fragment f = pattern.getRepresentative();
        List<EGroumNode> nodes = f.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            EGroumNode node = nodes.get(i);
            augPattern.addVertex(node);

            if (node instanceof EGroumDataNode) {
                for (Fragment fragment : pattern.getFragments()) {
                    augPattern.addLiteral(node, fragment.getNodes().get(i).getDataName());
                }
            }
        }
        for (EGroumNode node : f.getNodes()) {
            augPattern.addVertex(node);
        }
        for (EGroumNode node : f.getNodes()) {
            for (EGroumEdge e : node.getInEdges()) {
                if (f.getNodes().contains(e.getSource()))
                    augPattern.addEdge(e.getSource(), e.getTarget(), e);
            }
        }

        return augPattern;
    }
}
