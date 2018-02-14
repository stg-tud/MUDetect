package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.utils.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

class EdgeMatcher extends BaseMatcher<Edge> {
    private final Matcher<? super Node> sourceMatcher;
    private final Class<? extends Edge> edgeType;
    private final Matcher<? super Node> targetMatcher;

    EdgeMatcher(Matcher<? super Node> sourceMatcher,
                Class<? extends Edge> edgeType,
                Matcher<? super Node> targetMatcher) {
        this.sourceMatcher = sourceMatcher;
        this.edgeType = edgeType;
        this.targetMatcher = targetMatcher;
    }

    @Override
    public boolean matches(Object item) {
        if (item instanceof Edge) {
            Edge edge = (Edge) item;
            return sourceMatcher.matches(edge.getSource())
                    && edgeType.isAssignableFrom(edge.getClass())
                    && targetMatcher.matches(edge.getTarget());
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        if (edgeType == Edge.class) {
            description.appendText("an");
        } else {
            description.appendText("a ").appendValue(getEdgeTypeName());
        }
        description.appendText(" edge from ");
        description.appendDescriptionOf(sourceMatcher).appendText(" to ").appendDescriptionOf(targetMatcher);
    }

    private String getEdgeTypeName() {
        String name = edgeType.getSimpleName();
        if (name.endsWith("Edge")) {
            name = name.substring(0, name.length() - 4);
        }
        return StringUtils.splitCamelCase(name);
    }
}
