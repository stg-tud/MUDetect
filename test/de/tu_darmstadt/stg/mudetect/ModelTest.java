package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.Pattern;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ModelTest {
    @Test
    public void computesMaxSupport() throws Exception {
        Model model = () -> new HashSet<>(Arrays.asList(new Pattern(someAUG(), 5), new Pattern(someAUG(), 23)));

        int maxPatternSupport = model.getMaxPatternSupport();

        assertThat(maxPatternSupport, is(23));
    }
}
