package de.tu_darmstadt.stg.mudetect.model;

import de.tu_darmstadt.stg.mudetect.Instance;
import egroum.EGroumNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.hasItems;

public class InstanceTestUtils {
    public static Matcher<Instance> contains(EGroumNode node) {
        return new BaseMatcher<Instance>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof Instance && ((Instance) item).containsVertex(node);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("instance containing ");
                description.appendValue(node);
            }
        };
    }

    public static Matcher<Iterable<Instance>> hasInstance(AUG aug) {
        return hasItems(new Instance(aug, aug.vertexSet(), aug.edgeSet()));
    }
}
