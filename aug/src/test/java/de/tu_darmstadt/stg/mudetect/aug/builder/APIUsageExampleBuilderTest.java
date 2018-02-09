package de.tu_darmstadt.stg.mudetect.aug.builder;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.builder.APIUsageExampleBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNode;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasReceiverEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.variable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class APIUsageExampleBuilderTest {
    private static final Location SOME_LOCATION = new Location(":project:", ":file:", ":method():");

    @Test
    public void addMethodCall() {
        APIUsageExample aug = buildAUG(SOME_LOCATION).withMethodCall(":id:", "O", "m()", 42).build();

        assertThat(aug, hasNode(methodCall("O", "m()")));
    }

    @Test
    public void addVariable() {
        APIUsageExample aug = buildAUG(SOME_LOCATION).withVariable(":id:", "O", "o").build();

        assertThat(aug, hasNode(variable("O", "o")));
    }

    @Test
    public void addEdge() {
        APIUsageExample aug = buildAUG(SOME_LOCATION)
                .withVariable(":varId:", "O", "o")
                .withMethodCall(":methodId:", "O", "m()", 42)
                .withReceiverEdge(":varId:", ":methodId:").build();

        assertThat(aug, hasReceiverEdge(variable("O", "o"), methodCall("O", "m()")));
    }

    @Test
    public void setsLocation() {
        APIUsageExample aug = buildAUG(SOME_LOCATION).build();

        assertThat(aug.getLocation(), is(SOME_LOCATION));
    }
}
