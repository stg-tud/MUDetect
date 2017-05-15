package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mudetect.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.MissingElementViolationFactory;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.mining.MinPatternActionsModel;
import de.tu_darmstadt.stg.mudetect.mining.MinedPatternsModel;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import egroum.DenseGroumPredicate;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.Configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

class IntraProjectStrategy extends MuDetectStrategy {
    @Override
    Collection<EGroumGraph> loadTrainingExamples(DetectorArgs args) throws IOException {
        return new EGroumBuilder(new DefaultAUGConfiguration())
                .buildBatch(args.getTargetPath().srcPath, args.getDependencyClassPath());
    }

    @Override
    Miner createMiner() {
        return examples -> new MinPatternActionsModel(new MinedPatternsModel(new DefaultMiningConfiguration(), examples), 2);
    }

    @Override
    MuDetect createDetector(Model model) {
        return new MuDetect(
                model,
                new AlternativeMappingsOverlapsFinder(new AlternativeMappingsOverlapsFinder.Config() {{
                    nodeMatcher = new EquallyLabelledNodeMatcher(((Configuration) new DefaultMiningConfiguration()).nodeToLabel);
                }}),
                new MissingElementViolationFactory(),
                new WeightRankingStrategy(
                        new ProductWeightFunction(
                                new PatternSupportWeightFunction(),
                                new PatternViolationsWeightFunction(),
                                new OverlapWithoutEdgesToMissingNodesWeightFunction(
                                        new ConstantNodeWeightFunction()
                                ))));
    }
}
