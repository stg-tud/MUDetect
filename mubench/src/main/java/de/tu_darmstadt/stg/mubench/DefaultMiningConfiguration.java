package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.VeryPrevalentNodePredicate;
import de.tu_darmstadt.stg.mudetect.matcher.DataNodeConstantLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.NodeLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.NullCheckNodeLabelProvider;
import edu.iastate.cs.mudetect.mining.Configuration;

class DefaultMiningConfiguration extends Configuration {
    {
        minPatternSupport = 10;
        occurenceLevel = Level.WITHIN_METHOD;
        isStartNode = super.isStartNode.and(new VeryPrevalentNodePredicate());
        disableSystemOut = true;
        outputPath = System.getProperty("mudetect.mining.outputpath");
        nodeToLabel = NodeLabelProvider.firstOrDefaultLabel(
                new NullCheckNodeLabelProvider(),
                new DataNodeConstantLabelProvider()
        );
    }
}
