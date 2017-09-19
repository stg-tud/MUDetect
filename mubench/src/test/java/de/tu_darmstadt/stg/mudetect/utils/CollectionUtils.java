package de.tu_darmstadt.stg.mudetect.utils;

import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CollectionUtils {
    public static <T> T only(Collection<T> collection) {
        assertThat(collection, hasSize(1));
        return collection.iterator().next();
    }

    public static <T> T first(Collection<T> collection) {
        assertThat(collection, is(not(empty())));
        return collection.iterator().next();
    }
}
