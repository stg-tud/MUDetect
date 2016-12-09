package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static egroum.EGroumDataEdge.Type.RECEIVER;
import static org.junit.Assert.assertThat;

public class EncodeCallsTest {
    @Test
    public void addsCall() throws Exception {
        AUG aug = buildAUG("void m(Object o) { o.toString(); }");

        assertThat(aug, hasEdge(dataNodeWithLabel("Object"), RECEIVER, actionNodeWithLabel("Object.toString()")));
    }

    /**
     * I'm not sure we want static calls to have a receiver, because this makes them indistinguishable from instance
     * calls.
     */
    @Test
    public void addsStaticCall() throws Exception {
        AUG aug = buildAUG("void m() { C.staticMethod(); }");

        assertThat(aug, hasEdge(dataNodeWithLabel("C"), RECEIVER, actionNodeWithLabel("C.staticMethod()")));
    }

    @Test
    public void addsConstructorInvocation() throws Exception {
        AUG aug = buildAUG("void m() { new Object(); }");

        assertThat(aug, hasNode(actionNodeWithLabel("Object.<init>")));
    }
}
