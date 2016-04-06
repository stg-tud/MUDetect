/**
 * 
 */
package mining;

import exas.ExasFeature;
import graphics.DotGraph;
import groum.GROUMEdge;
import groum.GROUMGraph;
import groum.GROUMNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

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
	private ArrayList<GROUMNode> nodes = new ArrayList<>();
	private GROUMGraph graph;
	private HashMap<Integer, Integer> vector = new HashMap<>();
	private int idSum = 0;
	
	private Fragment() {
		this.id = nextFragmentId++;
		numofFragments++;
	}
	
	public Fragment(GROUMNode node) {
		this();
		this.graph = node.getGraph();
		nodes.add(node);
		this.idSum = node.getId();
		vector.put(1, 1);
	}
	
	public Fragment(Fragment fragment, ArrayList<GROUMNode> ens) {
		this();
		this.genFragmen = fragment;
		this.graph = fragment.graph;
		this.nodes = new ArrayList<GROUMNode>(fragment.getNodes());
		this.idSum = fragment.getIdSum();
		this.vector = new HashMap<Integer, Integer>(fragment.getVector());
		for (GROUMNode en : ens) {
			this.nodes.add(en);
			this.idSum += en.getId();
			ExasFeature exasFeature = new ExasFeature(nodes);
			buildVector(en, exasFeature);
		}
	}
	
	public void buildVector(GROUMNode node, ExasFeature exasFeature) {
		ArrayList<String> sequence = new ArrayList<>();
		sequence.add(node.getLabel());
		backwardDFS(node, node, sequence, exasFeature);
	}
	
	private void backwardDFS(GROUMNode firstNode, GROUMNode lastNode, ArrayList<String> sequence, ExasFeature exasFeature) {
		forwardDFS(firstNode, lastNode, sequence, exasFeature);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH) {
			for(GROUMEdge e : firstNode.getInEdges()) {
				if (nodes.contains(e.getSrc())) {
					GROUMNode n = e.getSrc();
					sequence.add(0, e.getLabel());
					sequence.add(0, n.getLabel());
					backwardDFS(n, lastNode, sequence, exasFeature);
					sequence.remove(0);
					sequence.remove(0);
				}
			}
		}
	}
	
	private void forwardDFS(GROUMNode firstNode, GROUMNode lastNode, ArrayList<String> sequence, ExasFeature exasFeature) {
		int feature = exasFeature.getFeature(sequence);
		addFeature(feature, vector);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH) {
			for(GROUMEdge e : lastNode.getOutEdges()) {
				if (nodes.contains(e.getDest())) {
					GROUMNode n = e.getDest();
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

	public ArrayList<GROUMNode> getNodes() {
		return nodes;
	}
	public void setNodes(ArrayList<GROUMNode> nodes) {
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
	public GROUMGraph getGraph() {
		return graph;
	}
	/**
	 * @param graph the graph to set
	 */
	public void setGraph(GROUMGraph graph) {
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
		if (nodes.size() >= fragment.nodes.size() && nodes.containsAll((fragment.nodes)))
			return true;
		else
			return false;
	}
	/**
	 * 
	 */
	public boolean contains(GROUMNode node) {
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
		HashSet<GROUMNode> tempNodes = new HashSet<GROUMNode>();
		tempNodes.addAll(fragment.nodes);
		tempNodes.retainAll(this.nodes);
		for (GROUMNode node : tempNodes)
			if (node.isMethod())
				return true;
		return false;
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
			result.append("File: " + GROUMNode.fileNames.get(this.graph.getFileID()) + "\r\n");
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println(this.graph);
			System.err.println(this.graph.getFileID());
			System.err.println(GROUMNode.fileNames.get(this.graph.getFileID()));
		}
		//result.append("Vector: " + this.gramVector + "\r\n");
		result.append(this.nodes.size() + " Nodes: ");
		for(GROUMNode node : this.nodes)
			result.append(node.getLabel() + " ");
		result.append("\r\n");
		for (GROUMNode node : this.nodes) {
			result.append("Node: " + node.getId() + 
					" - Label: " + node.getLabel() + 
					"\tLines: " + node.getStartLine() + "-->" + node.getEndLine() + "\r\n");
		}
		result.append("Edges:\r\n");
		HashSet<GROUMNode> nodes = new HashSet<GROUMNode>(this.nodes);
		LinkedList<GROUMNode> queue = new LinkedList<GROUMNode>();
		for(GROUMNode node : nodes)
		{
			HashSet<GROUMNode> tmp = new HashSet<GROUMNode>(node.getOutNodes());
			tmp.retainAll(nodes);
			if(tmp.isEmpty())
				queue.add(node);
		}
		nodes.removeAll(queue);
		while(!queue.isEmpty())
		{
			GROUMNode node = queue.poll();
			HashSet<GROUMNode> tmp = new HashSet<GROUMNode>(node.getInNodes());
			tmp.retainAll(this.nodes);
			for(GROUMNode n : tmp)
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
		graph.append(dg.addStart());

		HashMap<GROUMNode, Integer> ids = new HashMap<GROUMNode, Integer>();
		// add nodes
		int id = 0;
		for(GROUMNode node : nodes) {
			id++;
			ids.put(node, id);
			if(node.getType() == GROUMNode.TYPE_CONTROL)
				graph.append(dg.addNode(id, node.getLabel(), DotGraph.SHAPE_DIAMOND, null, null, null));
			else if (node.getType() == GROUMNode.TYPE_METHOD)
				graph.append(dg.addNode(id, node.getLabel(), DotGraph.SHAPE_BOX, DotGraph.STYLE_ROUNDED, null, null));
			else
				graph.append(dg.addNode(id, node.getLabel(), DotGraph.SHAPE_ELLIPSE, null, null, null));
		}
		// add edges
		for(GROUMNode node : nodes) {
			int sId = ids.get(node);
			for(GROUMEdge out : node.getOutEdges()) {
				if (nodes.contains(out.getDest())) {
					int eId = ids.get(out.getDest());
					graph.append(dg.addEdge(sId, eId, null, null, out.getLabel()));
				}
			}
		}

		graph.append(dg.addEnd());
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdirs();
		//name += "_ " + FileIO.getSimpleFileName(GROUMNode.fileNames.get(this.graph.getFileID()));
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

	public HashMap<String, HashSet<ArrayList<GROUMNode>>> extend() {
		HashSet<GROUMNode> ens = new HashSet<>();
		for (GROUMNode node : nodes) {
			for (GROUMNode n : node.getInNodes()) {
				if (!nodes.contains(n))
					ens.add(n);
			}
			for (GROUMNode n : node.getOutNodes()) {
				if (!nodes.contains(n))
					ens.add(n);
			}
		}
		HashMap<String, HashSet<ArrayList<GROUMNode>>> lens = new HashMap<>();
		for (GROUMNode node : ens) {
			if (node.getLabel().equals(nodes.get(nodes.size() - 1).getLabel())) 
				continue;
			if (node.getType() == GROUMNode.TYPE_CONTROL) {
				HashSet<GROUMNode> ins = node.getInNodes(), outs = node.getOutNodes();
				if (!ins.isEmpty() && !outs.isEmpty()) {
					boolean found = false;
					for (GROUMNode n : ins) {
						if (nodes.contains(n)) {
							found = true;
							break;
						}
					}
					if (found) {
						found = false;
						for (GROUMNode n : outs) {
							if (n.isMethod() && nodes.contains(n)) {
								found = true;
								break;
							}
						}
						if (found) {
							String label = node.getLabel();
							HashSet<ArrayList<GROUMNode>> s = lens.get(label);
							if (s == null) {
								s = new HashSet<>();
								lens.put(label, s);
							}
							ArrayList<GROUMNode> l = new ArrayList<>();
							l.add(node);
							s.add(l);
						} else {
							for (GROUMNode next : outs) {
								if (next.isMethod()) {
									String label = node.getLabel() + "-" + next.getLabel();
									HashSet<ArrayList<GROUMNode>> s = lens.get(label);
									if (s == null) {
										s = new HashSet<>();
										lens.put(label, s);
									}
									ArrayList<GROUMNode> l = new ArrayList<>();
									l.add(node);
									l.add(next);
									s.add(l);
								}
							}
						}
					} else {
						for (GROUMNode next : ins) {
							String label = node.getLabel() + "-" + next.getLabel();
							HashSet<ArrayList<GROUMNode>> s = lens.get(label);
							if (s == null) {
								s = new HashSet<>();
								lens.put(label, s);
							}
							ArrayList<GROUMNode> l = new ArrayList<>();
							l.add(node);
							l.add(next);
							s.add(l);
						}
					}
				}
			} else {
				String label = node.getLabel();
				HashSet<ArrayList<GROUMNode>> s = lens.get(label);
				if (s == null) {
					s = new HashSet<>();
					lens.put(label, s);
				}
				ArrayList<GROUMNode> l = new ArrayList<>();
				l.add(node);
				s.add(l);
			}
		}
		Random r = new Random();
		for (String label : lens.keySet()) {
			HashSet<ArrayList<GROUMNode>> s = lens.get(label);
			if (s.size() > 50) {
				ArrayList<ArrayList<GROUMNode>> l = new ArrayList<>(s);
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
}
