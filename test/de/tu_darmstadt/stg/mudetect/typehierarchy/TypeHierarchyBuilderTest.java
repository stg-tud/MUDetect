package de.tu_darmstadt.stg.mudetect.typehierarchy;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeHierarchyBuilderTest {
    @Test
    public void buildsHierarchyFromRTJar() throws Exception {
        TypeHierarchyBuilder builder = new TypeHierarchyBuilder();

        TypeHierarchy hierarchy = builder.build(new String[] {TypeHierarchyBuilder.RT_JAR_PATH});

        assertTrue(hierarchy.isA("ArrayList", "AbstractList"));       // extends
        assertTrue(hierarchy.isA("ArrayList", "AbstractCollection")); // extends transitively
        assertTrue(hierarchy.isA("ArrayList", "Cloneable"));          // implements
        assertTrue(hierarchy.isA("ArrayList", "List"));               // implements transitively
        assertTrue(hierarchy.isA("List", "Collection"));              // interface extends
        assertTrue(hierarchy.isA("List", "Iterable"));                // interface extends transitively
        
        assertFalse(hierarchy.isA("1", "Object"));                    // excludes anonymous types
        assertFalse(hierarchy.isA("List", "Set"));                    // non-existent relation
    }

    @Test @Ignore("Only works after running 'mvn package'")
    public void buildsHierarchyFromCustomJar() throws Exception {
        TypeHierarchyBuilder builder = new TypeHierarchyBuilder();

        TypeHierarchy hierarchy = builder.build(new String[] {"./target/MuDetect.jar"});

        assertTrue(hierarchy.isA("TypeHierarchy", "Object"));
    }
}
