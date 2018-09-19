package de.tu_darmstadt.stg.mudetect.aug.model.dot;

import com.google.common.io.Files;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.visitors.AUGLabelProvider;
import org.jgrapht.Graph;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.IntegerComponentNameProvider;

import java.io.*;
import java.util.Map;
import java.util.function.Function;

public class AUGDotExporter {
    private static final String WINDOWS_EXEC_DOT = "D:/Program Files (x86)/Graphviz2.36/bin/dot.exe";	// Windows
    private static final String LINUX_EXEC_DOT = "dot";	// Linux
    private static String EXEC_DOT = null;

    static {
        if (System.getProperty("os.name").startsWith("Windows"))
            EXEC_DOT = WINDOWS_EXEC_DOT;
        else
            EXEC_DOT = LINUX_EXEC_DOT;
    }

    private static final String NEW_LINE = System.getProperty("line.separator");

    private final IntegerComponentNameProvider<Node> nodeIdProvider = new IntegerComponentNameProvider<>();
    private final DOTExporter<Node, Edge> exporter;
    private final AUGAttributeProvider<APIUsageGraph> augAttributeProvider;

    public AUGDotExporter(AUGLabelProvider labelProvider,
                          ComponentAttributeProvider<Node> nodeAttributeProvider,
                          ComponentAttributeProvider<Edge> edgeAttributeProvider) {
        this(labelProvider::getLabel, labelProvider::getLabel, nodeAttributeProvider, edgeAttributeProvider);
    }

    public AUGDotExporter(Function<Node, String> nodeLabelProvider,
                          Function<Edge, String> edgeLabelProvider,
                          ComponentAttributeProvider<Node> nodeAttributeProvider,
                          ComponentAttributeProvider<Edge> edgeAttributeProvider) {
        this(nodeLabelProvider, edgeLabelProvider, nodeAttributeProvider, edgeAttributeProvider, null);
    }

    public AUGDotExporter(Function<Node, String> nodeLabelProvider,
                          Function<Edge, String> edgeLabelProvider,
                          ComponentAttributeProvider<Node> nodeAttributeProvider,
                          ComponentAttributeProvider<Edge> edgeAttributeProvider,
                          AUGAttributeProvider<APIUsageGraph> augAttributeProvider) {
        this.exporter = new DOTExporter<>(nodeIdProvider,
                nodeLabelProvider::apply, edgeLabelProvider::apply,
                nodeAttributeProvider, edgeAttributeProvider, this::getGraphName);
        this.augAttributeProvider = augAttributeProvider;
    }

    private String getGraphName(Graph<Node, Edge> g) {
        if (g instanceof APIUsageExample) {
            return ((APIUsageExample) g).getLocation().getMethodSignature();
        } else {
            return "AUG";
        }
    }

    public String toDotGraph(APIUsageGraph aug) {
        StringWriter writer = new StringWriter();
        toDotGraph(aug, writer);
        return writer.toString();
    }

    public void toDotGraph(APIUsageGraph aug, Writer writer) {
        nodeIdProvider.clear();
        Map<String, String> augAttributes = null;
        if (augAttributeProvider != null) {
            augAttributes = augAttributeProvider.getAUGAttributes(aug);
            for (Map.Entry<String, String> attribute : augAttributes.entrySet()) {
                exporter.putGraphAttribute(attribute.getKey(), attribute.getValue());
            }
        }
        exporter.exportGraph(aug, writer);
        if (augAttributes != null) {
            for (String attributeKey : augAttributes.keySet()) {
                exporter.removeGraphAttribute(attributeKey);
            }
        }
    }

    public void toDotFile(APIUsageGraph aug, File file) throws IOException {
        if (!file.getPath().endsWith(".dot")) {
            file = new File(file.getPath() + ".dot");
        }
        file = file.getAbsoluteFile();
        ensureDirectory(file.getParentFile());
        try (BufferedWriter fout = new BufferedWriter(new FileWriter(file))) {
            fout.append(toDotGraph(aug));
            fout.flush();
        }
    }

    public void toPNGFile(APIUsageGraph aug, File file) throws IOException, InterruptedException {
        file = file.getAbsoluteFile();
        File directory = file.getParentFile();
        ensureDirectory(directory);
        String nameWithoutExtension = new File(directory, Files.getNameWithoutExtension(file.getPath())).getPath();
        toDotFile(aug, new File(nameWithoutExtension + ".dot"));
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(new String[]{EXEC_DOT, "-T"+ "png", nameWithoutExtension +".dot", "-o", nameWithoutExtension +"."+ "png"});
        p.waitFor();
    }

    private void ensureDirectory(File path) {
        if (!path.exists()) path.mkdirs();
    }
}
