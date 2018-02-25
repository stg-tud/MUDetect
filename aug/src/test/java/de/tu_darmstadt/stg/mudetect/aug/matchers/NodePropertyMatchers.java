package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.DataNode;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.instanceOf;

public class NodePropertyMatchers {
    public static Matcher<Node> label(String label) {
        return propertyEquals(new BaseAUGLabelProvider()::getLabel, "label", label);
    }

    public static Matcher<Node> type(Class<? extends Node> nodeType) {
        return instanceOf(nodeType);
    }

    public static Matcher<DataNode> name(String dataName) {
        return propertyEquals(DataNode::getName, "name", dataName);
    }

    public static Matcher<DataNode> type(String dataType) {
        return propertyEquals(DataNode::getType, "type", dataType);
    }

    public static Matcher<DataNode> value(String value) {
        return propertyEquals(DataNode::getValue, "value", value);
    }

    static Matcher<MethodCallNode> declaringType(String declaringType) {
        return propertyEquals(MethodCallNode::getDeclaringTypeName, "declaring type", declaringType);
    }

    static Matcher<MethodCallNode> signature(String methodSignature) {
        return propertyEquals(MethodCallNode::getMethodSignature, "signature", methodSignature);
    }

    private static <T extends Node, V> Matcher<T> propertyEquals(Function<T, V> getProperty, String propertyName, V value) {
        return property(n -> Objects.equals(getProperty.apply(n), value), d -> d.appendText(propertyName).appendText(" ").appendValue(value));
    }

    private static <T extends Node> Matcher<T> property(Predicate<T> predicate, Consumer<Description> describeTo) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object item) {
                T t;
                try {
                    //noinspection unchecked
                    t = (T) item;
                } catch(ClassCastException e) {
                    // item is of incompatible type -> no match
                    return false;
                }
                return predicate.test(t);
            }

            @Override
            public void describeTo(Description description) {
                describeTo.accept(description);
            }
        };
    }
}
