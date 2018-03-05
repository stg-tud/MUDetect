package de.tu_darmstadt.stg.mudetect.aug.persistence;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import org.jgrapht.ext.*;
import org.junit.Test;

import java.io.*;
import java.util.Collection;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasReceiverEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.type;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.RECEIVER;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AUGPersistenceTest {
    @Test
    public void persistsNode() throws ImportException, IOException {
        APIUsageGraph aug = buildAUG().withActionNode("O.m()").build();

        Collection<APIUsageGraph> augs = persistAndRestore(aug);

        assertThat(augs, hasSize(1));
        APIUsageGraph newAUG = augs.iterator().next();
        assertThat(newAUG.getNodeSize(), is(1));
        assertThat(newAUG, hasNode(methodCall("O", "m()")));
    }

    @Test
    public void persistsEdge() throws ImportException, IOException {
        APIUsageGraph aug = buildAUG().withDataNode("O").withActionNode("O.m()").withEdge("O", RECEIVER, "O.m()").build();

        Collection<APIUsageGraph> augs = persistAndRestore(aug);

        assertThat(augs, hasSize(1));
        APIUsageGraph newAUG = augs.iterator().next();
        assertThat(newAUG.getNodeSize(), is(2));
        assertThat(newAUG.getEdgeSize(), is(1));
        assertThat(newAUG, hasReceiverEdge(dataNodeWith(type("O")), methodCall("O", "m()")));
    }

    @Test
    public void persistsMultipleAUGs() throws IOException, ImportException {
        APIUsageExample aug1 = buildAUG().withActionNode("A.a()").build();
        APIUsageExample aug2 = buildAUG().withActionNode("B.b()").build();

        Collection<APIUsageGraph> augs = persistAndRestore(aug1, aug2);

        assertThat(augs, hasSize(2));
    }

    private Collection<APIUsageGraph> persistAndRestore(APIUsageGraph... augs) throws IOException, ImportException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (AUGWriter writer = new AUGWriter(out, new PersistenceAUGDotExporter())) {
            for (int i = 0; i < augs.length; i++) {
                writer.write(augs[i], "aug-" + i);
            }
        }
        byte[] buf = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        try (AUGReader<APIUsageGraph> reader = new AUGReader<>(in, new PersistenceAUGDotImporter(), APIUsageGraph::new)) {
            return reader.readAll();
        }
    }

}
