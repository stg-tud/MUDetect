package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.InstanceMethodCallPredicate;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.ConstructorCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;

import java.util.function.Function;
import java.util.function.Predicate;

public class Configuration {
    public int minPatternSize = 1, maxPatternSize = Integer.MAX_VALUE;
    public int minPatternSupport = 10, maxPatternSupport = 1000;

    /**
     * Predicate that decides whether mining is started from a give node.
     */
    public Predicate<Node> isStartNode = new InstanceMethodCallPredicate().and(Node::isCoreAction);

    public enum DataNodeExtensionStrategy { ALWAYS, IF_INCOMING, IF_INCOMING_AND_OUTGOING }
    /**
     * Whether or not the miner should extend a pattern with a data node.
     */
    public DataNodeExtensionStrategy extendByDataNode = DataNodeExtensionStrategy.IF_INCOMING;

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

    /**
     * Whether to extend patterns along order edges. If set to false, order edges will appear in patterns only between
     * nodes that are also connected by a different type of edge, either directly or via other nodes.
     */
    public boolean extendAlongOrderEdges = false;
}
