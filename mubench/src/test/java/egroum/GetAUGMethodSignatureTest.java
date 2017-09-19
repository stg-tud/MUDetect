package egroum;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import org.junit.Test;

import static egroum.EGroumTestUtils.buildGroumForMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GetAUGMethodSignatureTest {
    @Test
    public void simpleMethod() throws Exception {
        APIUsageExample aug = buildGroumForMethod("void m() {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m()"));
    }

    @Test
    public void withParameters() throws Exception {
        APIUsageExample aug = buildGroumForMethod("void m(Object o, int i) {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m(Object, int)"));
    }

    @Test
    public void withArrayParameter() throws Exception {
        APIUsageExample aug = buildGroumForMethod("void m(int[] is) {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m(int[])"));
    }

    @Test
    public void qualifiedParameter() throws Exception {
        APIUsageExample aug = buildGroumForMethod("void m(Foo.Bar b) {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m(Bar)"));
    }

    @Test
    public void parameterizedParameter() throws Exception {
        APIUsageExample aug = buildGroumForMethod("void m(List<String> l) {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m(List)"));
    }

    @Test
    public void nestedType() throws Exception {
        APIUsageExample aug = buildGroumForMethod("class Inner { void m() {} }");

        assertThat(aug.getLocation().getMethodSignature(), is("m()"));
    }

    @Test
    public void anaonymousClassMethod() throws Exception {
        APIUsageExample aug = buildGroumForMethod("void m() { new T { void n() {} }; }");

        // we currently generate one GROUM that includes the code of both methods
        assertThat(aug.getLocation().getMethodSignature(), is("m()"));
    }
}
