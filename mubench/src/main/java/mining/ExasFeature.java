package mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import de.tu_darmstadt.stg.mudetect.aug.Node;

import static de.tu_darmstadt.stg.mudetect.aug.ConditionEdge.ConditionType.REPETITION;
import static de.tu_darmstadt.stg.mudetect.aug.ConditionEdge.ConditionType.SELECTION;
import static de.tu_darmstadt.stg.mudetect.aug.Edge.Type.*;

public class ExasFeature {
	public static final int MAX_LENGTH = 4 * 2 - 1;
	private static HashMap<String, Integer> edgeFeatures = new HashMap<>();
	static {
		edgeFeatures.put(CONDITION.getLabel(), edgeFeatures.size());
		edgeFeatures.put(DEFINITION.getLabel(),  edgeFeatures.size());
		edgeFeatures.put(ORDER.getLabel(), edgeFeatures.size());
		edgeFeatures.put(PARAMETER.getLabel(), edgeFeatures.size());
		edgeFeatures.put(QUALIFIER.getLabel(), edgeFeatures.size());
		edgeFeatures.put(RECEIVER.getLabel(), edgeFeatures.size());
		edgeFeatures.put(THROW.getLabel(), edgeFeatures.size());
		edgeFeatures.put(FINALLY.getLabel(), edgeFeatures.size());
		edgeFeatures.put(SELECTION.getLabel(), edgeFeatures.size());
		edgeFeatures.put(REPETITION.getLabel(), edgeFeatures.size());
		edgeFeatures.put(SYNCHRONIZE.getLabel(), edgeFeatures.size());
		edgeFeatures.put(CONTAINS.getLabel(), edgeFeatures.size());
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
