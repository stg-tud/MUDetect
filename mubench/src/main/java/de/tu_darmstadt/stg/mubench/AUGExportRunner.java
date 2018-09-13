package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGEdgeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.AUGNodeAttributeProvider;
import de.tu_darmstadt.stg.mudetect.aug.persistence.PersistenceAUGDotExporter;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import edu.iastate.cs.egroum.aug.AUGBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class AUGExportRunner {
    private final static Logger LOGGER = Logger.getLogger(AUGExportRunner.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        new MuBenchRunner().withMineAndDetectStrategy((detectorArgs, builder) -> {
            Collection<APIUsageExample> augs = new AUGBuilder(new DefaultAUGConfiguration())
                    .build(detectorArgs.getTargetSrcPaths(), detectorArgs.getDependencyClassPath());

            PersistenceAUGDotExporter exporter = new PersistenceAUGDotExporter();
            Path exportDest = detectorArgs.getAdditionalOutputPath().resolve("export");
            AUGDotExporter prettyPrinter = new AUGDotExporter(new BaseAUGLabelProvider(), new AUGNodeAttributeProvider(), new AUGEdgeAttributeProvider());
            Path prettyPrintDest = detectorArgs.getAdditionalOutputPath().resolve("pretty");
            for (APIUsageExample aug : augs) {
                try {
                    Location location = aug.getLocation();
                    Path relativePath = Paths.get("/mubench/checkouts").relativize(Paths.get(location.getFilePath())).resolve(location.getMethodSignature());
                    exporter.toDotFile(aug, exportDest.resolve(relativePath).toFile());
                    prettyPrinter.toDotFile(aug, prettyPrintDest.resolve(relativePath).toFile());
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
            }

            return builder.withFindings(new ArrayList<>());
        }).run(args);
    }
}
