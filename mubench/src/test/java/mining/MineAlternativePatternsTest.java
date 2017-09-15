package mining;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

import static mining.MinerTestUtils.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class MineAlternativePatternsTest {
    @Rule
    public TestName testName = new TestName();

    @Test
    public void mineCorePattern() throws Exception {
        String iterColl = "void m(Collection c) { Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        String iterAddList = "void m(Collection c) { c.add(); Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        String iterRemList = "void m(Collection c) { c.remove(); Iterator i = c.iterator(); while(i.hasNext()) i.next(); }";
        List<Pattern> patterns = mineMethodsWithMinSupport2(
                iterColl, iterColl,
                iterRemList, iterRemList,
                iterAddList, iterAddList, iterAddList
        );
        print(patterns, testName);
        assertThat(patterns, hasSize(3));
    }

}
