package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodePreAndPostFixOperatorsTest {
    @Test
    public void encodesPrefixOperator() {
        APIUsageExample aug = buildAUG("int m(int i) { return ++i; }");

        // TODO we cannot currently really test this, because too many nodes have the same label
        assertThat(aug, hasEdge(dataNodeWithLabel("int"), PARAMETER, actionNodeWithLabel("<a>")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<a>"), DEFINITION, dataNodeWithLabel("int")));
    }

    @Test
    public void encodesPostfixOperator() {
        APIUsageExample aug = buildAUG("int m(int i) { return i++; }");

        // TODO we cannot currently really test this, because too many nodes have the same label
        assertThat(aug, hasEdge(dataNodeWithLabel("int"), PARAMETER, actionNodeWithLabel("<a>")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<a>"), DEFINITION, dataNodeWithLabel("int")));
    }
}
