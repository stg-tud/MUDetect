package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static de.tu_darmstadt.stg.mudetect.model.TestInstanceBuilder.fullInstance;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

public class InstanceTestUtils {
    public static Matcher<Instance> contains(EGroumNode node) {
        return new BaseMatcher<Instance>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof Instance && ((Instance) item).mapsNode(node);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("instance containing ");
                description.appendValue(node);
            }
        };
    }

    public static Matcher<Instance> contains(EGroumEdge edge) {
        return new BaseMatcher<Instance>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof Instance && ((Instance) item).mapsEdge(edge);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected AUG with edge ");
                description.appendValue(edge);
            }
        };
    }

    public static Matcher<Iterable<Instance>> hasInstance(AUG aug) {
        return hasInstances(aug);
    }

    public static Matcher<Iterable<Instance>> hasInstances(AUG... augs) {
        Instance[] instances = new Instance[augs.length];
        for (int i = 0; i < augs.length; i++) {
            instances[i] = fullInstance(augs[i]);
        }
        return hasItems(instances);
    }
}
