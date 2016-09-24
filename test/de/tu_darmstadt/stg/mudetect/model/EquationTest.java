package de.tu_darmstadt.stg.mudetect.model;

import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static egroum.EGroumDataEdge.Type.PARAMETER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class EquationTest {
    @Test
    public void findsEquationWithoutArgument() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes(">");
        AUG aug = builder.build();

        Equation equation = Equation.from(builder.getNode(">"), aug);

        assertThat(equation, is(new Equation(builder.getNode(">"))));
    }

    @Test
    public void findsEquationWithOneArgument() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", ">").withDataEdge("A", PARAMETER, ">");
        AUG aug = builder.build();

        Equation equation = Equation.from(builder.getNode(">"), aug);

        assertThat(equation, is(new Equation(builder.getNode(">"), builder.getNode("A"))));
    }

    @Test
    public void findsEquationWithTwoArgument() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", ">", "B")
                .withDataEdge("A", PARAMETER, ">")
                .withDataEdge("B", PARAMETER, ">");
        AUG aug = builder.build();

        Equation equation = Equation.from(builder.getNode(">"), aug);

        assertThat(equation, is(new Equation(builder.getNode(">"), builder.getNode("A"), builder.getNode("B"))));
    }

    @Test
    public void sameIsInstance() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", ">", "B", "+");
        Equation instance = new Equation(builder.getNode(">"), builder.getNode("A"), builder.getNode("B"));

        assertTrue(instance.isInstanceOf(instance));
    }

    @Test
    public void instanceNeedsSameOperator() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNodes("A", ">", "B", "+");
        Equation noInstance = new Equation(builder.getNode(">"), builder.getNode("A"), builder.getNode("B"));
        Equation pattern = new Equation(builder.getNode("+"), builder.getNode("A"), builder.getNode("B"));

        assertFalse(noInstance.isInstanceOf(pattern));
    }

    @Test
    public void onlyOperatorIsNeverAnInstance() throws Exception {
        TestAUGBuilder builder = buildAUG().withActionNode(">");
        Equation instance = new Equation(builder.getNode(">"));

        assertFalse(instance.isInstanceOf(instance));
    }
}
