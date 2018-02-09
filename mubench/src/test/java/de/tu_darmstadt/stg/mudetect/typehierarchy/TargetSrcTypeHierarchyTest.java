package de.tu_darmstadt.stg.mudetect.typehierarchy;

import de.tu_darmstadt.stg.mudetect.typehierarchy.testtargets.*;
import org.apache.bcel.util.ClassPath;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class TargetSrcTypeHierarchyTest {
    private static TypeHierarchy hierarchy;

    @BeforeClass
    public static void setUp() {
        // Excluding project binary paths from classpath, because it slows down analysis manifold.
        String[] projectDependencyClassPath = Arrays.stream(ClassPath.getClassPath().split(":"))
                .filter(dependencyPath ->
                        !dependencyPath.endsWith("target/classes") &&
                        !dependencyPath.endsWith("target/test-classes"))
                .toArray(String[]::new);
        hierarchy = TargetSrcTypeHierarchy.build(
                "src/test/java/de/tu_darmstadt/stg/mudetect/typehierarchy/testtargets",
                projectDependencyClassPath);
    }

    @Test
    public void findsSuperclass() {
        assertIsA(hierarchy, C.class, "extends", Super.class);
    }

    @Test
    public void findsSuperclassTransitively() {
        assertIsA(hierarchy, C.class, "extends (transitively)", SuperSuper.class);
    }

    @Test
    public void findsInterface() {
        assertIsA(hierarchy, C.class, "implements", I.class);
    }

    @Test
    public void findsInterfaceOfSuperclass() {
        assertIsA(hierarchy, C.class, "implements (transitively)", SuperI.class);
    }

    @Test
    public void findsInterfaceOfInterface() {
        assertIsA(hierarchy, I.class, "interface extends", ISuper.class);
    }

    @Test
    public void findsInterfaceOfInterfaceTransitively() {
        assertIsA(hierarchy, I.class, "interface extends (transitively)", ISuperSuper.class);
    }

    @Test
    public void analyzesInnerClass() {
        assertIsA(hierarchy, C.D.class, "(inner class) extends", Super.class);
    }

    @Test
    public void analyzesDeclaredMethodReturnType() {
        assertIsA(hierarchy, Set.class, "return type of C.a() is a", Collection.class);
    }

    @Test
    public void analyzesDeclaredMethodParameter() {
        assertIsA(hierarchy, ArrayList.class, "parameter type of C.b() is a", Collection.class);
    }

    @Test
    public void analyzesReferencedType_import() {
        assertIsA(hierarchy, HashSet.class, "referenced in C.c() is a", Collection.class);
    }

    @Test
    public void analyzedReferencedType_qualified() {
        assertIsA(hierarchy, LinkedList.class, "referenced (qualified) in C.d() is a", Collection.class);
    }

    @Test
    public void analyzesInvokedMethodReturnType() {
        assertIsA(hierarchy, List.class, "returned from Arrays.asList() in C.e() is a", Collection.class);
    }

    private <T> void assertIsA(TypeHierarchy hierarchy, Class<? extends T> type, String relation, Class<T> supertype) {
        String typeName = type.getSimpleName();
        String supertypeName = supertype.getSimpleName();
        assertTrue("expected that " + typeName + " " + relation + " " + supertypeName, hierarchy.isA(typeName, supertypeName));
    }
}
