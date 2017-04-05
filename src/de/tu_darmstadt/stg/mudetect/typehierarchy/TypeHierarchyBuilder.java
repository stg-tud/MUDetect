package de.tu_darmstadt.stg.mudetect.typehierarchy;

import org.apache.bcel.classfile.*;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TypeHierarchyBuilder {
    public static final String RT_JAR_PATH = System.getProperty("java.home") + "/lib/rt.jar";

    public TypeHierarchy build(String[] classPath) {
        TypeHierarchy hierarchy = new TypeHierarchy();
        for (String jar : classPath) {
            addJarHierarchy(hierarchy, new File(jar));
        }
        return hierarchy;
    }

    private void addJarHierarchy(TypeHierarchy hierarchy, File jar) {
        try {
            String jarPath = jar.getAbsolutePath();
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try {
                        ClassParser parser = new ClassParser(jarPath, entry.getName());
                        addHierarchy(hierarchy, parser.parse());
                    } catch (IOException | ClassNotFoundException | ClassFormatException e) {
                        System.err.println("Error in parsing class file: " + entry.getName());
                        System.err.println(e.getMessage());
                    }
                }
            }
            jarFile.close();
        } catch (IOException e) {
            System.err.println("Failed to parse JAR '" + jar.getAbsoluteFile() + "'");
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private String addHierarchy(TypeHierarchy hierarchy, JavaClass jc) throws ClassNotFoundException {
        String type = getSimpleClassName(jc);
        Set<String> supertypes = new HashSet<>();
        for (JavaClass ji : jc.getAllInterfaces()) {
            if (ji != jc) {
                supertypes.add(addHierarchy(hierarchy, ji));
            }
        }
        for (JavaClass js : jc.getSuperClasses()) {
            supertypes.add(addHierarchy(hierarchy, js));
        }
        hierarchy.addSupertypes(type, supertypes);
        return type;
    }

    private String getSimpleClassName(JavaClass jc) {
        String className = jc.getClassName();
        className = className.replace('$', '.');
        return className.substring(className.lastIndexOf('.') + 1);
    }
}
