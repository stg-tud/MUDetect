package de.tu_darmstadt.stg.mudetect.matcher;

import egroum.EGroumDataNode;
import egroum.EGroumNode;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class FirstAlternativeNodeLabelProviderTest {

    @Test
    public void usesAlternative() throws Exception {
        Function<EGroumNode, String> getter = NodeLabelProvider.firstOrDefaultLabel(
                node -> Optional.of(":alternative:")
        );
        EGroumDataNode node = new EGroumDataNode(":label:");

        assertThat(getter.apply(node), is(":alternative:"));
    }

    @Test
    public void usesFirstAlternative() throws Exception {
        Function<EGroumNode, String> getter = NodeLabelProvider.firstOrDefaultLabel(
                node -> Optional.of(":first:"),
                node -> Optional.of(":second:")
        );
        EGroumDataNode node = new EGroumDataNode(":label:");

        assertThat(getter.apply(node), is(":first:"));
    }

    @Test
    public void defaultsToLabel() throws Exception {
        Function<EGroumNode, String> getter = NodeLabelProvider.firstOrDefaultLabel(
                node -> Optional.empty()
        );
        EGroumDataNode node = new EGroumDataNode(":label:");

        assertThat(getter.apply(node), is(":label:"));
    }
}