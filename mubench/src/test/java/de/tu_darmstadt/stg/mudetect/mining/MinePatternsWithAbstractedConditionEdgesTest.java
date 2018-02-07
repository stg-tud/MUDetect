package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

import static de.tu_darmstadt.stg.mudetect.mining.MinerTestUtils.mineMethods;
import static de.tu_darmstadt.stg.mudetect.mining.MinerTestUtils.print;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MinePatternsWithAbstractedConditionEdgesTest {
    @Rule
    public TestName testName = new TestName();

    @Test
    public void minePatternsWithoutAbstractedConditionEdgesTest() {
        String iterRep = "void m(Collection c) { Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        String iterSel = "void m(Collection c) { Iterator i = c.iterator(); if(i.hasNext()) i.next(); }";
        List<APIUsagePattern> patterns = mineMethods(new Configuration() {{ abstractConditionEdges = false; minPatternSupport = 2; maxPatternSize = 300; }}, iterRep, iterSel);
        print(patterns);
        assertThat(patterns, hasSize(2));
    }

	@Test
    public void minePatternsWithAbstractedConditionEdgesTest() {
        String iterRep = "void m(Collection c) { Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        String iterSel = "void m(Collection c) { Iterator i = c.iterator(); if(i.hasNext()) i.next(); }";
        List<APIUsagePattern> patterns = mineMethods(new Configuration() {{ abstractConditionEdges = true; minPatternSupport = 2; maxPatternSize = 300; }}, iterRep, iterSel);
        print(patterns);
        assertThat(patterns, hasSize(1));
    }

}
