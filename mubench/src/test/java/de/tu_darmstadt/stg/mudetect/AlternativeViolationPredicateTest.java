package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import org.junit.Test;

import java.util.stream.Stream;

import static de.tu_darmstadt.stg.mudetect.AlternativeViolationPredicate.firstAlternativeViolation;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder.extend;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static de.tu_darmstadt.stg.mudetect.model.TestViolationBuilder.someViolation;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AlternativeViolationPredicateTest {
    @Test
    public void keepsIndependentViolations() {
        Violation violation1 = someViolation(buildOverlap(buildAUG().withActionNode("m()")).withNode("m()"));
        Violation violation2 = someViolation(buildOverlap(buildAUG().withActionNode("f()")).withNode("f()"));

        assertThat(Stream.of(violation1, violation2).filter(firstAlternativeViolation()).count(), is(2L));
    }

    @Test
    public void filtersSubsequentAlternativeViolations() {
        TestAUGBuilder commonPart = buildAUG().withActionNode("m()");
        Violation violation1 = someViolation(buildOverlap(extend(commonPart).withActionNode("n()")).withNodes("m()", "n()"));
        Violation violation2 = someViolation(buildOverlap(extend(commonPart).withActionNode("f()")).withNodes("m()", "f()"));
        Violation violation3 = someViolation(buildOverlap(extend(commonPart).withActionNode("x()")).withNodes("m()", "x()"));

        assertThat(Stream.of(violation1, violation2, violation3).filter(firstAlternativeViolation()).count(), is(1L));
    }
}
