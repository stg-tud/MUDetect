package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.ConditionEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class ExasFeature {
	public static final int MAX_LENGTH = 4 * 2 - 1;
	private static HashMap<String, Integer> edgeFeatures = new HashMap<>();
	static {
		edgeFeatures.put(Edge.Type.CONDITION.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.DEFINITION.getLabel(),  edgeFeatures.size());
		edgeFeatures.put(Edge.Type.PARAMETER.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.QUALIFIER.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.RECEIVER.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.ORDER.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.THROW.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.FINALLY.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.CONTAINS.getLabel(), edgeFeatures.size());
		edgeFeatures.put(ConditionEdge.ConditionType.SELECTION.getLabel(), edgeFeatures.size());
		edgeFeatures.put(ConditionEdge.ConditionType.REPETITION.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.SYNCHRONIZE.getLabel(), edgeFeatures.size());
		edgeFeatures.put(Edge.Type.EXCEPTION_HANDLING.getLabel(), edgeFeatures.size());
	}

	public static void abstractConditionEdges() {
		Integer conditionFeatureId = edgeFeatures.get(Edge.Type.CONDITION.getLabel());
		edgeFeatures.put(ConditionEdge.ConditionType.SELECTION.getLabel(), conditionFeatureId);
		edgeFeatures.put(ConditionEdge.ConditionType.REPETITION.getLabel(), conditionFeatureId);
		edgeFeatures.put(Edge.Type.SYNCHRONIZE.getLabel(), conditionFeatureId);
		edgeFeatures.put(Edge.Type.EXCEPTION_HANDLING.getLabel(), conditionFeatureId);
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
