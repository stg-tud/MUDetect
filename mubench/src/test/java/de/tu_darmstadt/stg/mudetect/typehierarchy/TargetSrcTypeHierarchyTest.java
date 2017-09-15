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
    public static void setUp() throws Exception {
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
    public void findsSuperclass() throws Exception {
        assertIsA(hierarchy, C.class, "extends", Super.class);
    }

    @Test
    public void findsSuperclassTransitively() throws Exception {
        assertIsA(hierarchy, C.class, "extends (transitively)", SuperSuper.class);
    }

    @Test
    public void findsInterface() throws Exception {
        assertIsA(hierarchy, C.class, "implements", I.class);
    }

    @Test
    public void findsInterfaceOfSuperclass() throws Exception {
        assertIsA(hierarchy, C.class, "implements (transitively)", SuperI.class);
    }

    @Test
    public void findsInterfaceOfInterface() throws Exception {
        assertIsA(hierarchy, I.class, "interface extends", ISuper.class);
    }

    @Test
    public void findsInterfaceOfInterfaceTransitively() throws Exception {
        assertIsA(hierarchy, I.class, "interface extends (transitively)", ISuperSuper.class);
    }

    @Test
    public void analyzesInnerClass() throws Exception {
        assertIsA(hierarchy, C.D.class, "(inner class) extends", Super.class);
    }

    @Test
    public void analyzesDeclaredMethodReturnType() throws Exception {
        assertIsA(hierarchy, Set.class, "return type of C.a() is a", Collection.class);
    }

    @Test
    public void analyzesDeclaredMethodParameter() throws Exception {
        assertIsA(hierarchy, ArrayList.class, "parameter type of C.b() is a", Collection.class);
    }

    @Test
    public void analyzesReferencedType_import() throws Exception {
        assertIsA(hierarchy, HashSet.class, "referenced in C.c() is a", Collection.class);
    }

    @Test
    public void analyzedReferencedType_qualified() throws Exception {
        assertIsA(hierarchy, LinkedList.class, "referenced (qualified) in C.d() is a", Collection.class);
    }

    @Test
    public void analyzesInvokedMethodReturnType() throws Exception {
        assertIsA(hierarchy, List.class, "returned from Arrays.asList() in C.e() is a", Collection.class);
    }

    private <T> void assertIsA(TypeHierarchy hierarchy, Class<? extends T> type, String relation, Class<T> supertype) {
        String typeName = type.getSimpleName();
        String supertypeName = supertype.getSimpleName();
        assertTrue("expected that " + typeName + " " + relation + " " + supertypeName, hierarchy.isA(typeName, supertypeName));
    }
}
