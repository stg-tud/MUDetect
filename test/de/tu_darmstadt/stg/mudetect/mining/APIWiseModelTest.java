package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumGraph;
import mining.Configuration;
import org.junit.Test;

import java.util.*;

import static egroum.EGroumTestUtils.buildGroumForMethod;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class APIWiseModelTest {
    @Test
    public void findsAPIs() throws Exception {
        Collection<EGroumGraph> groums = asSet(
                buildGroumForMethod("void m(Iterator i) { i.hasNext(); }"),
                buildGroumForMethod("void n(Object o) { o.hashCode(); }"));
        APIWiseModel model = new APIWiseModel(new Configuration() {{ minPatternSupport = 1; }}, groums);

        assertThat(model.getPatternAPIs(), containsInAnyOrder("Iterator", "Object"));
    }

    @Test
    public void excludesPrimitiveTypesFromAPIs() throws Exception {
        Collection<EGroumGraph> groums = asSet(buildGroumForMethod("void m(Object o) { o.equals(5); }"));
        APIWiseModel model = new APIWiseModel(new Configuration() {{ minPatternSupport = 1; }}, groums);

        assertThat(model.getPatternAPIs(), not(hasItem("int")));
    }

    @Test
    public void ignoresAPIsWithTooFewExamples() throws Exception {
        Set<EGroumGraph> groums = asSet(buildGroumForMethod("void m(Object o) { o.hashCode(); }"));
        APIWiseModel model = new APIWiseModel(new Configuration() {{ minPatternSupport = 2; }}, groums);

        assertThat(model.getPatternAPIs(), not(hasItem("Object")));
    }

    @Test
    public void countsNumberOfExamples() throws Exception {
        Set<EGroumGraph> groums = asSet(
                buildGroumForMethod("void m(A a) { a.m(); }"),
                buildGroumForMethod("void n(A a) { a.n(); }"),
                buildGroumForMethod("void o(B b) { b.m(); }"));

        APIWiseModel model = new APIWiseModel(new Configuration() {{ minPatternSupport = 2; }}, groums);

        assertThat(model.getExampleAPIs(), containsInAnyOrder("A", "B"));
        assertThat(model.getNumberOfExamples("A"), is(2));
        assertThat(model.getNumberOfExamples("B"), is(1));
    }
}
