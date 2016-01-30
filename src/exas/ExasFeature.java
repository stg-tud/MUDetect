package exas;

import groum.GROUMNode;

import java.util.ArrayList;
import java.util.HashMap;

public class ExasFeature {
	public static final int MAX_LENGTH = 4 * 2 - 1;
	private static HashMap<String, Integer> edgeFeatures = new HashMap<>();
	static {
		edgeFeatures.put("_cond_", 0);
		edgeFeatures.put("_control_", 1);
		edgeFeatures.put("_def_",  2);
		edgeFeatures.put("_dep_",  3);
		edgeFeatures.put("_para_", 4);
		edgeFeatures.put("_qual_", 5);
		edgeFeatures.put("_recv_", 6);
		edgeFeatures.put("_ref_", 7);
	}
	
	private HashMap<String, Integer> nodeFeatures = new HashMap<>();
	
	public ExasFeature(ArrayList<GROUMNode> nodes) {
		for (int i = 0; i < nodes.size(); i++)
			nodeFeatures.put(nodes.get(i).getLabel(), i + 1);
	}

	private int getNodeFeature(String label) {
		return nodeFeatures.get(label);
	}
	
	private int getEdgeFeature(String label) {
		return edgeFeatures.get(label);
	}
	
	public int getFeature(ArrayList<String> labels) {
		int f = 0, s;
		for (int i = 0; i < labels.size(); i++) {
			if (i % 2 == 0) {
				s = getNodeFeature(labels.get(i));
			}
			else {
				s = getEdgeFeature(labels.get(i));
				s = s << 5;
				f = f << 8;
			}
			f += s;
		}
		return f;
	}

	public int getFeature(String label) {
		return getNodeFeature(label);
	}
}
