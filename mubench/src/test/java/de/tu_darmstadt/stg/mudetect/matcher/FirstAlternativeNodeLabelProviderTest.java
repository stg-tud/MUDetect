package de.tu_darmstadt.stg.mudetect.matcher;

import de.tu_darmstadt.stg.mudetect.aug.Node;
import de.tu_darmstadt.stg.mudetect.aug.data.VariableNode;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FirstAlternativeNodeLabelProviderTest {

    @Test
    public void usesAlternative() throws Exception {
        Function<Node, String> getter = NodeLabelProvider.firstOrDefaultLabel(
                node -> Optional.of(":alternative:")
        );
        // TODO check whether we need the second parameter here (also subsequent tests)
        VariableNode node = new VariableNode(":label:", null);

        assertThat(getter.apply(node), is(":alternative:"));
    }

    @Test
    public void usesFirstAlternative() throws Exception {
        Function<Node, String> getter = NodeLabelProvider.firstOrDefaultLabel(
                node -> Optional.of(":first:"),
                node -> Optional.of(":second:")
        );
        VariableNode node = new VariableNode(":label:", null);

        assertThat(getter.apply(node), is(":first:"));
    }

    @Test
    public void defaultsToLabel() throws Exception {
        Function<Node, String> getter = NodeLabelProvider.firstOrDefaultLabel(
                node -> Optional.empty()
        );
        VariableNode node = new VariableNode(":label:", null);

        assertThat(getter.apply(node), is(":label:"));
    }
}