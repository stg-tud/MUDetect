package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Pattern;
import de.tu_darmstadt.stg.mudetect.model.TestPatternBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ModelTest {
    @Test
    public void computesMaxSupport() throws Exception {
        Pattern pattern1 = somePattern(3, 5);
        Pattern pattern2 = somePattern(3, 23);
        Model model = () -> new HashSet<>(Arrays.asList(pattern1, pattern2));

        int maxPatternSupport = model.getMaxPatternSupport(3);

        assertThat(maxPatternSupport, is(23));
    }

    @Test
    public void computesMaxSupportOnPatternsWithSameNodeCount() throws Exception {
        Pattern pattern1 = somePattern(3, 23);
        Pattern pattern2 = somePattern(5, 42);
        Model model = () -> new HashSet<>(Arrays.asList(pattern1, pattern2));

        int maxPatternSupport = model.getMaxPatternSupport(3);

        assertThat(maxPatternSupport, is(23));
    }

    private Pattern somePattern(int nodeCount, int support) {
        String[] nodeNames = new String[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            nodeNames[i] = Integer.toString(i);
        }
        return TestPatternBuilder.somePattern(buildAUG().withActionNodes(nodeNames), support);
    }
}
