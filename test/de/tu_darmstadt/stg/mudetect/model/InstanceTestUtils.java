package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumEdge;
import egroum.EGroumNode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.instance;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class InstanceTestUtils {
    public static Matcher<Overlap> contains(EGroumNode node) {
        return new BaseMatcher<Overlap>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof Overlap && ((Overlap) item).mapsNode(node);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("instance containing ");
                description.appendValue(node);
            }
        };
    }

    public static Matcher<Overlap> contains(EGroumEdge edge) {
        return new BaseMatcher<Overlap>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof Overlap && ((Overlap) item).mapsEdge(edge);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected AUG with edge ");
                description.appendValue(edge);
            }
        };
    }

    public static Matcher<Iterable<? extends Overlap>> hasInstance(AUG aug) {
        return hasInstances(aug);
    }

    public static Matcher<Iterable<? extends Overlap>> hasInstances(AUG... augs) {
        Overlap[] instances = new Overlap[augs.length];
        for (int i = 0; i < augs.length; i++) {
            instances[i] = instance(augs[i]);
        }
        return containsInAnyOrder(instances);
    }
}
