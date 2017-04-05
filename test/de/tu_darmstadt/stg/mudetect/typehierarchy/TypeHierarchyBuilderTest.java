package de.tu_darmstadt.stg.mudetect.typehierarchy;

import de.tu_darmstadt.stg.mudetect.model.TypeHierarchy;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TypeHierarchyBuilderTest {
    @Test
    public void buildsHierarchyFromRTJar() throws Exception {
        TypeHierarchyBuilder builder = new TypeHierarchyBuilder();

        TypeHierarchy hierarchy = builder.build(new String[] {TypeHierarchyBuilder.RT_JAR_PATH});

        assertTrue(hierarchy.isA("List", "Collection"));
    }

    @Test @Ignore("Only works after running 'mvn package'")
    public void buildsHierarchyFromCustomJar() throws Exception {
        TypeHierarchyBuilder builder = new TypeHierarchyBuilder();

        TypeHierarchy hierarchy = builder.build(new String[] {"./target/MuDetect.jar"});

        assertTrue(hierarchy.isA("TypeHierarchy", "Object"));
    }
}
