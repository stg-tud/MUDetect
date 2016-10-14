package de.tu_darmstadt.stg.mudetect.dot;

import de.tu_darmstadt.stg.mudetect.model.Instance;

import java.io.PrintWriter;
import java.io.Writer;

class GraphNameCorrectingPrintWriter extends PrintWriter {
    private final String name;

    GraphNameCorrectingPrintWriter(Writer writer, String name) {
        super(writer);
        this.name = name;
    }

    @Override
    public void write(String s, int off, int len) {
        if (s.equals("digraph G {")) {
            s = "digraph \"" + name + "\" {";
        }
        super.write(s, 0, s.length());
    }
}
