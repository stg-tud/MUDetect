package de.tu_darmstadt.stg.mudetect.aug.model;

import de.tu_darmstadt.stg.mudetect.aug.builder.APIUsageExampleBuilder;
import org.junit.Test;

import java.io.*;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.isomorphicTo;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.ORDER;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AUGSerializationTest {

    @Test
    public void shouldSerializeAPIUsageExample() throws IOException, ClassNotFoundException {
        String projectName = "dummyProject";
        String filePath = "a/b/c/d";
        String methodSignature = "dummyMethod";
        Location loc = new Location(projectName, filePath, methodSignature);

        APIUsageExample originalAUG = APIUsageExampleBuilder.buildAUG(loc)
                .withAssignment("A1", 1)
                .withConstant("A2", "int", "VAL", "1337")
                .withReceiverEdge("A2", "A1")
                .withMethodCall("A3", "T1", "foo1", 3)
                .withMethodCall("A4", "T2", "foo2", 4)
                .withOrderEdge("A1", "A3")
                .withOrderEdge("A3", "A4").build();

        APIUsageExample copyAUG = serializeAndDeserializeAUG(originalAUG);

        assertThat(copyAUG, is(isomorphicTo(originalAUG)));
        assertThat(copyAUG.getLocation(), is(originalAUG.getLocation()));
    }

    @Test
    public void shouldSerialzeAPIUsageGraph() throws IOException, ClassNotFoundException {
        APIUsageGraph originalAUG = TestAUGBuilder.buildAUG()
                .withActionNodes("A.m()", "B.n()")
                .withDataNodes("D1", "D2", "D3")
                .withEdge("A.m()", PARAMETER, "B.n()")
                .withEdge("A.m()", ORDER, "B.n()")
                .withEdge("D1", PARAMETER, "A.m()")
                .withEdge("D2", PARAMETER, "A.m()")
                .withEdge("D3", PARAMETER, "B.n()").build();

        APIUsageGraph copyAUG = serializeAndDeserializeAUG(originalAUG);

        assertThat(copyAUG, is(isomorphicTo(originalAUG)));
    }

    @SuppressWarnings("unchecked")
    private <T extends APIUsageGraph> T serializeAndDeserializeAUG(APIUsageGraph originalAUG) throws IOException, ClassNotFoundException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(originalAUG);
            oos.flush();
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                return (T) ois.readObject();
            }
        }
    }
}
