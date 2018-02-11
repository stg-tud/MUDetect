package de.tu_darmstadt.stg.mudetect.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class AUGTestUtils {
    public static <T extends APIUsageGraph> void exportAUGsAsPNG(Collection<T> augs, String filePath, String baseName) {
        Iterator<? extends APIUsageGraph> it = augs.iterator();
        for (int i = 0; it.hasNext() ; i++) {
            APIUsageGraph aug = it.next();
            exportAUGasPNG(aug, new File(filePath, baseName + "-" + i + ".png").getPath());
        }
    }

    public static void exportAUGasPNG(APIUsageGraph aug, String filePath) {
        try {
            new DisplayAUGDotExporter().toPNGFile(aug, new File(filePath));
        } catch (Exception e) {
            throw new RuntimeException("failed to export DOT as PNG", e);
        }
    }
}
