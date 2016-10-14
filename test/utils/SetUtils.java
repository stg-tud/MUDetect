package utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SetUtils {
    public static <T> Set<T> asSet(T... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }
}
