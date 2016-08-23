/**
 * 
 */
package mining;

import exas.ExasFeature;
import graphics.DotGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.eclipse.jdt.core.dom.ASTNode;

import egroum.EGroumActionNode;
import egroum.EGroumControlNode;
import egroum.EGroumDataEdge;
import egroum.EGroumDataNode;
import egroum.EGroumEdge;
import egroum.EGroumGraph;
import egroum.EGroumNode;
import egroum.EGroumDataEdge.Type;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class Fragment {
	public static final int minSize = 2;
	public static final int maxSize = 20;
	
	public static int nextFragmentId = 1, numofFragments = 0;
	
	private int id = -1;
	private Fragment genFragmen;
	private ArrayList<EGroumNode> nodes = new ArrayList<>();
	private EGroumGraph graph;
	private HashMap<Integer, Integer> vector = new HashMap<>();
	private int idSum = 0;
	
	private Fragment() {
		this.id = nextFragmentId++;
		numofFragments++;
	}
	
	public Fragment(EGroumNode node) {
		this();
		this.graph = node.getGraph();
		nodes.add(node);
		this.idSum = node.getId();
		vector.put(1, 1);
	}
	
	public Fragment(Fragment fragment, ArrayList<EGroumNode> ens) {
		this();
		this.genFragmen = fragment;
		this.graph = fragment.graph;
		this.nodes = new ArrayList<EGroumNode>(fragment.getNodes());
		this.idSum = fragment.getIdSum();
		this.vector = new HashMap<Integer, Integer>(fragment.getVector());
		for (EGroumNode en : ens) {
			this.nodes.add(en);
			this.idSum += en.getId();
			ExasFeature exasFeature = new ExasFeature(nodes);
			buildVector(en, exasFeature);
		}
	}
	
	public void buildVector(EGroumNode node, ExasFeature exasFeature) {
		ArrayList<String> sequence = new ArrayList<>();
		sequence.add(node.getLabel());
		backwardDFS(node, node, sequence, exasFeature);
	}
	
	private void backwardDFS(EGroumNode firstNode, EGroumNode lastNode, ArrayList<String> sequence, ExasFeature exasFeature) {
		forwardDFS(firstNode, lastNode, sequence, exasFeature);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH) {
			for(EGroumEdge e : firstNode.getInEdges()) {
				if (nodes.contains(e.getSource())) {
					EGroumNode n = e.getSource();
					sequence.add(0, e.getLabel());
					sequence.add(0, n.getLabel());
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
				if (nodes.contains(e.getTarget())) {
					EGroumNode n = e.getTarget();
					sequence.add(e.getLabel());
					sequence.add(n.getLabel());
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
	public void setId(int id) {
		this.id = id;
	}
	
	public Fragment getGenFragmen() {
		return genFragmen;
	}

	public void setGenFragmen(Fragment genFragmen) {
		this.genFragmen = genFragmen;
	}

	public ArrayList<EGroumNode> getNodes() {
		return nodes;
	}

	public HashSet<EGroumEdge> getEdges() {
		HashSet<EGroumEdge> edges = new HashSet<>();
		for (EGroumNode node : nodes) {
			for (EGroumEdge e : node.getInEdges())
				if (nodes.contains(e.getSource()))
					edges.add(e);
			for (EGroumEdge e : node.getOutEdges())
				if (nodes.contains(e.getTarget()))
					edges.add(e);
		}
		return edges;
	}
	
	public void setNodes(ArrayList<EGroumNode> nodes) {
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
	public void setId() {
		this.id = nextFragmentId++;
		//return this.id;
	}
	/**
	 * @return the graph
	 */
	public EGroumGraph getGraph() {
		return graph;
	}
	/**
	 * @param graph the graph to set
	 */
	public void setGraph(EGroumGraph graph) {
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
		return nodes.equals(other.nodes);
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
	public boolean overlap(Fragment fragment) {
		if (this == fragment) {
			System.err.println("Same fragment in checking overlap");
			return false;
		}
		if (fragment == null) {
			System.err.println("NULL fragment in checking overlap");
			return false;
		}
		HashSet<EGroumNode> tempNodes = new HashSet<EGroumNode>();
		tempNodes.addAll(fragment.nodes);
		tempNodes.retainAll(this.nodes);
		return !tempNodes.isEmpty();
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
		StringBuilder graph = new StringBuilder();
		DotGraph dg = new DotGraph(graph);
		graph.append(dg.addStart("" + getId()));

		HashMap<EGroumNode, Integer> ids = new HashMap<EGroumNode, Integer>();
		// add nodes
		int id = 0;
		for(EGroumNode node : nodes) {
			id++;
			ids.put(node, id);
			if(node instanceof EGroumControlNode)
				graph.append(dg.addNode(id, node.getLabel(), DotGraph.SHAPE_DIAMOND, null, null, null));
			else if (node instanceof EGroumActionNode)
				graph.append(dg.addNode(id, node.getLabel(), DotGraph.SHAPE_BOX, null, null, null));
			else
				graph.append(dg.addNode(id, node.getLabel(), DotGraph.SHAPE_ELLIPSE, null, null, null));
		}
		// add edges
		for(EGroumNode node : nodes) {
			int sId = ids.get(node);
			for(EGroumEdge out : node.getOutEdges()) {
				if (nodes.contains(out.getTarget())) {
					int eId = ids.get(out.getTarget());
					graph.append(dg.addEdge(sId, eId, null, null, out.getLabel()));
				}
			}
		}

		graph.append(dg.addEnd());
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdirs();
		//name += "_ " + FileIO.getSimpleFileName(EGroumNode.fileNames.get(this.graph.getFileID()));
		dg.toDotFile(new File(path + "/" + name + ".dot"));
		dg.toGraphics(path + "/" + name, "png");
	}
	
	public void delete() {
		this.genFragmen = null;
		this.graph = null;
		this.nodes.clear();
		this.vector.clear();
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

	public HashMap<String, HashSet<ArrayList<EGroumNode>>> extend() {
		HashSet<EGroumNode> ens = new HashSet<>();
		for (EGroumNode node : nodes) {
			for (EGroumNode n : node.getInNodes()) {
				if (!nodes.contains(n))
					ens.add(n);
			}
			for (EGroumNode n : node.getOutNodes()) {
				if (!nodes.contains(n))
					ens.add(n);
			}
		}
		HashMap<String, HashSet<ArrayList<EGroumNode>>> lens = new HashMap<>();
		for (EGroumNode node : ens) {
			if (node instanceof EGroumActionNode){
				if (node.isCoreAction()) {
					add(node, lens);
				} else {
					HashSet<EGroumNode> ins = node.getInNodes(), outs = node.getOutNodes();
					if (!ins.isEmpty() && !outs.isEmpty()) {
						boolean found = false;
						for (EGroumNode n : ins) {
							if (nodes.contains(n)) {
								found = true;
								break;
							}
						}
						if (found) {
							found = false;
							for (EGroumNode n : outs) {
								if (nodes.contains(n) && n.isCoreAction()) {
									found = true;
									break;
								}
							}
							if (found) {
								add(node, lens);
							} else {
								for (EGroumNode n : outs) {
									if (n.isCoreAction())
										add(node, n, lens);
								}
							}
						} else {
							found = false;
							for (EGroumNode n : outs) {
								if (nodes.contains(n) && n.isCoreAction()) {
									found = true;
									break;
								}
							}
							if (found) {
								for (EGroumNode next : ins) {
									add(node, next, lens);
								}
							}
						}
					}
				}
			} else if (node instanceof EGroumDataNode) {
				boolean hasThrow = false;
				for (EGroumEdge e : node.getInEdges()) {
					if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).getType() == Type.THROW && nodes.contains(e.getSource())) {
						add(node, lens);
						hasThrow = true;
						break;
					}
				}
				if (!hasThrow) {
					int count = 0;
					HashSet<EGroumNode> outs = node.getOutNodes();
					for (EGroumNode next : outs) {
						if (nodes.contains(next)) {
							count++;
							if (count == 1)
								break;
						}
					}
					if (count == 1) {
						for (EGroumEdge e : node.getInEdges()) {
							if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).getType() == Type.THROW) {
								add(node, e.getSource(), lens);
								hasThrow = true;
							}
						}
						if (!hasThrow) {
							HashSet<EGroumNode> defs = new HashSet<>();
							if (node.getAstNodeType() == ASTNode.SIMPLE_NAME)
								defs.addAll(null);
							else
								for (EGroumEdge e : node.getInEdges())
									if (e instanceof EGroumDataEdge && ((EGroumDataEdge) e).getType() == Type.DEFINITION) {
										defs.add(e.getSource());
										break;
									}
							if (defs.isEmpty())
								add(node, lens);
						}
					}
				}
			}
		}
		Random r = new Random();
		for (String label : lens.keySet()) {
			HashSet<ArrayList<EGroumNode>> s = lens.get(label);
			if (s.size() > 50) {
				ArrayList<ArrayList<EGroumNode>> l = new ArrayList<>(s);
				s.clear();
				for (int i = 0; i < 50; i++) {
					int j = r.nextInt(l.size());
					s.add(l.get(j));
					l.remove(j);
				}
			}
		}
		return lens;
	}

	private void add(EGroumNode node, HashMap<String, HashSet<ArrayList<EGroumNode>>> lens) {
		String label = node.getLabel();
		HashSet<ArrayList<EGroumNode>> s = lens.get(label);
		if (s == null) {
			s = new HashSet<>();
			lens.put(label, s);
		}
		ArrayList<EGroumNode> l = new ArrayList<>();
		l.add(node);
		s.add(l);
	}

	private void add(EGroumNode node, EGroumNode next, HashMap<String, HashSet<ArrayList<EGroumNode>>> lens) {
		String label = node.getLabel() + "-" + next.getLabel();
		HashSet<ArrayList<EGroumNode>> s = lens.get(label);
		if (s == null) {
			s = new HashSet<>();
			lens.put(label, s);
		}
		ArrayList<EGroumNode> l = new ArrayList<>();
		l.add(node);
		l.add(next);
		s.add(l);
	}
}
