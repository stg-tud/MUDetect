package de.tu_darmstadt.stg.mudetect.typehierarchy;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeHierarchyTest {
    @Test
    public void intIsALong() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchy();

        assertTrue(hierarchy.isA("int", "long"));
    }

    @Test
    public void intIsNotAString() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchy();

        assertFalse(hierarchy.isA("int", "String"));
    }

    @Test
    public void addSuperType() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchy();

        hierarchy.addSupertype("A", "S");

        assertTrue(hierarchy.isA("A", "S"));
    }

    @Test
    public void addMultipleSupertypes() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchy();

        hierarchy.addSupertypes("A", Arrays.asList("S", "I"));

        assertTrue(hierarchy.isA("A", "S"));
        assertTrue(hierarchy.isA("A", "I"));
    }

    @Test
    public void reflexive() throws Exception {
        TypeHierarchy hierarchy = new TypeHierarchy();

        assertTrue(hierarchy.isA("A", "A"));
    }
}
