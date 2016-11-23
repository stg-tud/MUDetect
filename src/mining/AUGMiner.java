package mining;

import egroum.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class AUGMiner {
    private final Configuration config;

    private PrintStream out = null;

    public AUGMiner(Configuration config) {
        this.config = config;
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
        EGroumNode.numOfNodes = 0;
        Fragment.nextFragmentId = 0;
        Fragment.numofFragments = 0;

        PrintStream originalOut = System.out;
        try {
            if (out != null) {
                System.setOut(out);
            }
            mining.Miner miner = new mining.Miner("-subgraph-finder-", config);
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
