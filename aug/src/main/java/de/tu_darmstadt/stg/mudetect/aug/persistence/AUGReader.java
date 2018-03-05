package de.tu_darmstadt.stg.mudetect.aug.persistence;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import org.jgrapht.ext.ImportException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AUGReader<G extends APIUsageGraph> implements AutoCloseable {
    private final ZipInputStream zip;
    private final PersistenceAUGDotImporter importer;
    private final Supplier<G> emptyGraphFactory;

    public AUGReader(InputStream in, PersistenceAUGDotImporter importer, Supplier<G> emptyGraphFactory) {
        this.zip = new ZipInputStream(in);
        this.importer = importer;
        this.emptyGraphFactory = emptyGraphFactory;
    }

    public G read() throws IOException, ImportException {
        ZipEntry entry = zip.getNextEntry();
        if (entry == null) {
            return null;
        } else {
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            ByteStreams.copy(zip, contentStream);
            G graph = emptyGraphFactory.get();
            importer.read(contentStream.toString(Charsets.UTF_8.name()), graph);
            return graph;
        }
    }

    public Collection<G> readAll() throws IOException, ImportException {
        Collection<G> augs = new ArrayList<>();
        G aug;
        while ((aug = read()) != null) {
            augs.add(aug);
        }
        return augs;
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}
