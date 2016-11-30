package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static egroum.EGroumTestUtils.buildGroumForMethod;
import static org.junit.Assert.assertThat;

public class AUGDataNodesTest {
    @Test
    public void addsDataNodeForParameter() throws Exception {
        AUG aug = buildAUG("void m(Object o) { o.hashCode(); }");

        assertThat(aug, hasNode(dataNodeWithLabel("Object")));
    }

    private Matcher<? super AUG> hasNode(Matcher<? super EGroumNode> matcher) {
        return new BaseMatcher<AUG>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof AUG) {
                    Set<EGroumNode> nodes = ((AUG) item).vertexSet();
                    return Matchers.hasItem(matcher).matches(nodes);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an AUG containing ");
                description.appendDescriptionOf(matcher);
            }
        };
    }


    private Matcher<? super EGroumNode> dataNodeWithLabel(String label) {
        return new BaseMatcher<EGroumNode>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof EGroumDataNode && ((EGroumDataNode) item).getLabel().equals(label);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a data node with label ");
                description.appendValue(label);
            }
        };
    }

    private AUG buildAUG(String code) {
        return AUGBuilder.toAUG(buildGroumForMethod(code));
    }
}
