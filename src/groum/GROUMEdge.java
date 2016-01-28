package groum;

public class GROUMEdge {
	public static int numOfEdges = 0;
	
	private int id;
	private GROUMNode src;
	private GROUMNode dest;
	private String label = ".";
	
	public GROUMEdge(GROUMNode src, GROUMNode dest) {
		this.id = ++numOfEdges;
		this.src = src;
		this.dest = dest;
		src.addOutEdge(this);
		dest.addInEdge(this);
	}
	public GROUMEdge(GROUMNode src, GROUMNode dest, String label) {
		this(src, dest);
		this.label = label;
	}
	
	public GROUMNode getSrc() {
		return src;
	}
	public void setSrc(GROUMNode node) {
		if (dest.getInNodes().contains(node))
			delete();
		else
		{
			this.src = node;
			node.addOutEdge(this);
		}
	}
	public GROUMNode getDest() {
		return dest;
	}
	public void setDest(GROUMNode node) {
		if (src.getOutNodes().contains(node))
			delete();
		else
		{
			this.dest = node;
			node.addInEdge(this);
		}
	}
	/**
	 * @return the index
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the index to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	public boolean isCore()
	{
		return src.isCore() || dest.isCore();
	}
	public void delete() {
		this.src.getOutEdges().remove(this);
		this.dest.getInEdges().remove(this);
	}
}
