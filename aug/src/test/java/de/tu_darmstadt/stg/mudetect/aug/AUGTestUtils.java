package de.tu_darmstadt.stg.mudetect.aug;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;

import java.io.File;
import java.io.IOException;

public class AUGTestUtils {
    public static void exportAUGasPNG(APIUsageExample aug, String filepath) throws IOException, InterruptedException {
        new DisplayAUGDotExporter().toPNGFile(aug, new File(filepath));
    }
}
