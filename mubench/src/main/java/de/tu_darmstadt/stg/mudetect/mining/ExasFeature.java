package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.src2aug.EGroumDataEdge;
import de.tu_darmstadt.stg.mudetect.src2aug.EGroumNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class ExasFeature {
	public static final int MAX_LENGTH = 4 * 2 - 1;
	private static HashMap<String, Integer> edgeFeatures = new HashMap<>();
	static {
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.DEFINITION),  edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.PARAMETER), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.QUALIFIER), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.RECEIVER), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.REFERENCE), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.ORDER), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.THROW), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.FINALLY), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONTAINS), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION, "sel"), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION, "rep"), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION, "syn"), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION, "hdl"), edgeFeatures.size());
		edgeFeatures.put("_control_", edgeFeatures.size());
	}

	public static void abstractConditionEdges() {
		edgeFeatures.clear();
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.DEFINITION),  edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.PARAMETER), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.QUALIFIER), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.RECEIVER), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.REFERENCE), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.ORDER), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.THROW), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.FINALLY), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONTAINS), edgeFeatures.size());
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION, "sel"), 0);
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION, "rep"), 0);
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION, "syn"), 0);
		edgeFeatures.put(EGroumDataEdge.getLabel(EGroumDataEdge.Type.CONDITION, "hdl"), 0);
		edgeFeatures.put("_control_", edgeFeatures.size());
	}

	private HashMap<String, Integer> nodeFeatures = new HashMap<>();

	public ExasFeature(ArrayList<Node> nodes, Function<Node, String> nodeToLabel) {
		for (int i = 0; i < nodes.size(); i++) {
			String label = nodeToLabel.apply(nodes.get(i));
			if (!nodeFeatures.containsKey(label))
				nodeFeatures.put(label, i + 1);
		}
	}

	public int getNodeFeature(String label) {
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
