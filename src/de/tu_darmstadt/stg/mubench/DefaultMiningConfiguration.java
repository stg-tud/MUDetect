package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.matcher.DataNodeConstantLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.NodeLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.NullCheckNodeLabelProvider;
import mining.Configuration;

class DefaultMiningConfiguration extends Configuration {
    {
        minPatternSupport = 10;
        disableSystemOut = true;
        outputPath = System.getProperty("mudetect.mining.outputpath");
        nodeToLabel = NodeLabelProvider.firstOrDefaultLabel(
                new NullCheckNodeLabelProvider(),
                new DataNodeConstantLabelProvider()
        );
    }
}
