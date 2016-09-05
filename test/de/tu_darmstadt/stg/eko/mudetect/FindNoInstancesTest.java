package de.tu_darmstadt.stg.eko.mudetect;

import egroum.EGroumGraph;
import org.junit.Test;

import java.util.List;

import static egroum.EGroumTestUtils.buildGroumForMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class FindNoInstancesTest {
    @Test
    public void ignoresNonOverlappingTarget() throws Exception {
        EGroumGraph target = buildGroumForMethod("void m(Object o) {" +
                "  o.hashCode();" +
                "}");
        EGroumGraph pattern = buildGroumForMethod("void m(Object o) {" +
                "  o.hashCode();" +
                "}");

        List<Instance> instances = InstanceFinder.findInstances(target, pattern);

        assertThat(instances, is(empty()));
    }

}
