package de.tu_darmstadt.stg.mudetect.aug.persistence;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import org.jgrapht.ext.*;
import org.junit.Test;

import java.io.StringWriter;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasReceiverEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.type;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AUGPersistenceTest {
    @Test
    public void persistsNode() throws ImportException {
        APIUsageGraph aug = buildAUG().withActionNode("O.m()").build();

        APIUsageGraph newAUG = persistAndRestore(aug);

        assertThat(newAUG.getNodeSize(), is(1));
        assertThat(newAUG, hasNode(methodCall("O", "m()")));
    }

    @Test
    public void persistsEdge() throws ImportException {
        APIUsageGraph aug = buildAUG().withDataNode("O").withActionNode("O.m()").withEdge("O", RECEIVER, "O.m()").build();

        APIUsageGraph newAUG = persistAndRestore(aug);

        assertThat(newAUG.getNodeSize(), is(2));
        assertThat(newAUG.getEdgeSize(), is(1));
        assertThat(newAUG, hasReceiverEdge(dataNodeWith(type("O")), methodCall("O", "m()")));
    }

    private APIUsageGraph persistAndRestore(APIUsageGraph aug) throws ImportException {
        StringWriter writer = new StringWriter();
        new AUGWriter().export(writer, aug);
        String augString = writer.toString();

        APIUsageGraph newAUG = new APIUsageGraph();
        new AUGReader().read(augString, newAUG);

        return newAUG;
    }

}
