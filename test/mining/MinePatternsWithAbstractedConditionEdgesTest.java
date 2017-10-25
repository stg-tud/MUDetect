package mining;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

import static mining.MinerTestUtils.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MinePatternsWithAbstractedConditionEdgesTest {
    @Rule
    public TestName testName = new TestName();

    @Test
    public void minePatternsWithoutAbstractedConditionEdgesTest() throws Exception {
        String iterRep = "void m(Collection c) { Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        String iterSel = "void m(Collection c) { Iterator i = c.iterator(); if(i.hasNext()) i.next(); }";
        List<Pattern> patterns = mineMethods(new Configuration() {{ abstractConditionEdges = false; minPatternSupport = 2; maxPatternSize = 300; }}, iterRep, iterSel);
        print(patterns, testName);
        assertThat(patterns, hasSize(2));
    }

	@Test
    public void minePatternsWithAbstractedConditionEdgesTest() throws Exception {
        String iterRep = "void m(Collection c) { Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        String iterSel = "void m(Collection c) { Iterator i = c.iterator(); if(i.hasNext()) i.next(); }";
        List<Pattern> patterns = mineMethods(new Configuration() {{ abstractConditionEdges = true; minPatternSupport = 2; maxPatternSize = 300; }}, iterRep, iterSel);
        print(patterns, testName);
        assertThat(patterns, hasSize(1));
    }

}
