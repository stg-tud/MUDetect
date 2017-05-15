package mining;

import egroum.EGroumNode;

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
     * Whether or not the miner should allow the same call to appear multiple times in a pattern.
     */
    public boolean disallowRepeatedCalls = true;

    /**
     * Path to write mined patterns to. <code>null</code> to disable output.
     */
    public String outputPath = "output/patterns";

    /**
     * Function that maps nodes to labels used in the mining.
     */
    public Function<EGroumNode, String> nodeToLabel = EGroumNode::getLabel;
    
    /**
     * The level of occurrence of instances of a pattern
     * 0: with a method graph
     * 1: cross method graphs
     * 2: cross projects
     */
    public int occurenceLevel = 0;
}
