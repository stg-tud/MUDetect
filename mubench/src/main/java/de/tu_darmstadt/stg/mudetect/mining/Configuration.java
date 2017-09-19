package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.Node;

import java.util.function.Function;

public class Configuration {
    public int minPatternSize = 1, maxPatternSize = Integer.MAX_VALUE;
    public int minPatternSupport = 10, maxPatternSupport = 1000;

    /**
     * Whether or not the miner should extend a pattern with an incoming data node, if that data node is not defined by
     * a core-action node.
     */
    public boolean extendSourceDataNodes = true;

    /**
     * Whether or not the miner should output log information to System.out.
     */
    public boolean disableSystemOut = false;
    
    /**
     * Whether or not the miner should allow the same call to appear multiple times successively, i.e., they are connected, in a pattern.
     * Repeated calls on disconnected nodes, e.g., on different branches, are still allowed.
     */
    public boolean disallowRepeatedCalls = true;

    /**
     * Path to write mined patterns to. <code>null</code> to disable output.
     */
    public String outputPath = "output/patterns";

    /**
     * Function that maps nodes to labels used in the mining.
     */
    public Function<Node, String> nodeToLabel = Node::getLabel;
    
    public enum Level {WITHIN_METHOD, CROSS_METHOD, CROSS_PROJECT}
    /**
     * The level of occurrence of instances of a pattern
     */
    public Level occurenceLevel = Level.WITHIN_METHOD;
    
    /**
     * Abstract labels of condition edges (sel, rep, sync, handle)
     */
    public boolean abstractConditionEdges = false;
}
