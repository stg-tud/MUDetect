package de.tu_darmstadt.stg.mudetect.mining;

import egroum.EGroumGraph;
import mining.Configuration;
import org.junit.Test;

import java.util.*;

import static egroum.EGroumTestUtils.buildGroumForMethod;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static utils.SetUtils.asSet;

public class APIWiseModelTest {
    @Test
    public void findsAPIs() throws Exception {
        Collection<EGroumGraph> groums = asSet(
                buildGroumForMethod("void m(Iterator i) { i.hasNext(); }"),
                buildGroumForMethod("void n(Object o) { o.hashCode(); }"));
        APIWiseModel model = new APIWiseModel(new Configuration() {{ minPatternSupport = 1; }}, groums);

        assertThat(model.getAPIs(), contains("Iterator", "Object"));
    }

    @Test
    public void excludesPrimitiveTypesFromAPIs() throws Exception {
        Collection<EGroumGraph> groums = asSet(buildGroumForMethod("void m(Object o) { o.equals(5); }"));
        APIWiseModel model = new APIWiseModel(new Configuration() {{ minPatternSupport = 1; }}, groums);

        assertThat(model.getAPIs(), not(hasItem("int")));
    }

    @Test
    public void ignoresAPIsWithTooFewExamples() throws Exception {
        Set<EGroumGraph> groums = asSet(buildGroumForMethod("void m(Object o) { o.hashCode(); }"));
        APIWiseModel model = new APIWiseModel(new Configuration() {{ minPatternSupport = 2; }}, groums);

        assertThat(model.getAPIs(), not(hasItem("Object")));
    }
}
