package de.tu_darmstadt.stg.mudetect.typehierarchy.testtargets;

import java.util.*;

@SuppressWarnings("unused")
public class C extends Super implements I {
    Set<String> a() { return null; }

    void b(ArrayList<String> l) {}

    void c() { new HashSet<>(); }

    void d() { new java.util.LinkedList<>(); }

    void e() { Arrays.asList("a", "b"); }

    public class D extends Super {}
}
