package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.VeryUnspecificReceiverTypePredicate;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.*;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.DefinitionEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ParameterEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.ReceiverEdge;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.AllDataNodesSameLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.SelAndRepSameLabelProvider;
import edu.iastate.cs.mudetect.mining.Configuration;

import java.util.Arrays;
import java.util.HashSet;

class DefaultMiningConfiguration extends Configuration {
    {
        minPatternSupport = 10;
        occurenceLevel = Level.WITHIN_METHOD;
        isStartNode = super.isStartNode.and(new VeryUnspecificReceiverTypePredicate().negate());
        extendByDataNode = DataNodeExtensionStrategy.IF_INCOMING;
        disableSystemOut = true;
        outputPath = System.getProperty("mudetect.mining.outputpath");
        labelProvider = new SelAndRepSameLabelProvider(new AllDataNodesSameLabelProvider(new BaseAUGLabelProvider()));
        extensionEdgeTypes = new HashSet<>(Arrays.asList(
                ReceiverEdge.class, ParameterEdge.class, DefinitionEdge.class, ThrowEdge.class, ContainsEdge.class
        ));
    }
}
