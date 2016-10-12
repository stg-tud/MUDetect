package de.tu_darmstadt.stg.mudetect.model;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.isEqual;

public class PatternTestUtils {
    public static Matcher<Pattern> isPattern(TestAUGBuilder builder, int support) {
        return isPattern(builder.build(), support);
    }

    public static Matcher<Pattern> isPattern(AUG aug, int support) {
        Matcher<AUG> augMatcher = isEqual(aug);
        return new BaseMatcher<Pattern>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof Pattern) {
                    Pattern actual = (Pattern) item;
                    return support == actual.getSupport() &&
                            augMatcher.matches(actual.getAUG());
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(new Pattern(aug, support));
            }
        };
    }
}
