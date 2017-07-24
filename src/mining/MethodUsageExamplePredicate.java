package mining;

import egroum.EGroumGraph;
import egroum.EGroumNode;

import org.eclipse.jdt.core.dom.*;

import de.tu_darmstadt.stg.mudetect.model.AUG;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MethodUsageExamplePredicate extends UsageExamplePredicate {
    private final Set<String> labels;

	public static MethodUsageExamplePredicate usageExamplesOf(AUG target) {
		return new MethodUsageExamplePredicate(target);
	}

    private MethodUsageExamplePredicate(String[] labels) {
        this.labels = new HashSet<>();
        for (String l : labels)
        	this.labels.add(l);
    }

    private MethodUsageExamplePredicate(AUG target) {
    	this.labels = new HashSet<>();
    	for (EGroumNode node : target.getMeaningfulActionNodes())
    		this.labels.add(node.getLabel());
	}

	@Override
	protected boolean matchesAnyExample() {
        return labels.isEmpty();
    }

    @Override
	public boolean matches(EGroumGraph graph) {
        return matchesAnyExample() || !Collections.disjoint(graph.getNodeLabels(), labels);
    }

    @Override
	public boolean matches(ASTNode node) {
        throw new UnsupportedOperationException("Do not support matching API methods when parsing syntax!");
    }
}
