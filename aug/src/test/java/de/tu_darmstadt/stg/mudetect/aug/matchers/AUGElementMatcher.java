package de.tu_darmstadt.stg.mudetect.aug.matchers;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Set;
import java.util.function.Function;

public class AUGElementMatcher<E> extends BaseMatcher<APIUsageGraph> {
    private final static AUGDotExporter augDotExporter = new DisplayAUGDotExporter();

    private final Function<APIUsageGraph, Set<E>> selector;
    private final Matcher<? super E> elementMatcher;

    public AUGElementMatcher(Function<APIUsageGraph, Set<E>> selector, Matcher<? super E> elementMatcher) {
        this.selector = selector;
        this.elementMatcher = elementMatcher;
    }

    @Override
    public boolean matches(Object item) {
        return item instanceof APIUsageGraph && Matchers.hasItem(elementMatcher).matches(selector.apply((APIUsageGraph) item));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an AUG containing ");
        description.appendDescriptionOf(elementMatcher);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (item instanceof APIUsageGraph) {
            description.appendText("was AUG: ").appendText(augDotExporter.toDotGraph((APIUsageGraph) item));
        } else {
            super.describeMismatch(item, description);
        }
    }
}
