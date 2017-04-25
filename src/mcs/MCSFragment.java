package mcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import exas.ExasFeature;
import mining.Configuration;

public class MCSFragment {
	public static int nextFragmentId = 1, numofFragments = 0;
	private final Configuration config;

	int id = -1;
	MCSFragment genFragment;
	ArrayList<EGroumNode> nodes = new ArrayList<>();
	ArrayList<EGroumEdge> edges = new ArrayList<>();
	EGroumGraph graph;
	HashMap<Integer, Integer> vector = new HashMap<>();
	int idSum = 0;
	
	public MCSFragment(Configuration config) {
		this.config = config;
		this.id = nextFragmentId++;
		numofFragments++;
	}
	
	public MCSFragment(EGroumNode node, Configuration config) {
		this(config);
		this.graph = node.getGraph();
		nodes.add(node);
		this.idSum = node.getId();
		vector.put(1, 1);
	}
	
	public MCSFragment(MCSFragment fragment, EGroumEdge ee, Configuration config) {
		this(config);
		this.genFragment = fragment;
		this.graph = fragment.graph;
		this.nodes = new ArrayList<EGroumNode>(fragment.nodes);
		this.edges = new ArrayList<>(fragment.edges);
		this.idSum = fragment.idSum;
		this.vector = new HashMap<Integer, Integer>(fragment.vector);
		this.edges.add(ee);
		this.idSum += ee.getId();
		ExasFeature exasFeature = null;
		if (!this.nodes.contains(ee.getSource())) {
			this.nodes.add(ee.getSource());
			this.idSum += ee.getSource().getId();
			exasFeature = new ExasFeature(nodes, config.nodeToLabel);
			int feature = exasFeature.getNodeFeature(config.nodeToLabel.apply(ee.getSource()));
			addFeature(feature, vector);
		}
		if (!this.nodes.contains(ee.getTarget())) {
			this.nodes.add(ee.getTarget());
			this.idSum += ee.getTarget().getId();
			exasFeature = new ExasFeature(nodes, config.nodeToLabel);
			int feature = exasFeature.getNodeFeature(config.nodeToLabel.apply(ee.getTarget()));
			addFeature(feature, vector);
		}
		if (exasFeature == null)
			exasFeature = new ExasFeature(nodes, config.nodeToLabel);
		buildVector(ee, exasFeature);
	}

	public EGroumGraph getGraph() {
		return graph;
	}

	public ArrayList<EGroumNode> getNodes() {
		return nodes;
	}

	public ArrayList<EGroumEdge> getEdges() {
		return edges;
	}

	private void buildVector(EGroumEdge ee, ExasFeature exasFeature) {
		ArrayList<String> sequence = new ArrayList<>();
		sequence.add(config.nodeToLabel.apply(ee.getSource()));
		sequence.add(ee.getLabel());
		sequence.add(config.nodeToLabel.apply(ee.getTarget()));
		backwardDFS(ee.getSource(), ee.getTarget(), sequence, exasFeature);
	}
	
	private void backwardDFS(EGroumNode firstNode, EGroumNode lastNode, ArrayList<String> sequence, ExasFeature exasFeature) {
		forwardDFS(firstNode, lastNode, sequence, exasFeature);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH) {
			for(EGroumEdge e : firstNode.getInEdges()) {
				if (edges.contains(e)) {
					EGroumNode n = e.getSource();
					sequence.add(0, e.getLabel());
					sequence.add(0, config.nodeToLabel.apply(n));
					backwardDFS(n, lastNode, sequence, exasFeature);
					sequence.remove(0);
					sequence.remove(0);
				}
			}
		}
	}
	
	private void forwardDFS(EGroumNode firstNode, EGroumNode lastNode, ArrayList<String> sequence, ExasFeature exasFeature) {
		int feature = exasFeature.getFeature(sequence);
		addFeature(feature, vector);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH) {
			for(EGroumEdge e : lastNode.getOutEdges()) {
				if (edges.contains(e)) {
					EGroumNode n = e.getTarget();
					sequence.add(e.getLabel());
					sequence.add(config.nodeToLabel.apply(n));
					forwardDFS(firstNode, n, sequence, exasFeature);
					sequence.remove(sequence.size()-1);
					sequence.remove(sequence.size()-1);
				}
			}
		}
	}

	private void addFeature(int feature, HashMap<Integer, Integer> vector) {
		int c = 0;
		if (vector.containsKey(feature))
			c = vector.get(feature);
		vector.put(feature, c+1);
	}
	
	public int getVectorHashCode() {
		ArrayList<Integer> keys = new ArrayList<>(vector.keySet());
		Collections.sort(keys);
		int h = 0;
		for (int key : keys) {
			h = h * 31 + vector.get(key);
		}
		return h;
	}
	/**
	 * Set of nodes contains all the nodes of the other fragment
	 * @param fragment
	 * @return
	 */
	public boolean contains(MCSFragment fragment) {
		if(fragment == null) return false;
		if (nodes == null) 
			return false;
		if (graph != fragment.graph)
			return false;
		HashSet<EGroumNode> inter = new HashSet<>(nodes);
		inter.retainAll(fragment.nodes);
		if (inter.size() == fragment.nodes.size())
			return true;
		return false;
	}
	/**
	 * 
	 */
	public boolean contains(EGroumNode node) {
		return this.nodes.contains(node);
	}
	
	public boolean isSameAs(MCSFragment other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (this.idSum != other.idSum)
			return false;
		return nodes.equals(other.nodes);
	}
	
	public void delete() {
		this.genFragment = null;
		this.graph = null;
		this.nodes.clear();
		this.nodes = null;
		this.vector.clear();
		this.vector = null;
		numofFragments--;
	}

	public HashMap<String, HashSet<EGroumEdge>> extend() {
		HashMap<String, HashSet<EGroumEdge>> extendedFragments = new HashMap<>();
		for (EGroumNode node : nodes) {
			extend(node.getInEdges(), extendedFragments);
			extend(node.getOutEdges(), extendedFragments);
		}
		return extendedFragments;
	}

	private void extend(ArrayList<EGroumEdge> nextEdges, HashMap<String, HashSet<EGroumEdge>> labelEdges) {
		for (EGroumEdge e : nextEdges) {
			if (!edges.contains(e)) {
				String label = e.getLabel();
				HashSet<EGroumEdge> es = labelEdges.get(label);
				if (es == null) {
					es = new HashSet<>();
					labelEdges.put(label, es);
				}
				es.add(e);
			}
		}
	}
}
