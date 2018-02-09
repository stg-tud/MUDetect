package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasDefinitionEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasParameterEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.dataNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUG;
import static org.junit.Assert.assertThat;

public class EncodePreAndPostFixOperatorsTest {
    @Test
    public void encodesPrefixOperator() {
        APIUsageExample aug = buildAUG("int m(int i) { return ++i; }");

        // TODO we cannot currently really test this, because too many nodes have the same label
        assertThat(aug, hasParameterEdge(dataNodeWith(label("int")), actionNodeWith(label("<a>"))));
        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("<a>")), dataNodeWith(label("int"))));
    }

    @Test
    public void encodesPostfixOperator() {
        APIUsageExample aug = buildAUG("int m(int i) { return i++; }");

        // TODO we cannot currently really test this, because too many nodes have the same label
        assertThat(aug, hasParameterEdge(dataNodeWith(label("int")), actionNodeWith(label("<a>"))));
        assertThat(aug, hasDefinitionEdge(actionNodeWith(label("<a>")), dataNodeWith(label("int"))));
    }
}
