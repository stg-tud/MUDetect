package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.model.Location;
import egroum.*;
import mining.Configuration;
import mining.Fragment;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultAUGMiner implements AUGMiner {
    private final Configuration config;

    private PrintStream out = null;

    public DefaultAUGMiner(Configuration config) {
        this.config = config;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    private void disableOut() {
        this.out = new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {}
        });
    }

    public Model mine(EGroumGraph... groums) {
        ArrayList<EGroumGraph> groumList = new ArrayList<>();
        Collections.addAll(groumList, groums);
        return mine(groumList);
    }

    public Model mine(Collection<EGroumGraph> groums) {
        return mine(new ArrayList<>(groums));
    }

    public Model mine(ArrayList<EGroumGraph> groums) {
        EGroumNode.numOfNodes = 0;
        Fragment.nextFragmentId = 0;
        Fragment.numofFragments = 0;

        if (config.disableSystemOut) {
            disableOut();
        }

        PrintStream originalOut = System.out;
        try {
            if (out != null) {
                System.setOut(out);
            }
            mining.Miner miner = new mining.Miner("-subgraph-finder-", config);
            return toModel(miner.mine(groums));
        } finally {
            System.setOut(originalOut);
        }
    }

    private Model toModel(Set<mining.Pattern> patterns) {
        return () -> patterns.stream().map(DefaultAUGMiner::toAUGPattern).collect(Collectors.toSet());
    }

    private static de.tu_darmstadt.stg.mudetect.mining.Pattern toAUGPattern(mining.Pattern pattern) {
        de.tu_darmstadt.stg.mudetect.mining.Pattern augPattern =
                new de.tu_darmstadt.stg.mudetect.mining.Pattern(pattern.getFreq());

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
        for (Fragment example : pattern.getFragments()) {
            augPattern.addExampleLocation(getLocation(example));
        }

        return augPattern;
    }

    private static Location getLocation(Fragment example) {
        EGroumGraph graph = example.getGraph();
        return new Location(graph.getFilePath(), AUGBuilder.getMethodSignature(graph));
    }
}
