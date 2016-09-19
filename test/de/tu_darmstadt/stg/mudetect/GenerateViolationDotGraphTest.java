package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.junit.Test;

import java.io.StringWriter;

import static de.tu_darmstadt.stg.mudetect.model.AUGBuilder.buildAUG;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GenerateViolationDotGraphTest {

    @Test
    public void includesNodeLabel() throws Exception {
        AUG aug = buildAUG().withActionNode(":action:").build();
        Violation violation = new Violation(new Instance(aug, aug.vertexSet(), aug.edgeSet()));

        assertDotGraph(violation, "digraph G {\n" +
                "  1 [ label=\":action:\" ];\n" +
                "}\n");
    }

    private void assertDotGraph(Violation violation, String dotGraph) {
        StringWriter writer = new StringWriter();

        violation.toDotGraph(writer);

        assertThat(writer.toString(), is(dotGraph));
    }
}
