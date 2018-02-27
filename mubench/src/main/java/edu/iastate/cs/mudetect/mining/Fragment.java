package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.*;
import de.tu_darmstadt.stg.mudetect.aug.model.data.VariableNode;
import edu.iastate.cs.egroum.dot.DotGraph;

import java.io.File;
import java.util.*;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.THROW;
import static edu.iastate.cs.mudetect.mining.Configuration.DataNodeExtensionStrategy.*;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class Fragment {
	public static final int minSize = 2;
	public static final int maxSize = 20;
	
	public static int nextFragmentId = 1, numofFragments = 0;
	private final Configuration config;

	private int id = -1;
	private Fragment genFragment;
	private ArrayList<Node> nodes = new ArrayList<>();
	private APIUsageExample graph;
	private HashMap<Integer, Integer> vector = new HashMap<>();
	private int idSum = 0;
	
	private Fragment(Configuration config) {
		this.config = config;
		this.id = nextFragmentId++;
		numofFragments++;
	}
	
	public Fragment(Node node, Configuration config) {
		this(config);
		this.graph = (APIUsageExample) node.getGraph();
		nodes.add(node);
		this.idSum = node.getId();
		vector.put(1, 1);
	}
	
	public Fragment(Fragment fragment, ArrayList<Node> ens) {
		this(fragment.config);
		this.genFragment = fragment;
		this.graph = fragment.graph;
		this.nodes = new ArrayList<>(fragment.getNodes());
		this.idSum = fragment.getIdSum();
		this.vector = new HashMap<>(fragment.getVector());
		for (Node en : ens) {
			this.nodes.add(en);
			this.idSum += en.getId();
			ExasFeature exasFeature = new ExasFeature(nodes, config.labelProvider);
			buildVector(en, exasFeature);
		}
	}
	
	public void buildVector(Node node, ExasFeature exasFeature) {
		ArrayList<String> sequence = new ArrayList<>();
		sequence.add(config.labelProvider.getLabel(node));
		backwardDFS(node, node, sequence, exasFeature);
	}
	
	private void backwardDFS(Node firstNode, Node lastNode, ArrayList<String> sequence, ExasFeature exasFeature) {
		forwardDFS(firstNode, lastNode, sequence, exasFeature);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH) {
			APIUsageGraph graph = firstNode.getGraph();
			for(Edge e : graph.incomingEdgesOf(firstNode)) {
				Node n = graph.getEdgeSource(e);
				if (nodes.contains(n)) {
					sequence.add(0, config.labelProvider.getLabel(e));
					sequence.add(0, config.labelProvider.getLabel(n));
					backwardDFS(n, lastNode, sequence, exasFeature);
					sequence.remove(0);
					sequence.remove(0);
				}
			}
		}
	}
	
	private void forwardDFS(Node firstNode, Node lastNode, ArrayList<String> sequence, ExasFeature exasFeature) {
		int feature = exasFeature.getFeature(sequence);
		addFeature(feature, vector);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH) {
			APIUsageGraph graph = lastNode.getGraph();
			for(Edge e : graph.outgoingEdgesOf(lastNode)) {
				Node n = graph.getEdgeTarget(e);
				if (nodes.contains(n)) {
					sequence.add(config.labelProvider.getLabel(e));
					sequence.add(config.labelProvider.getLabel(n));
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
	
	public int getId() {
		return id;
	}

	public Fragment getGenFragment() {
		return genFragment;
	}

	public void setGenFragmen(Fragment genFragment) {
		this.genFragment = genFragment;
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public HashSet<Edge> getEdges() {
		HashSet<Edge> edges = new HashSet<>();
		for (Node node : nodes) {
			APIUsageGraph graph = node.getGraph();
			for (Edge e : graph.incomingEdgesOf(node))
				if (nodes.contains(graph.getEdgeSource(e)))
					edges.add(e);
			for (Edge e : graph.outgoingEdgesOf(node))
				if (nodes.contains(graph.getEdgeTarget(e)))
					edges.add(e);
		}
		return edges;
	}
	
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}
	
	public int getIdSum() {
		return idSum;
	}
	public HashMap<Integer, Integer> getVector() {
		return vector;
	}
	public void setVector(HashMap<Integer, Integer> vector) {
		this.vector = vector;
	}

	/**
	 * @return the graph
	 */
	public APIUsageExample getGraph() {
		return graph;
	}
	/**
	 * @param graph the graph to set
	 */
	public void setGraph(APIUsageExample graph) {
		this.graph = graph;
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
	 * Not exactly matched but the same vector
	 * @param frag
	 * @return
	 */
	public boolean exactCloneTo(Fragment frag) {
		if (this == frag) {
			System.err.println("Same fragment in exactCloneTo!!!");
			return false;
		}
		if (frag == null) {
			System.err.println("NULL fragment in exactCloneTo!!!");
			return false;
		}
		/*HashSet<CFGNode> tempNodes = new HashSet<CFGNode>();
		tempNodes.addAll(frag.nodes);
		tempNodes.retainAll(this.nodes);
		if(tempNodes.size() > 0)
			return false;*/
		if (vector == null || frag.vector == null) {
			System.err.println("NULL vector!!!");
			return false;
		}
		if(this.nodes.size() != frag.nodes.size() || !this.vector.equals(frag.vector))
			return false;
		return true;
	}
	/**
	 * The same subgraph - same set of nodes
	 * @param other
	 * @return
	 */
	public boolean isSameAs(Fragment other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (this.idSum != other.getIdSum())
			return false;
		return new HashSet<>(nodes).equals(new HashSet<>(other.nodes));
	}
	/**
	 * Set of nodes contains all the nodes of the other fragment
	 * @param fragment
	 * @return
	 */
	public boolean contains(Fragment fragment) {
		if(fragment == null) return false;
		if (nodes == null) 
			return false;
		if (graph != fragment.getGraph())
			return false;
		HashSet<Node> inter = new HashSet<>(nodes);
		inter.retainAll(fragment.nodes);
		if (inter.size() == fragment.nodes.size())
			return true;
		return false;
	}
	/**
	 * 
	 */
	public boolean contains(Node node) {
		return this.nodes.contains(node);
	}
	public boolean overlap(Fragment fragment) {
		if (this == fragment) {
			System.err.println("Same fragment in checking overlap");
			return false;
		}
		if (fragment == null) {
			System.err.println("NULL fragment in checking overlap");
			return false;
		}
		return nodes.stream().filter(node -> node instanceof ActionNode).anyMatch(fragment.nodes::contains);
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	/*@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Fragment " + this.id + ": " + 
				this.nodes.size() + " nodes\r\n");
		try {
			result.append("File: " + EGroumNode.fileNames.get(this.graph.getFileID()) + "\r\n");
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println(this.graph);
			System.err.println(this.graph.getFileID());
			System.err.println(EGroumNode.fileNames.get(this.graph.getFileID()));
		}
		//result.append("Vector: " + this.gramVector + "\r\n");
		result.append(this.nodes.size() + " Nodes: ");
		for(EGroumNode node : this.nodes)
			result.append(node.getLabel() + " ");
		result.append("\r\n");
		for (EGroumNode node : this.nodes) {
			result.append("Node: " + node.getId() + 
					" - Label: " + node.getLabel() + 
					"\tLines: " + node.getStartLine() + "-->" + node.getEndLine() + "\r\n");
		}
		result.append("Edges:\r\n");
		HashSet<EGroumNode> nodes = new HashSet<EGroumNode>(this.nodes);
		LinkedList<EGroumNode> queue = new LinkedList<EGroumNode>();
		for(EGroumNode node : nodes)
		{
			HashSet<EGroumNode> tmp = new HashSet<EGroumNode>(node.getOutNodes());
			tmp.retainAll(nodes);
			if(tmp.isEmpty())
				queue.add(node);
		}
		nodes.removeAll(queue);
		while(!queue.isEmpty())
		{
			EGroumNode node = queue.poll();
			HashSet<EGroumNode> tmp = new HashSet<EGroumNode>(node.getInNodes());
			tmp.retainAll(this.nodes);
			for(EGroumNode n : tmp)
			{
				//result.append(node.getLabel() + "-->" + n.getLabel() + " ");
				result.append(node.getId() + "<--" + n.getId() + " ");
			}
			tmp.retainAll(nodes);
			queue.addAll(tmp);
			nodes.removeAll(tmp);
		}
		result.append("\r\n--------------------------------------------------\r\n");
		
		return result.toString();
	}*/
	
	public void toGraphics(String path, String name) {
		DotGraph dg = getDotGraph();
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdirs();
		//name += "_ " + FileIO.getSimpleFileName(EGroumNode.fileNames.get(this.graph.getFileID()));
		dg.toDotFile(new File(path + "/" + name + ".dot"));
		dg.toGraphics(path + "/" + name, "png");
	}

	public DotGraph getDotGraph() {
		StringBuilder graph = new StringBuilder();
		DotGraph dg = new DotGraph(graph);
		graph.append(dg.addStart("" + getId()));

		HashMap<Node, Integer> ids = new HashMap<>();
		// add nodes
		int id = 0;
		for(Node node : nodes) {
			id++;
			ids.put(node, id);
			if(node instanceof DataNode)
				graph.append(dg.addNode(id, config.labelProvider.getLabel(node), DotGraph.SHAPE_ELLIPSE, null, null, null));
			else if (node instanceof ActionNode)
				graph.append(dg.addNode(id, config.labelProvider.getLabel(node), DotGraph.SHAPE_BOX, null, null, null));
			else
				graph.append(dg.addNode(id, config.labelProvider.getLabel(node), DotGraph.SHAPE_DIAMOND, null, null, null));
		}
		// add edges
		for(Node node : nodes) {
			int sId = ids.get(node);
			APIUsageGraph graph1 = node.getGraph();
			for(Edge out : graph1.outgoingEdgesOf(node)) {
				Node target = graph1.getEdgeTarget(out);
				if (nodes.contains(target)) {
					int eId = ids.get(target);
					graph.append(dg.addEdge(sId, eId, out.isDirect() ? null : DotGraph.STYLE_DOTTED, null, config.labelProvider.getLabel(out)));
				}
			}
		}

		graph.append(dg.addEnd());
		return dg;
	}

	void delete() {
		this.genFragment = null;
		this.graph = null;
		if (nodes != null)
			this.nodes.clear();
		this.nodes = null;
		if (vector != null)
			this.vector.clear();
		this.vector = null;
		numofFragments--;
	}
	
	@Override
	protected void finalize() throws Throwable {
		delete();
	}
	
	HashSet<Fragment> exactCloneList(HashSet<Fragment> group, Fragment frag) {
		HashSet<Fragment> res = new HashSet<Fragment>();
		group.remove(frag);
		for (Fragment u : group) {
			if (frag.exactCloneTo(u)) 
				res.add(u);
		}
		if(res.contains(frag))
			res.remove(frag);
		return res;
	}
	
	HashSet<Fragment> nonOverlapCloneList(HashSet<Fragment> group, Fragment frag) {
		HashSet<Fragment> res = new HashSet<Fragment>();
		group.remove(frag);
		for (Fragment u : group) {
			if (!frag.overlap(u))
				res.add(u);
		}
		if(res.contains(frag))
			res.remove(frag);
		return res;
	}

	public HashMap<String, HashSet<ArrayList<Node>>> extend() {
		HashSet<Node> ens = new HashSet<>(), exclusions = new HashSet<>();
		for (Node node : nodes) {
			APIUsageGraph graph = node.getGraph();
			for (Edge e : graph.incomingEdgesOf(node)) {
                Node n = e.getSource();
                boolean extendAlongEdge = isExtendAlongEdge(e);
                if (n.isCoreAction() && config.labelProvider.getLabel(n).equals(config.labelProvider.getLabel(node)))
                    exclusions.add(n);
                else if (!nodes.contains(n) && extendAlongEdge)
                    ens.add(n);
			}

            for (Edge e : graph.outgoingEdgesOf(node)) {
                Node n = e.getTarget();
                boolean extendAlongEdge = isExtendAlongEdge(e);
                if (n.isCoreAction() && config.labelProvider.getLabel(n).equals(config.labelProvider.getLabel(node)))
                    exclusions.add(n);
                else if (!nodes.contains(n) && extendAlongEdge)
                    ens.add(n);
            }
		}
		if (config.disallowRepeatedCalls)
			ens.removeAll(exclusions);
		HashMap<String, HashSet<ArrayList<Node>>> lens = new HashMap<>();
		for (Node node : ens) {
			APIUsageGraph graph = node.getGraph();
			if (node instanceof ActionNode){
				if (node.isCoreAction()) {
					add(node, lens);
				} else {
				    // extend by non-core action, if ...
					Set<Node> ins = graph.incomingNodesOf(node), outs = graph.outgoingNodesOf(node);
					if (!ins.isEmpty() && !outs.isEmpty()) {
                        if (containsAnyOf(ins)) {
                            // ... it consumes data in the fragment and ...
                            if (containsAnyOf(outs)) {
                                // ... it produces data in the fragment.
								add(node, lens);
							} else {
                                // ... it is a predecessor of a core action.
								for (Node n : outs) {
									if (n.isCoreAction())
										add(node, n, lens);
								}
							}
						} else if (containsAnyOf(outs)) {
                            // ... it produces data in the fragment and ...
                            for (Node next : ins) {
                                if (next.isCoreAction()
                                        || ((config.extendByDataNode == ALWAYS || config.extendByDataNode == IF_INCOMING) && next instanceof DataNode))
                                    // ... it is a successor of a core action OR of a data node by which we might extend
                                    add(node, next, lens);
                            }
						}
					}
				}
			} else if (node instanceof DataNode) {
			    if (config.extendByDataNode == ALWAYS) {
			        add(node, lens);
                } else if (config.extendByDataNode == IF_INCOMING) {
					boolean hasThrow = false;
					for (Edge e : graph.incomingEdgesOf(node)) {
						if (e.getType() == THROW && nodes.contains(graph.getEdgeSource(e))) {
							add(node, lens);
							hasThrow = true;
							break;
						}
					}
					if (!hasThrow) {
						Set<Node> outs = graph.outgoingNodesOf(node);
                        if (containsAnyOf(outs))
							add(node, lens);
					}
				} else if (config.extendByDataNode == IF_INCOMING_AND_OUTGOING) {
					boolean hasThrow = false;
					for (Edge e : graph.incomingEdgesOf(node)) {
						if (e.getType() == THROW && nodes.contains(graph.getEdgeSource(e))) {
							add(node, lens);
							hasThrow = true;
							break;
						}
					}
					if (!hasThrow) {
						int count = 0;
						Set<Node> outs = graph.outgoingNodesOf(node);
						for (Node next : outs) {
							if (nodes.contains(next)) {
								count++;
								if (count == 1)
									break;
							}
						}
						if (count == 1) {
							for (Edge e : graph.incomingEdgesOf(node)) {
								if (e.getType() == THROW) {
									add(node, graph.getEdgeSource(e), lens);
									hasThrow = true;
								}
							}
							if (!hasThrow) {
								HashSet<Node> defs = new HashSet<>();
								if (node instanceof VariableNode)
									defs.add(null);
								else
									for (Edge e : graph.incomingEdgesOf(node))
										if (e.getType() == DEFINITION) {
											defs.add(graph.getEdgeSource(e));
											break;
										}
								if (defs.isEmpty())
									add(node, lens);
							}
						}
					}
				} else {
			        throw new IllegalArgumentException("missing handling for data-node extension strategy: " + config.extendByDataNode);
                }
			}
		}
//		Random r = new Random();
//		for (String label : lens.keySet()) {
//			HashSet<ArrayList<EGroumNode>> s = lens.get(label);
//			if (s.size() > 50) {
//				ArrayList<ArrayList<EGroumNode>> l = new ArrayList<>(s);
//				s.clear();
//				for (int i = 0; i < 50; i++) {
//					int j = r.nextInt(l.size());
//					s.add(l.get(j));
//					l.remove(j);
//				}
//			}
//		}
		return lens;
	}

    private boolean containsAnyOf(Set<Node> nodes) {
        for (Node n : nodes) {
            if (this.nodes.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExtendAlongEdge(Edge e) {
		return config.extensionEdgeTypes.contains(e.getClass());
    }

    private void add(Node node, HashMap<String, HashSet<ArrayList<Node>>> lens) {
		String label = config.labelProvider.getLabel(node);
		HashSet<ArrayList<Node>> s = lens.computeIfAbsent(label, k -> new HashSet<>());
		ArrayList<Node> l = new ArrayList<>();
		l.add(node);
		s.add(l);
	}

	private void add(Node node, Node next, HashMap<String, HashSet<ArrayList<Node>>> lens) {
		String label = config.labelProvider.getLabel(node) + "-" + config.labelProvider.getLabel(next);
		HashSet<ArrayList<Node>> s = lens.computeIfAbsent(label, k -> new HashSet<>());
		ArrayList<Node> l = new ArrayList<>();
		l.add(node);
		l.add(next);
		s.add(l);
	}
}
