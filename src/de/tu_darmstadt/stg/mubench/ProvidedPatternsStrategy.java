package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mudetect.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.EmptyOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.EverythingViolationPredicate;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import de.tu_darmstadt.stg.mudetect.mining.AUGMiner;
import de.tu_darmstadt.stg.mudetect.mining.DefaultAUGMiner;
import de.tu_darmstadt.stg.mudetect.mining.MinPatternActionsModel;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.ranking.NoRankingStrategy;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;

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
                        new AlternativeMappingsOverlapsFinder(
                                new DefaultOverlapFinderConfig(new DefaultMiningConfiguration()))),
                new EverythingViolationPredicate(),
                new NoRankingStrategy());
    }
}
