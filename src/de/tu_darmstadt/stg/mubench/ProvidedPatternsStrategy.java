package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.mining.*;
import de.tu_darmstadt.stg.mudetect.ranking.NoRankingStrategy;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.Configuration;

import java.io.FileNotFoundException;
import java.util.Collection;

class ProvidedPatternsStrategy extends MuDetectStrategy {
    @Override
    Collection<EGroumGraph> loadTrainingExamples(DetectorArgs args) throws FileNotFoundException {
        return new EGroumBuilder(new DefaultAUGConfiguration())
                .buildBatch(args.getPatternPath().srcPath, args.getDependencyClassPath());
    }

    @Override
    AUGMiner createMiner() {
        DefaultMiningConfiguration config = new DefaultMiningConfiguration() {{
            minPatternSupport = 1; // create a pattern from each provided example
        }};
        return new DefaultAUGMiner(config);
    }

    @Override
    MuDetect createDetector(Model model) {
        return new MuDetect(
                new MinPatternActionsModel(model, 2),
                new EmptyOverlapsFinder(
                        new AlternativeMappingsOverlapsFinder(new AlternativeMappingsOverlapsFinder.Config() {{
                            nodeMatcher = new EquallyLabelledNodeMatcher(((Configuration) new DefaultMiningConfiguration()).nodeToLabel);
                        }})),
                new EverythingViolationFactory(),
                new NoRankingStrategy());
    }
}
