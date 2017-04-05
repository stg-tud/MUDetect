package de.tu_darmstadt.stg.mudetect.typehierarchy;

import de.tu_darmstadt.stg.mudetect.model.TypeHierarchy;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TypeHierarchyBuilderTest {
    @Test
    public void buildsHierarchyFromJRE() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchyBuilder().build();

        assertTrue(hierarchy.isA("List", "Collection"));
    }
}
