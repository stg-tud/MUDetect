package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.hasNode;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class AUGDataNodesTest {
    @Test
    public void addsDataNodeForParameter() throws Exception {
        AUG aug = AUGBuilderTestUtils.buildAUG("void m(Object o) { o.hashCode(); }");

        assertThat(aug, hasNode(dataNodeWithLabel("Object")));
    }

    @Test
    public void addsDataNodeForVariable() throws Exception {
        AUG aug = AUGBuilderTestUtils.buildAUG("String m() { String self = toString(); return self; }");

        assertThat(aug, hasNode(dataNodeWithLabel("String")));
    }

    @Test
    public void addsDataNodeForImplicitReference() throws Exception {
        AUG aug = AUGBuilderTestUtils.buildAUG("String m() { return toString(); }");

        assertThat(aug, hasNode(dataNodeWithLabel("String")));
    }

    @Test
    public void noDataNodeForUnusedReturnValue() throws Exception {
        AUG aug = AUGBuilderTestUtils.buildAUG("void m() { toString(); }");

        assertThat(aug, not(hasNode(dataNodeWithLabel("String"))));
    }

    @Test
    public void noDataNodeForUnusedVariable() throws Exception {
        AUG aug = AUGBuilderTestUtils.buildAUG("void m() { String s = toString(); }");

        assertThat(aug, not(hasNode(dataNodeWithLabel("String"))));
    }

    @Test
    public void noDataNodeForUnusedLiteral() throws Exception {
        AUG aug = AUGBuilderTestUtils.buildAUG("void m() { String s = \"foo\"; }");

        assertThat(aug, not(hasNode(dataNodeWithLabel("String"))));
    }
}
