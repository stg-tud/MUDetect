package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.mining.MinPatternActionsModel;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.mining.ProvidedPatternsModel;
import de.tu_darmstadt.stg.mudetect.ranking.NoRankingStrategy;
import egroum.DenseGroumPredicate;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.Configuration;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.stream.Collectors;

class ProvidedPatternsStrategy extends MuDetectStrategy {
    @Override
    Collection<EGroumGraph> loadTrainingExamples(DetectorArgs args) throws FileNotFoundException {
        return new EGroumBuilder(new DefaultAUGConfiguration())
                .buildBatch(args.getPatternPath().srcPath, args.getDependencyClassPath());
    }

    @Override
    Miner createMiner() {
        return examples -> new MinPatternActionsModel(new ProvidedPatternsModel(new DefaultMiningConfiguration(), examples), 2);
    }

    @Override
    MuDetect createDetector(Model model) {
        return new MuDetect(
                model,
                new EmptyOverlapsFinder(
                        new AlternativeMappingsOverlapsFinder(new AlternativeMappingsOverlapsFinder.Config() {{
                            nodeMatcher = new EquallyLabelledNodeMatcher(((Configuration) new DefaultMiningConfiguration()).nodeToLabel);
                        }})),
                new EverythingViolationFactory(),
                new NoRankingStrategy());
    }
}
