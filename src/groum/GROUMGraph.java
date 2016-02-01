package groum;

import graphics.DotGraph;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;


public class GROUMGraph implements Serializable {
	private static final long serialVersionUID = 1L;
	public static int nextId = 1;
	private int id;
	private int fileID = -1;
	private String name;
	protected HashSet<GROUMNode> nodes = new HashSet<GROUMNode>();
	//set of output nodes which have out-degree = 0;
	protected HashSet<GROUMNode> outs = new HashSet<GROUMNode>();
	//set of intput nodes which have in-degree = 0;
	protected HashSet<GROUMNode> ins = new HashSet<GROUMNode>();
	
	public GROUMGraph() {
		this.id = nextId++;
	}

	public void addNode(GROUMNode node) 
	{
		if (!nodes.contains(node)){
			outs.add(node);
			nodes.add(node);
			ins.add(node);
		}
	}
	
	public void removeNode(GROUMNode node)
	{
		if (nodes.contains(node))
		{
			nodes.remove(node);
			ins.remove(node);
			outs.remove(node);
		}
	}

	/* merge two graph together
	 * merge outs with target's ins
	 * update outs and ins 
	 */
	public void mergeSeq(GROUMGraph target) {
		//merge Node
		if (target.getNodes().size() == 0) return;
		
		if (nodes.size() == 0){
			//add Nodes
			nodes.addAll(target.getNodes());
			//merge Edges
			ins.addAll(target.getIns());
			outs.addAll(target.getOuts());
			return;
		}
		
		//add Nodes
		nodes.addAll(target.getNodes());
		//merge Edges
		
		for (GROUMNode aNode:outs){
			for (GROUMNode anoNode:target.ins){
				createNewEdge(aNode,anoNode);
				/*CFGDirectedEdge edge = new CFGDirectedEdge(aNode,anoNode);
				edges.add(edge);*/
			}
		}
		
		//keep only outs of right most child iff the child has outs
		if (target.getOuts().size() != 0){
			outs.removeAll(outs);
			outs.addAll(target.getOuts());
		}
	}

	public GROUMEdge createNewEdge(GROUMNode node, GROUMNode anoNode) {
		if (!node.getOutNodes().contains(anoNode))
			return new GROUMEdge(node, anoNode);
		return null;
	}
	/**
	 * @return the index
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param index the index to set
	 */
	public void setId(int index) {
		this.id = index;
	}

	/**
	 * @return the fileID
	 */
	public int getFileID() {
		return fileID;
	}

	/**
	 * @param fileID the fileID to set
	 */
	public void setFileID(int fileID) {
		this.fileID = fileID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashSet<GROUMNode> getNodes() {
		return nodes;
	}

	public HashSet<GROUMNode> getOuts() {
		return outs;
	}

	public HashSet<GROUMNode> getIns() {
		return ins;
	}

	public void mergeABranch(GROUMGraph target, GROUMNode branch) {
		//merge Node
		if (target.getNodes().size() == 0) return;
		
		//add Nodes
		nodes.addAll(target.getNodes());
		//merge Edges
		for (GROUMNode aNode:target.ins){
			
			createNewEdge(branch, aNode);
			/*CFGDirectedEdge edge = new CFGDirectedEdge(branch,aNode);
			edges.add(edge);*/
		}
		//keep all outs node of children
		outs.addAll(target.getOuts());
	}

	public void mergeSeq(GROUMNode branch) {
		this.addNode(branch);
		/*
		 * branch will not be considered ins node except the input graph is size 0
		 */
		ins.remove(branch);
		for (GROUMNode aNode:outs){
			if (!aNode.equals(branch)){
				createNewEdge(aNode,branch);
				/*CFGDirectedEdge edge = new CFGDirectedEdge(aNode,branch);
				edges.add(edge);*/
			}
		}
		
		if (ins.size() == 0) ins.add(branch);
			outs.removeAll(outs);
			outs.add(branch);
		
	}

	public void mergeBranches(GROUMGraph target, HashSet<GROUMNode> saveOuts) {
		if (target.getNodes().size() == 0) return;
		
		if (saveOuts.size() == 0){
			//add Nodes
			nodes.addAll(target.getNodes());
			//merge Edges
			
			ins.addAll(target.getIns());
			outs.addAll(target.getOuts());
			return;
		}
		
		//add Nodes
		nodes.addAll(target.getNodes());
		//merge Edges
		
		for (GROUMNode aNode:saveOuts){
			for (GROUMNode anoNode:target.ins){
				createNewEdge(aNode,anoNode);
				/*CFGDirectedEdge edge = new CFGDirectedEdge(aNode,anoNode);
				edges.add(edge);*/
			}
		}
		outs.addAll(target.getOuts());
	}

	/**
	 * 
	 */
	public void prune()
	{
		for(GROUMNode node : new HashSet<GROUMNode>(this.nodes))
		{
			if(node.getType() != GROUMNode.TYPE_METHOD)
				continue;
			for(GROUMEdge edge : new HashSet<GROUMEdge>(node.getOutEdges()))
			{
				GROUMNode next = edge.getDest();
				if(node.getOutEdges().size() == 1 && node.getObjectNameId() == next.getObjectNameId() && node.getMethod() == next.getMethod())
				{
					for (GROUMEdge inEdge : node.getInEdges())
					{
						inEdge.setDest(next);
					}
					if(node.getParameters() != null && !node.getParameters().isEmpty())
					{
						if(next.getParameters() == null)
							next.setParameters(new HashSet<Integer>());
						next.getParameters().addAll(node.getParameters());
						//next.getParameters().remove(next.getObjectNameId());
					}
					next.setSingletonType(GROUMNode.TYPE_MULTIPLE);
					edge.delete();
					this.nodes.remove(node);
					break;
				}
			}
		}
	}
	/*
	 * 
	 */
	public void addDataDependency(){
		for (GROUMNode node : nodes){
			addDataDependency(node);
		}
	}

	private void addDataDependency(GROUMNode node) {
		/*if(node.getLabel() == "IF" || node.getLabel() == "FOR" || node.getLabel() == "WHILE" || node.getLabel() == "DOWHILE" || node.getLabel() == "SWITCH")
			return;*/
		/*if(node.getType() == GROUMNode.TYPE_CONTROL)
			return;*/
		
		Vector<GROUMNode> queue = new Vector<GROUMNode>();
		//queue.add(node);
		queue.addAll(node.getOutNodes());
		HashSet<GROUMNode> checkedNodes = new HashSet<GROUMNode>();
		//HashMap<Integer, HashSet<String>> checkedLabels = new HashMap<Integer, HashSet<String>>();//HashSet<Integer> checkedLabels = new HashSet<Integer>();
		HashMap<Integer, HashMap<Integer, Integer>> checkedLabels = new HashMap<Integer, HashMap<Integer,Integer>>();
		HashSet<Integer> checkedObjects = new HashSet<Integer>();
		checkedNodes.add(node);
		/*HashSet<String> pids = new HashSet<String>();
		pids.add(node.getPid());*/
		HashMap<Integer, Integer> depths = new HashMap<Integer, Integer>();
		depths.put(getDepth(node.getPid()), 1);
		int label = (node.getObjectNameId() << 16) + node.getMethodID();
		checkedLabels.put(label, depths);
		checkedObjects.add(node.getObjectNameId());
		while(!queue.isEmpty())
		{
			GROUMNode aNode = queue.firstElement();
			queue.remove(aNode);
			
			for (GROUMNode anoNode : aNode.getOutNodes())
			{
				if(!checkedNodes.contains(anoNode))
				{
					queue.add(anoNode);
					checkedNodes.add(anoNode);
					//if(anoNode.getType() == GROUMNode.TYPE_CONTROL)
					{
						/*if(node.getPid().length() < anoNode.getPid().length() && anoNode.getPid().startsWith(node.getPid()))
						{
							node.addOutNodes(anoNode);
							anoNode.addInNodes(node);
						}*/
						//continue;
					}
					/*if(node.getPid().equals(anoNode.getPid()) && !checkedObjects.contains(anoNode.getObjectName()))
					{
						checkedObjects.add(anoNode.getObjectName());
						node.addOutNodes(anoNode);
						anoNode.addInNodes(node);
					}*/
					label = (anoNode.getObjectNameId() << 16) + anoNode.getMethodID();
					int depth = getDepth(anoNode.getPid());
					//pids = checkedLabels.get(label);
					depths = checkedLabels.get(label);
					//if(!checkedLabels.contains(anoNode.getCompoundLabel()))
					if(depths == null || !depths.containsKey(depth) || depths.get(depth) < 2)
					{
						if((node.getPid().startsWith(anoNode.getPid()) || anoNode.getPid().startsWith(node.getPid())) && 
								node.hasDataDependency(anoNode))
						{
							int c = 0;
							if(depths == null)
								depths = new HashMap<Integer, Integer>();
							else if(depths.containsKey(depth))
								c = depths.get(depth);
							c++;
							depths.put(depth, c);
							checkedLabels.put(label, depths);
							createNewEdge(node, anoNode);
						}
					}
				}
			}
		}
		//System.out.println("STOP QUEUE");
		
	}
	public void removeNonDependents()
	{
		for(GROUMNode node : this.nodes)
		{
			HashSet<GROUMEdge> tmp = new HashSet<GROUMEdge>(node.getOutEdges());
			for(GROUMEdge outEdge : tmp)
			{
				if(!node.hasDataDependency(outEdge.getDest()))
				{
					outEdge.delete();
				}
			}
		}
	}
	public void cleanUp()
	{
		for(GROUMNode node : this.nodes)
		{
			node.setPid(null);
			node.setParameters(null);
		}
	}
	private int getDepth(String pid)
	{
		int depth = 0;
		for(int i = 0; i < pid.length(); i++)
			if(pid.charAt(i) == '.')
				depth++;
		return depth - 1;
	}
	public void toGraphics(String path){
		DotGraph graph = new DotGraph(this);
		graph.toDotFile(new File(path + "/" + name + ".dot"));
		graph.toGraphics(path + "/" + name, "png");
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		HashMap<String,Integer> countLabel = new HashMap<String,Integer>();
		sb.append("Method: " + this.name + " File: " + GROUMNode.fileNames.get(fileID) + "\r\n");
		
		sb.append("List of nodes:" + nodes.size() + "\r\n");
		for (GROUMNode node:nodes){
			sb.append("node " + node.getId() + " with label:" + 
					GROUMNode.labelOfID.get(node.getClassNameId()) + "." + GROUMNode.labelOfID.get(node.getObjectNameId()) + "." + node.getMethod() + 
					" - " + node.getMethodID() +
					" - " + node.getLabel() + "\r\n");

			if (countLabel.get(node.getMethod()) != null){
				int count = (int) countLabel.get(node.getMethod()) + 1;
				countLabel.put(node.getMethod(), count);
			}
			else
				countLabel.put(node.getMethod(),1);
		}

		int numEdges = 0;
		sb.append("List of edges:" + "\r\n");
		for (GROUMNode node:nodes){
			for (GROUMEdge edge : node.getOutEdges()) {
				GROUMNode anoNode = edge.getDest();
				if (!anoNode.getInEdges().contains(edge)){
					System.err.println("ERRORERRORERRORERRORERRORERROR");
					System.err.println(node.getId() + "-" + anoNode.getId());
					//System.exit(0);
				}

				sb.append("node " + node.getId() + " --> node"  + anoNode.getId() + "\r\n");

				numEdges++;
			}
		}
		sb.append("Total " + numEdges + " edges" + "\r\n");
		numEdges = 0;
		for (GROUMNode node:nodes){
			for (GROUMEdge edge : node.getInEdges()) {
				GROUMNode anoNode = edge.getSrc();
				if (!anoNode.getOutEdges().contains(edge)){
					System.err.println("ERRORERRORERRORERRORERRORERROR");
					System.err.println(node.getId() + "-" + anoNode.getId());
					//System.exit(0);
				}

				sb.append("node " + node.getId() + " <-- node"  + anoNode.getId() + "\r\n");

				numEdges++;
			}
		}
		sb.append("Total " + numEdges + " edges" + "\r\n");
		
		return sb.toString();
	}
}
