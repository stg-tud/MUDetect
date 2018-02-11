package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.*;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.AggregateDataNode;

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

    public Model mine(Collection<APIUsageExample> examples) {
        return mine(new ArrayList<>(examples));
    }

    private Model mine(ArrayList<APIUsageExample> examples) {
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
            Miner miner = new Miner("-subgraph-finder-", config);
            return toModel(miner.mine(examples));
        } finally {
            System.setOut(originalOut);
        }
    }

    private Model toModel(Set<Pattern> patterns) {
        return () -> patterns.stream().map(DefaultAUGMiner::toAUGPattern).collect(Collectors.toSet());
    }

    private static APIUsagePattern toAUGPattern(Pattern pattern) {
        Set<Location> exampleLocations = new HashSet<>();
        for (Fragment example : pattern.getFragments()) {
            exampleLocations.add(example.getGraph().getLocation());
        }

        APIUsagePattern augPattern = new APIUsagePattern(pattern.getFreq(), exampleLocations);

        Fragment f = pattern.getRepresentative();
        List<Node> nodes = f.getNodes();
        Map<Node, Node> nodeMap = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Node newNode;
            if (node instanceof DataNode) {
                Set<DataNode> equivalentNodes = new HashSet<>();
                for (Fragment fragment : pattern.getFragments()) {
                    DataNode eqivalentNode = (DataNode) fragment.getNodes().get(i);
                    equivalentNodes.add(eqivalentNode);
                }
                newNode = new AggregateDataNode(((DataNode) node).getType(), equivalentNodes);
            } else {
                newNode = node.clone();
            }
            newNode.setGraph(augPattern);
            nodeMap.put(node, newNode);
            augPattern.addVertex(newNode);
        }
        APIUsageGraph graph = f.getGraph();
        for (Node node : nodes) {
            for (Edge e : graph.incomingEdgesOf(node)) {
                Node source = graph.getEdgeSource(e);
                if (nodeMap.containsKey(source)) {
                    Node newSourceNode = nodeMap.get(source);
                    Node newTargetNode = nodeMap.get(graph.getEdgeTarget(e));
                    Edge newEdge = e.clone(newSourceNode, newTargetNode);
                    augPattern.addEdge(newSourceNode, newTargetNode, newEdge);
                }
            }
        }

        return augPattern;
    }
}
