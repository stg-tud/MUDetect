package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.*;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodeAnonymousClassInstancesTest {
    private APIUsageExample aug;
	private AUGConfiguration conf = new AUGConfiguration();

    @Before
    public void setUp() {
        aug = buildAUG("void m() {" +
                "  SwingUtilities.invokeLater(new Runnable() {" +
                "    public void run() {" +
                "      new Object();" +
                "    }" +
                "  });" +
                "}", conf );
    }

    @Test
    public void addsActionOnInstance() {
        if (conf.buildTransitiveDataEdges)
            assertThat(aug, hasParameterEdge(actionNodeWith(label("Runnable.<init>")), actionNodeWith(label("SwingUtilities.invokeLater()"))));
        assertThat(aug, hasParameterEdge(dataNodeWith(label("Runnable")), actionNodeWith(label("SwingUtilities.invokeLater()"))));
    }

    @Test
    public void addsCodeFromAnonymousClassMethod() {
        assertThat(aug, hasNode(actionNodeWith(label("Object.<init>"))));
    }

    @Test
    public void addsDataNodeForAnyonymousClassMethods() {
        assertThat(aug, hasNode(dataNodeWith(label("run()"))));
    }

    @Test
    public void addsContainsEdges() {
        assertThat(aug, hasContainsEdge(dataNodeWith(label("Runnable")), dataNodeWith(label("run()"))));
        assertThat(aug, hasContainsEdge(dataNodeWith(label("run()")), actionNodeWith(label("Object.<init>"))));
    }
}
