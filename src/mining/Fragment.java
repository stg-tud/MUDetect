/**
 * 
 */
package mining;

import exas.ExasFeature;
import exas.ExasSingleFeature;

import graphics.DotGraph;
import groum.GROUMEdge;
import groum.GROUMGraph;
import groum.GROUMNode;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import utils.FileIO;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class Fragment implements Serializable {
	private static final long serialVersionUID = 3L;
	
	public static final int minSize = 2;
	public static final int maxSize = 20;
	
	public static int nextFragmentId = 1, numofFragments = 0;
	
	private int id = -1;
	private HashSet<GROUMNode> nodes = new HashSet<GROUMNode>();
	private GROUMGraph graph;
	private Fragment genParent;
	private HashMap<ExasFeature, Integer> vector = new HashMap<ExasFeature, Integer>();
	private HashSet<Fragment> clones = null; //new HashSet<Fragment>();
	private HashMap<Integer, Integer> labels = new HashMap<Integer, Integer>();
	private boolean isExtended = false;
	private Pattern pattern = null;
	private HashMap<String, HashSet<GROUMNode>> neighbors = new HashMap<String, HashSet<GROUMNode>>();
	private int idSum = 0;
	
	private Fragment()
	{
		this.id = nextFragmentId++;
		numofFragments++;
	}
	
	public Fragment(GROUMNode node)
	{
		this();
		this.graph = node.getGraph();
		nodes.add(node);
		this.idSum = node.getId();
		HashMap<String, HashSet<GROUMNode>> newNeighbors = node.getNeighbors();
		for (String label : newNeighbors.keySet())
		{
			HashSet<GROUMNode> ns = neighbors.get(label);
			if (ns == null)
				ns = new HashSet<GROUMNode>();
			ns.addAll(new HashSet<GROUMNode>(newNeighbors.get(label)));
			neighbors.put(label, ns);
		}
		ExasFeature f = ExasFeature.getFeature(node.getLabel());
		vector.put(f, 1);
	}
	
	public Fragment(Fragment fragment, GROUMNode node)
	{
		this();
		this.genParent = fragment;
		this.graph = fragment.getGraph();
		this.nodes = new HashSet<GROUMNode>(fragment.getNodes());
		this.nodes.add(node);
		this.idSum = fragment.getIdSum() + node.getId();
		this.vector = new HashMap<ExasFeature, Integer>(fragment.getVector());
		buildVector(node);
	}
	
	public void addNeighbors(Fragment fragment, GROUMNode node)
	{
		this.neighbors = new HashMap<String, HashSet<GROUMNode>>();
		for (String label : fragment.getNeighbors().keySet())
			this.neighbors.put(label, new HashSet<GROUMNode>(fragment.getNeighbors().get(label)));
		this.neighbors.get(node.getLabel()).remove(node);
		if (this.neighbors.get(node.getLabel()).isEmpty())
			this.neighbors.remove(node.getLabel());
		HashMap<String, HashSet<GROUMNode>> newNeighbors = node.getNeighbors();
		for (String label : newNeighbors.keySet())
		{
			HashSet<GROUMNode> ns = neighbors.get(label);
			if (ns == null)
				ns = new HashSet<GROUMNode>();
			HashSet<GROUMNode> newNs = new HashSet<GROUMNode>(newNeighbors.get(label));
			newNs.removeAll(nodes);
			ns.addAll(newNs);
			if (ns.isEmpty())
				neighbors.remove(label);
			else
				neighbors.put(label, ns);
		}
	}
	
	public void buildVector(GROUMNode node)
	{
		ExasSingleFeature feature = ExasFeature.getFeature(node.getLabel());
		ArrayList<ExasSingleFeature> sequence = new ArrayList<ExasSingleFeature>();
		sequence.add(feature);
		backwardDFS(node, node, sequence);
	}
	
	private void backwardDFS(GROUMNode firstNode, GROUMNode lastNode, ArrayList<ExasSingleFeature> sequence)
	{
		forwardDFS(firstNode, lastNode, sequence);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH)
		{
			HashSet<GROUMNode> inNodes = new HashSet<GROUMNode>(firstNode.getInNodes());
			inNodes.retainAll(nodes);
			for(GROUMNode n : inNodes)
			{
				ExasSingleFeature sf = ExasFeature.getFeature(n.getLabel());
				sequence.add(0, sf);
				backwardDFS(n, lastNode, sequence);
				sequence.remove(0);
			}
		}
	}
	
	private void forwardDFS(GROUMNode firstNode, GROUMNode lastNode, ArrayList<ExasSingleFeature> sequence)
	{
		ExasFeature feature = ExasFeature.getFeature(sequence);
		addFeature(feature, vector);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH)
		{
			HashSet<GROUMNode> outNodes = new HashSet<GROUMNode>(lastNode.getOutNodes());
			outNodes.retainAll(nodes);
			for(GROUMNode n : outNodes)
			{
				ExasSingleFeature sf = ExasFeature.getFeature(n.getLabel());
				sequence.add(sf);
				forwardDFS(firstNode, n, sequence);
				sequence.remove(sequence.size()-1);
			}
		}
	}
	
	private void addFeature(ExasFeature feature, HashMap<ExasFeature, Integer> vector)
	{
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
	public void setNodes(HashSet<GROUMNode> nodes) {
		this.nodes = nodes;
	}
	public int getIdSum() {
		return idSum;
	}
	public Fragment getGenParent() {
		return genParent;
	}
	public void setGenParent(Fragment genParent) {
		this.genParent = genParent;
	}
	public HashMap<ExasFeature, Integer> getVector() {
		return vector;
	}
	public void setVector(HashMap<ExasFeature, Integer> vector) {
		this.vector = vector;
	}
	public HashSet<Fragment> getClones() {
		return clones;
	}
	public void setClones(HashSet<Fragment> clones) {
		this.clones = clones;
	}
	public HashMap<Integer, Integer> getLabels() {
		return labels;
	}
	public void setLabels(HashMap<Integer, Integer> labels) {
		this.labels = labels;
	}
	public boolean isExtended() {
		return isExtended;
	}
	public void setExtended(boolean isExtended) {
		this.isExtended = isExtended;
	}
	public Pattern getPattern() {
		return pattern;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
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
	public int getHashCode() {
		Hash hash = new Hash();
		Hash.reset(1, 1, ExasFeature.numOfFeatures);
		return hash.hashEuclidean(this)[0];
	}
	public HashSet<GROUMNode> getNodes() {
		return nodes;
	}
	public HashMap<String, HashSet<GROUMNode>> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(HashMap<String, HashSet<GROUMNode>> neighbors) {
		this.neighbors = neighbors;
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
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} 
		else if (this.idSum != other.getIdSum() || !nodes.equals(other.nodes))
			return false;
		return true;
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
		return (tempNodes.size() > 0);
	}
	/**
	 * 
	 * @return
	 */
	public int coverNonOverlapWithMultiSettings()
	{
		HashSet<GROUMGraph> cfgs = new HashSet<GROUMGraph>();
		cfgs.add(this.graph);
		for(Fragment fragment : this.clones)
		{
			if(!cfgs.contains(fragment.getGraph()))
				cfgs.add(fragment.getGraph());
			if(cfgs.size() >= Pattern.minFreq)
				return Pattern.minFreq;
		}
		
		return cfgs.size();
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		/*result.append("Clone " + this.id + ": " + 
				this.nodes.size() + " nodes, " + 
				this.edges.size() + " edges, " +
				"generated from " + this.genParent +
				"\n");*/
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
			result.append(node.getMethodID() + " ");
		result.append("\r\n");
		for (GROUMNode node : this.nodes) {
			result.append("Node: " + node.getId() + 
					" - Label: " + GROUMNode.labelOfID.get(node.getClassNameId()) + "." + GROUMNode.labelOfID.get(node.getObjectNameId()) + "." + node.getMethod() + " - " + node.getMethodID() + 
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
	}
	
	public void toGraphics(String path, String name)
	{
		StringBuilder graph = new StringBuilder();
		DotGraph dg = new DotGraph(graph);
		graph.append(dg.addStart());

		HashMap<GROUMNode, Integer> ids = new HashMap<GROUMNode, Integer>();
		// add nodes
		int id = 0;
		for(GROUMNode node : nodes)
		{
			id++;
			ids.put(node, id);
			if(node.getType() == GROUMNode.TYPE_CONTROL)
				graph.append(dg.addNode(id, node.getLabel(), DotGraph.SHAPE_DIAMOND, null, null, null));
			else
				graph.append(dg.addNode(id, node.getLabel(), DotGraph.SHAPE_BOX, DotGraph.STYLE_ROUNDED, null, null));
		}
		// add edges
		for(GROUMNode node : nodes)
		{
			int sId = ids.get(node);
			for(GROUMEdge out : node.getOutEdges())
			{
				if (nodes.contains(out.getDest()))
				{
					int eId = ids.get(out.getDest());
					graph.append(dg.addEdge(sId, eId, null, null, out.getLabel()));
				}
			}
		}

		graph.append(dg.addEnd());
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdirs();
		name += "_ " + FileIO.getSimpleFileName(GROUMNode.fileNames.get(this.graph.getFileID()));
		dg.toDotFile(new File(path + "/" + name + ".dot"));
		dg.toGraphics(path + "/" + name, "png");
	}
	
	public void delete()
	{
		this.genParent = null;
		try {
			this.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		numofFragments--;
	}
	
	HashSet<Fragment> exactCloneList(HashSet<Fragment> group, Fragment frag) {
		HashSet<Fragment> res = new HashSet<Fragment>();
		group.remove(frag);
		for (Fragment u : group){
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
		for (Fragment u : group){
			if (!frag.overlap(u))
				res.add(u);
		}
		if(res.contains(frag))
			res.remove(frag);
		return res;
	}
}
