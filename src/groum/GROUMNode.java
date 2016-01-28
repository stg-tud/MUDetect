package groum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;

public class GROUMNode implements Serializable {
	private static final long serialVersionUID = 4L;
	public static final int TYPE_FIELD = 0;
	public static final int TYPE_METHOD = 1;
	public static final int TYPE_CONTROL = 2;
	public static final int TYPE_OTHER = 11111;
	public static final int TYPE_SINGLE = 1;
	public static final int TYPE_MULTIPLE = 2;
	public static final int TYPE_LOOP = 3;
	public static int numOfNodes = 0;
	
	private int id;
	private String label;
	private int methodId;
	private int objectNameId;
	private int classNameId;
	private HashSet<Integer> parameters;
	private int type = TYPE_OTHER;
	private int singletonType = 1;
	private int fileID;
	private int startLine;
	private int endLine;
	private String pid;
	private GROUMGraph graph;
	/*private HashSet<GROUMNode> inNodes = new HashSet<GROUMNode>();
	private HashSet<GROUMNode> outNodes = new HashSet<GROUMNode>();*/
	private HashSet<GROUMEdge> inEdges = new HashSet<GROUMEdge>();
	private HashSet<GROUMEdge> outEdges = new HashSet<GROUMEdge>();

	public static HashMap<String, Integer> idOfLabel = new HashMap<String, Integer>();
	public static HashMap<Integer, String> labelOfID = new HashMap<Integer, String>();
	
	public static ArrayList<String> fileNames = new ArrayList<String>();
	
	public GROUMNode(String label) {
		this.id = ++numOfNodes;
		this.label = label;
	}
	
	public GROUMNode(String methodName, int type, String className, String objectName){
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.type = type;
		this.classNameId = convertLabel(className);
		this.objectNameId = convertLabel(objectName);
		//setLabel(String.valueOf((this.classNameId << 16) + methodId));
		setLabel();
	}
	
	public GROUMNode(String methodName, int type, String className,
			String objectName, HashSet<Integer> parameters) {
		this.id = ++numOfNodes;
		this.methodId = convertLabel(methodName);
		this.type = type;
		this.classNameId = convertLabel(className);
		this.objectNameId = convertLabel(objectName);
		this.parameters = new HashSet<Integer>(parameters);
		//setLabel(String.valueOf((this.classNameId << 16) + methodId));
		setLabel();
	}

	public static int convertLabel(String label){
		//System.out.println(label + " with id " + index);
		if (GROUMNode.idOfLabel.get(label) == null){
			int index = GROUMNode.idOfLabel.size()+1;
			GROUMNode.idOfLabel.put(label, index);
			GROUMNode.labelOfID.put(index,label);
			return index;
		}
		else
			return GROUMNode.idOfLabel.get(label);
	}
	
	public int getId() {
		return id;
	}
	
	public String getMethod() {
		return GROUMNode.labelOfID.get(methodId);
	}
	
	public int getMethodID() {
		return methodId;
	}
	public String getLabel()
	{
		return label;
	}
	public void setLabel() {
		//label = (this.classNameId << 16) + this.methodId;
		//label = this.classNameId + "." + this.methodId;
		label = getClassName() + "." + getMethod();
	}
	public boolean hasDataDependency(GROUMNode node){
		//if (this.labelID == node.getLabelID()) return true;
		/*if(this.getCompoundLabel() == node.getCompoundLabel())
			return false;*/
		/*if (this.objectNameId == node.getObjectName()) 
			return true;*/
		//if (this.className == node.getClassName()) return true;
		if((this.parameters != null && !this.parameters.isEmpty() && this.parameters.contains(node.getObjectNameId())) || 
				(node.getParameters() != null && !node.getParameters().isEmpty() && node.getParameters().contains(this.objectNameId)))
			return true;
		//if (this.type == node.getType() && this.type == GROUMNode.TYPE_METHOD && parameters.size() > 0)
		{
			HashSet<Integer> pars = new HashSet<Integer>();
			pars.addAll(parameters);
			pars.retainAll(node.getParameters());
			if (!pars.isEmpty()) 
				return true;
		}
		return false;
	}

	public int getType() {
		return type;
	}

	public void setParameters(HashSet<Integer> parameters)
	{
		this.parameters = parameters; 
	}
	public HashSet<Integer> getParameters() {
		return parameters;
	}

	public int getClassNameId() {
		return classNameId;
	}
	
	public String getClassName() {
		return labelOfID.get(classNameId);
	}

	public int getObjectNameId() {
		return objectNameId;
	}
	
	public String getObjectName() {
		return labelOfID.get(objectNameId);
	}

	/**
	 * @return the singletonType
	 */
	public int getSingletonType() {
		return singletonType;
	}

	/**
	 * @param singletonType the singletonType to set
	 */
	public void setSingletonType(int singletonType) {
		this.singletonType = singletonType;
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

	/**
	 * @return the startLine
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * @param startLine the startLine to set
	 */
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	/**
	 * @return the endLine
	 */
	public int getEndLine() {
		return endLine;
	}

	/**
	 * @param endLine the endLine to set
	 */
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public GROUMGraph getGraph() {
		return graph;
	}

	/**
	 * @param graph the graph to set
	 */
	public void setGraph(GROUMGraph graph) {
		this.graph = graph;
	}

	/**
	 * @return the pid
	 */
	public String getPid() {
		return pid;
	}
	/**
	 * @param pid the pid to set
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public HashSet<GROUMEdge> getInEdges() {
		return inEdges;
	}

	public HashSet<GROUMEdge> getOutEdges() {
		return outEdges;
	}
	
	public HashSet<GROUMNode> getInNodes() {
		HashSet<GROUMNode> nodes = new HashSet<GROUMNode>();
		for (GROUMEdge e : inEdges)
			nodes.add(e.getSrc());
		return nodes;
	}
	
	public HashSet<GROUMNode> getOutNodes() {
		HashSet<GROUMNode> nodes = new HashSet<GROUMNode>();
		for (GROUMEdge e : outEdges)
			nodes.add(e.getDest());
		return nodes;
	}
	public void addInEdge(GROUMEdge edge) {
		inEdges.add(edge);
	}
	public void addOutEdge(GROUMEdge edge) {
		outEdges.add(edge);
	}
	
	public HashMap<String, HashSet<GROUMNode>> getNeighbors()
	{
		HashMap<String, HashSet<GROUMNode>> neighbors = new HashMap<String, HashSet<GROUMNode>>();
		for (GROUMEdge e : inEdges)
		{
			if (e.isCore())
			{
				GROUMNode n = e.getSrc();
				String label = n.getLabel();
				HashSet<GROUMNode> ns = neighbors.get(label);
				if (ns == null)
					ns = new HashSet<GROUMNode>();
				ns.add(n);
				neighbors.put(label, ns);
			}
		}
		for (GROUMEdge e : outEdges)
		{
			if (e.isCore())
			{
				GROUMNode n = e.getDest();
				String label = n.getLabel();
				HashSet<GROUMNode> ns = neighbors.get(label);
				if (ns == null)
					ns = new HashSet<GROUMNode>();
				ns.add(n);
				neighbors.put(label, ns);
			}
		}
		return neighbors;
	}
	
	public boolean isCore()
	{
		//return webType != NodeType.Action;
		return true;
	}
	
	public boolean isFunctionInvocation()
	{
		return type == TYPE_METHOD || getLabel().endsWith("()");
	}
	
	public void delete() {
		for (GROUMEdge e  : new HashSet<GROUMEdge>(inEdges))
			e.delete();
		for (GROUMEdge e : new HashSet<GROUMEdge>(outEdges))
			e.delete();
		graph.removeNode(this);
	}
	
	public String toString()
	{
		return label;
	}
	/*public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(labelOfID.get(this.className) + "." + labelOfID.get(this.objectName) + "." + labelOfID.get(this.labelID) + "\r\n");
		buf.append("Type: " + this.singletonType + "\r\n");
		buf.append("File: " + fileNames.get(this.fileID) + "\t" + "Lines: " + this.startLine + " - " + this.endLine);
		
		return buf.toString();
	}*/
	public static boolean isCore(String label)
	{
		return label.endsWith("()");
	}
}
