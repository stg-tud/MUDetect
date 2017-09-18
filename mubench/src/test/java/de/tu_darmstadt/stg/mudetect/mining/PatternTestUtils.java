package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static de.tu_darmstadt.stg.mudetect.model.AUGTestUtils.isEqual;
import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;

public class PatternTestUtils {
    public static Matcher<APIUsagePattern> isPattern(TestAUGBuilder builder, int support) {
        return isPattern(builder.build(), support);
    }

    public static Matcher<APIUsagePattern> isPattern(APIUsageExample aug, int support) {
        Matcher<APIUsageExample> augMatcher = isEqual(aug);
        return new BaseMatcher<APIUsagePattern>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof APIUsagePattern) {
                    APIUsagePattern actual = (APIUsagePattern) item;
                    return support == actual.getSupport() &&
                            augMatcher.matches(actual);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(somePattern(aug, support));
            }
        };
    }
}
