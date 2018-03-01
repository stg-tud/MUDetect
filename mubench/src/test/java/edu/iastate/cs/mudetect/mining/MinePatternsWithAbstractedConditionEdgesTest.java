package edu.iastate.cs.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.SelAndRepSameLabelProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

import static edu.iastate.cs.mudetect.mining.MinerTestUtils.mineMethods;
import static edu.iastate.cs.mudetect.mining.MinerTestUtils.print;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MinePatternsWithAbstractedConditionEdgesTest {
    @Rule
    public TestName testName = new TestName();

    @Test
    public void minePatternsWithoutAbstractedConditionEdgesTest() {
        String iterRep = "void m(Collection c) { Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        String iterSel = "void m(Collection c) { Iterator i = c.iterator(); if(i.hasNext()) i.next(); }";
        List<APIUsagePattern> patterns = mineMethods(new Configuration() {{
            minPatternSupport = 2;
            labelProvider = new BaseAUGLabelProvider();
        }}, iterRep, iterSel);
        print(patterns);
        assertThat(patterns, hasSize(2));
    }

	@Test
    public void minePatternsWithAbstractedConditionEdgesTest() {
        String iterRep = "void m(Collection c) { Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        String iterSel = "void m(Collection c) { Iterator i = c.iterator(); if(i.hasNext()) i.next(); }";
        List<APIUsagePattern> patterns = mineMethods(new Configuration() {{
            minPatternSupport = 2;
            labelProvider = new SelAndRepSameLabelProvider(new BaseAUGLabelProvider());
        }}, iterRep, iterSel);
        print(patterns);
        assertThat(patterns, hasSize(1));
    }

}
