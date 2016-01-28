package exas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import utils.Pair;
import utils.PairDescendingOrder;

public class ExasGraph {
	public static double THRESHOLD_SIM = 0.65;
	private HashSet<ExasNode> nodes = new HashSet<ExasNode>();
	
	public ExasGraph() {
		
	}
	
	public ExasGraph(HashSet<ExasNode> nodes) {
		this.nodes = nodes;
	}
	
	public HashSet<ExasNode> getNodes() {
		return nodes;
	}

	public void setNodes(HashSet<ExasNode> nodes) {
		this.nodes = nodes;
	}
	
	public void buildVectors()
	{
		for (ExasNode node : this.nodes)
			node.buildVector();
	}
	
	public void printVector()
	{
		for (ExasNode node : this.nodes)
		{
			//System.out.println(node + ": " + node.getVector());
			System.out.println(node + ": " + node.printVector());
		}
	}
	
	public void print()
	{
		int numOfEdges = 0;
		for (ExasNode node : nodes)
		{
			System.out.println(node);
			System.out.println("\tIn: " + node.getIncomingNodes());
			System.out.println("\tOut: " + node.getOutgoingNodes());
			numOfEdges += node.getIncomingNodes().size();
			numOfEdges += node.getOutgoingNodes().size();
		}
		System.out.println(nodes.size() + " nodes " + (numOfEdges/2) + " edges");
	}
	
	public static void match(ExasGraph g1, ExasGraph g2, HashSet<ExasNode> matchedNodes1, HashSet<ExasNode> matchedNodes2)
	{
		HashSet<ExasNode> nodes1 = g1.getNodes(), nodes2 = g2.getNodes();
		PairDescendingOrder comparator = new PairDescendingOrder();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		HashMap<ExasNode, HashSet<Pair>> pairsOf1 = new HashMap<ExasNode, HashSet<Pair>>(), pairsOf2 = new HashMap<ExasNode, HashSet<Pair>>();
		for(ExasNode node1 : nodes1)
		{
			HashSet<Pair> pairs1 = pairsOf1.get(node1);
			if(pairs1 == null)
				pairs1 = new HashSet<Pair>();
			for(ExasNode node2 : nodes2)
			{
				Double sim = node1.computeSimilarity(node2);
				if(sim >= THRESHOLD_SIM)
				{
					Pair pair = new Pair(node1, node2, sim);
					int index = Collections.binarySearch(pairs, pair, comparator);
					if(index < 0)
						pairs.add(-1-index, pair);
					else
						pairs.add(index, pair);
					pairs1.add(pair);
					HashSet<Pair> pairs2 = pairsOf2.get(node2);
					if(pairs2 == null)
						pairs2 = new HashSet<Pair>();
					pairs2.add(pair);
					pairsOf2.put(node2, pairs2);
				}
			}
			pairsOf1.put(node1, pairs1);
		}
		while(!pairs.isEmpty())
		{
			Pair pair = pairs.get(0);
			ExasNode node1 = (ExasNode)pair.getObj1(), node2 = (ExasNode)pair.getObj2();
			matchedNodes1.add(node1); matchedNodes2.add(node2);
			for(Pair p : pairsOf1.get(node1))
				pairs.remove(p);
			for(Pair p : pairsOf2.get(node2))
				pairs.remove(p);
		}
	}
}
