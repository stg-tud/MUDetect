package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.ActionNode;
import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.data.ConstantNode;
import de.tu_darmstadt.stg.mudetect.aug.model.data.LiteralNode;
import de.tu_darmstadt.stg.mudetect.aug.model.data.VariableNode;
import de.tu_darmstadt.stg.utils.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.*;
import static org.hamcrest.Matchers.both;

public abstract class NodeMatchers {
    public static Matcher<? super Node> variable(String dataTypeName, String variableName) {
        return variableNodeWith(both(type(dataTypeName)).and(name(variableName)));
    }

    private static Matcher<Node> variableNodeWith(Matcher<? super VariableNode> matcher) {
        return nodeWith(VariableNode.class, matcher);
    }

    public static Matcher<? super Node> methodCall(String declaringTypeName, String methodSignature) {
        return methodCallNodeWith(both(declaringType(declaringTypeName)).and(signature(methodSignature)));
    }

    private static Matcher<Node> methodCallNodeWith(Matcher<? super MethodCallNode> matcher) {
        return nodeWith(MethodCallNode.class, matcher);
    }

    public static Matcher<Node> actionNodeWith(Matcher<? super ActionNode> matcher) {
        return nodeWith(ActionNode.class, matcher);
    }

    public static Matcher<Node> dataNodeWith(Matcher<? super DataNode> matcher) {
        return nodeWith(DataNode.class, matcher);
    }

    public static Matcher<Node> nodeWith(Matcher<? super Node> matcher) {
        return nodeWith(Node.class, matcher);
    }

    static Matcher<Node> literalNodeWith(Matcher<? super LiteralNode> matcher) {
        return nodeWith(LiteralNode.class, matcher);
    }

    static Matcher<Node> constantNodeWith(Matcher<? super ConstantNode> matcher) {
        return nodeWith(ConstantNode.class, matcher);
    }

    private static <N extends Node> Matcher<Node> nodeWith(Class<N> clazz, Matcher<? super N> matcher) {
        return node(clazz, matcher::matches,
                d -> d.appendText("a ").appendText(StringUtils.splitCamelCase(clazz.getSimpleName()))
                        .appendText(" with ").appendDescriptionOf(matcher));
    }

    private static <NT extends Node> Matcher<Node> node(Class<NT> clazz, Predicate<NT> predicate, Consumer<Description> describeTo) {
        return new BaseMatcher<Node>() {
            @Override
            public boolean matches(Object item) {
                //noinspection unchecked
                return (clazz.isAssignableFrom(item.getClass())) && predicate.test((NT) item);
            }

            @Override
            public void describeTo(Description description) {
                describeTo.accept(description);
            }
        };
    }
}
