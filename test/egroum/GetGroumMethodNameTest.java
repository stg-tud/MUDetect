package egroum;

import org.junit.Test;

import static egroum.EGroumTestUtils.buildGroumForMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GetGroumMethodNameTest {
    @Test
    public void simpleMethod() throws Exception {
        EGroumGraph groum = buildGroumForMethod("void m() {}", null);

        String methodName = AUGBuilder.getMethodSignature(groum);

        assertThat(methodName, is("m()"));
    }

    @Test
    public void withParameters() throws Exception {
        EGroumGraph groum = buildGroumForMethod("void m(Object o, int i) {}", null);

        String methodName = AUGBuilder.getMethodSignature(groum);

        assertThat(methodName, is("m(Object, int)"));
    }

    @Test
    public void withArrayParameter() throws Exception {
        EGroumGraph groum = buildGroumForMethod("void m(int[] is) {}", null);

        String methodName = AUGBuilder.getMethodSignature(groum);

        assertThat(methodName, is("m(int[])"));
    }

    @Test
    public void qualifiedParameter() throws Exception {
        EGroumGraph groum = buildGroumForMethod("void m(Foo.Bar b) {}", null);

        String methodName = AUGBuilder.getMethodSignature(groum);

        assertThat(methodName, is("m(Bar)"));
    }

    @Test
    public void parameterizedParameter() throws Exception {
        EGroumGraph groum = buildGroumForMethod("void m(List<String> l) {}", null);

        String methodName = AUGBuilder.getMethodSignature(groum);

        assertThat(methodName, is("m(List)"));
    }

    @Test
    public void nestedType() throws Exception {
        EGroumGraph groum = buildGroumForMethod("class Inner { void m() {} }", null);

        String methodName = AUGBuilder.getMethodSignature(groum);

        assertThat(methodName, is("m()"));
    }

    @Test
    public void anaonymousClassMethod() throws Exception {
        EGroumGraph groum = buildGroumForMethod("void m() { new T { void n() {} }; }", null);

        String methodName = AUGBuilder.getMethodSignature(groum);

        // we currently generate one GROUM that includes the code of both methods
        assertThat(methodName, is("m()"));
    }
}
