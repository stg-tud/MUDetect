package de.tu_darmstadt.stg.mudetect.mining;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static de.tu_darmstadt.stg.mudetect.aug.model.AUGTestUtils.isEqual;
import static de.tu_darmstadt.stg.mudetect.mining.TestPatternBuilder.somePattern;

class PatternTestUtils {
    static Matcher<APIUsagePattern> isPattern(TestAUGBuilder builder, int support) {
        return isPattern(builder.build(APIUsagePattern.class), support);
    }

    static Matcher<APIUsagePattern> isPattern(APIUsagePattern aug, int support) {
        Matcher<? super APIUsageGraph> augMatcher = isEqual(aug);
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
