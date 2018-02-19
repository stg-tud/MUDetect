package edu.iastate.cs.egroum.aug;

import de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import org.junit.Test;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasThrowEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.actionNodeWith;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodePropertyMatchers.label;
import static edu.iastate.cs.egroum.aug.AUGBuilderTestUtils.buildAUGForMethod;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class EncodeCallOrderTest {
    @Test
    public void encodesTransitiveOrderEdges() {
        APIUsageExample aug = buildAUGForMethod("void m(java.util.List l) {\n" +
                "  l.add(null);\n" +
                "  l.get(0);\n" +
                "  l.clear();\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("Collection.add()")), actionNodeWith(label("List.get()"))));
        assertThat(aug, hasOrderEdge(actionNodeWith(label("Collection.add()")), actionNodeWith(label("Collection.clear()"))));
        assertThat(aug, hasOrderEdge(actionNodeWith(label("List.get()")), actionNodeWith(label("Collection.clear()"))));
        assertThat(aug, not(hasThrowEdge(actionNodeWith(label("Collection.add()")), actionNodeWith(label("List.get()")))));
        assertThat(aug, not(hasThrowEdge(actionNodeWith(label("Collection.add()")), actionNodeWith(label("Collection.clear()")))));
        assertThat(aug, not(hasThrowEdge(actionNodeWith(label("List.get()")), actionNodeWith(label("Collection.clear()")))));
    }

    @Test
    public void capturesOrderDespiteIntermediateOperation() {
        APIUsageExample aug = buildAUGForMethod("void m() {\n" +
                "  java.util.StringTokenizer tokenizer = new java.util.StringTokenizer();\n" +
                "  float yPos = 5 - 3;\n" +
                "  tokenizer.hasMoreTokens();");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("StringTokenizer.<init>")), actionNodeWith(label("StringTokenizer.hasMoreTokens()"))));
    }

    @Test
    public void capturesOrderIntoSynchronized() {
        APIUsageExample aug = buildAUGForMethod("void m(Object members) {\n" +
                "  java.util.ArrayList listOfMembers = new java.util.ArrayList();\n" +
                "  synchronized (members) {\n" +
                "    listOfMembers.add(members);\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("ArrayList.<init>")), actionNodeWith(label("Collection.add()"))));
    }

    @Test
    public void capturesOrderIntoTry() {
        APIUsageExample aug = buildAUGForMethod("void m(javax.crypto.Cipher c, javax.crypto.SecretKey sk, javax.crypto.spec.IvParameterSpec ivp) {\n" +
                "    c.init(javax.crypto.Cipher.DECRYPT_MODE, sk, ivp);\n" +
                "  try {\n" +
                "    c.doFinal();\n" +
                "  } catch (Exception e) {} \n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("Cipher.init()")), actionNodeWith(label("Cipher.doFinal()"))));
    }

    @Test
    public void capturesOrderOutOfTry() {
        APIUsageExample aug = buildAUGForMethod("void m(javax.crypto.Cipher c, javax.crypto.SecretKey sk, javax.crypto.spec.IvParameterSpec ivp) {\n" +
                "  try {\n" +
                "    c.init(javax.crypto.Cipher.DECRYPT_MODE, sk, ivp);\n" +
                "  } catch (Exception e) {}\n" +
                "  c.doFinal();\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("Cipher.init()")), actionNodeWith(label("Cipher.doFinal()"))));
    }

    @Test
    public void capturesOrderAcrossTries() {
        APIUsageExample aug = buildAUGForMethod("void m(javax.crypto.Cipher c, javax.crypto.SecretKey sk, javax.crypto.spec.IvParameterSpec ivp) {\n" +
                "  try {\n" +
                "    c.init(javax.crypto.Cipher.DECRYPT_MODE, sk, ivp);\n" +
                "  } catch (Exception e) {}\n" +
                "  try {\n" +
                "    c.doFinal();\n" +
                "  } catch (Exception e) {} \n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("Cipher.init()")), actionNodeWith(label("Cipher.doFinal()"))));
    }

    @Test
    public void capturesOrderIntoForeach() {
        APIUsageExample aug = buildAUGForMethod("void m(java.util.Collection c) {\n" +
                "  c.size();\n" +
                "  for (Object o : c) {\n" +
                "    o.hashCode();\n" +
                "  }\n" +
                "  c.clear();\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("Collection.size()")), actionNodeWith(label("Object.hashCode()"))));
    }

    @Test
    public void capturesOrderIntoFor() {
        APIUsageExample aug = buildAUGForMethod("void m(java.util.Collection c) {\n" +
                "  c.add(null);\n" +
                "  for (int i = 0; i < c.size(); i++) {\n" +
                "    c.clear();\n" +
                "  }\n" +
                "}");

        assertThat(aug, hasOrderEdge(actionNodeWith(label("Collection.add()")), actionNodeWith(label("Collection.size()"))));
        assertThat(aug, hasOrderEdge(actionNodeWith(label("Collection.size()")), actionNodeWith(label("Collection.clear()"))));
    }
}
