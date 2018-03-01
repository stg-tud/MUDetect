package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge;
import de.tu_darmstadt.stg.mudetect.aug.visitors.AUGLabelProvider;

import java.util.ArrayList;
import java.util.HashMap;

public class ExasFeature {
	public static final int MAX_LENGTH = 4 * 2 - 1;
	private static HashMap<String, Integer> edgeFeatures = new HashMap<>();

	private HashMap<String, Integer> nodeFeatures = new HashMap<>();

	public ExasFeature(ArrayList<Node> nodes, AUGLabelProvider labelProvider) {
		for (int i = 0; i < nodes.size(); i++) {
			String label = labelProvider.getLabel(nodes.get(i));
			if (!nodeFeatures.containsKey(label))
				nodeFeatures.put(label, i + 1);
		}
	}

	public int getNodeFeature(String label) {
		return nodeFeatures.get(label);
	}

	private int getEdgeFeature(String label) {
		if (!edgeFeatures.containsKey(label)) {
			edgeFeatures.put(label, edgeFeatures.size());
		}
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
