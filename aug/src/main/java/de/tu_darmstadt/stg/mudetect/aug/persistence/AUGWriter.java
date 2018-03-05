package de.tu_darmstadt.stg.mudetect.aug.persistence;

import com.google.common.base.Charsets;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGDotExporter;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AUGWriter implements AutoCloseable {
    private final ZipOutputStream zip;
    private final AUGDotExporter exporter;

    public AUGWriter(OutputStream out, AUGDotExporter exporter) {
        zip = new ZipOutputStream(out);
        this.exporter = exporter;
    }

    public void write(APIUsageGraph graph, String graphName) throws IOException {
        zip.putNextEntry(new ZipEntry(graphName + ".dot"));
        zip.write(exporter.toDotGraph(graph).getBytes(Charsets.UTF_8));
        zip.closeEntry();
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}
