package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.DEFINITION;
import static de.tu_darmstadt.stg.mudetect.aug.model.Edge.Type.PARAMETER;
import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodePreAndPostFixOperatorsTest {
    @Test
    public void encodesPrefixOperator() throws Exception {
        APIUsageExample aug = buildAUG("int m(int i) { return ++i; }");

        // TODO we cannot currently really test this, because too many nodes have the same label
        assertThat(aug, hasEdge(dataNodeWithLabel("int"), PARAMETER, actionNodeWithLabel("<a>")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<a>"), DEFINITION, dataNodeWithLabel("int")));
    }

    @Test
    public void encodesPostfixOperator() throws Exception {
        APIUsageExample aug = buildAUG("int m(int i) { return i++; }");

        // TODO we cannot currently really test this, because too many nodes have the same label
        assertThat(aug, hasEdge(dataNodeWithLabel("int"), PARAMETER, actionNodeWithLabel("<a>")));
        assertThat(aug, hasEdge(actionNodeWithLabel("<a>"), DEFINITION, dataNodeWithLabel("int")));
    }
}
