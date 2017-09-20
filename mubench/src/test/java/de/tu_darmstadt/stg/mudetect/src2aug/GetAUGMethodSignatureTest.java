package de.tu_darmstadt.stg.mudetect.src2aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilderTestUtils.buildAUGForMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GetAUGMethodSignatureTest {
    @Test
    public void simpleMethod() throws Exception {
        APIUsageExample aug = buildAUGForMethod("void m() {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m()"));
    }

    @Test
    public void withParameters() throws Exception {
        APIUsageExample aug = buildAUGForMethod("void m(Object o, int i) {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m(Object, int)"));
    }

    @Test
    public void withArrayParameter() throws Exception {
        APIUsageExample aug = buildAUGForMethod("void m(int[] is) {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m(int[])"));
    }

    @Test
    public void qualifiedParameter() throws Exception {
        APIUsageExample aug = buildAUGForMethod("void m(Foo.Bar b) {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m(Bar)"));
    }

    @Test
    public void parameterizedParameter() throws Exception {
        APIUsageExample aug = buildAUGForMethod("void m(List<String> l) {}");

        assertThat(aug.getLocation().getMethodSignature(), is("m(List)"));
    }

    @Test
    public void nestedType() throws Exception {
        APIUsageExample aug = buildAUGForMethod("class Inner { void m() {} }");

        assertThat(aug.getLocation().getMethodSignature(), is("m()"));
    }

    @Test
    public void anaonymousClassMethod() throws Exception {
        APIUsageExample aug = buildAUGForMethod("void m() { new T { void n() {} }; }");

        // we currently generate one GROUM that includes the code of both methods
        assertThat(aug.getLocation().getMethodSignature(), is("m()"));
    }
}
