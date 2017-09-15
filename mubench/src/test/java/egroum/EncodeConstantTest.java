package egroum;

import de.tu_darmstadt.stg.mudetect.model.AUG;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.*;
import static egroum.AUGBuilderTestUtils.buildAUG;
import static org.hamcrest.MatcherAssert.assertThat;

public class EncodeConstantTest {

    @Test
    public void encodeConstantTest0() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration(){{encodeConstants = 0;}};
        AUG aug = buildAUG(
                "void m(String s) { if (s.length() < Integer.MAX_VALUE) s.getBytes(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithLabel("int")));
        assertThat(aug, hasNode(dataNodeWithType("int")));
    }

    @Test
    public void encodeConstantTest1() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration(){{encodeConstants = 1;}};
        AUG aug = buildAUG(
                "void m(String s) { if (s.length() < Integer.MAX_VALUE) s.getBytes(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithLabel("Integer.MAX_VALUE")));
        assertThat(aug, hasNode(dataNodeWithName("Integer.MAX_VALUE")));
    }

    @Test
    public void encodeConstantTest2() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration(){{encodeConstants = 2;}};
        AUG aug = buildAUG(
                "void m(String s) { if (s.length() < Integer.MAX_VALUE) s.getBytes(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithLabel(String.valueOf(Integer.MAX_VALUE))));
        assertThat(aug, hasNode(dataNodeWithValue(String.valueOf(Integer.MAX_VALUE))));
    }

    @Test
    public void encodeEnumTest0() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration(){{encodeConstants = 0;}};
        AUG aug = buildAUG(
                "void m(java.awt.Color c) { if (c == java.awt.Color.BLACK) c.getRGB(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithLabel("Color")));
        assertThat(aug, hasNode(dataNodeWithType("Color")));
    }

    @Test
    public void encodeEnumTest1() throws Exception {
    	AUGConfiguration conf = new AUGConfiguration(){{encodeConstants = 1;}};
        AUG aug = buildAUG(
                "void m(java.awt.Color c) { if (c == java.awt.Color.BLACK) c.getRGB(); }",
                conf);
        assertThat(aug, hasNode(dataNodeWithName("Color.BLACK")));
        assertThat(aug, hasNode(dataNodeWithLabel("Color.BLACK")));
    }
}
