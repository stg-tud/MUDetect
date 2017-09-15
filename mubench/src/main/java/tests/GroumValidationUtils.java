package tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import egroum.EGroumGraph;
import egroum.EGroumNode;

public class GroumValidationUtils {

	public static void validate(EGroumGraph groum) {
		validateEdges(groum);
		checkAcyclic(groum);
	}

	public static void validateEdges(EGroumGraph groum) {
		HashSet<EGroumNode> nodes = groum.getNodes();
		for (EGroumNode node : nodes) {
			for (EGroumNode outNode : node.getOutNodes()) {
				if (!nodes.contains(outNode)) {
					throw new IllegalStateException("Groum '" + groum.getName() + "' has an edge from '" + node
							+ "' that points outside the groum");
				}
			}
			for (EGroumNode inNode : node.getInNodes()) {
				if (!nodes.contains(inNode)) {
					throw new IllegalStateException("Groum '" + groum.getName() + "' has an edge to '" + node
							+ "' that comes from outside the groum");
				}
			}
		}
	}

	public static void checkAcyclic(EGroumGraph target) {
		for (EGroumNode node : target.getNodes()) {
			if (node.getInEdges().isEmpty())
				checkAcyclic(target, new ArrayList<EGroumNode>(), node);
		}
	}

	private static void checkAcyclic(EGroumGraph groum, List<EGroumNode> path, EGroumNode node) {
		if (path.contains(node)) {
			throw new IllegalStateException("Groum '" + groum + "' is cyclic at " + path);
		}
		path.add(node);
		for (EGroumNode successor : node.getOutNodes()) {
			checkAcyclic(groum, path, successor);
		}
		path.remove(node);
	}
}
