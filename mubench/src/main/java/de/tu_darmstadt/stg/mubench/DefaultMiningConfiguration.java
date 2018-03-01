package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.VeryUnspecificReceiverTypePredicate;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.AllDataNodesSameLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.SelAndRepSameLabelProvider;
import edu.iastate.cs.mudetect.mining.Configuration;

class DefaultMiningConfiguration extends Configuration {
    {
        minPatternSupport = 10;
        occurenceLevel = Level.WITHIN_METHOD;
        isStartNode = super.isStartNode.and(new VeryUnspecificReceiverTypePredicate().negate());
        extendByDataNode = DataNodeExtensionStrategy.IF_INCOMING;
        disableSystemOut = true;
        outputPath = System.getProperty("mudetect.mining.outputpath");
        labelProvider = new SelAndRepSameLabelProvider(new AllDataNodesSameLabelProvider(new BaseAUGLabelProvider()));
    }
}
