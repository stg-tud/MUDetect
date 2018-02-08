package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.data.VariableNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jgrapht.graph.AbstractBaseGraph;

import org.hamcrest.Matcher;

public abstract class AUGNodeMatchers {
    public static Matcher<? super APIUsageGraph> hasNode(Matcher<? super Node> nodeMatcher) {
        return new AUGElementMatcher<>(AbstractBaseGraph::vertexSet, nodeMatcher);
    }

    public static Matcher<? super Node> variable(String dataTypeName, String variableName) {
        return new BaseMatcher<Node>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof VariableNode) {
                    VariableNode node = (VariableNode) item;
                    return node.getType().equals(dataTypeName) && node.getName().equals(variableName);
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a variable data node '").appendText(dataTypeName).appendText(" ")
                        .appendText(dataTypeName).appendText("'");
            }
        };
    }

    public static Matcher<? super Node> methodCall(String declaringTypeName, String methodSignature) {
        return new BaseMatcher<Node>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof MethodCallNode) {
                    MethodCallNode node = (MethodCallNode) item;
                    return node.getDeclaringTypeName().equals(declaringTypeName)
                            && node.getMethodSignature().equals(methodSignature);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a method-call action node '").appendText(declaringTypeName).appendText(".")
                        .appendText(methodSignature).appendText("'");
            }
        };
    }
}
