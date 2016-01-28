package exas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import utils.FeatureAscendingOrder;

public class ExasNode {
	public static int nextID = 1;
	private int id = 1;
	private String label;
	private ArrayList<ExasNode> incomingNodes = new ArrayList<ExasNode>(), outgoingNodes = new ArrayList<ExasNode>();
	private HashMap<ExasFeature, Integer> vector = new HashMap<ExasFeature, Integer>();
	
	public ExasNode() {}
	
	public ExasNode(String label) {
		this.id = nextID++;
		this.label = label;
	}
	
	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public ArrayList<ExasNode> getIncomingNodes() {
		return incomingNodes;
	}
	public void setIncomingNodes(ArrayList<ExasNode> incomingNodes) {
		this.incomingNodes = incomingNodes;
	}
	public ArrayList<ExasNode> getOutgoingNodes() {
		return outgoingNodes;
	}
	public void setOutgoingNodes(ArrayList<ExasNode> outgoingNodes) {
		this.outgoingNodes = outgoingNodes;
	}
	public HashMap<ExasFeature, Integer> getVector() {
		return vector;
	}
	public void setVector(HashMap<ExasFeature, Integer> vector) {
		this.vector = vector;
	}
	
	public void buildVector()
	{
		ExasSingleFeature feature = ExasFeature.getFeature(label);
		ArrayList<ExasSingleFeature> sequence = new ArrayList<ExasSingleFeature>();
		sequence.add(feature);
		backwardDFS(this, this, sequence);
	}
	
	private void backwardDFS(ExasNode firstNode, ExasNode lastNode, ArrayList<ExasSingleFeature> sequence)
	{
		forwardDFS(firstNode, lastNode, sequence);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH)
		{
			for(ExasNode n : firstNode.getIncomingNodes())
			{
				ExasSingleFeature sf = ExasFeature.getFeature(n.getLabel());
				sequence.add(0, sf);
				backwardDFS(n, lastNode, sequence);
				sequence.remove(0);
			}
		}
	}
	
	private void forwardDFS(ExasNode firstNode, ExasNode lastNode, ArrayList<ExasSingleFeature> sequence)
	{
		ExasFeature feature = ExasFeature.getFeature(sequence);
		addFeature(feature, vector);
		feature.setFrequency(feature.getFrequency() + 1);
		
		if(sequence.size() < ExasFeature.MAX_LENGTH)
		{
			for(ExasNode n : lastNode.getOutgoingNodes())
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
	
	public double computeSimilarity(ExasNode other)
	{
		HashMap<ExasFeature, Integer> v1 = this.vector, v2 = other.getVector();
		HashSet<ExasFeature> keys = new HashSet<ExasFeature>(v1.keySet());
		keys.retainAll(v2.keySet());
		int common = 0;
		for (ExasFeature f : keys)
			common += Math.min(v1.get(f), v2.get(f));
		return common * 2.0 / (v1.size() + v2.size());
	}
	
	@Override
	public String toString()
	{
		return "ID: " + this.id + " Label: " + this.label;
	}
	
	public String printVector()
	{
		ArrayList<ExasFeature> keys = new ArrayList<ExasFeature>(this.vector.keySet());
		Collections.sort(keys, new FeatureAscendingOrder());
		StringBuilder sb = new StringBuilder();
		for (ExasFeature f : keys)
		{
			sb.append(f + "=" + vector.get(f) + ' ');
		}
		return sb.toString();
	}
}
