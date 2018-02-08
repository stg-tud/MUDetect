package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils;
import org.junit.Before;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGNodeMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.actionNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.dataNodeWithLabel;
import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.hasEdge;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.CONTAINS;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
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
        	assertThat(aug, hasEdge(actionNodeWithLabel("Runnable.<init>"), PARAMETER, actionNodeWithLabel("SwingUtilities.invokeLater()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("Runnable"), PARAMETER, actionNodeWithLabel("SwingUtilities.invokeLater()")));
    }

    @Test
    public void addsCodeFromAnonymousClassMethod() {
        assertThat(aug, hasNode(actionNodeWithLabel("Object.<init>")));
    }

    @Test
    public void addsDataNodeForAnyonymousClassMethods() {
        assertThat(aug, hasNode(dataNodeWithLabel("Runnable.run()")));
    }

    @Test
    public void addsContainsEdges() {
        assertThat(aug, hasEdge(dataNodeWithLabel("Runnable"), CONTAINS, dataNodeWithLabel("Runnable.run()")));
        assertThat(aug, hasEdge(dataNodeWithLabel("Runnable.run()"), CONTAINS, actionNodeWithLabel("Object.<init>")));
    }
}
